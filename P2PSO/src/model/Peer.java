package model;

import java.io.Serializable;
import java.net.InetAddress;

    public class Peer implements Serializable{
        
        private static final long serialVersionUID = -1237489610617312345L; 
        private final InetAddress addr;
        private final int port;
        
        public Peer(InetAddress addr, int port){
            this.addr = addr;
            this.port = port;
        }

        public InetAddress getAddr() {
            return addr;
        }

        public int getPort() {
            return port;
        }
        
}