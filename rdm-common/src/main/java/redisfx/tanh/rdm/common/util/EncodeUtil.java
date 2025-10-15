package redisfx.tanh.rdm.common.util;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/7/19 16:59
 */
public class EncodeUtil {


    /**
     * 检查字节数组中是否包含特殊字符
     *
     * @param bytes 字节数组
     * @return 如果字节数组中包含特殊字符，则返回 true；否则返回 false
     */
    public static boolean containsSpecialCharacters( byte[] bytes) {
        // 检查字节数组中是否包含特殊字符的字节
        for (byte b : bytes) {
            if ((b & 0x80) != 0) {
                // 非ASCII字符，可能是特殊字符
                return true;
            }
        }

        return false;
    }

    /**
     * 将字节转换为无符号整数
     *
     * @param data 字节
     * @return 无符号整数
     */
    private static int byteToUnsignedInt(byte data) {
        return data & 0xff;
    }

    /**
     * 判断字节数组是否为UTF-8编码
     *
     * @param pBuffer 字节数组
     * @return 如果字节数组为UTF-8编码，则返回 true；否则返回 false
     */
    public static boolean isUTF8(byte[] pBuffer) {
        boolean isUTF8 = true;
        boolean isASCII = true;
        int size = pBuffer.length;
        int i = 0;
        while (i < size) {
            int value = byteToUnsignedInt(pBuffer[i]);
            if (value < 0x80) {
                // (10000000): 值小于 0x80 的为 ASCII 字符
                if (i >= size - 1) {
                    if (isASCII) {
                        // 假设纯 ASCII 字符不是 UTF 格式
                        isUTF8 = false;
                    }
                    break;
                }
                i++;
            } else if (value < 0xC0) {
                // (11000000): 值介于 0x80 与 0xC0 之间的为无效 UTF-8 字符
                isASCII = false;
                isUTF8 = false;
                break;
            } else if (value < 0xE0) {
                // (11100000): 此范围内为 2 字节 UTF-8 字符
                isASCII = false;
                if (i >= size - 1) {
                    break;
                }

                int value1 = byteToUnsignedInt(pBuffer[i + 1]);
                if ((value1 & (0xC0)) != 0x80) {
                    isUTF8 = false;
                    break;
                }

                i += 2;
            } else if (value < 0xF0) {
                isASCII = false;
                // (11110000): 此范围内为 3 字节 UTF-8 字符
                if (i >= size - 2) {
                    break;
                }

                int value1 = byteToUnsignedInt(pBuffer[i + 1]);
                int value2 = byteToUnsignedInt(pBuffer[i + 2]);
                if ((value1 & (0xC0)) != 0x80 || (value2 & (0xC0)) != 0x80) {
                    isUTF8 = false;
                    break;
                }

                i += 3;
            }  else if (value < 0xF8) {
                isASCII = false;
                // (11111000): 此范围内为 4 字节 UTF-8 字符
                if (i >= size - 3) {
                    break;
                }

                int value1 = byteToUnsignedInt(pBuffer[i + 1]);
                int value2 = byteToUnsignedInt(pBuffer[i + 2]);
                int value3 = byteToUnsignedInt(pBuffer[i + 3]);
                if ((value1 & (0xC0)) != 0x80
                        || (value2 & (0xC0)) != 0x80
                        || (value3 & (0xC0)) != 0x80) {
                    isUTF8 = false;
                    break;
                }

                i += 3;
            } else {
                isUTF8 = false;
                isASCII = false;
                break;
            }
        }

        return isUTF8;
    }
}
