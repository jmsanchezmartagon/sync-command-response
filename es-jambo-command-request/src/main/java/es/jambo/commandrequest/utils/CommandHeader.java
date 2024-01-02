package es.jambo.commandrequest.utils;

public final class CommandHeader {

    public static final String ID = "id";
    public static final String REPLY_CHANNEL = "reply_channel";
    public static final String CORRELATION_ID = "correlation_id";

    private CommandHeader() {
        throw new IllegalCallerException();
    }

}
