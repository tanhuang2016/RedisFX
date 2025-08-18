package xyz.hashdog.rdm.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/7/19 17:13
 */
public class FileUtil {
    protected static Logger log = LoggerFactory.getLogger(FileUtil.class);

    /**
     * File转byte[]数组
     *
     * @param fileFullPath 文件全路径
     * @return byte[]
     */
    public static byte[] file2byte(String fileFullPath) {
        if (fileFullPath == null || fileFullPath.isEmpty()) {
            return null;
        }
        return file2byte(new File(fileFullPath));
    }

    /**
     * File转byte[]数组
     *
     * @param file  文件
     * @return byte[]
     */
    public static byte[] file2byte(File file) {
        if (file == null) {
            return null;
        }
        try (FileInputStream fileInputStream = new FileInputStream(file); ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] b = new byte[1024];
            int n;
            while ((n = fileInputStream.read(b)) != -1) {
                byteArrayOutputStream.write(b, 0, n);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            log.error("file2byte Exception", e);
        }
        return null;
    }

    /**
     * byte[]数组转File
     *
     * @param bytes byte[]
     * @param fileFullPath 文件全路径
     */
    public static void byteWrite2file(byte[] bytes, String fileFullPath) {
        if (bytes == null) {
            return;
        }
        File file = new File(fileFullPath);
        boolean mkdirs = file.mkdirs();
        try(FileOutputStream fileOutputStream = new FileOutputStream(file);) {
            fileOutputStream.write(bytes);
        } catch (Exception e) {
            log.error("byteWrite2file Exception", e);
        }
    }


    public final static Map<String, String> FILE_TYPE_MAP = new HashMap<>();


    /*
      初始化文件类型信息
     */
    static {
        FILE_TYPE_MAP.put("jpg", "FFD8FF");
        FILE_TYPE_MAP.put("png", "89504E47");
        FILE_TYPE_MAP.put("gif", "47494638");
        FILE_TYPE_MAP.put("tif", "49492A00");
        FILE_TYPE_MAP.put("bmp", "424D");
        FILE_TYPE_MAP.put("dwg", "41433130");
        FILE_TYPE_MAP.put("html", "68746D6C3E");
        FILE_TYPE_MAP.put("rtf", "7B5C727466");
        FILE_TYPE_MAP.put("xml", "3C3F786D6C");
        FILE_TYPE_MAP.put("zip", "504B0304");
        FILE_TYPE_MAP.put("rar", "52617221");
        FILE_TYPE_MAP.put("psd", "38425053");
        FILE_TYPE_MAP.put("eml", "44656C69766572792D646174653A");
        FILE_TYPE_MAP.put("dbx", "CFAD12FEC5FD746F");
        FILE_TYPE_MAP.put("pst", "2142444E");
        FILE_TYPE_MAP.put("xls", "D0CF11E0");
        FILE_TYPE_MAP.put("doc", "D0CF11E0");
        FILE_TYPE_MAP.put("mdb", "5374616E64617264204A");
        FILE_TYPE_MAP.put("wpd", "FF575043");
        FILE_TYPE_MAP.put("eps", "252150532D41646F6265");
        FILE_TYPE_MAP.put("ps", "252150532D41646F6265");
        FILE_TYPE_MAP.put("pdf", "255044462D312E");
        FILE_TYPE_MAP.put("qdf", "AC9EBD8F");
        FILE_TYPE_MAP.put("pwl", "E3828596");
        FILE_TYPE_MAP.put("wav", "57415645");
        FILE_TYPE_MAP.put("avi", "41564920");
        FILE_TYPE_MAP.put("ram", "2E7261FD");
        FILE_TYPE_MAP.put("rm", "2E524D46");
        FILE_TYPE_MAP.put("mpg", "000001BA");
        FILE_TYPE_MAP.put("mov", "6D6F6F76");
        FILE_TYPE_MAP.put("asf", "3026B2758E66CF11");
        FILE_TYPE_MAP.put("mid", "4D546864");
    }





    /**
     * 获取图片文件实际类型
     * @param f
     * @return
     * @throws IOException
     */
    public final static String getImageFileType(File f) throws IOException {
        if (isImage(f)) {
            ImageInputStream iis = ImageIO.createImageInputStream(f);
            Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
            if (!iter.hasNext()) {
                return null;
            }
            ImageReader reader = iter.next();
            iis.close();
            return reader.getFormatName();

        }
        return null;
    }

    /**
     * 获取文件类型,包括图片,若格式不是已配置的,则返回null
     * @param file
     * @return
     */
    public final static String getFileByFile(File file) {
        String filetype = null;
        byte[] b = new byte[50];
        try {
            InputStream is = new FileInputStream(file);
            is.read(b);
            filetype = getFileTypeByStream(b);
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filetype;
    }

    /**
     * 获取文件类型,只用转前面一段16进制就行,没必要全部转
     * @param b
     * @return
     */
    public final static String getFileTypeByStream(byte[] b) {
        String filetypeHex = String.valueOf(getFileHexString(b,50));
        Iterator<Map.Entry<String, String>> entryiterator = FILE_TYPE_MAP.entrySet().iterator();
        while (entryiterator.hasNext()) {
            Map.Entry<String, String> entry = entryiterator.next();
            String fileTypeHexValue = entry.getValue();
            if (filetypeHex.toUpperCase().startsWith(fileTypeHexValue)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 判断文件是否为图片
     * @param file
     * @return
     */
    public static final boolean isImage(File file) {
        boolean flag = false;
        try {
            BufferedImage bufreader = ImageIO.read(file);
            int width = bufreader.getWidth();
            int height = bufreader.getHeight();
            if (width == 0 || height == 0) {
                flag = false;
            } else {
                flag = true;
            }
        } catch (IOException e) {
            flag = false;
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 转16进制,从开头截止到stop
     * @param
     * @return
     */
    public final static String getFileHexString(byte[] byteArray,int stop) {
        if(byteArray.length<stop){
            stop=byteArray.length;
        }
        byte[] bytes = Arrays.copyOfRange(byteArray, 0, stop);
        return byte2HexString(bytes);
    }



    public static String byte2HexString(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        if (bytes != null) {
            for (Byte b : bytes) {
                hex.append( String.format("%02X", b.intValue() & 0xFF));
            }
        }
        return hex.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        try {
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i+1), 16));
            }
        } catch (Exception e) {
            // Log.d("", "Argument(s) for hexStringToByteArray(String s)"+ "was not a hex string");
        }
        return data;
    }

    /**
     * byte[]转二进制字符串
     * @param byteArray
     * @return
     */
    public static String byteArrayToBinaryString(byte[] byteArray) {
        StringBuilder binaryString = new StringBuilder();
        for (byte b : byteArray) {
            for (int i = 7; i >= 0; i--) {
                binaryString.append((b >> i) & 1);
            }
        }
        return binaryString.toString();
    }

    /**
     * 二进制字符串转byte[]
     * @param binaryString
     * @return
     */
    public static byte[] binaryStringToByteArray(String binaryString) {
        int length = binaryString.length() / 8;
        byte[] byteArray = new byte[length];
        for (int i = 0; i < length; i++) {
            String byteString = binaryString.substring(i * 8, (i + 1) * 8);
            byteArray[i] = (byte) Integer.parseInt(byteString, 2);
        }
        return byteArray;
    }



}
