package com.dumbster.smtp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * The RecipientMailStore stores all mails for a specific recipient. For each recipient it declares a
 * rolling mail store so the limitation according to the amount of MailMessages applies to each
 * recipient instead of globally for the store.
 * 
 * @author saschawille
 *
 */
public class RecipientMailStore extends MailStore {

	private Map<String, MailStore> receivedMail;
	/* Flat list is used to gain a performance optimization for getMessage(index)
	 * Besides the implementation of getMessages() comes down to 1 line
	 * Of cause we have to invest a portion of memory
	 */
	private List<MailMessage> lookupList;
	private int mailCount = 0;

	public RecipientMailStore() {
		receivedMail = new ConcurrentHashMap<String, MailStore>();
		lookupList = Collections.synchronizedList(new ArrayList<MailMessage>());
	}

	public int getEmailCount() {
		return mailCount;
	}

	void addMessage(List<String> recipients, final MailMessage message) {
		for (final String r : recipients) {
			MailStore entry = receivedMail.get(r);

			if (entry == null) {
				entry = new RollingMailStore();
				receivedMail.put(r, entry);
			}

			entry.addMessage(null, message);
			++mailCount;
		}
	}

	public MailMessage[] getMessages() {
		return lookupList.toArray(new MailMessage[mailCount]);
	}

	public MailMessage getMessage(int index) {
		return lookupList.get(index);
	}

	public MailMessage[] getMessageFor(String recipient) {
		MailStore userStore = receivedMail.get(recipient);
		return userStore != null ? userStore.getMessages() : null;
	}
}
