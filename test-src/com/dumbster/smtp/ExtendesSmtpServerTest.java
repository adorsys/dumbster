package com.dumbster.smtp;

import static com.dumbster.smtp.configuration.MailStoreConfigItem.mailStoreClass;
import static com.dumbster.smtp.configuration.OutStreamLoggingConfigItem.outstream;
import static com.dumbster.smtp.configuration.FileLoggingConfigItem.logFile;
import static com.dumbster.smtp.configuration.PortConfigItem.port;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dumbster.smtp.configuration.ServerConfiguration;
import java.io.File;
import java.io.FilenameFilter;

public class ExtendesSmtpServerTest {

    private final String SERVER = "localhost";
    private final int SMTP_PORT = 1081;
    private final String FROM = "sender@here.com";
    private final String SUBJECT = "Extended Test";
    private final String BODY = "Test Body";
    private final String[] RECIPIENTS = {"foo@bar.com", "receiver@there.com",
        "42@theanswer.gov"};
    private final int WAIT_TICKS = 10000;
    private static final String ALTERNATIVE_MAIL_STORE = "com.dumbster.smtp.RecipientMailStore";
    private SmtpServer server;

    @Before
    public void setup() {
        server = SmtpServerFactory.startServer(ServerConfiguration.is(port).withValue(SMTP_PORT).and(mailStoreClass).withValue(ALTERNATIVE_MAIL_STORE));
    }

    @After
    public void tearDown() {
        server.stop();
    }

    @Test
    public void multipleMailRecipients() {
        MailSendUtility.sendMessage(SERVER, SMTP_PORT, FROM, SUBJECT, BODY,
                RECIPIENTS, null, null);
        server.anticipateMessageCountFor(3, WAIT_TICKS);
        assertTrue("Expeceted 3 mails but was " + server.getEmailCount(),
                server.getEmailCount() == RECIPIENTS.length);
        for (int i = 0; i < RECIPIENTS.length; ++i) {
            MailMessage[] emails = server.getMessagesFor(RECIPIENTS[i]);
            assertNotNull("No mails found for " + RECIPIENTS[i], emails);
            MailMessage email = emails[0];
            assertEquals(SUBJECT, email.getFirstHeaderValue("Subject"));
            assertEquals(BODY, email.getBody());
        }
    }

    @Test
    public void multipleMailsToMulpileRecipientsInOneSession() {
        for (int i = 1; i <= 3; ++i) {
        	int mailCountForAll = 0;
        	MailSendUtility.sendMessage(SERVER, SMTP_PORT, FROM, SUBJECT, BODY,
                    RECIPIENTS, null, null);
            server.anticipateMessageCountFor(RECIPIENTS.length * i, WAIT_TICKS);
            assertTrue("Expeceted " + RECIPIENTS.length * i + " mails but was "
                    + server.getEmailCount(),
                    server.getEmailCount() == RECIPIENTS.length * i);
            assertEquals(server.getEmailCount(), server.getMessages().length);
            for (String r : RECIPIENTS) {
				mailCountForAll += server.getMessagesFor(r).length;
			}
            assertEquals(server.getEmailCount(), mailCountForAll);        
        }
    }

    @Test
    public void ccTest() {
        String[] to = new String[]{RECIPIENTS[0]};
        String[] cc = new String[]{RECIPIENTS[1]};
        MailSendUtility.sendMessage(SERVER, SMTP_PORT, FROM, SUBJECT, BODY, to,
                cc, null);
        server.anticipateMessageCountFor(2, WAIT_TICKS);
        assertTrue("Expeceted 2 mails but was " + server.getEmailCount(),
                server.getEmailCount() == 2);
        MailMessage[] ccMails = server.getMessagesFor(cc[0]);
        MailMessage[] toMails = server.getMessagesFor(to[0]);
        assertTrue(ccMails.length == 1);
        assertTrue(toMails.length == 1);

        assertEquals(toMails[0].getFirstHeaderValue("To"), to[0]);
        assertTrue(toMails[0].getFirstHeaderValue("To") != null
                && !toMails[0].getFirstHeaderValue("To").isEmpty());
        assertEquals(toMails[0].getFirstHeaderValue("To"),
                ccMails[0].getFirstHeaderValue("To"));
        assertEquals(toMails[0].getFirstHeaderValue("Cc"), cc[0]);
        assertTrue(toMails[0].getFirstHeaderValue("Cc") != null
                && !toMails[0].getFirstHeaderValue("Cc").isEmpty());
        assertEquals(toMails[0].getFirstHeaderValue("Cc"),
                ccMails[0].getFirstHeaderValue("Cc"));
        assertEquals(toMails[0].getBody(), BODY);
        assertEquals(toMails[0].getBody(), ccMails[0].getBody());
        assertEquals(toMails[0].getFirstHeaderValue("Subject"), SUBJECT);
        assertEquals(toMails[0].getFirstHeaderValue("Subject"),
                ccMails[0].getFirstHeaderValue("Subject"));
        assertEquals(server.getEmailCount(), server.getMessages().length);
        assertEquals(server.getEmailCount(), server.getMessagesFor(to[0]).length + server.getMessagesFor(cc[0]).length);        
    }

