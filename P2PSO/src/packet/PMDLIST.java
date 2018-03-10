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
import java.util.List;
import model.MidiaInfo;
import model.Peer;

/**
 *
 * @author Matheus
 */
public class PMDLIST implements Serializable, PACKET{
    
    private static final long serialVersionUID = -1237489610617312345L; 
    private Peer peer;
    private Peer psession;
    private List<MidiaInfo> mdlist;
    
    public PMDLIST(Peer peer, Peer psession, List<MidiaInfo> mdlist){
        this.peer = peer;
        this.psession = psession;
        this.mdlist = mdlist;
    }

    public Peer getPsession() {
        return psession;
    }

    public List<MidiaInfo> getMdlist() {
        return mdlist;
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
        stream.writeObject(mdlist);
    }

    @Override
    public void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        peer = (Peer) stream.readObject();
        psession = (Peer) stream.readObject();
        mdlist = (List) stream.readObject();
    }
    
}
