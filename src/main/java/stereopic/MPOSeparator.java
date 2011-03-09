/*
 * Copyright 2011 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package stereopic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.ResourceBundle;

import static stereopic.HexUtil.*;

/**
 * http://www.cipa.jp/hyoujunka/kikaku/pdf/DC-007_J.pdf
 * http://www.cipa.jp/english/hyoujunka/kikaku/pdf/DC-007_E.pdf
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class MPOSeparator {
    private final static Logger LOG = LoggerFactory.getLogger(MPOSeparator.class);

    private final static ResourceBundle bundle = ResourceBundle.getBundle("messages");

    MPOSeparator() {
    }

    public void separate(File file, Split split, boolean separateJPEG
            , boolean generateAnimatedGif, int gifDelay
            , boolean generateStereoImage, int stereoImageWidth) {

        JPEGImage[] images = null;
        RandomAccessFile raf = null;
        OutputStream fos = null;
        try {
            raf = new RandomAccessFile(file, "r");
            int read;
            while (-1 != (read = raf.read())) {
                if (read == 0xff
                        && 0xe2 == raf.read()) {
                    raf.read();
                    raf.read();
                    int endian = 0;
                    if (0x4d == raf.read()
                            && 0x50 == raf.read()
                            && 0x46 == raf.read()
                            && 0x00 == raf.read()
                            ) {
                        //mpf
                        byte[] endianPart = new byte[4];
                        endian = 0;
                        raf.read(endianPart);
                        if (endianPart[0] == 0x4d
                                && endianPart[1] == 0x4d
                                && endianPart[2] == 0x00
                                && endianPart[3] == 0x2a) {
                            // big endian
                            endian = BIG_ENDIAN;
                        } else if (endianPart[0] == 0x49
                                && endianPart[1] == 0x49
                                && endianPart[2] == 0x2a
                                && endianPart[3] == 0x00) {
                            // little endian
                            endian = LITTLE_ENDIAN;
                        }

                        if (endian != 0) {
                            long offsetStart = raf.getFilePointer() - 4;
                            long idfOffset = get(raf, endian, 4);
                            raf.seek(offsetStart + idfOffset);
                            //MP index IDF
                            LOG.debug("offsetstart:" + offsetStart);
                            raf.seek(raf.getFilePointer() + 2);
                            ExifIFD mpfVersion = new ExifIFD(raf, endian);
                            LOG.debug(mpfVersion.toString());
                            ExifIFD numberOfImages = new ExifIFD(raf, endian);
                            LOG.debug(numberOfImages.toString());
                            ExifIFD mpEntryIndex = new ExifIFD(raf, endian);
                            LOG.debug(mpEntryIndex.toString());
                            LOG.info(bundle.getString("numberOfImages") + numberOfImages.getDataAsLong());
                            LOG.debug("offset:" + mpEntryIndex.getDataAsLong());
                            MPEntry[] entries = new MPEntry[(int) numberOfImages.getDataAsLong()];
                            images = new JPEGImage[entries.length];

                            LOG.debug("mpentry offset:" + (offsetStart + mpEntryIndex.getDataAsLong()));
                            raf.seek(offsetStart + mpEntryIndex.getDataAsLong());
                            for (int i = 0; i < entries.length; i++) {
                                entries[i] = new MPEntry(raf, endian);
                                images[i] = new JPEGImage(
                                        i == 0 ? 0 : offsetStart + entries[i].getOffset()
                                        , entries[i].getSize()
                                        , entries[i].getMPType());
                            }
                            break;
                        }
                    }
                }
            }
            if (null == images) {
                LOG.warn("MP Entry not found.");
            } else {
                File[] separatedFiles = new File[images.length];
                for (int i = 0, imagesLength = images.length; i < imagesLength; i++) {
                    JPEGImage image = images[i];
                    raf.seek(image.getOffset());
                    separatedFiles[i] = File.createTempFile("mpo", "jpeg");
                    fos = new FileOutputStream(separatedFiles[i]);
                    for (int j = 0; j < image.getSize(); j++) {
                        fos.write(raf.read());
                    }
                    fos.close();
                }
                if (generateAnimatedGif) {
                    generateAnimatedGIF(split, gifDelay, separatedFiles);
                }
                if(generateStereoImage){
                    generateStereoImage(split, separatedFiles, stereoImageWidth);

                }
                if (separateJPEG) {
                    moveJPEGFiles(split, images, separatedFiles);
                } else {
                    for (File separated : separatedFiles) {
                        separated.deleteOnExit();
                    }
                }
                LOG.info(bundle.getString("done"));
            }
        } catch (IOException ignore) {
            ignore.printStackTrace();
        } finally {

            if (null != raf) {
                try {
                    raf.close();
                } catch (IOException ignore) {
                }
            }
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    private void generateStereoImage(Split split, File[] separatedFiles, int width) throws IOException {
        LOG.info(bundle.getString("generatingStereoJPEG"));

        BufferedImage bi0 = ImageIO.read(separatedFiles[0]);
        int bi0Width = bi0.getWidth();

        int bi0Height = bi0.getHeight();
        double ratio = (double)width / (double)(bi0Width * 2);
        int height = (int) (bi0Height * ratio);
        BufferedImage bi = new BufferedImage(width, height, bi0.getType());

        Graphics2D g2d = bi.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING,
                RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);

        g2d.drawImage(bi0, 0, 0, width / 2, height, null);

        BufferedImage bi1 = ImageIO.read(separatedFiles[1]);
        g2d.drawImage(bi1, width / 2, 0, width / 2, height, null);

        ImageIO.write(bi, "jpg", split.getStereoJpegFile());

    }

    private void moveJPEGFiles(Split split, JPEGImage[] images, File[] separatedFiles) throws IOException {
        LOG.info(bundle.getString("storingJPEG"));
        for (int i = 0, imagesLength = images.length; i < imagesLength; i++) {
            File jpegFile = split.getJpegFile(images[i].getMPType());
            jpegFile.delete();
            separatedFiles[i].renameTo(jpegFile);
        }
    }

    private void generateAnimatedGIF(Split split, int gifDelay, File[] separatedFiles) throws IOException {
        LOG.info(bundle.getString("generatingAnimatedGIF"));
        Iterator it = ImageIO.getImageWritersByFormatName("gif");
        ImageWriter iw = it.hasNext() ?
                (ImageWriter) it.next() : null;
        ImageOutputStream out = ImageIO.createImageOutputStream(split.getGifFile());
        iw.setOutput(out);
        iw.prepareWriteSequence(null);


        for (File separated : separatedFiles) {
            BufferedImage bi = ImageIO.read(separated);

            ImageWriteParam iwp = iw.getDefaultWriteParam();

//                        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
//
//                        iwp.setCompressionType("lzw");
//                        iwp.setCompressionQuality(1f);

            IIOMetadata meta = iw.getDefaultImageMetadata(
                    new ImageTypeSpecifier(bi), iwp);

            String metaFormat = meta.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) meta.getAsTree(metaFormat);

            IIOMetadataNode child = new IIOMetadataNode("GraphicControlExtension");
            // required flags
            child.setAttribute("disposalMethod", "none");
            child.setAttribute("userInputFlag", "FALSE");
            child.setAttribute("transparentColorFlag", "FALSE");
            child.setAttribute("transparentColorIndex", "0");

            // set delay time
            child.setAttribute("delayTime", String.valueOf(gifDelay));
            root.appendChild(child);

            // infinite loop
            IIOMetadataNode list = new IIOMetadataNode("ApplicationExtensions");
            child = new IIOMetadataNode("ApplicationExtension");
            child.setAttribute("applicationID", "NETSCAPE");
            child.setAttribute("authenticationCode", "2.0");
            child.setUserObject(new byte[]{0, 0});
            list.appendChild(child);

            root.appendChild(list);


            meta.setFromTree(metaFormat, root);

            iw.writeToSequence(new IIOImage(bi, null, meta), null);
        }

        iw.endWriteSequence();
        out.close();
    }

}
