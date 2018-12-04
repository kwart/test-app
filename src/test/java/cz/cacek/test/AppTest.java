package cz.cacek.test;

import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;

import org.junit.Test;

import com.ibm.crypto.fips.provider.IBMJCEFIPS;

/**
 * A test template.
 */
public class AppTest {

    @Test
    public void testNullToMain() throws Exception {
        Provider[] providers = Security.getProviders();
        System.out.println("Provider 1: " + Security.getProperty("security.provider.1"));
        System.out.println("IBMJCEFIPS: " + Security.getProvider("IBMJCEFIPS"));
        System.out.println("IBMJSSEFIPSProvider: " + Security.getProvider("IBMJSSEFIPSProvider"));
        System.out.println("Providers (" + System.getProperty("java.home") + "):");
        for (Provider provider : providers) {
            System.out.println("\t" + provider.getName());
        }
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(new byte[0]);
        System.out.println("MD5 digest" + Arrays.toString(md5.digest()));
        System.out.println("FIPS provider name: " + IBMJCEFIPS.getInstance().getName());
        System.out.println("FIPS certified: " + IBMJCEFIPS.getInstance().isFipsCertified());
        System.out.println("FIPS level: " + IBMJCEFIPS.getInstance().getFipsLevel());
        Object st = IBMJCEFIPS.getInstance().getSelfTest();
        Class<?> stClass = Class.forName("com.ibm.crypto.fips.provider.SelfTest");
        Method method = stClass.getMethod("runSelfTest");
        method.setAccessible(true);
        System.out.println("runSelfTest: " + method.invoke(st));
        method = stClass.getMethod("isSelfTestInProgress");
        method.setAccessible(true);
        System.out.println("isSelfTestInProgress: " + method.invoke(st));
        method = stClass.getMethod("getSelfTestFailure");
        method.setAccessible(true);
        System.out.println("getSelfTestFailure: " + method.invoke(st));
        method = stClass.getMethod("isFipsRunnable");
        method.setAccessible(true);
        System.out.println("isFipsRunnable: " + method.invoke(st));
    }

}
