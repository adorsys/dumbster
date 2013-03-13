package com.dumbster.smtp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SmtpStateTest {

    @Test
    public void testToString() {
        SmtpState state = SmtpState.CONNECT;
        assertEquals("CONNECT", state.toString());
    }

}