    @Test
    public void bccTest() {
        String[] to = new String[]{RECIPIENTS[0]};
        String[] bcc = new String[]{RECIPIENTS[1]};
        MailSendUtility.sendMessage(SERVER, SMTP_PORT, FROM, SUBJECT, BODY, to,
                null, bcc);
        server.anticipateMessageCountFor(2, WAIT_TICKS);
        assertTrue("Expeceted 2 mails but was " + server.getEmailCount(),
                server.getEmailCount() == 2);

        MailMessage[] toMails = server.getMessagesFor(to[0]);
        MailMessage[] bccMails = server.getMessagesFor(bcc[0]);

        assertTrue(bccMails.length == 1);
        assertTrue(toMails.length == 1);

        assertEquals(toMails[0].getFirstHeaderValue("To"), to[0]);
        assertEquals(toMails[0].getFirstHeaderValue("To"),
                bccMails[0].getFirstHeaderValue("To"));

        // bcc.TO == to
        assertEquals(bccMails[0].getFirstHeaderValue("To"), to[0]);

        assertNull(toMails[0].getFirstHeaderValue("Bcc"));

        assertEquals(toMails[0].getBody(), BODY);
        assertEquals(toMails[0].getBody(), bccMails[0].getBody());

        assertEquals(toMails[0].getFirstHeaderValue("Subject"), SUBJECT);
        assertEquals(toMails[0].getFirstHeaderValue("Subject"),
                bccMails[0].getFirstHeaderValue("Subject"));
        
        assertEquals(server.getEmailCount(), server.getMessages().length);
        assertEquals(server.getEmailCount(), server.getMessagesFor(to[0]).length + server.getMessagesFor(bcc[0]).length);        
    }

    @Test
    public void testOutstreamLogging() {
        String logContent;
        // Stop server started by default (@Before)
        server.stop();

        OutputStream stream = new OutputStream() {
        	
        	private StringBuilder s = new StringBuilder();
            @Override
            public void write(int arg0) throws IOException {
            	s = s.append((char) arg0);
            }
            
            @Override
            public String toString() {
            	return s.toString();
            }
        };
        // Create new server instance with outstream-logging enabled
        server = SmtpServerFactory.startServer(ServerConfiguration.is(port).withValue(SMTP_PORT).and(outstream).withValue(stream));
        while(!server.isReady()){}
        MailSendUtility.sendMessage(SERVER, SMTP_PORT, FROM, SUBJECT, BODY, RECIPIENTS[0]);
        server.anticipateMessageCountFor(1, WAIT_TICKS);
        
        logContent = stream.toString();

        // This one does not test the complete logging. It verifies just the fact that logging is active
        assertTrue("Logentries have to be produced", logContent.length() > 0);
        assertTrue("log contains TO:",
                logContent.contains("RCPT TO:<" + RECIPIENTS[0] + ">"));
        assertTrue("log contains FROM:",
                logContent.contains("MAIL FROM:<" + FROM + ">"));
        assertTrue("log contains subject:", logContent.contains(SUBJECT));
        assertTrue("log contains body:", logContent.contains(BODY));
        
        server.stop();
        while(!server.isStopped()){}
    }

    @Test
    public void testLogFile() {
        String logDirName = System.getProperty("java.io.tmpdir");
        File logDir = new File(logDirName);
        String pattern = SocketWrapper.FILE_PATTERN.replace("%s", ".*");
        FilenameFilter filter = createFileFilter(pattern);
        int fileCountBefore = logDir.listFiles(filter).length;

        MailSendUtility.sendMessage(SERVER, SMTP_PORT, FROM, "This one should not be logged.", BODY, RECIPIENTS[0]);
        server.anticipateMessageCountFor(1, WAIT_TICKS);
        
        assertEquals("One or more logging files were created", fileCountBefore,
                logDir.list(filter).length);
        server.stop();
        
        // Create new server instance with logging enabled and resend message
        server = SmtpServerFactory.startServer(ServerConfiguration.is(port).withValue(SMTP_PORT).and(logFile).withValue(logDirName));
        MailSendUtility.sendMessage(SERVER, SMTP_PORT, FROM, "This one should be logged.", BODY, RECIPIENTS[0]);
        server.anticipateMessageCountFor(1, WAIT_TICKS);
        
        assertEquals("Excactly one logfile must be created. ",
                fileCountBefore + 1, logDir.list(filter).length);
    }

    private FilenameFilter createFileFilter(final String pattern) {
        return new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.matches(pattern);
            }
        };
    }
}
