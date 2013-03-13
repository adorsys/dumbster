package com.dumbster.smtp;

import static com.dumbster.smtp.configuration.FileLoggingConfigItem.logFile;
import static com.dumbster.smtp.configuration.MailStoreConfigItem.mailStoreClass;
import static com.dumbster.smtp.configuration.OutStreamLoggingConfigItem.outstream;
import static com.dumbster.smtp.configuration.PortConfigItem.port;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dumbster.smtp.configuration.ServerConfiguration;

public class ConfigurationTest {

    @Test
    public void testIllegalPortRange() {
        SmtpServer server = SmtpServerFactory.startServer(ServerConfiguration.is(port).withValue(65599));
        assertNull("Server started started but it shouldn't", server);
    }

    @Test
    public void testIllegalOutStream() {
        SmtpServer server = SmtpServerFactory.startServer(ServerConfiguration.is(outstream).withValue(null));
        assertNull("Server started started but it shouldn't", server);
    }

    @Test
    public void testLoggingDirectory() {
        SmtpServer server = SmtpServerFactory.startServer(ServerConfiguration.is(logFile).withValue("none"));
        assertNull("Server started but it shouldn't", server);
    }

    @Test
    public void testWrongMailStoreClass() {
        SmtpServer server = SmtpServerFactory.startServer(ServerConfiguration.is(mailStoreClass).withValue("com.dumbster.smtp.thisClassDoesNotExist"));
        assertNull("Server started but it shouldn't", server);
    }

    @Test
    public void testCompleteConfiguration() {
        SmtpServer server = SmtpServerFactory.startServer(ServerConfiguration.is(logFile).withValue(".").and(port).withValue(50025).and(mailStoreClass).withValue(RecipientMailStore.class.getCanonicalName()));
        assertNotNull("Server not started", server);
        assertTrue("Server not started",
                server.isReady() && !server.isStopped());
        server.stop();
        try {
            for (int i = 0; i < 10 && !server.isStopped(); ++i) {
                Thread.sleep(i * 500);
            }
        } catch (InterruptedException ex) {
        }
        assertTrue(server.isStopped());
    }
}
