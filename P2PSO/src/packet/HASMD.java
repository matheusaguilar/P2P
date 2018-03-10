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
public class HASMD implements Serializable, PACKET{
    
    private static final long serialVersionUID = -1237489610617312345L; 
    private Peer peer;
    private Peer psession;
    private String midia;
    
    public HASMD(Peer peer, Peer psession, String media){
        this.peer = peer;
        this.psession = psession;
        this.midia = media;
    }

    public Peer getPsession() {
        return psession;
    }

    public String getMidia() {
        return midia;
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
        stream.writeObject(midia);
    }

    @Override
    public void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        peer = (Peer) stream.readObject();
        psession = (Peer) stream.readObject();
        midia = (String) stream.readObject();
    }
    
}
