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
class ExifIFD {
        public byte[] tag = new byte[2];
        public byte[] type = new byte[2];
        public byte[] count = new byte[4];
        private int endian;

        public byte[] data = new byte[4];

        public long getDataAsLong() {
            return HexUtil.get(data, endian);
        }

        public ExifIFD(RandomAccessFile raf, int endian) throws IOException {
            this.endian = endian;
            raf.read(tag);
            raf.read(type);
            raf.read(count);
            raf.read(data);
        }

        public String toString() {
            return "tag:" + getTagName() + ":" +
                    "type:" + getType() + ":" +
                    "count:" + HexUtil.hex(count) + ":" +
                    "data:" + HexUtil.hex(data);
        }


        public String getType() {
            switch ((int) HexUtil.get(type, endian)) {
                case 1:
                    return "BYTE";
                case 2:
                    return "ASCII";
                case 3:
                    return "SHORT";
                case 4:
                    return "LONG";
                case 5:
                    return "RATIONAL";
                case 7:
                    return "UNDEFINED";
                case 9:
                    return "SLONG";
                case 10:
                    return "SRATIONAL";
                default:
                    return "UNKNOWN";
            }
        }

        public String getTagName() {
            switch ((int) HexUtil.get(tag, endian)) {
                case 0:
                    return "GPSVersionID";
                case 1:
                    return "GPSLatitudeRef";
                case 2:
                    return "GPSLatitude";
                case 3:
                    return "GPSLongitudeRef";
                case 4:
                    return "GPSLongitude";
                case 5:
                    return "GPSAltitudeRef";
                case 6:
                    return "GPSAltitude";
                case 7:
                    return "GPSTimeStamp";
                case 8:
                    return "GPSSatellites";
                case 9:
                    return "GPSStatus";
                case 10:
                    return "GPSMeasureMode";
                case 11:
                    return "GPSDOP";
                case 12:
                    return "GPSSpeedRef";
                case 13:
                    return "GPSSpeed";
                case 14:
                    return "GPSTrackRef";
                case 15:
                    return "GPSTrack";
                case 16:
                    return "GPSImgDirectionRef";
                case 17:
                    return "GPSImgDirection";
                case 18:
                    return "GPSMapDatum";
                case 19:
                    return "GPSDestLatitudeRef";
                case 20:
                    return "GPSDestLatitude";
                case 21:
                    return "GPSDestLongitudeRef";
                case 22:
                    return "GPSDestLongitude";
                case 23:
                    return "GPSBearingRef";
                case 24:
                    return "GPSBearing";
                case 25:
                    return "GPSDestDistanceRef";
                case 26:
                    return "GPSDestDistance";
                case 256:
                    return "ImageWidth";
                case 257:
                    return "ImageLength";
                case 258:
                    return "BitsPerSample";
                case 259:
                    return "Compression";
                case 262:
                    return "PhotometricInterpretation";
                case 270:
                    return "ImageDescription";
                case 271:
                    return "Make";
                case 272:
                    return "Model";
                case 273:
                    return "StripOffsets";
                case 274:
                    return "Orientation";
                case 277:
                    return "SamplesPerPixel";
                case 278:
                    return "RowsPerStrip";
                case 279:
                    return "StripByteCounts";
                case 282:
                    return "XResolution";
                case 283:
                    return "YResolution";
                case 284:
                    return "PlanarConfiguration";
                case 296:
                    return "ResolutionUnit";
                case 301:
                    return "TransferFunction";
                case 305:
                    return "Software";
                case 306:
                    return "DateTime";
                case 315:
                    return "Artist";
                case 318:
                    return "WhitePoint";
                case 319:
                    return "PrimaryChromaticities";
                case 513:
                    return "JPEGInterchangeFormat";
                case 514:
                    return "JPEGInterchangeFormatLength";
                case 529:
                    return "YCbCrCoefficients";
                case 530:
                    return "YCbCrSubSampling";
                case 531:
                    return "YCbCrPositioning";
                case 532:
                    return "ReferenceBlackWhite";
                case 3432:
                    return "Copyright";
                case 33434:
                    return "ExposureTime";
                case 33437:
                    return "FNumber";
                case 34665:
                    return "ExifIFDPointer";
                case 34850:
                    return "ExposureProgram";
                case 34852:
                    return "SpectralSensitivity";
                case 34853:
                    return "GPSInfoIFDPointer";
                case 34855:
                    return "ISOSpeedRatings";
                case 34856:
                    return "OECF";
                case 36864:
                    return "ExifVersion";
                case 36867:
                    return "DateTimeOriginal";
                case 36868:
                    return "DateTimeDigitized";
                case 37121:
                    return "ComponentsConfiguration";
                case 37122:
                    return "CompressedBitsPerPixel";
                case 37377:
                    return "ShutterSpeedValue";
                case 37378:
                    return "ApertureValue";
                case 37379:
                    return "BrightnessValue";
                case 37380:
                    return "ExposureBiasValue";
                case 37381:
                    return "MaxApertureValue";
                case 37382:
                    return "SubjectDistance";
                case 37383:
                    return "MeteringMode";
                case 37384:
                    return "LightSource";
                case 37385:
                    return "Flash";
                case 37386:
                    return "FocalLength";
                case 37500:
                    return "MakerNote";
                case 37510:
                    return "UserComment";
                case 37520:
                    return "SubSecTime";
                case 37521:
                    return "SubSecTimeOriginal";
                case 37522:
                    return "SubSecTimeDigitized";
                case 40960:
                    return "FlashPixVersion";
                case 40961:
                    return "ColorSpace";
                case 40962:
                    return "PixelXDimension";
                case 40963:
                    return "PixelYDimension";
                case 40964:
                    return "RelatedSoundFile";
                case 40965:
                    return "InteroperabilityIFDPointer";
                case 41483:
                    return "FlashEnergy";
                case 41484:
                    return "SpatialFrequencyResponse";
                case 41486:
                    return "FocalPlaneXResolution";
                case 41487:
                    return "FocalPlaneYResolution";
                case 41488:
                    return "FocalPlaneResolutionUnit";
                case 41492:
                    return "SubjectLocation";
                case 41493:
                    return "ExposureIndex";
                case 41495:
                    return "SensingMethod";
                case 41728:
                    return "FileSource";
                case 41729:
                    return "SceneType";
                case 41730:
                    return "CFAPattern";
                case 45056:
                    return "MPFVersion";
                case 45057:
                    return "NumberOfImages";
                case 45058:
                    return "MPEntry";
                case 45059:
                    return "ImageUIDList";
                case 45060:
                    return "TotalFrames";
                case 45313:
                    return "MPIndividualNum";
                case 45569:
                    return "PanOrientation";
                case 45570:
                    return "PanOverlap_H";
                case 45571:
                    return "PanOverlap_V";
                case 45572:
                    return "BaseViewpointNum";
                case 45573:
                    return "ConvergenceAngle";
                case 45574:
                    return "BaselineLength";
                case 45575:
                    return "VerticalDivergence";
                case 45576:
                    return "AxisDistance_X";
                case 45577:
                    return "AxisDistance_Y";
                case 45578:
                    return "AxisDistance_Z";
                case 45579:
                    return "YawAngle";
                case 45580:
                    return "PitchAngle";
                case 45581:
                    return "RollAngle";
                default:
                    return "UNKNOWN";
            }
        }
    }