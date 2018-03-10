package packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import model.Peer;

public class ELECTPEER implements Serializable, PACKET{
    
    private static final long serialVersionUID = -1237489610617312345L; 
    private Peer peer;
    
    public ELECTPEER(Peer peer){
        this.peer = peer;
    }
    
    public Peer getPeer(){
        return peer;
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
        stream.writeObject(peer);
    }

    @Override
    public void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        peer = (Peer) stream.readObject();
    }
    
}
