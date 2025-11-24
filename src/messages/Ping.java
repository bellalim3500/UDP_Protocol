package messages;



public class Ping implements Message {
    private final MsgHeader header;
  

    public Ping(MsgHeader header) {
        this.header = header;
    
    }// maybe constant using generalized bye-message instead of constructing it

    @Override
    public MsgHeader header() {
        return header;
    }



    @Override
    public String toString() {
        return "PING " + header().sequence() +"\r\n";
        
    }

}
