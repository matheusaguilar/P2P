package packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;

public interface PACKET {
    public InetAddress getAddr();
    public int getPorta();
    public void writeObject(ObjectOutputStream stream) throws IOException;
    public void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException;
}
