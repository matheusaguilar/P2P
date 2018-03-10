/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import model.MidiaInfo;
import model.MidiaUtil;
import model.Peer;
import p2pso.P2PSO;
import packet.ACK;
import packet.CRTSESSION;
import packet.ELECT;
import packet.ELECTEND;
import packet.ELECTPEER;
import packet.FILEMD;
import packet.FILEREQ;
import packet.FILERESP;
import packet.GETFILE;
import packet.HASMD;
import packet.HASMDRESP;
import packet.PMDLIST;
import packet.PacketReceiver;
import view.Message;

/**
 *
 * @author Matheus
 */
public class Server implements Observer{
    
    private final Peer myPeer;
    private final long time;
    private final List<Integer> porta_list; 
    private final DatagramSocket server;
    private final Sender sender;
    private final Receiver receiver;
    private boolean leader;
    private final List<Peer> peers;
    private Peer pleader;
    private long pleaderTime;
    private final List<Session> sessions;
    private final PacketReceiver packetReceiver;
    
    private boolean keepElection;
    private boolean electionFinished;
    
    private final int window = 50;
    
    public Server(int porta, List<Integer> porta_list) throws UnknownHostException, SocketException{
        this.myPeer = new Peer(InetAddress.getLocalHost(), porta);
        this.time = System.currentTimeMillis();
        this.porta_list = porta_list;
        this.server = new DatagramSocket(porta);
        this.sender = new Sender();
        this.receiver = new Receiver(server);
        this.receiver.addObserver(this);
        this.leader = false;
        this.peers = new LinkedList<>();
        this.pleader = null;
        this.sessions = new LinkedList<>();
        this.packetReceiver = new PacketReceiver();
        this.electionFinished = false;
    }

    public Peer getMyPeer() {
        return myPeer;
    }

    public Peer getPleader() {
        return pleader;
    }
    
    public Sender getSender() {
        return sender;
    }

    public List<Peer> getPeers() {
        return peers;
    }
    
    public boolean isLeader() {
        return leader;
    }

    public int getWindow() {
        return window;
    }
    
    public void startReceiver(){
        System.out.println(" Processo para receber pacotes iniciado com sucesso.");
        receiver.start();
    }
    
    public void startElection(){
        System.out.println(" Processo de eleição iniciado...");
        Thread thread;
        thread = new Thread(new Election());
        thread.start();
    }
    
    private void createSession(String name){
        //Criar sessao local:
        Session session;
        int pos;
        pos = isSessionAlreadAdded(myPeer);
        if (pos == -1){
            System.out.println(" Sessão local criada com sucesso.");
            session = new Session(this, myPeer);
            sessions.add(session);
        } else{
            session = sessions.get(pos);
        }
        session.setMidia(name);
        //Requisitar uma sessao no lider:
        if (!leader){
            System.out.println(" Requisitar sessão no peer líder...");
            sender.sendWithCount(new CRTSESSION(myPeer, name), pleader, ACK.ACK_CRT_SESSION, true, this);
        } else{
            session.peersWithMedia();
        }
    }
    
    public void getMidia(String name){
        waitElection();
        System.out.println(" Preparando o sistema para receber arquivo de mídia...");
        createSession(name);
    }
    
