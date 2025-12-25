package AutoDriveEditor.Import;

/*
 *
 *    DDSReader.java MIT License (MIT) ( https://github.com/npedotnet/DDSReader )
 *
 *    Copyright (c) 2015 Kenji Sasaki
 *
 *    Permission is hereby granted, free of charge, to any person obtaining a copy
 *    of this software and associated documentation files (the "Software"), to deal
 *    in the Software without restriction, including without limitation the rights
 *    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *    copies of the Software, and to permit persons to whom the Software is
 *    furnished to do so, subject to the following conditions:
 *
 *    The above copyright notice and this permission notice shall be included in all
 *    copies or substantial portions of the Software.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *    SOFTWARE.
*/

/*
 * Modified by KillBait (20/10/24)
 *
 */

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;

@SuppressWarnings({"ResultOfMethodCallIgnored", "unused"})
public class DDSReader {

    public static BufferedImage image;

    public DDSReader() {}

    public static class Order {
        @SuppressWarnings("SameParameterValue")
        Order(int redShift, int greenShift, int blueShift, int alphaShift) {
            this.redShift = redShift;
            this.greenShift = greenShift;
            this.blueShift = blueShift;
            this.alphaShift = alphaShift;
        }
        public final int redShift;
        public final int greenShift;
        public final int blueShift;
        public final int alphaShift;
    }

    public static final Order ARGB = new Order(16, 8, 0, 24);
    public static final Order ABGR = new Order(0, 8, 16, 24);

    public static int getHeight(byte [] buffer) {
        return (buffer[12] & 0xFF) | (buffer[13] & 0xFF) << 8 | (buffer[14] & 0xFF) << 16 | (buffer[15] & 0xFF) << 24;
    }

    public static int getWidth(byte [] buffer) {
        return (buffer[16] & 0xFF) | (buffer[17] & 0xFF) << 8 | (buffer[18] & 0xFF) << 16 | (buffer[19] & 0xFF) << 24;
    }

    public static int getMipMap(byte [] buffer) {
        return (buffer[28] & 0xFF) | (buffer[29] & 0xFF) << 8 | (buffer[30] & 0xFF) << 16 | (buffer[31] & 0xFF) << 24;
    }

    public static int getPixelFormatFlags(byte [] buffer) {
        return (buffer[80] & 0xFF) | (buffer[81] & 0xFF) << 8 | (buffer[82] & 0xFF) << 16 | (buffer[83] & 0xFF) << 24;
    }

    public static int getFourCC(byte [] buffer) {
        return (buffer[84] & 0xFF) << 24 | (buffer[85] & 0xFF) << 16 | (buffer[86] & 0xFF) << 8 | (buffer[87] & 0xFF);
    }

    public static int getBitCount(byte [] buffer) {
        return (buffer[88] & 0xFF) | (buffer[89] & 0xFF) << 8 | (buffer[90] & 0xFF) << 16 | (buffer[91] & 0xFF) << 24;
    }

    public static int getRedMask(byte [] buffer) {
        return (buffer[92] & 0xFF) | (buffer[93] & 0xFF) << 8 | (buffer[94] & 0xFF) << 16 | (buffer[95] & 0xFF) << 24;
    }

    public static int getGreenMask(byte [] buffer) {
        return (buffer[96] & 0xFF) | (buffer[97] & 0xFF) << 8 | (buffer[98] & 0xFF) << 16 | (buffer[99] & 0xFF) << 24;
    }

    public static int getBlueMask(byte [] buffer) {
        return (buffer[100] & 0xFF) | (buffer[101] & 0xFF) << 8 | (buffer[102] & 0xFF) << 16 | (buffer[103] & 0xFF) << 24;
    }

    public static int getAlphaMask(byte [] buffer) {
        return (buffer[104] & 0xFF) | (buffer[105] & 0xFF) << 8 | (buffer[106] & 0xFF) << 16 | (buffer[107] & 0xFF) << 24;
    }

    public static BufferedImage read(FileInputStream fis, Order order, int mipmapLevel) throws IOException {
        return read(fis, order, mipmapLevel, null);
    }

