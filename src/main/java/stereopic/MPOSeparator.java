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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import static stereopic.HexUtil.*;

/**
 * http://www.cipa.jp/hyoujunka/kikaku/pdf/DC-007_J.pdf
 * http://www.cipa.jp/english/hyoujunka/kikaku/pdf/DC-007_E.pdf
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class MPOSeparator {
    public static void main(String args[]) throws IOException {
        MPOSeparator mpos = new MPOSeparator();
        mpos.separate(new File("HNI_0001.MPO"), new Split() {
            int i = 0;

            public OutputStream getOutputStream() throws IOException {
                String suffix;
                switch (i++) {
                    case 0:
                        suffix = "L";
                        break;
                    case 1:
                        suffix = "R";
                        break;
                    default:
                        suffix = String.valueOf(i);
                }
                return new FileOutputStream("jpg-" + suffix + ".jpg");
            }
        });

    }

    MPOSeparator() {
    }

    public void separate(File file, Split split) {

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
                            System.out.println("offsetstart:" + offsetStart);
                            raf.seek(raf.getFilePointer() + 2);
                            ExifIFD mpfVersion = new ExifIFD(raf, endian);
                            System.out.println(mpfVersion);
                            ExifIFD numberOfImages = new ExifIFD(raf, endian);
                            System.out.println(numberOfImages);
                            ExifIFD mpEntryIndex = new ExifIFD(raf, endian);
                            System.out.println(mpEntryIndex);
                            System.out.println("number of images:" + numberOfImages.getDataAsLong());
                            System.out.println("offset:" + mpEntryIndex.getDataAsLong());
                            MPEntry[] entries = new MPEntry[(int) numberOfImages.getDataAsLong()];
                            images = new JPEGImage[entries.length];

                            System.out.println("mpentry offset:" + (offsetStart + mpEntryIndex.getDataAsLong()));
                            raf.seek(offsetStart + mpEntryIndex.getDataAsLong());
                            for (int i = 0; i < entries.length; i++) {
                                entries[i] = new MPEntry(raf, endian);
                                images[i] = new JPEGImage(i == 0 ? 0 : offsetStart + entries[i].getOffset(), entries[i].getSize());
                            }
                            break;
                        }
                    }
                }
            }
            if (null == images) {
                throw new AssertionError("MP Entry not found.");
            }
            for (JPEGImage image : images) {
                raf.seek(image.getOffset());
                fos = split.getOutputStream();
                for (int j = 0; j < image.getSize(); j++) {
                    fos.write(raf.read());
                }
                fos.close();
            }
        } catch (IOException ignore) {
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

}