    public void waitElection(){
        boolean msg = false;
        while(!electionFinished){
            if (!msg){
                msg = true;
                 new Message().infoMessage("Processo de eleição em andamento, aguardar...");
            }
            System.out.println(" Aguardando finalização de eleição para peer líder...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                new Message().errorMessage(ex.getMessage());
            }
        }
    }
    
    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof Receiver){
            try {
                receiveAction(((Receiver) o).getPacket());
            } catch (IOException | ClassNotFoundException ex) {
                System.err.println(ex.getMessage());
                //new Message().errorMessage(ex.getMessage());
            }
        } else if (o instanceof Sender.SendWithCount){
            receiveNotify((Sender.SendWithCount)o);
        }
    }
    
    private void receiveAction(Object packet){
        //Pacote ACK:
        if (packet instanceof ACK){
            sender.addAck((ACK)packet);
        }
        //Pacote Eleicao(ELECT):
        else if (packet instanceof ELECT){
            ELECT pkg = (ELECT) packet;
            Peer peer = new Peer(pkg.getAddr(), pkg.getPorta());
            if (pkg.getTime() < time){
                keepElection = false;
                leader = false;
                if (pleader != null){
                    if (pleaderTime > pkg.getTime()){
                        pleader = peer;
                        pleaderTime = pkg.getTime();
                    }
                } else{
                    pleader = peer;
                    pleaderTime = pkg.getTime();
                }
                sender.send(new ELECTPEER(myPeer), peer);
            } else{
                sender.sendWithCount(new ELECT(time, myPeer), peer, ACK.ACK_ELECT, false, this);
                if (electionFinished){
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException ex) {
                        System.err.println(ex.getMessage());
                    }
                    sender.send(new ELECTEND(myPeer), peer);
                }
                if (!leader && electionFinished){
                    startElection();
                }
            }
        }
        //Pacote Eleicao Registro de Peer(ELECTPEER):
        else if (packet instanceof ELECTPEER){
            ELECTPEER pkg = (ELECTPEER) packet;
            Peer peer = new Peer(pkg.getAddr(), pkg.getPorta());
            sender.addAck(new ACK(ACK.ACK_ELECT, System.currentTimeMillis(), peer));
            synchronized(peers){
                if (!isPeerAlreadAdded(peer)){
                    peers.add(peer);
                }
            }
        }
        //Pacote Eleicao Finalisada(ELECTEND):
        else if (packet instanceof ELECTEND){
            System.out.println(" Eleição finalizada!");
            electionFinished = true;
            keepElection = false;
            ELECTEND pkg = (ELECTEND) packet;
            Peer peer = new Peer(pkg.getAddr(), pkg.getPorta());
            sender.send(new ACK(ACK.ACK_ELECT_END, System.currentTimeMillis(), myPeer), peer);
        }
        //Pacote Sessao, criar(CRTSESSION):
        else if (packet instanceof CRTSESSION){
            CRTSESSION pkg = (CRTSESSION) packet;
            Peer peer = new Peer(pkg.getAddr(), pkg.getPorta());
            if (!packetReceiver.isReceived(pkg)){
                Session session;
                int pos;
                pos = isSessionAlreadAdded(peer);
                if (pos == -1){
                    System.out.println(" Criar sessão para Peer: (" + peer.getAddr().getHostAddress() + ", " + peer.getPort() + ")");
                    session = new Session(this, peer);
                    sessions.add(session);
                } else{
                    session = sessions.get(pos);
                }
                session.setMidia(pkg.getName());
                session.peersWithMedia();
            }
            sender.send(new ACK(ACK.ACK_CRT_SESSION, System.currentTimeMillis(), myPeer), peer);
        }
        //Pacote Midia, possui?(HASMD):
        else if (packet instanceof HASMD){
            HASMD pkg = (HASMD) packet;
            Peer peer = new Peer(pkg.getAddr(), pkg.getPorta());
            Session session = getSessionOfPeer(pkg.getPsession());
            if (session == null){
                System.out.println(" Criar sessão para Peer: (" + peer.getAddr().getHostAddress() + ", " + peer.getPort() + ")");
                session = new Session(this, pkg.getPsession());
                session.setMidia(pkg.getMidia());
                sessions.add(session);
            }
            sender.send(new HASMDRESP(myPeer, pkg.getPsession(), hasMidia(pkg.getMidia())), peer);
        }
        //Pacote Midia, resposta(HASMDRESP):
        else if (packet instanceof HASMDRESP){
            System.out.println(" Resposta sobre possuir mídia, recebida.");
            HASMDRESP pkg = (HASMDRESP) packet;
            Peer peer = new Peer(pkg.getAddr(), pkg.getPorta());
            sender.addAck(new ACK(ACK.ACK_HASMD, System.currentTimeMillis(), peer));
            if (!packetReceiver.isReceived(pkg)){
                Session session = getSessionOfPeer(pkg.getPsession());
                if (session != null){
                    session.addMediaInfo(pkg.getMidia());
                }
            }
            
        }
        //Pacote Midia List, lista de peer's que possuem midia solicitada(PMDLIST):
        else if (packet instanceof PMDLIST){
            System.out.println(" Lista de peer's que possuem mídia, recebida.");
            PMDLIST pkg = (PMDLIST) packet;
            Peer peer = new Peer(pkg.getAddr(), pkg.getPorta());
            sender.send(new ACK(ACK.ACK_PMDLIST, System.currentTimeMillis(), myPeer), peer);
            if (!packetReceiver.isReceived(pkg)){
                Session session = getSessionOfPeer(pkg.getPsession());
                if (session != null){
                    session.joinList(pkg.getMdlist());
                }
            }
            
        }
        //Pacote FILE, arquivo sendo solicitado:
        else if (packet instanceof GETFILE){
            System.out.println(" Requisição informações do arquivo, recebida");
            GETFILE pkg = (GETFILE) packet;
            Peer peer = new Peer(pkg.getAddr(), pkg.getPorta());
            MidiaUtil mUtil = new MidiaUtil();
            Session session = getSessionOfPeer(pkg.getPsession());
            if (session != null){
                if (!packetReceiver.isReceived(pkg)){
                    List<byte[]> file = mUtil.getFileByNumberToBytes(pkg.getName(), pkg.getNumber());
                    if (file != null){
                        session.setFileBytes(file);
                        session.setFileSize(file.size());
                    }
                }
                sender.send(new FILERESP(myPeer, pkg.getPsession(), pkg.getName(), pkg.getNumber(), session.getFileSize()), peer);
            } else{
                sender.send(new FILERESP(myPeer, pkg.getPsession(), pkg.getName(), pkg.getNumber(), 0), peer);
            }
        }
        //Pacote FILE, arquivo sendo solicitado, informando tamanho do arquivo:
        else if (packet instanceof FILERESP){
            System.out.println(" Informações do arquivo recebido");
            FILERESP pkg = (FILERESP) packet;
            Peer peer = new Peer(pkg.getAddr(), pkg.getPorta());
            Session session = getSessionOfPeer(pkg.getPsession());
            if (session != null){
                session.setFileSize(pkg.getSize());
                session.initFileBytes();
            }
            sender.addAck(new ACK(ACK.ACK_GETFILE, System.currentTimeMillis(), peer));
        }
        //Pacote FILE, arquivo sendo solicitado, informando tamanho do arquivo:
        else if (packet instanceof FILEREQ){
            FILEREQ pkg = (FILEREQ) packet;
            Peer peer = new Peer(pkg.getAddr(), pkg.getPorta());
            Session session = getSessionOfPeer(pkg.getPsession());
            List<Object> packs = new ArrayList<>();
            if (session != null){
                for(int i=pkg.getNumber(); i<pkg.getNumber() + window; i++){
                    if (i < session.getFileSize()){
                        packs.add(new FILEMD(myPeer, pkg.getPsession(), i, session.getFileBytes().get(i)));
                    }
                }
                sender.sendMultiplePacks(packs, peer);
            }
        }
        //Pacote FILE, arquivo sendo solicitado, informando tamanho do arquivo:
        else if (packet instanceof FILEMD){
            //System.out.println(" Parte de arquivo recebido...");
            FILEMD pkg = (FILEMD) packet;
            Peer peer = new Peer(pkg.getAddr(), pkg.getPorta());
            Session session = getSessionOfPeer(pkg.getPsession());
            if (session != null){
                session.addFileBytes(pkg.getNumber(), pkg.getBytes());
                if (session.verifyFileBytesForACK(session.getFileCount(), session.getFileCount() + window)){
                    System.out.println(" Pacote de mídia recebido(" + pkg.getNumber() + ") do Peer: " + peer.getAddr().getHostAddress() + ", " + peer.getPort());
                    sender.addAck(new ACK(ACK.ACK_FILEREQ, System.currentTimeMillis(), peer));
                }
            }
        }
        
    }
    
    private void receiveNotify(Sender.SendWithCount obj){
        
        switch(obj.getType()){
            //Apenas não lider solicita um ACK_CRT_SESSION
            case ACK.ACK_CRT_SESSION:
                if(!obj.isReceived()){
                    startElection();
                } else{
                    System.out.println(" Sessão no peer líder criada com sucesso.");
                }
                break;
        }
        
    }
    
    public MidiaInfo hasMidia(String name){
        MidiaUtil mUtil = new MidiaUtil();
        return mUtil.getAllFiles(myPeer, name);
    }
    
    private Session getSessionOfPeer(Peer peer){
        Session session = null;
        int pos;
        pos = isSessionAlreadAdded(peer);
        if (pos != -1){
            session = sessions.get(pos);
        }
        return session;
    }
    
    private int isSessionAlreadAdded(Peer peer){
        int pos = -1;
        for(int i=0; i<sessions.size(); i++){
            if (sessions.get(i).getPeer().getAddr().getHostAddress().equals(peer.getAddr().getHostAddress()) && sessions.get(i).getPeer().getPort() == peer.getPort()){
                pos = i;
            }
        }
        return pos;
    }
    
    private boolean isPeerAlreadAdded(Peer peer){
        if (peer.getAddr().getHostAddress().equals(myPeer.getAddr().getHostAddress()) && peer.getPort() == myPeer.getPort()){
            return true;
        } else{
            for(Peer peer1: peers){
                if (peer.getAddr().getHostAddress().equals(peer1.getAddr().getHostAddress()) && peer.getPort() == peer1.getPort()){
                    return true;
                }
            }
        }
        return false;
    }
    
    public void printPeerList(LinkedList<Peer> list){
        if (!list.isEmpty()){
            System.out.println(" Lista de peers:");
            for(Peer peer: list){
                System.out.println("    Addres: " + peer.getAddr().getHostAddress()+ " Port:" + peer.getPort());
            }
        }
    }
    
    private class Election implements Runnable{
        
        private final long sleep = 600;
        private final int limit = 30;

        @Override
        public void run() {
            ELECT packet;
            int count = 0;
            pleader = null;
            electionFinished = false;
            keepElection = true;
            packet = new ELECT(time, myPeer);
            while(keepElection && count < limit){
                System.out.println(" " + (count + 1) + " ... (" + limit + ")");
                for(int i=0; i<porta_list.size(); i++){
                    sender.broadcast(packet, porta_list.get(i));
                }
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ex) {
                    new Message().warningMessage(ex.getMessage());
                }
                count++;
            }
            if (keepElection){
                leader = true;
                electionFinished = true;
                P2PSO.SMANAGER.setLeader(isLeader());
                System.out.println(" Eleição finalizada:");
                System.out.println(" Lider: " + isLeader());
                synchronized(peers){
                    for(Peer p: peers){
                        sender.send(new ELECTEND(myPeer), p);
                    }
                }
            } else{
                P2PSO.SMANAGER.setLeader(isLeader());
                System.out.println(" Lider: " + isLeader());
                System.out.println(" Aguardando eleição ser finalizada pelo lider...");
            }
            keepElection = false;
        }
        
    }
     
}
