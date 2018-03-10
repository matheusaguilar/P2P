/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import model.Peer;
import packet.ACK;
import view.Message;

/**
 *
 * @author Matheus
 */
public class Sender extends Observable{
    
    private final List<ACK> ackList;
    
    public Sender(){
        ackList = new LinkedList<>();
    }
    
    public void send(Object obj, Peer peer){
        Thread thread;
        thread = new Thread(new Send(obj, peer));
        thread.start();
        
    }
    
    public void sendWithCount(Object obj, Peer peer, int type, boolean forLeader, Observer observer){
        SendWithCount send = new SendWithCount(obj, peer, type, forLeader);
        send.addObserver(observer);
        Thread thread = new Thread(send);
        thread.start();
    }
    
    public void sendMultiplePacks(List<Object> list, Peer peer){
        for(Object o: list){
            send(o, peer);
        }
    }
    
    public void addAck(ACK ack){
        synchronized(ackList){
            ackList.add(ack);
        }
    }
    
    public void broadcast(Object obj, int porta){
        Thread thread;
        thread = new Thread(new SendBroadcast(obj, porta));
        thread.start();
    }
    
    private byte[] objectToByte(Object obj) throws IOException{
        ByteArrayOutputStream input = new ByteArrayOutputStream();
        ObjectOutputStream stream = new ObjectOutputStream(input);
        stream.writeObject(obj);
        stream.flush();
        return input.toByteArray();
    }
    
    private class Send implements Runnable{
        
        private final Object obj;
        private final Peer peer;
        
        public Send(Object obj, Peer peer){
            this.obj = obj;
            this.peer = peer;
        }

        @Override
        public void run() {
            byte[] buffer;
            DatagramPacket packet;
            DatagramSocket socket;
            try {
                socket = new DatagramSocket(null);
                socket.connect(peer.getAddr(), peer.getPort());
                buffer = objectToByte(obj);
                packet = new DatagramPacket(buffer, buffer.length, peer.getAddr(), peer.getPort());
                socket.send(packet);
                socket.disconnect();
            }  catch (IOException ex) {
                new Message().errorMessage(ex.getMessage());
            }
        }
        
    }
    
    public class SendWithCount extends Observable implements Runnable{
        
        private final Object obj;
        private final Peer peer;
        private final int type;
        private final boolean forLeader;
        private final int max = 300;
        private final long sleepTime = 100;
        private final long ackLimitTime = 15000;
        private boolean received;
        
        public SendWithCount(Object obj, Peer peer, int type, boolean forLeader){
            this.obj = obj;
            this.peer = peer;
            this.type = type;
            this.forLeader = forLeader;
            this.received = false;
        }

        @Override
        public void run() {
            Thread thread;
            int i = 0;
            
            while(i < max && !received){
                //Enviar pacote:
                thread = new Thread(new Send(obj, peer));
                thread.start();
                //Verificar se foi recebido ACK:
                synchronized(ackList){
                    for(int j=0; j<ackList.size(); j++){
                        if (ackList.get(j).getTime() > System.currentTimeMillis() + ackLimitTime){
                            ackList.remove(j);
                            j--;
                        }
                        if (ackList.get(j).getAck() == type){
                            if(ackList.get(j).getAddr().getHostAddress().equals(peer.getAddr().getHostAddress()) && ackList.get(j).getPorta() == peer.getPort()){
                                ackList.remove(j);
                                j--;
                                received = true;
                            }
                        }
                        /*
                        if (!received){
                            if (ackList.get(j).getTime() > System.currentTimeMillis() + ackLimitTime){
                                ackList.remove(j);
                                j--;
                            }
                        }*/
                    }
                }
                //Aguardar 1 seg para reenviar:
                try {
                    //System.out.println(" Contador para receber ACK: " + (i + 1));
                    Thread.currentThread().sleep(sleepTime);
                } catch (InterruptedException ex) {
                    new Message().errorMessage(ex.getMessage());
                }
                i++;
            }
            setChanged();
            notifyObservers();
        }

        public int getType() {
            return type;
        }

        public boolean isReceived() {
            return received;
        }
        
        public boolean isForLeader() {
            return forLeader;
        }

        public Peer getPeer() {
            return peer;
        }
        
    }
        
    private class SendBroadcast implements Runnable{
        
        private final Object obj;
        private final int porta;
        
        public SendBroadcast(Object obj, int porta){
            this.obj = obj;
            this.porta = porta;
        }

        @Override
        public void run() {
            byte[] buffer;
            DatagramPacket packet;
            DatagramSocket socket;
            try {
                socket = new DatagramSocket(null);
                socket.setBroadcast(true);
                buffer = objectToByte(obj);
                for(InetAddress addr : listAllBroadcastAddresses()){
                    socket.connect(addr, porta);
                    packet = new DatagramPacket(buffer, buffer.length, addr, porta);
                    socket.send(packet);
                    socket.disconnect();
                }
                socket.setBroadcast(false);
            } catch (SocketException | UnknownHostException ex) {
                new Message().errorMessage(ex.getMessage());
            } catch (IOException ex) {
                new Message().errorMessage(ex.getMessage());
            }
        }
        
        private List<InetAddress> listAllBroadcastAddresses() throws SocketException {
            List<InetAddress> broadcastList = new ArrayList<>();
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                networkInterface.getInterfaceAddresses().stream().map(a -> a.getBroadcast()).filter(Objects::nonNull).forEach(broadcastList::add);
            }
            return broadcastList;
        }
        
    }
    
}
