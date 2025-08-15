package com.tcss.filewatcher.Model;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.tcss.filewatcher.Common.Properties;
import com.tcss.filewatcher.Viewer.EmailClientScene;
import com.tcss.filewatcher.Viewer.MainSceneController;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 * The Gmail EMAIL CLIENT API
 *
 * @author Google, Kassie Whitney
 * @version 2.5.1
 */
public class EmailClient implements PropertyChangeListener {

    /**
     * The name of the email client.
     */
    private static final String MY_APPLICATION_NAME = "Email Client API";

    /**
     * The Json factory.
     */
    private static final GsonFactory MY_JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Lists of o-auth 2.0 scopes for sending an email.
     */
    private static final List<String> MY_SCOPES =
            Collections.singletonList(GmailScopes.GMAIL_SEND);

    /**
     * The path of the oauth.json file for o_auth credential checking.
     */
    private static final Path CREDENTIALS_FILE_PATH =
            Paths.get(System.getProperty("user.home"), ".filewatcher", "gmail_oauth.json");

    /**
     * The directory path for the oauth2.0 token.
     * Path is based on users operating home directory.
     */
    private static final File TOKENS_DIR =
            Paths.get(System.getProperty("user.home"), ".filewatcher_tokens").toFile();

    /**
     * The email address of the administrative user.
     */
    private static String MY_ADMIN_EMAIL_ADDRESS;

    private final PropertyChangeSupport myChanges = new PropertyChangeSupport(this);

