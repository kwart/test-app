package org.kodejava.example.mail;

import javax.mail.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class ReadEmail {
    public static final String USERNAME = "jsignpdf";
    public static final String PASSWORD = getPassword();

    public static final String MAIL_SERVER = "pop.gmail.com";
    public static final String MAIL_SERVER_PORT = "995";

    public static void main(String[] args) throws Exception {
        // 1. Setup properties for the mail session.
        Properties props = new Properties();
        props.put("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.pop3.socketFactory.fallback", "false");
        props.put("mail.pop3.socketFactory.port", MAIL_SERVER_PORT);
        props.put("mail.pop3.port", MAIL_SERVER_PORT);
        props.put("mail.pop3.host", MAIL_SERVER);
        props.put("mail.pop3.user", USERNAME);
        props.put("mail.store.protocol", "pop3");

        // 2. Creates a javax.mail.Authenticator object.
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        };

        // 3. Creating mail session.
        Session session = Session.getDefaultInstance(props, auth);

        // 4. Get the POP3 store provider and connect to the store.
        Store store = session.getStore("pop3");
        store.connect(MAIL_SERVER, USERNAME, PASSWORD);

        // 5. Get folder and open the INBOX folder in the store.
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        // 6. Retrieve the messages from the folder.
        Message[] messages = inbox.getMessages();
        for (Message message : messages) {
            message.writeTo(System.out);
        }

        // 7. Close folder and close store.
        inbox.close(false);
        store.close();
    }

    private static String getPassword() {
        try {
            return new String(Files.readAllBytes(Paths.get(System.getProperty("user.home"), ".gmail-password")),
                    StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // public static final String MAIL_SERVER = "localhost";
    // public static final String MAIL_SERVER_PORT = "1995";

    // System.setProperty("javax.net.debug", "ssl:handshake");
    // props.put("mail.pop3.ssl.checkserveridentity", "true");
    // props.put("mail.pop3.ssl.protocols", "TLSv1.2");
}