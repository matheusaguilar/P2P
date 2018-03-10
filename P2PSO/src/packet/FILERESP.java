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
public class FILERESP implements Serializable, PACKET{
    
    private static final long serialVersionUID = -1237489610617312345L; 
    private Peer peer;
    private Peer psession;
    private String name;
    private int number;
    private int size;
    
    public FILERESP(Peer peer, Peer psession, String name, int number, int size){
        this.peer = peer;
        this.psession = psession;
        this.name = name;
        this.number = number;
        this.size = size;
    }

    public Peer getPsession() {
        return psession;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public int getSize() {
        return size;
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
        stream.writeObject(name);
        stream.writeObject(number);
        stream.writeObject(size);
    }

    @Override
    public void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        peer = (Peer) stream.readObject();
        psession = (Peer) stream.readObject();
        name = (String) stream.readObject();
        number = (int) stream.readObject();
        size = (int) stream.readObject();
    }
    
}
