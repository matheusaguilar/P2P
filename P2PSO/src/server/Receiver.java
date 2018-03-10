/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Observable;
import view.Message;

/**
 *
 * @author Matheus
 */
public class Receiver extends Observable{
    
    private final DatagramSocket server;
    private final Thread thread;
    private byte[] received;
    private final int bufferSize = 1536;
    
    public Receiver(DatagramSocket server){
        this.server = server;
        this.thread = new Thread(new ReceivePacket());
    }
    
    public void start(){
        thread.start();
    }
    
    public void interrupt(){
        thread.interrupt();
    }
    
    public Object getPacket() throws IOException, ClassNotFoundException{
        ByteArrayInputStream input = new ByteArrayInputStream(received);
        ObjectInputStream stream = new ObjectInputStream(input);
        return stream.readObject();
    }
    
    private class ReceivePacket implements Runnable{

        @Override
        public void run() {
            byte[] bytes;
            DatagramPacket packet;
         
            while(!Thread.interrupted()){
                bytes = new byte[bufferSize];
                packet = new DatagramPacket(bytes, bytes.length);
                try {
                    //Aguarda receber os pacotes:
                    server.receive(packet);
                    received = packet.getData();
                    //Notifica os observadores:
                    setChanged();
                    notifyObservers();
                } catch (IOException ex) {
                    new Message().errorMessage(ex.getMessage());
                }        
            }
        }
        
    }
    
}
