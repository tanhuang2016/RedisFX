package xyz.hashdog.rdm.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/8/9 12:37
 */
public class GzipUtil {
    private static final Logger log = LoggerFactory.getLogger(GzipUtil.class);
    /**
     * 使用gzip压缩字符串
     */
    public static byte[] compress(String str, Charset charset) {
        if (str == null || str.isEmpty()) {
            return new byte[0];
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(str.getBytes(charset));
        } catch (IOException e) {
           log.error("compress Exception", e);
        }
        return out.toByteArray();
    }

    /**
     * 使用gzip解压缩
     */
    public static String uncompress(byte [] compressed,Charset charset) {
        if (compressed == null || compressed.length == 0) {
            return "";
        }

        String decompressed = null;
        try( ByteArrayOutputStream out = new ByteArrayOutputStream();
             ByteArrayInputStream in= new ByteArrayInputStream(compressed);
             GZIPInputStream zip = new GZIPInputStream(in); ) {
            byte[] buffer = new byte[1024];
            int offset = -1;
            while ((offset = zip.read(buffer)) != -1) {
                out.write(buffer, 0, offset);
            }
            decompressed = out.toString(charset.displayName());
        } catch (IOException e) {
            log.error("uncompress Exception", e);
        }
        return decompressed;
    }
}
