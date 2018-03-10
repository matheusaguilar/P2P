/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package packet;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Matheus
 */
public class PacketReceiver {
    
    private final List<PacketInfo> packs;
    private final long limit = 5000;
    
    public PacketReceiver(){
        packs = new LinkedList<>();
    }
    
    private synchronized void addPacket(PACKET packet){
        packs.add(new PacketInfo(packet, System.currentTimeMillis()));
    }
    
    private synchronized void update(){
        long time = System.currentTimeMillis();
        for(int i=0; i<packs.size(); i++){
            if(packs.get(i).getTime() + limit < time){
                packs.remove(i);
                i--;
            }
        }
    }
    
    public boolean isReceived(PACKET packet){
        update();
        for(PacketInfo p: packs){
            if(p.getPacket().getAddr().getHostAddress().equals(packet.getAddr().getHostAddress()) && p.getPacket().getPorta() == packet.getPorta()){
                if (p.getPacket() instanceof CRTSESSION && packet instanceof CRTSESSION){
                    return true;
                } else if (p.getPacket() instanceof HASMDRESP && packet instanceof HASMDRESP){
                    return true;
                } else if (p.getPacket() instanceof PMDLIST && packet instanceof PMDLIST){
                    return true;
                } else if (p.getPacket() instanceof GETFILE && packet instanceof GETFILE){
                    if (((GETFILE)p.getPacket()).getNumber() == ((GETFILE)packet).getNumber()){
                        return true;
                    }
                }
            }
        }
        addPacket(packet);
        return false;
    }
    
    private class PacketInfo{
        
        private PACKET packet;
        private long time;
        
        private PacketInfo(PACKET packet, long time){
            this.packet = packet;
            this.time = time;
        }

        public PACKET getPacket() {
            return packet;
        }

        public long getTime() {
            return time;
        }
        
    }
    
}
