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

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class HexUtil {

    static int BIG_ENDIAN = 1;
    static int LITTLE_ENDIAN = 2;

    static long get(RandomAccessFile raf, int endian, int bytes) throws IOException {
        byte[] buf = new byte[bytes];
        raf.read(buf);
        return get(buf, endian);
    }

    static long get(byte[] buf, int endian) {
        long value = 0;
        if (endian == BIG_ENDIAN) {
            for (int i = 0; i < buf.length; i++) {
                value = value << 8;
                value += buf[i] < 0 ? buf[i] + 256 : buf[i];
            }
        } else {
            for (int i = (buf.length - 1); i >= 0; i--) {
                value = value << 8;
                value += buf[i] < 0 ? buf[i] + 256 : buf[i];
            }
        }
        return value;
    }


    public static String hex(byte[] b) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            buf.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
        }
        return buf.toString();
    }
}
