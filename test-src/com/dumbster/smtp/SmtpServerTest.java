/*
 * Dumbster - a dummy SMTP server
 * Copyright 2004 Jason Paul Kitchen
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dumbster.smtp;

import static com.dumbster.smtp.configuration.PortConfigItem.port;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.dumbster.smtp.configuration.ServerConfiguration;

public class SmtpServerTest {

	private static final int SMTP_PORT = 1081;

	private SmtpServer server;

	private final String SERVER = "localhost";
	private final String FROM = "sender@here.com";
	private final String TO = "receiver@there.com";
	private final String SUBJECT = "Test";
	private final String BODY = "Test Body";
	private final String FILENAME = "license.txt";

	private final int WAIT_TICKS = 10000;

	@Before
	public void setup() {
		server = SmtpServerFactory.startServer(ServerConfiguration.is(port)
				.withValue(SMTP_PORT));
	}

	@After
	public void teardown() {
		server.stop();
		server = null;
	}

	@Test
	public void testNoMessageSentButWaitingDoesNotHang() {
		server.anticipateMessageCountFor(1, 10);
		assertEquals(0, server.getEmailCount());
	}

	@Test
	public void testSend() {
		MailSendUtility.sendMessage(SERVER, SMTP_PORT, FROM, SUBJECT, BODY, TO);
		server.anticipateMessageCountFor(1, WAIT_TICKS);
		assertTrue(server.getEmailCount() == 1);
		MailMessage email = server.getMessage(0);
		assertEquals("Test", email.getFirstHeaderValue("Subject"));
		assertEquals("Test Body", email.getBody());
	}

	@Test
	public void testSendWithLongSubject() {
		String longSubject = StringUtil.longString(500);
		MailSendUtility.sendMessage(SERVER, SMTP_PORT, FROM, longSubject, BODY,
				TO);
		server.anticipateMessageCountFor(1, WAIT_TICKS);
		assertTrue(server.getEmailCount() == 1);
		MailMessage email = server.getMessage(0);
		assertEquals(longSubject, email.getFirstHeaderValue("Subject"));
		assertEquals(500, longSubject.length());
		assertEquals("Test Body", email.getBody());
	}

	@Test
	public void testSendWithFoldedSubject() {
		String subject = "This\r\n is a folded\r\n Subject line.";
		MailMessage email = sendMessageWithSubject(subject);
		assertEquals("This is a folded Subject line.",
				email.getFirstHeaderValue("Subject"));
	}

	private MailMessage sendMessageWithSubject(String subject) {
		MailSendUtility.sendMessage(SERVER, SMTP_PORT, FROM, subject, BODY, TO);
		server.anticipateMessageCountFor(1, WAIT_TICKS);
		assertEquals(1, server.getEmailCount());
		return server.getMessage(0);
	}

	@Test
	public void testSendWithFoldedSubjectLooksLikeHeader() {
		String subject = "This\r\n really: looks\r\n strange.";
		MailMessage email = sendMessageWithSubject(subject);
		assertEquals("This really: looks strange.",
				email.getFirstHeaderValue("Subject"));
	}

	@Test
	@Ignore
	// should this work?
	public void testSendMessageWithCarriageReturn() {
		String bodyWithCR = "\r\nKeep these pesky carriage returns\r\n";
		MailSendUtility.sendMessage(SERVER, SMTP_PORT, FROM, SUBJECT,
				bodyWithCR, TO);
		assertEquals(1, server.getEmailCount());
		MailMessage email = server.getMessage(0);
		assertEquals(bodyWithCR, email.getBody());
	}

	@Test
	public void testThreadedSend() {
		server.setThreaded(true);
		MailSendUtility.sendMessage(SERVER, SMTP_PORT, FROM, SUBJECT, BODY, TO);
		server.anticipateMessageCountFor(1, WAIT_TICKS);
		assertTrue(server.getEmailCount() == 1);
		MailMessage email = server.getMessage(0);
		assertEquals("Test", email.getFirstHeaderValue("Subject"));
		assertEquals("Test Body", email.getBody());
	}

	@Test
	public void testSendTwoMessagesSameConnection() {
		try {
			MimeMessage[] mimeMessages = new MimeMessage[2];
			Properties mailProps = MailSendUtility.getMailProperties(SERVER,
					SMTP_PORT);
			Session session = Session.getInstance(mailProps, null);

			mimeMessages[0] = MailSendUtility.createMessage(session,
					"sender@whatever.com",
					new String[] { "receiver@home.com" }, null, null,
					"Doodle1", "Bug1");
			mimeMessages[1] = MailSendUtility.createMessage(session,
					"sender@whatever.com",
					new String[] { "receiver@home.com" }, null, null,
					"Doodle2", "Bug2");

			Transport transport = session.getTransport("smtp");
			transport.connect("localhost", SMTP_PORT, null, null);

			for (MimeMessage mimeMessage : mimeMessages) {
				transport.sendMessage(mimeMessage,
						mimeMessage.getAllRecipients());
			}

			transport.close();
		} catch (MessagingException e) {
			e.printStackTrace();
			fail("Unexpected exception: " + e);
		}
		server.anticipateMessageCountFor(2, WAIT_TICKS);
		assertEquals(2, server.getEmailCount());
	}

	@Test
	public void testSendingFileAttachment() throws EmailException, IOException {
		MultiPartEmail email = new MultiPartEmail();
		email.setHostName("127.0.0.1");
		email.setSmtpPort(SMTP_PORT);
		email.addTo(TO);
		email.setFrom(FROM);
		email.setSubject(SUBJECT);
		email.setMsg(BODY);

		email.attach(buildAttachment(), FILENAME, "",
				EmailAttachment.ATTACHMENT);
		
		email.send();
		server.anticipateMessageCountFor(1, WAIT_TICKS);
		assertTrue(server.getMessage(0).getBody().indexOf("Apache License") > 0);
	}

	private ByteArrayDataSource buildAttachment() throws IOException {
		FileInputStream fi = new FileInputStream(new File(FILENAME));
		ByteArrayOutputStream res = new ByteArrayOutputStream();
		int i;
		while ((i = fi.read()) >= 0) {
			res.write(i);
		}
		return new ByteArrayDataSource(res.toByteArray(), "text/plain");
	}
	@Test
	public void testSendTwoMsgsWithLogin() {
		try {

			Properties props = System.getProperties();

			Session session = Session.getDefaultInstance(props, null);
			Message msg = new MimeMessage(session);

			msg.setFrom(new InternetAddress(FROM));

			InternetAddress.parse(TO, false);
			msg.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(TO, false));
			msg.setSubject(SUBJECT);

			msg.setText(BODY);
			msg.setHeader("X-Mailer", "musala");
			msg.setSentDate(new Date());
			msg.saveChanges();

			Transport transport = null;

			try {
				transport = session.getTransport("smtp");
				transport.connect(SERVER, SMTP_PORT, "ddd", "ddd");
				transport.sendMessage(msg, InternetAddress.parse(TO, false));
				transport.sendMessage(msg, InternetAddress.parse(
						"dimiter.bakardjiev@musala.com", false));
			} catch (javax.mail.MessagingException me) {
				me.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (transport != null) {
					transport.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		server.anticipateMessageCountFor(2, WAIT_TICKS);
		assertEquals(2, server.getEmailCount());
		MailMessage email = server.getMessage(0);
		assertEquals("Test", email.getFirstHeaderValue("Subject"));
		assertEquals("Test Body", email.getBody());
	}
}
