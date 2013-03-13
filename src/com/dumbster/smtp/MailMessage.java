package com.dumbster.smtp;

import java.util.Iterator;

public interface MailMessage {

    abstract Iterator<String> getHeaderNames();

    abstract String[] getHeaderValues(String name);

    abstract String getFirstHeaderValue(String name);

    abstract String getBody();

    abstract void addHeader(String name, String value);

    abstract void appendHeader(String name, String value);

    abstract void appendBody(String line);
}