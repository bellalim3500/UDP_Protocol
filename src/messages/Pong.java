package messages;



public class Pong  {
    
    private final int ack;

    public Pong(int ack) {
       
        this.ack = ack;
    }// maybe constant using generalized bye-message instead of constructing it


    public int ack() {
        return ack;
    }


    public byte[] data() {
        return (new String(String.valueOf(ack))).getBytes();

}
}
