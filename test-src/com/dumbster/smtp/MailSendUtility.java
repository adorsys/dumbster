package com.dumbster.smtp;

import static org.junit.Assert.fail;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailSendUtility {

	static void sendMessage(String host, int port, String from, String subject,
			String body, String to) {
		sendMessage(host, port, from, subject, body, new String[] { to }, null, null);
	}

	static void sendMessage(String host, int port, String from, String subject,
			String body, String[] to, String[] cc, String[] bcc) {
		try {
			Properties mailProps = getMailProperties(host,port);
			Session session = Session.getInstance(mailProps, null);

			MimeMessage msg = createMessage(session, from, to, cc, bcc, subject, body);
			Transport.send(msg);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception: " + e);
		}
	}

	static MimeMessage createMessage(Session session, String from,
			String[] to, String[] cc, String[] bcc, String subject, String body) throws MessagingException {
		MimeMessage msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(from));
		msg.setSubject(subject);
		msg.setSentDate(new Date());
		msg.setText(body);
		for (String t : to) {
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(t));
		}
		if (cc != null) {
			for (String r : cc) {
				msg.addRecipient(Message.RecipientType.CC, new InternetAddress(r));
			}	
		}
		if (bcc != null) {
			for (String r : bcc) {
				msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(r));
			}	
		}
		return msg;
	}
	
	static Properties getMailProperties(String host, int port) {
		Properties mailProps = new Properties();
		mailProps.setProperty("mail.smtp.host", host);
		mailProps.setProperty("mail.smtp.port", "" + port);
		mailProps.setProperty("mail.smtp.sendpartial", "true");
		return mailProps;
	}

}
