package cz.cacek.test;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfPKCS7;
import com.lowagie.text.pdf.PdfReader;
import org.bouncycastle.tsp.TimeStampToken;

import java.util.List;

public class App {

    public static void main(String[] args) {
        if (args == null || args.length < 1) {
            System.err.println("Provide PDF file-name(s) as argument(s)");
        }
        for (String pdfName : args) {
            System.out.println("PDF file: " + pdfName);
            try (PdfReader pr = new PdfReader(pdfName)) {
                AcroFields tmpAcroFields = pr.getAcroFields();
                List<String> tmpNames = tmpAcroFields.getSignedFieldNames();
                final int lastSignatureIdx = tmpNames.size() - 1;
                if (lastSignatureIdx < 0) {
                    System.out.println("No signature.");
                    return;
                }
                String name = tmpNames.get(lastSignatureIdx);
                PdfPKCS7 pk = tmpAcroFields.verifySignature(name);
                System.out.println("Signer: " + pk.getSigningCertificate()
                        .getSubjectX500Principal()
                        .getName());
                TimeStampToken tst = pk.getTimeStampToken();
                if (tst == null) {
                    System.out.println("No timestamp.");
                    return;
                }
                System.out.println("Timestamp: " + tst.getTimeStampInfo()
                        .getGenTime());
                System.out.println();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
