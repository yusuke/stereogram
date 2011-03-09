/*
 * Copyright 2007 Yusuke Yamamoto
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.2.0
 */
public class FileOutput implements Split {
    private final static ResourceBundle bundle = ResourceBundle.getBundle("messages");
    private final static Logger LOG = LoggerFactory.getLogger(FileOutput.class);
    private int count = 0;
    private String path;

    FileOutput(String path, boolean override) {
        this.path = path;
    }

    public File getGifFile() throws IOException {
        int extensionIndex = path.lastIndexOf(".");
        String fileName;
        if (-1 != extensionIndex) {
            fileName = path.substring(0, extensionIndex);
        } else {
            fileName = path;
        }
        fileName = fileName + ".gif";
        return getOuptputStream(fileName);
    }
    public File getJpegFile(MPEntry.MPType mpType) throws IOException {
        int extensionIndex = path.lastIndexOf(".");
        String fileName;
        if (-1 != extensionIndex) {
            fileName = path.substring(0, extensionIndex)  +"-";
        } else {
            fileName = path + "-";
        }
        if (mpType == MPEntry.MPType.DISPARITY) {
            String suffix;
            switch (count++) {
                case 0:
                    suffix = "L";
                    break;
                case 1:
                    suffix = "R";
                    break;
                default:
                    suffix = String.valueOf(count);
            }
            fileName = fileName + suffix + ".jpg";
        } else {
            fileName = fileName + (count++) + ".jpg";
        }
        return getOuptputStream(fileName);
    }
    public File getStereoJpegFile() throws IOException {
        int extensionIndex = path.lastIndexOf(".");
        String fileName;
        if (-1 != extensionIndex) {
            fileName = path.substring(0, extensionIndex);
        } else {
            fileName = path;
        }
        fileName = fileName + "-stereo.jpg";
        return getOuptputStream(fileName);
    }

    private File getOuptputStream(String path) throws FileNotFoundException {
        LOG.info("image "+count +": "+path);
        File file = new File(path);
        if(file.exists()){
            LOG.warn(file.getAbsolutePath() + bundle.getString("exists"));
        }
        return file;

    }
}
