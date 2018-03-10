package packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import model.Peer;

public class ELECT implements Serializable, PACKET {
    
    private static final long serialVersionUID = -1237489610617312345L; 
    private long time;
    private Peer peer;
    
    public ELECT(long time, Peer peer){
        this.time = time;
        this.peer = peer;
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
        stream.writeObject(time);
        stream.writeObject(peer);
    }

    @Override
    public void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        time = (long) stream.readObject();
        peer = (Peer) stream.readObject();
    }
  
}
