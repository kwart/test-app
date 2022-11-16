package cz.cacek.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

/**
 * A test template.
 */
public class AppTest {

    @Test
    public void test() throws Exception {
        System.out.println(Security.getAlgorithms("Cipher"));

        Cipher cout = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec paramSpec = new IvParameterSpec(new byte[16]);
        SecretKeySpec key = new SecretKeySpec(new byte[16], "AES");
        cout.init(Cipher.ENCRYPT_MODE, key, paramSpec);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] data = new byte[78];
        for (byte i = 0; i < data.length; i++) {
            data[i] = i;
        }
        try (CipherOutputStream cos = new CipherOutputStream(baos, cout)) {
            cos.write(data);
            cos.write(data);
        }
        byte[] encData = baos.toByteArray();
        System.out.println("Original size: " + 2 * data.length);
        System.out.println("Encrypted size: " + encData.length);
        byte[] cropEncData = new byte[encData.length];
        System.arraycopy(encData, 0, cropEncData, 0, cropEncData.length);
        System.out.println("Cropped size: " + encData.length);
        Cipher cin = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cin.init(Cipher.DECRYPT_MODE, key, paramSpec);
        ByteArrayInputStream bais = new ByteArrayInputStream(cropEncData);

        byte[] decryptedData1 = new byte[data.length];
        byte[] decryptedData2 = new byte[data.length];
        try (CipherInputStream cis = new CipherInputStream(bais, cin)) {
            int decLen;
            decLen = cis.read(decryptedData1);
            System.out.println("Decrypted: " + decLen);
            decLen = cis.read(decryptedData2);
            System.out.println("Decrypted: " + decLen);
            if (decLen<decryptedData2.length) {
                decLen = cis.read(decryptedData2, decLen, decryptedData2.length - decLen);
                System.out.println("Decrypted: " + decLen);
            }
        }
        assertArrayEquals(data, decryptedData1);
        assertArrayEquals(data, decryptedData2);
    }

}
