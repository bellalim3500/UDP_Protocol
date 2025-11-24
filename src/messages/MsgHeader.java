package messages;

public class MsgHeader {
    private final int version;
    private final int sequence;
    private final MsgType type;
    private final String msgId;
    private final String correlationId;
    private final long timestampMillis;

    public MsgHeader(int version, int sequence, MsgType type, String msgId, String correlationId,
            long timestampMillis) {
        this.version = version;
        this.sequence = sequence;
        this.type = type;
        this.msgId = msgId;
        this.correlationId = correlationId;
        this.timestampMillis = timestampMillis;
        // not used because of unit test (fails because of different timestamps)
    }

    public int version() {
        return version;
    }

    public int sequence() {
        return sequence;
    }

    public MsgType type() {
        return type;
    }

    public String msgId() {
        return msgId;
    }

    public String correlationId() {
        return correlationId;
    }

    public long timestampMillis() {
        return timestampMillis;
    }

    @Override
    public String toString() {
        return String.format(
                "Version: %d\r\nSequence: %d\r\nType: %s\r\nMsgId: %s\r\nCorrelationId: %s\r\nTimestamp in Millis: %d",
                version,
                sequence,
                type,
                msgId,
                correlationId,
                timestampMillis);
    }

}
