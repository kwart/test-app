package cz.cacek.test;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * Hazelcast Hello world!
 */
public class App {

    public static void main(String[] args) throws Exception {
        // TreeSet<String> algs = new TreeSet<>();
        // for (Provider provider : Security.getProviders()) {
        // provider.getServices().stream()
        // .filter(s -> "Cipher".equals(s.getType()))
        // .map(Service::getAlgorithm)
        // .forEach(algs::add);
        // }
        // algs.stream().forEach(System.out::println);
        for (Provider provider : Security.getProviders()) {
            System.out.println(provider.getName());
            for (Provider.Service s : provider.getServices()) {
                if (s.getType().equals("Cipher")) {
                    System.out.println("\t" + s.getType() + " " + s.getAlgorithm());
                    System.out.println("\t\tSupportedPaddings " + s.getAttribute("SupportedPaddings"));
                    System.out.println("\t\tSupportedModes " + s.getAttribute("SupportedModes"));
                    System.out.println("\t\tSupportedKeyFormats " + s.getAttribute("SupportedKeyFormats"));
                }
            }
        }
        Cipher c = Cipher.getInstance("AES/GCM/NOPADDING");
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, null);
        SecretKey key = keyGen.generateKey();
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] plain = "Ahoj".getBytes(UTF_8);
        byte[] enc = c.doFinal(plain);
        byte[] iv = c.getIV();
        AlgorithmParameters params = c.getParameters();
        GCMParameterSpec spec = params.getParameterSpec(GCMParameterSpec.class);
        System.out.println(params);
        System.out.println(spec);
        System.out.println(Arrays.toString(key.getEncoded()));
        System.out.println(Arrays.toString(enc));
        System.out.println(Arrays.toString(iv));
        
        c = Cipher.getInstance("AES/GCM/PKCS5PADDING");
        AlgorithmParameters par = c.getParameters();
        
        c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
        byte[] dec = c.doFinal(enc);
        System.out.println(new String(dec, UTF_8));

        c.init(Cipher.DECRYPT_MODE, key, params);
        System.out.println(new String(c.doFinal(enc), UTF_8));

        c.init(Cipher.DECRYPT_MODE, key, spec);
        System.out.println(new String(c.doFinal(enc), UTF_8));

        c.init(Cipher.ENCRYPT_MODE, key, spec);
        byte[] plain2 = "cau".getBytes(UTF_8);
        byte[] enc2 = c.doFinal(plain2);
        System.out.println(Arrays.toString(enc2));

        byte[] res = new byte[3]; 
        for (int i=0; i<res.length; i++)
            res[i] = (byte) (plain[i] ^ enc[i] ^ enc2[i]);
        System.out.println(Arrays.toString(res));
        System.out.println(new String(res, UTF_8));
    }
}
