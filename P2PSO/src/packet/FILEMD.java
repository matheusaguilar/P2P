/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import model.Peer;

/**
 *
 * @author Matheus
 */
public class FILEMD implements Serializable, PACKET{
    
    private static final long serialVersionUID = -1237489610617312345L; 
    private Peer peer;
    private Peer psession;
    private int number;
    private byte[] bytes;
    
    public FILEMD(Peer peer, Peer psession, int number, byte[] bytes){
        this.peer = peer;
        this.psession = psession;
        this.number = number;
        this.bytes = bytes;
    }

    public Peer getPsession() {
        return psession;
    }

    public int getNumber() {
        return number;
    }

    public byte[] getBytes() {
        return bytes;
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
        stream.writeObject(psession);
        stream.writeObject(number);
        stream.writeObject(bytes);
    }

    @Override
    public void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        peer = (Peer) stream.readObject();
        psession = (Peer) stream.readObject();
        number = (int) stream.readObject();
        bytes = (byte[]) stream.readObject();
    }
    
}
