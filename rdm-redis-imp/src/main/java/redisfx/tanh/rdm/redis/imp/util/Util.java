package redisfx.tanh.rdm.redis.imp.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.openssl.PEMDecryptorProvider;
//import org.bouncycastle.openssl.PEMEncryptedKeyPair;
//import org.bouncycastle.openssl.PEMKeyPair;
//import org.bouncycastle.openssl.PEMParser;
//import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
//import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
//import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
//import org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder;
import redisfx.tanh.rdm.common.tuple.Tuple2;
import redisfx.tanh.rdm.common.util.DataUtil;

import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
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
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

/**
 * 工具类
 * @author th
 */
public class Util extends redisfx.tanh.rdm.common.util.Util {


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
   /* *//**
     * 创建 SSLSocketFactory 工厂
     *
     * @param caCrtFile 服务端 CA 证书
     * @param crtFile 客户端 CRT 文件
     * @param keyFile 客户端 Key 文件
     * @param password SSL 密码，随机
     * @return {@link javax.net.ssl.SSLSocketFactory}
     *//*
    public static javax.net.ssl.SSLSocketFactory getSocketFactory2(final String caCrtFile, final String crtFile, final String keyFile, final char[] password)  {
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
            } else if(object instanceof PKCS8EncryptedPrivateKeyInfo encryptedInfo){
                // 解密私钥
                PrivateKey privateKey = new JcaPEMKeyConverter().getPrivateKey(
                        encryptedInfo.decryptPrivateKeyInfo(
                                new JcePKCSPBEInputDecryptorProviderBuilder()
                                        .setProvider("BC")
                                        .build(password)
                        )
                );
                PublicKey publicKey = null;
                if (cert != null) {
                    publicKey = cert.getPublicKey();
                }
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
    }*/

    /**
     * 使用标准 Java SSL API 创建 SSLSocketFactory
     * 减少对 Bouncy Castle 的依赖
     */
    public static javax.net.ssl.SSLSocketFactory getSocketFactory(
            final String caCrtFile, final String crtFile, final String keyFile, final char[] password) {
        try {
            // 使用标准 Java API 加载证书
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            // 加载 CA 证书
            try (InputStream caInputStream = Files.newInputStream(Paths.get(caCrtFile))) {
                X509Certificate caCert = (X509Certificate) cf.generateCertificate(caInputStream);

                // 加载客户端证书
                try (InputStream crtInputStream = Files.newInputStream(Paths.get(crtFile))) {
                    X509Certificate cert = (X509Certificate) cf.generateCertificate(crtInputStream);

                    // 加载私钥（需要自定义解析方法）
                    PrivateKey privateKey = loadPrivateKey(keyFile, password);

                    // 创建 KeyStore
                    KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
                    caKs.load(null, null);
                    caKs.setCertificateEntry("ca-certificate", caCert);
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
                    tmf.init(caKs);

                    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                    ks.load(null, null);
                    ks.setCertificateEntry("certificate", cert);
                    ks.setKeyEntry("private-key", privateKey, password, new java.security.cert.Certificate[]{cert});
                    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                    kmf.init(ks, password);

                    SSLContext context = SSLContext.getInstance("TLSv1.2");
                    context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

                    return context.getSocketFactory();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 加载私钥
     */
    private static PrivateKey loadPrivateKey(String keyFile, char[] password) throws Exception {
        String keyContent = Files.readString(Paths.get(keyFile));

        if (keyContent.contains("-----BEGIN ENCRYPTED PRIVATE KEY-----")) {
            // 处理加密私钥
            String encryptedKeyPEM = keyContent
                    .replace("-----BEGIN ENCRYPTED PRIVATE KEY-----", "")
                    .replace("-----END ENCRYPTED PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(encryptedKeyPEM);
            EncryptedPrivateKeyInfo encryptPKInfo = new EncryptedPrivateKeyInfo(decoded);

            if (password == null) {
                throw new IllegalArgumentException("Encrypted private key requires password");
            }

            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(encryptPKInfo.getAlgName());
            KeySpec keySpec = new PBEKeySpec(password);
            SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
            Cipher cipher = Cipher.getInstance(encryptPKInfo.getAlgName());
            cipher.init(Cipher.DECRYPT_MODE, secretKey, encryptPKInfo.getAlgParameters());

            PKCS8EncodedKeySpec pkcs8KeySpec = encryptPKInfo.getKeySpec(cipher);
            // 根据实际情况确定密钥算法，或从证书中获取
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(pkcs8KeySpec);

        } else if (keyContent.contains("-----BEGIN PRIVATE KEY-----")) {
            // 处理未加密私钥
            String privateKeyPEM = keyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } else {
            throw new IllegalArgumentException("Unsupported private key format");
        }
    }


    /**
     * 创建 SSH Session
     * @param sshUserName ssh用户名
     * @param sshHost ssh主机
     * @param sshPort ssh端口
     * @param sshPassword ssh密码
     * @param sshPrivateKey ssh私钥
     * @param sshPassphrase ssh密码
     * @return {@link com.jcraft.jsch.Session}
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
     * json 转为 Map<String, String>
     * @param jsonValue json字符串
     * @return 转map
     */
    public static Map<String, String> json2MapString(String jsonValue) {
        Gson gson = new Gson();
        // 先将 JSON 转换为 Map<String, String>
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        return gson.fromJson(jsonValue, type);
    }

    /**
     * 对象转json
     * @param obj 对象
     * @return json字符串
     */
    public static String obj2Json(Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }


    /**
     * keyspace解析
     * @param row keyspace
     * @return db,size
     */
    public static Tuple2<Integer, Integer> keyspaceParseDb(String row) {
        String[] a = row.split(":");
        int db =Integer.parseInt(a[0].substring(2));
        int size =Integer.parseInt(a[1].split(",")[0].substring(5)) ;
        return new Tuple2<>(db,size);
    }

    /**
     * keyspace解析
     * @param key keyspace
     * @param value keyspace
     * @return db,size
     */
    public static Tuple2<Integer, Integer> keyspaceParseDb(String key, String value) {
        return keyspaceParseDb(key+":"+value);
    }


    /**
     * 格式化
     * @param value 值
     * @param scale 小数位数
     * @return 格式化后的值
     */
    public static String format(double value,int scale) {
        BigDecimal bd = new BigDecimal(value).setScale(scale, RoundingMode.HALF_UP);
        return bd.stripTrailingZeros().toPlainString();
    }








}
