package packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import model.Peer;

public class ACK implements Serializable, PACKET{

    private static final long serialVersionUID = -1237489610617312345L; 
    private int ack;
    private long time;
    private Peer peer;
    public static final int ACK_ELECT = 1;
    public static final int ACK_ELECT_END = 2;
    public static final int ACK_CRT_SESSION = 3;
    public static final int ACK_HASMD = 4;
    public static final int ACK_PMDLIST = 5;
    public static final int ACK_GETFILE = 6;
    public static final int ACK_FILEREQ = 7;
    
    public ACK(int ack, long time, Peer peer){
        this.ack = ack;
        this.time = time;
        this.peer = peer;
    }
    
    public int getAck(){
        return ack;
    }
    
    public long getTime(){
        return time;
    }
    
    @Override
    public InetAddress getAddr() {
        return peer.getAddr();
    }

    @Override
    public int getPorta() {
        return peer.getPort();
    }

    
    @Override
    public void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(ack);
        stream.writeObject(time);
        stream.writeObject(peer);
    }

    @Override
    public void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        ack = (int) stream.readObject();
        time = (long) stream.readObject();
        peer = (Peer) stream.readObject();
    }
    
}
