package xyz.hashdog.rdm.ui.handler.convert;

import java.util.Base64;
/**
 * 标砖base64编解码转换工具
 * @author th
 * @version 2.3.5
 * @since 2025/9/13 22:48
 */
public class Base64Converter implements ValueConverter{
    @Override
    public byte[] encode(byte[] data) {
        return Base64.getEncoder().encode(data);
    }

    @Override
    public byte[] decode(byte[] data) {
        return Base64.getDecoder().decode(data);
    }

    @Override
    public boolean accept(byte[] data) {
        if (data == null || data.length == 0) {
            return false;
        }
        // 只检查前100个字节
        int checkLength = Math.min(data.length, 100);
        // 检查是否只包含Base64允许的字符
        for (int i = 0; i < checkLength; i++) {
            byte b = data[i];
            // 检查是否为Base64字符集: A-Z, a-z, 0-9, +, /
            if (!((b >= 'A' && b <= 'Z') ||
                    (b >= 'a' && b <= 'z') ||
                    (b >= '0' && b <= '9') ||
                    b == '+' || b == '/' || b == '=')) {
                return false;
            }
        }

        // 检查长度是否为4的倍数（只需要检查前几个字符即可）
        int trimmedLength = checkLength;
        // 去除末尾的等号
        while (trimmedLength > 0 && data[trimmedLength - 1] == '=') {
            trimmedLength--;
        }

        // 检查有效字符长度是否符合Base64规则
        return trimmedLength % 4 == 0 || (checkLength < data.length);
    }

    @Override
    public String name() {
        return "Base64";
    }
}
