package cz.cacek.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore.PasswordProtection;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.service.http.commons.TimestampDataLoader;
import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.KeyStoreSignatureTokenConnection;
import eu.europa.esig.dss.token.SignatureTokenConnection;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;

/**
 * The App!
 */
public class App {

    public static void main(String[] args) throws IOException {
        FileDocument toSignDocument = new FileDocument(new File("/home/kwart/test.pdf"));

        try (SignatureTokenConnection signingToken = new KeyStoreSignatureTokenConnection(
                "/home/kwart/test/key-material/client.p12", "PKCS12", new PasswordProtection("123456".toCharArray()))) {
            DSSPrivateKeyEntry privateKey = signingToken.getKeys().get(0);

            // Preparing parameters for the PAdES signature
            PAdESSignatureParameters parameters = new PAdESSignatureParameters();
            // We choose the level of the signature (-B, -T, -LT, -LTA).
            parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_LTA);
            // We set the digest algorithm to use with the signature algorithm. You must use the
            // same parameter when you invoke the method sign on the token. The default value is
            // SHA256
            parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

            // We set the signing certificate
            parameters.setSigningCertificate(privateKey.getCertificate());
            // We set the certificate chain
            parameters.setCertificateChain(privateKey.getCertificateChain());

            // Create common certificate verifier
            CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
            // Create PAdESService for signature
            PAdESService service = new PAdESService(commonCertificateVerifier);
         // Set the Timestamp source
            String tspServer = "http://dss.nowina.lu/pki-factory/tsa/good-tsa";
            OnlineTSPSource onlineTSPSource = new OnlineTSPSource(tspServer);
            onlineTSPSource.setDataLoader(new TimestampDataLoader()); // uses the specific content-type
            service.setTspSource(onlineTSPSource);


            // Get the SignedInfo segment that need to be signed.
            ToBeSigned dataToSign = service.getDataToSign(toSignDocument, parameters);

            // This function obtains the signature value for signed information using the
            // private key and specified algorithm
            DigestAlgorithm digestAlgorithm = parameters.getDigestAlgorithm();
            SignatureValue signatureValue = signingToken.sign(dataToSign, digestAlgorithm, privateKey);

            // Optionally or for debug purpose :
            // Validate the signature value against the original dataToSign
            System.out.println(
                    "Valid: " + service.isValidSignatureValue(dataToSign, signatureValue, privateKey.getCertificate()));

            // We invoke the padesService to sign the document with the signature value obtained in
            // the previous step.
            DSSDocument signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);
            try (FileOutputStream fos = new FileOutputStream("/home/kwart/test_signed.pdf")) {
                signedDocument.writeTo(fos);
            }
        }
    }
}
