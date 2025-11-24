package messages;



public interface Message {

    MsgHeader header();

    @Override
    String toString();
}
