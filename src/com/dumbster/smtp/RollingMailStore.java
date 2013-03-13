package com.dumbster.smtp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RollingMailStore extends MailStore {

    private List<MailMessage> receivedMail;

    public RollingMailStore() {
        receivedMail = Collections.synchronizedList(new ArrayList<MailMessage>());
    }

    public int getEmailCount() {
        return receivedMail.size();
    }

    void addMessage(List<String> recipients, MailMessage message) {
        receivedMail.add(message);
        if (getEmailCount() > 100) {
            receivedMail.remove(0);
        }
    }

    public MailMessage[] getMessages() {
        return receivedMail.toArray(new MailMessage[receivedMail.size()]);
    }

    public MailMessage getMessage(int index) {
        return receivedMail.get(index);
    }
}
