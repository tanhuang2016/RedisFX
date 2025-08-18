package xyz.hashdog.rdm.redis.imp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.common.util.DataUtil;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 工具类
 * @author th
 */
public class Util extends xyz.hashdog.rdm.common.util.Util {


    /**
     * 创建 SSLSocketFactory 工厂
     * @param caCrtFile 服务端 CA 证书
     * @param crtFile 客户端 CRT 文件
     * @param keyFile 客户端 Key 文件
     * @param passwordStr SSL 密码
     * @return {@link javax.net.ssl.SSLSocketFactory}
     */
    public static javax.net.ssl.SSLSocketFactory getSocketFactory(final String caCrtFile, final String crtFile, final String keyFile, final String passwordStr)  {
        char[] password = passwordStr==null?null:passwordStr.toCharArray();
        return getSocketFactory(caCrtFile, crtFile, keyFile, password);
    }
    /**
     * 创建 SSLSocketFactory 工厂
     *
     * @param caCrtFile 服务端 CA 证书
     * @param crtFile 客户端 CRT 文件
     * @param keyFile 客户端 Key 文件
     * @param password SSL 密码，随机
     * @return {@link javax.net.ssl.SSLSocketFactory}
     */
    public static javax.net.ssl.SSLSocketFactory getSocketFactory(final String caCrtFile, final String crtFile, final String keyFile, final char[] password)  {
        InputStream caInputStream = null;
        InputStream crtInputStream = null;
        InputStream keyInputStream = null;
        try {
            Security.addProvider(new BouncyCastleProvider());
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // load CA certificate
            caInputStream = Files.newInputStream(Paths.get(caCrtFile));
            X509Certificate caCert = null;
            while (caInputStream.available() > 0) {
                caCert = (X509Certificate) cf.generateCertificate(caInputStream);
            }
            // load client certificate
            crtInputStream = Files.newInputStream(Paths.get(crtFile));
            X509Certificate cert = null;
            while (crtInputStream.available() > 0) {
                cert = (X509Certificate) cf.generateCertificate(crtInputStream);
            }

            // load client private key
            keyInputStream = Files.newInputStream(Paths.get(keyFile));
            PEMParser pemParser = new PEMParser(new InputStreamReader(keyInputStream));
            Object object = pemParser.readObject();
            PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(password);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            KeyPair key;
            if (object instanceof PEMEncryptedKeyPair) {
                key = converter.getKeyPair(((PEMEncryptedKeyPair) object).decryptKeyPair(decProv));
            } else if(object instanceof PKCS8EncryptedPrivateKeyInfo){
                // 解密私钥
                PKCS8EncryptedPrivateKeyInfo encryptedInfo = (PKCS8EncryptedPrivateKeyInfo) object;
                PrivateKey privateKey = new JcaPEMKeyConverter().getPrivateKey(
                        encryptedInfo.decryptPrivateKeyInfo(
                                new JcePKCSPBEInputDecryptorProviderBuilder()
                                        .setProvider("BC")
                                        .build(password)
                        )
                );

                PublicKey publicKey = cert.getPublicKey();
                key=new KeyPair(publicKey, privateKey);
            } else{
                key = converter.getKeyPair((PEMKeyPair) object);
            }
            pemParser.close();

            // CA certificate is used to authenticate server
            KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
            caKs.load(null, null);
            caKs.setCertificateEntry("ca-certificate", caCert);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(caKs);

            // client key and certificates are sent to server so it can authenticate
            // us
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            ks.setCertificateEntry("certificate", cert);
            ks.setKeyEntry("private-key", key.getPrivate(), password, new java.security.cert.Certificate[]{cert});
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, password);

            // finally, create SSL socket factory
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            return context.getSocketFactory();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            close(caInputStream,crtInputStream,keyInputStream);
        }
    }




    /**
     * 创建 SSH Session
     * @param sshUserName
     * @param sshHost
     * @param sshPort
     * @param sshPassword
     * @param sshPrivateKey
     * @param sshPassphrase
     * @return
     */
    public static Session createTunnel(String sshUserName, String sshHost, int sshPort,String sshPassword,String sshPrivateKey, String sshPassphrase)  {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(sshUserName,sshHost,sshPort);
            session.setPassword(sshPassword);
            if(DataUtil.isNotEmpty(sshPrivateKey)){
                jsch.addIdentity(sshPrivateKey,sshPassphrase);
            }
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            return session;
            
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static int portForwardingL( Session session,String rhost,  int rport){
        try {
            return session.setPortForwardingL(0, rhost, rport);
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * json 转为 Map<byte[], byte[]>
     * @param jsonValue
     * @return
     */
    public static Map<byte[], byte[]> json2MapByte(String jsonValue) {
        Map<String, String> stringMap = json2MapString(jsonValue);
        // 转换为 Map<byte[], byte[]>
        Map<byte[], byte[]> byteMap = new HashMap<>();
        for (Map.Entry<String, String> entry : stringMap.entrySet()) {
            byte[] keyBytes = entry.getKey().getBytes();
            byte[] valueBytes = entry.getValue().getBytes();
            byteMap.put(keyBytes, valueBytes);
        }
        return byteMap;
    }

    /**
     * json 转为 Map<String, String>
     * @param jsonValue
     * @return
     */
    public static Map<String, String> json2MapString(String jsonValue) {
        Gson gson = new Gson();
        // 先将 JSON 转换为 Map<String, String>
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> stringMap = gson.fromJson(jsonValue, type);
        return stringMap;
    }

    /**
     * 对象转json
     * @param obj
     * @return
     */
    public static String obj2Json(Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }


    public static Tuple2<Integer, Integer> keyspaceParseDb(String row) {
        String[] a = row.split(":");
        int db =Integer.parseInt(a[0].substring(2));
        int size =Integer.parseInt(a[1].split(",")[0].substring(5)) ;
        return new Tuple2<>(db,size);
    }
    public static Tuple2<Integer, Integer> keyspaceParseDb(String key, String value) {
        return keyspaceParseDb(key+":"+value);
    }


    public static String format(double value,int scale) {
        BigDecimal bd = new BigDecimal(value).setScale(scale, RoundingMode.HALF_UP);
        return bd.stripTrailingZeros().toPlainString();
    }

}