    public static BufferedImage read(FileInputStream fis, Order order, int mipmapLevel, JProgressBar progressBar) throws IOException {
        // read the header info
        LOG.info("## DDSReader.read() ## Retrieving Image Header Info");

        byte[] buffer = new byte[128];
        try {
            // Read only the first 128 bytes
            fis.read(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // header
        int width = getWidth(buffer);
        int height = getHeight(buffer);
        int mipmap = getMipMap(buffer);
        LOG.info("## DDSReader.read() ## Image Size ( Width: {}, Height: {}, Mipmap: {} )", width, height, mipmap);

        if (image != null) {
            if (image.getWidth() != width || image.getHeight() != height) {
                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            } else {
                LOG.info("## DDSReader.read() ## Reusing existing BufferImage with size {} x {}", width, height);
            }
        } else {
            LOG.info("## DDSReader.read() ## Creating new BufferImage with size {} x {}", width, height);
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }

        // type
        int type = getType(buffer);
        if(type == 0) return null;

        // offset
        int offset = 0; // header size
        if(mipmapLevel > 0 && mipmapLevel < mipmap) {
            for(int i=0; i<mipmapLevel; i++) {
                switch(type) {
                    case DXT1: offset += 8*((width+3)/4)*((height+3)/4); break;
                    case DXT2:
                    case DXT3:
                    case DXT4:
                    case DXT5: offset += 16*((width+3)/4)*((height+3)/4); break;
                    case A1R5G5B5:
                    case X1R5G5B5:
                    case A4R4G4B4:
                    case X4R4G4B4:
                    case R5G6B5:
                    case R8G8B8:
                    case A8B8G8R8:
                    case X8B8G8R8:
                    case A8R8G8B8:
                    case X8R8G8B8: offset += (type&0xFF)*width*height; break;
                }
                width /= 2;
                height /= 2;
            }
            if(width <= 0) width = 1;
            if(height <= 0) height = 1;
        }

        switch (type) {
            case DXT1: decodeDXT1(width, height, offset, fis, image, order, progressBar); break;
            case DXT2: decodeDXT2(width, height, offset, fis, image, order); break;
            case DXT3:
                decodeDXT3(width, height, offset, fis, image, order);
                break;
            case DXT4:
                decodeDXT4(width, height, offset, fis, image, order);
                break;
            case DXT5:
                decodeDXT5(width, height, offset, fis, image, order);
                break;
            case A1R5G5B5:
                readA1R5G5B5(width, height, offset, fis, image, order);
                break;
            case X1R5G5B5:
                readX1R5G5B5(width, height, offset, fis, image, order);
                break;
            case A4R4G4B4:
                readA4R4G4B4(width, height, offset, fis, image, order);
                break;
            case X4R4G4B4:
                readX4R4G4B4(width, height, offset, fis, image, order);
                break;
            case R5G6B5:
                readR5G6B5(width, height, offset, fis, image, order);
                break;
            case R8G8B8:
                readR8G8B8(width, height, offset, fis, image, order);
                break;
            case A8B8G8R8:
                readA8B8G8R8(width, height, offset, fis, image, order);
                break;
            case X8B8G8R8:
                readX8B8G8R8(width, height, offset, fis, image, order);
                break;
            case A8R8G8B8:
                readA8R8G8B8(width, height, offset, fis, image, order);
                break;
            case X8R8G8B8:
                readX8R8G8B8(width, height, offset, fis, image, order);
                break;
        }

        LOG.info("## DDSReader.read() ## Finished Decoding DDS");
        return image;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private static int getType(byte [] buffer) {

        int type = 0;

        int flags = getPixelFormatFlags(buffer);

        if((flags & 0x04) != 0) {
            // DXT
            type = getFourCC(buffer);
        }
        else if((flags & 0x40) != 0) {
            // RGB
            int bitCount = getBitCount(buffer);
            int redMask = getRedMask(buffer);
            int greenMask = getGreenMask(buffer);
            int blueMask = getBlueMask(buffer);
            int alphaMask = ((flags&0x01) != 0) ? getAlphaMask(buffer) : 0; // 0x01 alpha
            if(bitCount == 16) {
                if(redMask==A1R5G5B5_MASKS[0] && greenMask==A1R5G5B5_MASKS[1] && blueMask==A1R5G5B5_MASKS[2] && alphaMask==A1R5G5B5_MASKS[3]) {
                    // A1R5G5B5
                    type = A1R5G5B5;
                }
                else if(redMask==X1R5G5B5_MASKS[0] && greenMask==X1R5G5B5_MASKS[1] && blueMask==X1R5G5B5_MASKS[2] && alphaMask==X1R5G5B5_MASKS[3]) {
                    // X1R5G5B5
                    type = X1R5G5B5;
                }
                else if(redMask==A4R4G4B4_MASKS[0] && greenMask==A4R4G4B4_MASKS[1] && blueMask==A4R4G4B4_MASKS[2] && alphaMask==A4R4G4B4_MASKS[3]) {
                    // A4R4G4B4
                    type = A4R4G4B4;
                }
                else if(redMask==X4R4G4B4_MASKS[0] && greenMask==X4R4G4B4_MASKS[1] && blueMask==X4R4G4B4_MASKS[2] && alphaMask==X4R4G4B4_MASKS[3]) {
                    // X4R4G4B4
                    type = X4R4G4B4;
                }
                else if(redMask==R5G6B5_MASKS[0] && greenMask==R5G6B5_MASKS[1] && blueMask==R5G6B5_MASKS[2] && alphaMask==R5G6B5_MASKS[3]) {
                    // R5G6B5
                    type = R5G6B5;
                }
                else {
                    // Unsupported 16bit RGB image
                }
            }
            else if(bitCount == 24) {
                if(redMask==R8G8B8_MASKS[0] && greenMask==R8G8B8_MASKS[1] && blueMask==R8G8B8_MASKS[2] && alphaMask==R8G8B8_MASKS[3]) {
                    // R8G8B8
                    type = R8G8B8;
                }
                else {
                    // Unsupported 24bit RGB image
                }
            }
            else if(bitCount == 32) {
                if(redMask==A8B8G8R8_MASKS[0] && greenMask==A8B8G8R8_MASKS[1] && blueMask==A8B8G8R8_MASKS[2] && alphaMask==A8B8G8R8_MASKS[3]) {
                    // A8B8G8R8
                    type = A8B8G8R8;
                }
                else if(redMask==X8B8G8R8_MASKS[0] && greenMask==X8B8G8R8_MASKS[1] && blueMask==X8B8G8R8_MASKS[2] && alphaMask==X8B8G8R8_MASKS[3]) {
                    // X8B8G8R8
                    type = X8B8G8R8;
                }
                else if(redMask==A8R8G8B8_MASKS[0] && greenMask==A8R8G8B8_MASKS[1] && blueMask==A8R8G8B8_MASKS[2] && alphaMask==A8R8G8B8_MASKS[3]) {
                    // A8R8G8B8
                    type = A8R8G8B8;
                }
                else if(redMask==X8R8G8B8_MASKS[0] && greenMask==X8R8G8B8_MASKS[1] && blueMask==X8R8G8B8_MASKS[2] && alphaMask==X8R8G8B8_MASKS[3]) {
                    // X8R8G8B8
                    type = X8R8G8B8;
                }
                else {
                    // Unsupported 32bit RGB image
                }
            }
        }
        else {
            // YUV or LUMINANCE image
        }

        return type;

    }

    private static void decodeDXT1(int width, int height, int offset, FileInputStream fis, BufferedImage image, Order order, JProgressBar progressBar) throws IOException {
        LOG.info("## DDSReader.decodeDXT1() ## Decoding DXT1...");
        // Buffer to read one DXT1 block at a time
        byte[] block = new byte[8];
        // Skip the header and any mipmap levels
        if (fis.skip(offset) == offset) {
            for (int y = 0; y < height; y += 4) {
                for (int x = 0; x < width; x += 4) {
                    if (fis.read(block) == block.length) {
                        // Read one DXT1 block into the buffer
                        int c0 = (block[0] & 0xFF) | (block[1] & 0xFF) << 8;
                        int c1 = (block[2] & 0xFF) | (block[3] & 0xFF) << 8;
                        int bits = (block[4] & 0xFF) | (block[5] & 0xFF) << 8 | (block[6] & 0xFF) << 16 | (block[7] & 0xFF) << 24;
                        for (int j = 0; j < 4; j++) {
                            for (int i = 0; i < 4; i++) {
                                int index = (bits >> 2 * (4 * j + i)) & 0x03;
                                int argb = getDXTColor(c0, c1, 255, index, order);
                                if (x + i < width && y + j < height) {
                                    image.setRGB(x + i, y + j, argb);
                                }
                            }
                        }
                    } else {
                        throw new IOException("## DDSReader.decodeDXT1() ## Could not read the specified DXT1 block length");
                    }
                }
                if (progressBar != null) {
                    final int progress = (int) ((y / 4.0 / (height / 4.0)) * 100);
                    SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
                }
            }
        } else {
            throw new IOException("## DDSReader.decodeDXT1() ## Could not skip to the DXT1 data");
        }
    }

    private static void decodeDXT2(int width, int height, int offset, FileInputStream fis, BufferedImage image, Order order) throws IOException {
        decodeDXT3(width, height, offset, fis, image, order);
    }

    private static void decodeDXT3(int width, int height, int offset, FileInputStream fis, BufferedImage image, Order order) throws IOException {
        LOG.info("## DDSReader.decodeDXT3() ##  ( WARNING --UNTESTED-- Report if it does not work )");
        byte[] scanline = new byte[width * 2]; // Buffer to read one scanline at a time
        for (int y = 0; y < height; y += 4) {
            for (int x = 0; x < width; x += 4) {
                fis.read(scanline); // Read one scanline into the buffer
                long alpha = (scanline[0] & 0xFFL) | (scanline[1] & 0xFFL) << 8 | (scanline[2] & 0xFFL) << 16 | (scanline[3] & 0xFFL) << 24 |
                        (scanline[4] & 0xFFL) << 32 | (scanline[5] & 0xFFL) << 40 | (scanline[6] & 0xFFL) << 48 | (scanline[7] & 0xFFL) << 56;
                int c0 = (scanline[8] & 0xFF) | (scanline[9] & 0xFF) << 8;
                int c1 = (scanline[10] & 0xFF) | (scanline[11] & 0xFF) << 8;
                int bits = (scanline[12] & 0xFF) | (scanline[13] & 0xFF) << 8 | (scanline[14] & 0xFF) << 16 | (scanline[15] & 0xFF) << 24;
                for (int j = 0; j < 4; j++) {
                    for (int i = 0; i < 4; i++) {
                        int alphaIndex = 3 * (4 * j + i);
                        int a = (int) ((alpha >> alphaIndex) & 0x0F) * 17;
                        int index = (bits >> 2 * (4 * j + i)) & 0x03;
                        int argb = getDXTColor(c0, c1, a, index, order);
                        image.setRGB(x + i, y + j, argb);
                    }
                }
            }
        }
    }

    private static void decodeDXT4(int width, int height, int offset, FileInputStream fis, BufferedImage image, Order order) throws IOException {
        decodeDXT5(width, height, offset, fis, image, order);
    }

    private static void decodeDXT5(int width, int height, int offset, FileInputStream fis, BufferedImage image, Order order) throws IOException {
        LOG.info("## DDSReader.decodeDXT5() ## ( WARNING --UNTESTED-- Report if it does not work )");
        byte[] scanline = new byte[width * 2]; // Buffer to read one scanline at a time
        fis.skip(offset); // Skip the offset
        for (int y = 0; y < height; y += 4) {
            for (int x = 0; x < width; x += 4) {
                fis.read(scanline); // Read one scanline into the buffer
                int a0 = scanline[0] & 0xFF;
                int a1 = scanline[1] & 0xFF;
                long alphaBits = (scanline[2] & 0xFFL) | (scanline[3] & 0xFFL) << 8 | (scanline[4] & 0xFFL) << 16 | (scanline[5] & 0xFFL) << 24 |
                        (scanline[6] & 0xFFL) << 32 | (scanline[7] & 0xFFL) << 40;
                int c0 = (scanline[8] & 0xFF) | (scanline[9] & 0xFF) << 8;
                int c1 = (scanline[10] & 0xFF) | (scanline[11] & 0xFF) << 8;
                int bits = (scanline[12] & 0xFF) | (scanline[13] & 0xFF) << 8 | (scanline[14] & 0xFF) << 16 | (scanline[15] & 0xFF) << 24;
                for (int j = 0; j < 4; j++) {
                    for (int i = 0; i < 4; i++) {
                        int alphaIndex = 3 * (4 * j + i);
                        int a = getDXT5Alpha(a0, a1, (int) ((alphaBits >> alphaIndex) & 0x07));
                        int index = (bits >> 2 * (4 * j + i)) & 0x03;
                        int argb = getDXTColor(c0, c1, a, index, order);
                        image.setRGB(x + i, y + j, argb);
                    }
                }
            }
        }
    }

    private static void readA1R5G5B5(int width, int height, int offset, FileInputStream fis, BufferedImage image, Order order) throws IOException {
        LOG.info("## DDSReader.readA1R5G5B5() ## Reading A1R5G5B5 file ( WARNING --UNTESTED-- Report if it does not work )");

        byte[] scanline = new byte[width * 2]; // Buffer to read one scanline at a time
        for (int y = 0; y < height; y++) {
            fis.read(scanline); // Read one scanline into the buffer
            for (int x = 0; x < width; x++) {
                int index = x * 2;
                int rgba = (scanline[index] & 0xFF) | (scanline[index + 1] & 0xFF) << 8;
                int r = BIT5[(rgba & A1R5G5B5_MASKS[0]) >> 10];
                int g = BIT5[(rgba & A1R5G5B5_MASKS[1]) >> 5];
                int b = BIT5[(rgba & A1R5G5B5_MASKS[2])];
                int a = 255 * ((rgba & A1R5G5B5_MASKS[3]) >> 15);
                int argb = (a << order.alphaShift) | (r << order.redShift) | (g << order.greenShift) | (b << order.blueShift);
                image.setRGB(x, y, argb);
            }
        }
    }

    private static void readX1R5G5B5(int width, int height, int offset, FileInputStream fis, BufferedImage image, Order order) throws IOException {
        LOG.info("## DDSReader.readX1R5G5B5() ## Reading X1R5G5B5 file ( WARNING --UNTESTED-- Report if it does not work )");

        byte[] scanline = new byte[width * 2]; // Buffer to read one scanline at a time
        for (int y = 0; y < height; y++) {
            fis.read(scanline); // Read one scanline into the buffer
            for (int x = 0; x < width; x++) {
                int index = x * 2;
                int rgba = (scanline[index] & 0xFF) | (scanline[index + 1] & 0xFF) << 8;
                int r = BIT5[(rgba & X1R5G5B5_MASKS[0]) >> 10];
                int g = BIT5[(rgba & X1R5G5B5_MASKS[1]) >> 5];
                int b = BIT5[(rgba & X1R5G5B5_MASKS[2])];
                int a = 255;
                int argb = (a << order.alphaShift) | (r << order.redShift) | (g << order.greenShift) | (b << order.blueShift);
                image.setRGB(x, y, argb);
            }
        }
    }

    private static void readA4R4G4B4(int width, int height, int offset, FileInputStream fis, BufferedImage image, Order order) throws IOException {
        LOG.info("## DDSReader.readA4R4G4B4() ## Reading A4R4G4B4 file ( WARNING --UNTESTED-- Report if it does not work )");
        byte[] scanline = new byte[width * 2]; // Buffer to read one scanline at a time
        for (int y = 0; y < height; y++) {
            fis.read(scanline); // Read one scanline into the buffer
            for (int x = 0; x < width; x++) {
                int index = x * 2;
                int rgba = (scanline[index] & 0xFF) | (scanline[index + 1] & 0xFF) << 8;
                int r = 17 * ((rgba & A4R4G4B4_MASKS[0]) >> 8);
                int g = 17 * ((rgba & A4R4G4B4_MASKS[1]) >> 4);
                int b = 17 * ((rgba & A4R4G4B4_MASKS[2]));
                int a = 17 * ((rgba & A4R4G4B4_MASKS[3]) >> 12);
                int argb = (a << order.alphaShift) | (r << order.redShift) | (g << order.greenShift) | (b << order.blueShift);
                image.setRGB(x, y, argb);
            }
        }
    }

    private static void readX4R4G4B4(int width, int height, int offset, FileInputStream fis, BufferedImage image, Order order) throws IOException {
        LOG.info("## DDSReader.readX4R4G4B4() ## Reading X4R4G4B4 file ( WARNING --UNTESTED-- Report if it does not work )");
        byte[] scanline = new byte[width * 2]; // Buffer to read one scanline at a time
        for (int y = 0; y < height; y++) {
            fis.read(scanline); // Read one scanline into the buffer
            for (int x = 0; x < width; x++) {
                int index = x * 2;
                int rgba = (scanline[index] & 0xFF) | (scanline[index + 1] & 0xFF) << 8;
                int r = 17 * ((rgba & A4R4G4B4_MASKS[0]) >> 8);
                int g = 17 * ((rgba & A4R4G4B4_MASKS[1]) >> 4);
                int b = 17 * ((rgba & A4R4G4B4_MASKS[2]));
                int a = 255;
                int argb = (a << order.alphaShift) | (r << order.redShift) | (g << order.greenShift) | (b << order.blueShift);
                image.setRGB(x, y, argb);
            }
        }
    }

    private static void readR5G6B5(int width, int height, int offset, FileInputStream fis, BufferedImage image, Order order) throws IOException {
        LOG.info("## DDSReader.readR5G6B5() ## Reading R5G6B5 file ( WARNING --UNTESTED-- Report if it does not work )");
        byte[] scanline = new byte[width * 2]; // Buffer to read one scanline at a time
        for (int y = 0; y < height; y++) {
            fis.read(scanline); // Read one scanline into the buffer
            for (int x = 0; x < width; x++) {
                int index = x * 2;
                int rgba = (scanline[index] & 0xFF) | (scanline[index + 1] & 0xFF) << 8;
                int r = BIT5[((rgba & R5G6B5_MASKS[0]) >> 11)];
                int g = BIT6[((rgba & R5G6B5_MASKS[1]) >> 5)];
                int b = BIT5[((rgba & R5G6B5_MASKS[2]))];
                int a = 255;
                int argb = (a << order.alphaShift) | (r << order.redShift) | (g << order.greenShift) | (b << order.blueShift);
                image.setRGB(x, y, argb);
            }
        }
    }

    private static void readR8G8B8(int width, int height, int offset, FileInputStream fis, BufferedImage image, Order order) throws IOException {
        LOG.info("## DDSReader.readR8G8B8() ## Reading R8G8B8 file ( WARNING --UNTESTED-- Report if it does not work )");
        byte[] scanline = new byte[width * 3]; // Buffer to read one scanline at a time
        for (int y = 0; y < height; y++) {
            fis.read(scanline); // Read one scanline into the buffer
            for (int x = 0; x < width; x++) {
                int index = x * 3;
                int b = scanline[index] & 0xFF;
                int g = scanline[index + 1] & 0xFF;
                int r = scanline[index + 2] & 0xFF;
                int a = 255;
                int argb = (a << order.alphaShift) | (r << order.redShift) | (g << order.greenShift) | (b << order.blueShift);
                image.setRGB(x, y, argb);
            }
        }
    }

    private static void readA8B8G8R8(int width, int height, int offset, FileInputStream fis, BufferedImage image, Order order) throws IOException {
        LOG.info("## DDSReader.readA8B8G8R8() ## Reading A8B8G8R8 file ( WARNING --UNTESTED-- Report if it does not work )");
        byte[] scanline = new byte[width * 4]; // Buffer to read one scanline at a time
        for (int y = 0; y < height; y++) {
            fis.read(scanline); // Read one scanline into the buffer
            for (int x = 0; x < width; x++) {
                int index = x * 4;
                int r = scanline[index] & 0xFF;
                int g = scanline[index + 1] & 0xFF;
                int b = scanline[index + 2] & 0xFF;
                int a = scanline[index + 3] & 0xFF;
                int argb = (a << order.alphaShift) | (r << order.redShift) | (g << order.greenShift) | (b << order.blueShift);
                image.setRGB(x, y, argb);
            }
        }
    }

    private static void readX8B8G8R8(int width, int height, int offset, FileInputStream fis, BufferedImage image, Order order) throws IOException {
        LOG.info("## DDSReader.readX8B8G8R8() ## Reading X8B8G8R8 file ( WARNING --UNTESTED-- Report if it does not work )");
        byte[] scanline = new byte[width * 4]; // Buffer to read one scanline at a time
        for (int y = 0; y < height; y++) {
            fis.read(scanline); // Read one scanline into the buffer
            for (int x = 0; x < width; x++) {
                int index = x * 4;
                int r = scanline[index] & 0xFF;
                int g = scanline[index + 1] & 0xFF;
                int b = scanline[index + 2] & 0xFF;
                int a = 255; // Skip alpha
                int argb = (a << order.alphaShift) | (r << order.redShift) | (g << order.greenShift) | (b << order.blueShift);
                image.setRGB(x, y, argb);
            }
        }
    }

    private static void readA8R8G8B8(int width, int height, int offset, FileInputStream fis, BufferedImage image, Order order) throws IOException {
        LOG.info("## DDSReader.readA8R8G8B8() ## Reading A8R8G8B8 file ( WARNING --UNTESTED-- Please report if does not work )");
        byte[] scanline = new byte[width * 4]; // Buffer to read one scanline at a time
        for (int y = 0; y < height; y++) {
            // Read one scanline into the buffer
            if (fis.read(scanline) == scanline.length) {
                for (int x = 0; x < width; x++) {
                    int index = x * 4;
                    int b = scanline[index] & 0xFF;
                    int g = scanline[index + 1] & 0xFF;
                    int r = scanline[index + 2] & 0xFF;
                    int a = scanline[index + 3] & 0xFF;
                    int argb = (a << order.alphaShift) | (r << order.redShift) | (g << order.greenShift) | (b << order.blueShift);
                    image.setRGB(x, y, argb);
                }
            } else {
                throw new IOException("## DDSReader.readA8R8G8B8() ## Returned read length doesn't equal scanline length");
            }
        }
    }

    private static void readX8R8G8B8(int width, int height, int offset, FileInputStream fis, BufferedImage image, Order order) throws IOException {
        LOG.info("DDSReader - Reading X8R8G8B8 file ( WARNING --UNTESTED-- Please report if does not work )");
        byte[] scanline = new byte[width * 4]; // Buffer to read one scanline at a time
        if (fis.skip(offset) == offset) {
            for (int y = 0; y < height; y++) {
                // Read one scanline into the buffer
                if (fis.read(scanline) == scanline.length) {
                    for (int x = 0; x < width; x++) {
                        int index = x * 4;
                        int b = scanline[index] & 0xFF;
                        int g = scanline[index + 1] & 0xFF;
                        int r = scanline[index + 2] & 0xFF;
                        int a = 255; // Skip alpha
                        int argb = (a << order.alphaShift) | (r << order.redShift) | (g << order.greenShift) | (b << order.blueShift);
                        image.setRGB(x, y, argb);
                    }
                } else {
                    throw new IOException("## DDSReader.readX8R8G8B8() ## Returned read length doesn't equal scanline length");
                }
            }
            throw new IOException("## DDSReader.readX8R8G8B8() ## Failed to skip to the image data");
        }
    }

    private static int getDXTColor(int c0, int c1, int a, int t, Order order) {
        switch(t) {
            case 0: return getDXTColor1(c0, a, order);
            case 1: return getDXTColor1(c1, a, order);
            case 2: return (c0 > c1) ? getDXTColor2_1(c0, c1, a, order) : getDXTColor1_1(c0, c1, a, order);
            case 3: return (c0 > c1) ? getDXTColor2_1(c1, c0, a, order) : 0;
        }
        return 0;
    }

    private static int getDXTColor2_1(int c0, int c1, int a, Order order) {
        // 2*c0/3 + c1/3
        int r = (2*BIT5[(c0 & 0xFC00) >> 11] + BIT5[(c1 & 0xFC00) >> 11]) / 3;
        int g = (2*BIT6[(c0 & 0x07E0) >> 5] + BIT6[(c1 & 0x07E0) >> 5]) / 3;
        int b = (2*BIT5[c0 & 0x001F] + BIT5[c1 & 0x001F]) / 3;
        return (a<<order.alphaShift)|(r<<order.redShift)|(g<<order.greenShift)|(b<<order.blueShift);
    }

    private static int getDXTColor1_1(int c0, int c1, int a, Order order) {
        // (c0+c1) / 2
        int r = (BIT5[(c0 & 0xFC00) >> 11] + BIT5[(c1 & 0xFC00) >> 11]) / 2;
        int g = (BIT6[(c0 & 0x07E0) >> 5] + BIT6[(c1 & 0x07E0) >> 5]) / 2;
        int b = (BIT5[c0 & 0x001F] + BIT5[c1 & 0x001F]) / 2;
        return (a<<order.alphaShift)|(r<<order.redShift)|(g<<order.greenShift)|(b<<order.blueShift);
    }

    private static int getDXTColor1(int c, int a, Order order) {
        int r = BIT5[(c & 0xFC00) >> 11];
        int g = BIT6[(c & 0x07E0) >> 5];
        int b = BIT5[(c & 0x001F)];
        return (a<<order.alphaShift)|(r<<order.redShift)|(g<<order.greenShift)|(b<<order.blueShift);
    }

    private static int getDXT5Alpha(int a0, int a1, int t) {
        if(a0 > a1) switch(t) {
            case 0: return a0;
            case 1: return a1;
            case 2: return (6*a0+a1)/7;
            case 3: return (5*a0+2*a1)/7;
            case 4: return (4*a0+3*a1)/7;
            case 5: return (3*a0+4*a1)/7;
            case 6: return (2*a0+5*a1)/7;
            case 7: return (a0+6*a1)/7;
        }
        else switch(t) {
            case 0: return a0;
            case 1: return a1;
            case 2: return (4*a0+a1)/5;
            case 3: return (3*a0+2*a1)/5;
            case 4: return (2*a0+3*a1)/5;
            case 5: return (a0+4*a1)/5;
            case 6: return 0;
            case 7: return 255;
        }
        return 0;
    }

    // Image Type
    private static final int DXT1 = (0x44585431);
    private static final int DXT2 = (0x44585432);
    private static final int DXT3 = (0x44585433);
    private static final int DXT4 = (0x44585434);
    private static final int DXT5 = (0x44585435);
    private static final int A1R5G5B5 = ((1<<16)|2);
    private static final int X1R5G5B5 = ((2<<16)|2);
    private static final int A4R4G4B4 = ((3<<16)|2);
    private static final int X4R4G4B4 = ((4<<16)|2);
    private static final int R5G6B5   = ((5<<16)|2);
    private static final int R8G8B8   = ((1<<16)|3);
    private static final int A8B8G8R8 = ((1<<16)|4);
    private static final int X8B8G8R8 = ((2<<16)|4);
    private static final int A8R8G8B8 = ((3<<16)|4);
    private static final int X8R8G8B8 = ((4<<16)|4);

    // RGBA Masks
    private static final int [] A1R5G5B5_MASKS = {0x7C00, 0x03E0, 0x001F, 0x8000};
    private static final int [] X1R5G5B5_MASKS = {0x7C00, 0x03E0, 0x001F, 0x0000};
    private static final int [] A4R4G4B4_MASKS = {0x0F00, 0x00F0, 0x000F, 0xF000};
    private static final int [] X4R4G4B4_MASKS = {0x0F00, 0x00F0, 0x000F, 0x0000};
    private static final int [] R5G6B5_MASKS   = {0xF800, 0x07E0, 0x001F, 0x0000};
    private static final int [] R8G8B8_MASKS   = {0xFF0000, 0x00FF00, 0x0000FF, 0x000000};
    private static final int [] A8B8G8R8_MASKS = {0x000000FF, 0x0000FF00, 0x00FF0000, 0xFF000000};
    private static final int [] X8B8G8R8_MASKS = {0x000000FF, 0x0000FF00, 0x00FF0000, 0x00000000};
    private static final int [] A8R8G8B8_MASKS = {0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000};
    private static final int [] X8R8G8B8_MASKS = {0x00FF0000, 0x0000FF00, 0x000000FF, 0x00000000};

    // BIT4 = 17 * index;
    private static final int [] BIT5 = {0,8,16,25,33,41,49,58,66,74,82,90,99,107,115,123,132,140,148,156,165,173,181,189,197,206,214,222,230,239,247,255};
    private static final int [] BIT6 = {0,4,8,12,16,20,24,28,32,36,40,45,49,53,57,61,65,69,73,77,81,85,89,93,97,101,105,109,113,117,121,125,130,134,138,142,146,150,154,158,162,166,170,174,178,182,186,190,194,198,202,206,210,215,219,223,227,231,235,239,243,247,251,255};
}
