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

import java.io.IOException;
import java.io.RandomAccessFile;

import static stereopic.HexUtil.*;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class MPEntry {
    private final byte[] attribute = new byte[4];
    private final byte[] size = new byte[4];
    private final byte[] dataOffset = new byte[4];
    private final byte[] imageEntryNumber1 = new byte[2];
    private final byte[] imageEntryNumber2 = new byte[2];

    private int endian;

    public MPEntry(RandomAccessFile raf, int endian) throws IOException {
        this.endian = endian;
        raf.read(attribute);
        raf.read(size);
        raf.read(dataOffset);
        raf.read(imageEntryNumber1);
        raf.read(imageEntryNumber2);
    }

    public long getSize() {
        return get(size, endian);
    }

    public long getOffset() {
        return get(dataOffset, endian);
    }

    public String toString() {
        return hex(attribute) + ":" +
                " size:" + getSize() + ":" +
                "dataOffset:" + getOffset() +
                hex(imageEntryNumber1) + ":" +
                hex(imageEntryNumber2);
    }

    public MPType getMPType() {
        // 5.2.3.3.1. Individual Image Attribute
        String mpType = hex(attribute).substring(2);
        if (mpType.equals("010001")) {
            return MPType.CLASS1;
        } else if (mpType.equals("010002")) {
            return MPType.CLASS2;
        } else if (mpType.equals("020001")) {
            return MPType.PANORAMA;
        } else if (mpType.equals("020002")) {
            return MPType.DISPARITY;
        } else if (mpType.equals("020003")) {
            return MPType.MULTI_ANGLE;
        } else if (mpType.equals("000000")) {
            return MPType.UNDEFINED;
        }else{
            return MPType.UNKNOWN;
        }
    }
    
    enum MPType {
         CLASS1
        , CLASS2
        , PANORAMA
        , DISPARITY
        , MULTI_ANGLE
        , UNDEFINED
        , UNKNOWN
        
    }
}
