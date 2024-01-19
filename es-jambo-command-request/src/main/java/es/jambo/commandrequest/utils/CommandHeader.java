package es.jambo.commandrequest.utils;

/**
 * @author Juan Manuel Sánchez Martagón <jmsanchezmartagon@gmail.com>
 */
public final class CommandHeader {
    public static final String ID = "id";
    public static final String REPLY_CHANNEL = "reply_channel";
    public static final String CORRELATION_ID = "correlation_id";

    private CommandHeader() {
        throw new IllegalCallerException();
    }

}