    /**
     * Starts the Gmail Client API.
     */
    private static boolean start(final String theToAddress, final Path theTmpPath) {

        if (theToAddress == null || theToAddress.isBlank()) {

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Receiver Address Invalid");
                alert.setContentText("""
                        The receivers email address was not properly configured!
                        Please make sure the address is configured and valid!
                        """);
                alert.showAndWait();
            });
            return false;
        }
         MY_ADMIN_EMAIL_ADDRESS = theToAddress;

        try {

            final Gmail service = new Gmail.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(), MY_JSON_FACTORY, authorize())
                    .setApplicationName(MY_APPLICATION_NAME).build();

            Files.createDirectories(theTmpPath);

            final Path csvPath = theTmpPath.resolve("events.csv");

            if (!Files.exists(csvPath)) {
                throw new FileNotFoundException("CSV not found: " + csvPath);
            }

            File csvFile = csvPath.toFile();

            final MimeMessage email = createEmail(theToAddress, csvFile);
            final Message sent = sendMessage(service, email);

            System.out.println("Sent message ID: " + sent.getId());

        } catch (final MessagingException | GeneralSecurityException |
                       IOException theException) {

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Unable to start Email Client");
                alert.setContentText("""
                        An Error occurred trying to start the email client!
                        Please check with a developer to resolve the issue!
                        """);
                alert.showAndWait();
            });
            return false;

        }
        return true;
    }

    /**
     * Checks user email authorization via O-Auth2.0.
     *
     * @return the authorization code.
     * @throws IOException              thrown if gmail_oauth.json file aren't found.
     * @throws GeneralSecurityException thrown if the users email is unauthorized.
     */
    private static Credential authorize() throws IOException, GeneralSecurityException {
        if (!Files.exists(CREDENTIALS_FILE_PATH)) {
            throw new FileNotFoundException("Missing gmail_oauth.json file!" + CREDENTIALS_FILE_PATH.toAbsolutePath());
        }
        if (!TOKENS_DIR.exists() && !TOKENS_DIR.mkdirs()) {
            throw new IOException("Could not create token dir: " + TOKENS_DIR.getAbsolutePath());
        }

        try (InputStream in = Files.newInputStream(CREDENTIALS_FILE_PATH)) {
            final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    MY_JSON_FACTORY,
                    GoogleClientSecrets.load(MY_JSON_FACTORY, new InputStreamReader(in)),
                    MY_SCOPES).setDataStoreFactory(new FileDataStoreFactory(TOKENS_DIR)).setAccessType("offline").build();

            final String tokenKey =
                    (MY_ADMIN_EMAIL_ADDRESS != null && !MY_ADMIN_EMAIL_ADDRESS.isBlank()) ?
                            MY_ADMIN_EMAIL_ADDRESS : "admin";

            final LocalServerReceiver receiver =
                    new LocalServerReceiver.Builder().setPort(8888).build();
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize(tokenKey);
        }
    }

    /**
     * Generates the email with the attached CSV file
     *
     * @param theTo  the email address of the person receiving.
     * @param theCSV the csv file
     * @return returns an email object ready to be sent.
     * @throws MessagingException thrown if the to email is invalid.
     * @throws IOException        thrown if the csv file is invalid.
     */
    private static MimeMessage createEmail(final String theTo, final File theCSV) throws MessagingException, IOException {

        final Session session = Session.getInstance(new java.util.Properties(), null);
        final MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress("tcss360filewatcher@gmail.com"));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(theTo));
        email.setSubject("<NEW FILEWATCHER EVENT>", "UTF-8");

        final MimeBodyPart text = new MimeBodyPart();
        text.setText("New events has been recorded!", "UTF-8");

        if (theCSV != null && theCSV.exists()) {
            final MimeBodyPart csv = new MimeBodyPart();
            csv.attachFile(theCSV);

            if (!theCSV.getName().toLowerCase().endsWith(".csv")) {
                csv.setFileName(theCSV.getName() + ".csv");
            }

            csv.setHeader("Content-Type", "text/csv; charset=UTF-8");

            final MimeMultipart mp = new MimeMultipart();
            mp.addBodyPart(text);
            mp.addBodyPart(csv);
            email.setContent(mp);
        } else {
            email.setText("New events has been recorded!", "UTF-8");
        }
        return email;
    }

    /**
     * Creates a raw email message that is ready to be sent with an attachment.
     *
     * @param theEmail theMessage that was generated from sendMessage
     * @return returns the raw encoded message.
     * @throws MessagingException thrown if an exception occurs when setting the raw email.
     * @throws IOException        thrown if the raw email encoder throws an exception.
     */
    private static Message createMessageWithEmail(final MimeMessage theEmail) throws MessagingException, IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        theEmail.writeTo(buffer);
        final String rawEmail =
                Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.toByteArray());

        final Message message = new Message();
        message.setRaw(rawEmail);
        return message;
    }

    /**
     * Sends the email to the users email address.
     *
     * @param theService This Google service.
     * @param theEmail   the raw encoded email.
     * @return returns a message object that was sent to the user.
     * @throws MessagingException thrown if an exception occurred with the message object.
     * @throws IOException        thrown if the createMessageWithEmail throws an exception.
     */
    private static Message sendMessage(final Gmail theService,
                                       final MimeMessage theEmail) throws MessagingException, IOException {
        return theService.users().messages().send("me", createMessageWithEmail(theEmail)).execute();
    }


    /**
     * Adds the email client scene as a listener.
     * @param theEmailClientScene the emailClientScene object.
     */
    public void addPropertyChangeListener(final MainSceneController theEmailClientScene) {
        theEmailClientScene.addPropertyChangeListener(this);
        theEmailClientScene.setEmailClientListener(this);
    }


    /**
     * Listens for property changes.
     *
     * @param theEvent A PropertyChangeEvent object describing the event source
     *                 and the property that has changed.
     */
    @Override
    public void propertyChange(final PropertyChangeEvent theEvent) {
        if (theEvent.getPropertyName().equals(Properties.USERS_EMAIL.toString())) {
            final String toAddress = (String) theEvent.getNewValue();
            final Path tmpLocation = (Path) theEvent.getOldValue();
            new Thread(() -> {

                boolean isEmailSent = start(toAddress, tmpLocation);

                Platform.runLater(() -> myChanges.firePropertyChange(Properties.EMAIL_SENT
                        .toString(), null, isEmailSent));
            }).start();
        }
    }


}
