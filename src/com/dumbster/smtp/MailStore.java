package com.dumbster.smtp;

import java.util.List;

public abstract class MailStore {
    public abstract int getEmailCount();

    public abstract MailMessage[] getMessages();

    public abstract MailMessage getMessage(int index);
    
    abstract void addMessage(List<String> recpipients, MailMessage message);
}
