/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import model.MidiaInfo;
import model.MidiaUtil;
import model.Peer;
import p2pso.P2PSO;
import static p2pso.P2PSO.SMANAGER;
import packet.ACK;
import packet.FILEREQ;
import packet.GETFILE;
import packet.HASMD;
import packet.PMDLIST;
import player.MPlayer;
import view.Message;

/**
 *
 * @author Matheus
 */
public class Session implements Observer{
    
    private final Server server;
    private final Peer peer;
    private List<MidiaInfo> peersMediaList;
    private String midia;
    private int peersListSize;
    private int count;
    private int midiaReq;
    private List<Integer> fileNumbers;
    private int fileSize;
    private List<byte[]> fileBytes;
    private int fileCount;
    private MPlayer mplayer;
    
    public Session(Server server, Peer peer){
        this.server = server;
        this.peer = peer;
        this.peersListSize = 0;
        this.fileSize = 0;
        this.fileBytes = null;
        this.peersMediaList = new LinkedList<>();
        this.midia = null;
    }

    public Peer getPeer() {
        return peer;
    }
    
    public String getMidia() {
        return midia;
    }

    public List<MidiaInfo> getPeersMediaList() {
        return peersMediaList;
    }

    public void setPeersMediaList(List<MidiaInfo> peersMediaList) {
        this.peersMediaList = peersMediaList;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getFileCount() {
        return fileCount;
    }
    
    public void initFileBytes(){
        fileBytes = new ArrayList<>();
        for(int i=0; i<fileSize; i++){
            fileBytes.add(null);
        }
    }
    
    public boolean verifyFileBytesForACK(int start, int end){
        if (end >= fileSize){
            end = fileSize - 1;
        }
        for(int i=start; i<end; i++){
            if (fileBytes.get(i) == null){
                return false;
            }
        }
        return true;
    }

    public List<byte[]> getFileBytes() {
        return fileBytes;
    }
    
    public void setFileBytes(List<byte[]> fileBytes) {
        this.fileBytes = fileBytes;
    }
    
    public void addFileBytes(int position, byte[] bytes){
        if (bytes != null){
            fileBytes.set(position, bytes);
        } else{
            System.out.println("Erro, bytes null...");
        }
    }
    
    public void setMidia(String midia) {
        this.clear();
        this.midia = midia;
        MidiaInfo midiaInfo = server.hasMidia(midia);
        if (midiaInfo != null){
            peersMediaList.add(midiaInfo);
        }
    }
    
    public void joinList(List<MidiaInfo> mdlist){
        int pos; 
        for(MidiaInfo m: mdlist){
            pos = isPeerAlreadAdded(m.getPeer());
            if (pos == -1){
                peersMediaList.add(m);
            } else{
                if (m.getQuant().size() > peersMediaList.get(pos).getQuant().size()){
                    peersMediaList.set(pos, m);
                }
            }
        }
        requestMidia();
    }
    
    //Apenas o lider executa esse metodo(adiciona midia na lista):
    public void addMediaInfo(MidiaInfo mediaInfo){
        int pos = isPeerAlreadAdded(mediaInfo.getPeer());
        if (pos == -1){
            peersMediaList.add(mediaInfo);
            count++;
        } else{
            if (mediaInfo.getQuant().size() > peersMediaList.get(pos).getQuant().size()){
                peersMediaList.set(pos, mediaInfo);
            }
        }
        startRequestMidia();
    }
    
    //Apenas o lider executa esse metodo(pede lista de peers que possuem midia):
    public void peersWithMedia(){
        peersListSize = server.getPeers().size();
        count = 0;
        if (!server.getPeers().isEmpty()){
            //Verificar se proprio peer esta adicionado na lista:
            for(Peer p: server.getPeers()){
                if (peer.getAddr().getHostAddress().equals(p.getAddr().getHostAddress()) && peer.getPort() == p.getPort()){
                    count++;
                }
            }
            if (server.getPeers().size() == 1 && count == 1){
                sendMidiaList();
            } else{
                System.out.println(" Pedindo resposta para peer's se possuem mídia solicitada...");
                for(Peer p: server.getPeers()){
                    if (!(peer.getAddr().getHostAddress().equals(p.getAddr().getHostAddress()) && peer.getPort() == p.getPort())){
                        server.getSender().sendWithCount(new HASMD(server.getMyPeer(), peer, midia), p, ACK.ACK_HASMD, false, this);
                    }
                }
            }
        } else{
            System.out.println(" Não existem peer's para realizar a solicitação de mídia.");
            if (isLocalSession()){
                if (!peersMediaList.isEmpty()){
                    if (peersMediaList.size() == 1){
                        if (peersMediaList.get(0).getQuant().isEmpty()){
                            new Message().warningMessage("Não há peer's para realizar busca, nem arquivo local para ser reproduzido");
                        } else{
                            startMidia();
                        }
                    } else{
                        startMidia();
                    }
                }
            }
        }
    }
   
    @Override
    public void update(Observable o, Object arg) {
        Sender.SendWithCount obj = (Sender.SendWithCount)o;
        int pos;
        switch(obj.getType()){
            //Apenas o lider solicita um ACK_HASMD
            case ACK.ACK_HASMD:
                if(!obj.isReceived()){
                    System.out.println("Peer removido: (" + obj.getPeer().getAddr().getHostAddress() + ", " + obj.getPeer().getPort() + ")");
                    pos = removePeerList(obj.getPeer());
                    if(pos != -1){
                        synchronized(server.getPeers()){
                            server.getPeers().remove(pos);
                            peersListSize--;
                            startRequestMidia();
                        }
                    }
                }
                break;
                
            case ACK.ACK_PMDLIST:
                if(!obj.isReceived()){
                    System.out.println("Peer removido: (" + obj.getPeer().getAddr().getHostAddress() + ", " + obj.getPeer().getPort() + ")");
                    pos = removePeerList(obj.getPeer());
                    if(pos != -1){
                        synchronized(server.getPeers()){
                            server.getPeers().remove(pos);
                        }
                    }
                }
                break;
                
            case ACK.ACK_GETFILE:
                if(!obj.isReceived()){
                    if (obj.isForLeader()){
                        new Message().errorMessage(" Líder removido da lista de requisições, nova eleição iniciada.");
                        pos = removePeerListMidia(obj.getPeer());
                        if(pos != -1){
                            synchronized(peersMediaList){
                                peersMediaList.remove(pos);
                                peersListSize--;
                            }
                        }
                        server.startElection();
                    } else{
                        new Message().errorMessage(" Peer removido da lista de requisições de mídia: (" + obj.getPeer().getAddr().getHostAddress() + ", " + obj.getPeer().getPort() + ")");
                        pos = removePeerListMidia(obj.getPeer());
                        if(pos != -1){
                            synchronized(peersMediaList){
                                peersMediaList.remove(pos);
                                peersListSize--;
                            }
                        }
                    }
                    new Message().errorMessage("Reiniciando solicitação de mídia...");
                    server.getMidia(midia);
                } else{
                    fileCount = 0;
                    startRequestFile(obj.getPeer());
                }
                break;
                
                case ACK.ACK_FILEREQ:
                if(!obj.isReceived()){
                    if (obj.isForLeader()){
                        new Message().errorMessage(" Líder removido da lista de requisições, nova eleição iniciada.");
                        pos = removePeerListMidia(obj.getPeer());
                        if(pos != -1){
                            synchronized(peersMediaList){
                                peersMediaList.remove(pos);
                                peersListSize--;
                            }
                        }
                        server.startElection();
                    } else{
                        new Message().errorMessage(" Peer removido da lista de requisições de mídia: (" + obj.getPeer().getAddr().getHostAddress() + ", " + obj.getPeer().getPort() + ")");
                        pos = removePeerListMidia(obj.getPeer());
                        if(pos != -1){
                            synchronized(peersMediaList){
                                peersMediaList.remove(pos);
                                peersListSize--;
                            }
                        }
                    }
                    new Message().errorMessage("Reiniciando solicitação de mídia...");
                    server.getMidia(midia);
                } else{
                    fileCount = fileCount + server.getWindow();
                    if (fileCount >= fileSize){
                        MidiaUtil mUtil = new MidiaUtil();
                        try {
                            mUtil.saveFile(midia, fileNumbers.get(midiaReq), fileBytes);
                            fileBytes.clear();
                            peersMediaList.get(0).getQuant().add(fileNumbers.get(midiaReq));
                            getNextMidia(fileNumbers);
                        } catch (IOException ex) {
                            System.err.println(ex.getMessage());
                            new Message().errorMessage("Erro ao salvar mídia.");
                        }
                        
                    } else{
                        startRequestFile(obj.getPeer());
                    }
                }
                break;
        }
    }
    
    private void startMidia(){
        System.out.println(" Iniciar mídia local...");
        MidiaUtil mUtil = new MidiaUtil();
        List<File> files = mUtil.getAllFiles(midia);
        if (!files.isEmpty()){
            mplayer = new MPlayer(SMANAGER.getScreen());
            for(int i = 0; i < files.size(); i++){
                 mplayer.addMedia(new Media(files.get(i).toURI().toString()));
            }
            Scene scene = new Scene(mplayer, P2PSO.WIDTH, P2PSO.HEIGHT);
            SMANAGER.push(scene);
        }
    }
    
    private void startRequestFile(Peer p){
        if (!server.isLeader()){
            if (p.getAddr().getHostAddress().equals(server.getPleader().getAddr().getHostAddress()) && p.getPort() == server.getPleader().getPort()){
                server.getSender().sendWithCount(new FILEREQ(server.getMyPeer(), peer, fileCount), p, ACK.ACK_FILEREQ, true, this);
            } else{
                server.getSender().sendWithCount(new FILEREQ(server.getMyPeer(), peer, fileCount), p, ACK.ACK_FILEREQ, false, this);
            }
        } else{
            server.getSender().sendWithCount(new FILEREQ(server.getMyPeer(), peer, fileCount), p, ACK.ACK_FILEREQ, false, this);
        }
    }
    
    private void requestMidia(){  
        peersListSize = peersMediaList.size();
        count = 1;
        midiaReq = 0;
        fileSize = 0;
        fileNumbers = sortListOfFile();
        
        if (!fileNumbers.isEmpty()){
            //Caso haja arquivos para reproduzir, adicionar player:
            Platform.runLater(new Runnable() {
                @Override 
                public void run() {        
                    mplayer = new MPlayer(SMANAGER.getScreen());
                    Scene scene = new Scene(mplayer, P2PSO.WIDTH, P2PSO.HEIGHT);
                    SMANAGER.push(scene);
                }
            });
            
            //Requisitar Mídia:
            new Message().infoMessage("Aguarde a busca do arquivo de mídia(" + midia + ")\nTotal de partes: " + fileNumbers.size() + "\n...");
            getNextMidia(fileNumbers);
            
        } else{
            new Message().warningMessage("Não há arquivos de mídia para serem solicitados ou reproduzidos.");
        }
    }
    
    private void sendMidiaList(){
        System.out.println(" Enviar lista para peer: (" + peer.getAddr().getHostAddress() + ", " + peer.getPort() + ")");
        server.getSender().sendWithCount(new PMDLIST(server.getMyPeer(), peer, peersMediaList), peer, ACK.ACK_PMDLIST, false, this);
    }
    
    private void startRequestMidia(){
        if (count == peersListSize){
            System.out.println(" Quantiade de peer's que possuem mídia: " + peersMediaList.size());
            if (isLocalSession()){
                requestMidia();
            } else{
                sendMidiaList();
            }
        }
    }
    
    private void getNextMidia(List<Integer> list){
        fileSize = 0;
        int max = list.size();
        MidiaUtil mUtil = new MidiaUtil();
        File file;
        Peer p;
        Media midiaFile;
        //Peer local:
        MidiaInfo local;
        local = peersMediaList.get(0);
        
        p = hasFileNumber(list.get(midiaReq));
        if (midiaReq < max){
            if (p != null){
                while(isLocalPeer(local.getPeer(), p)){
                    System.out.println(" Reproduzir arquivo local (" + midiaReq + ")");
                    file = mUtil.getFileByNumber(midia, list.get(midiaReq));
                    if (file != null){
                        midiaFile = new Media(file.toURI().toString());
                        RunLaterMidia runLater = new RunLaterMidia(midiaFile, list.get(list.size() - 1));
                        runLater.callLater();
                    }
                    midiaReq++;
                    if (midiaReq == max){
                        new Message().infoMessage("Arquivo recebido com sucesso");
                        break;
                    }
                    p = hasFileNumber(list.get(midiaReq));
                }
            }
        }
        if (midiaReq < max){
            if (p != null){
                System.out.println(" Peer para requisição (" + midiaReq + "):" + p.getAddr().getHostAddress() + ", " + p.getPort());
                if (!server.isLeader()){
                    if (p.getAddr().getHostAddress().equals(server.getPleader().getAddr().getHostAddress()) && p.getPort() == server.getPleader().getPort()){
                        server.getSender().sendWithCount(new GETFILE(server.getMyPeer(), peer, midia, list.get(midiaReq)), p, ACK.ACK_GETFILE, true, this);
                    } else{
                        server.getSender().sendWithCount(new GETFILE(server.getMyPeer(), peer, midia, list.get(midiaReq)), p, ACK.ACK_GETFILE, false, this);
                    }
                } else{
                    server.getSender().sendWithCount(new GETFILE(server.getMyPeer(), peer, midia, list.get(midiaReq)), p, ACK.ACK_GETFILE, false, this);
                }
            }
        }
        //Mensagem de recebimento de arquivo
        if (midiaReq >= max){
            new Message().infoMessage("Arquivo recebido com sucesso");
            this.clear();
        }
    }
    
    private boolean isLocalPeer(Peer local, Peer peer){
        return local == peer;
    }
    
    private List<Integer> sortListOfFile(){
        boolean added;
        List<Integer> list = new LinkedList<>();
        for(MidiaInfo m : peersMediaList){
            for(Integer i : m.getQuant()){
                added = false;
                for(Integer j : list){
                    if (Objects.equals(i, j)){
                        added = true;
                    }
                }
                if (!added){
                    list.add(i);
                }
            }
        }
        Collections.sort(list);
        
        return list;
    }
       
    private Peer hasFileNumber(int number){
        //Peer local:
        MidiaInfo local;
        local = peersMediaList.get(0);
        for(Integer i: local.getQuant()){
            if (i == number){
                return local.getPeer();
            }
        }
        //Caso nao haja o arquivo local, retornar peer que o possui:
        for(int i=count; i<peersListSize; i++){
            for(Integer j: peersMediaList.get(i).getQuant()){
                if (j == number){
                    count++;
                    if(count == peersListSize){
                        count = 1;
                    }
                    return peersMediaList.get(i).getPeer();
                }
            }
        }
        
        //Caso não o tenha encontrado, verificar todos os peers:
        count = 1;
        for(int i=count; i<peersListSize; i++){
            for(Integer j: peersMediaList.get(i).getQuant()){
                if (j == number){
                    count++;
                    if(count == peersListSize){
                        count = 1;
                    }
                    return peersMediaList.get(i).getPeer();
                }
            }
        }
        
        
        return null;
    }
 
    private boolean isLocalSession(){
        return server.getMyPeer().getAddr().getHostAddress().equals(peer.getAddr().getHostAddress()) && server.getMyPeer().getPort() == peer.getPort();
    }
    
    private int isPeerAlreadAdded(Peer peer){
        int pos = -1;
        for(int i=0; i<peersMediaList.size(); i++){
            if (peersMediaList.get(i).getPeer().getAddr().getHostAddress().equals(peer.getAddr().getHostAddress()) && peersMediaList.get(i).getPeer().getPort() == peer.getPort()){
                pos = i;
            }
        }
        return pos;
    }
    
    private int removePeerList(Peer p){
        int pos = -1;
        for(int i=0; i<server.getPeers().size(); i++){
            if (server.getPeers().get(i).getAddr().getHostAddress().equals(p.getAddr().getHostAddress()) && server.getPeers().get(i).getPort() == p.getPort()){
                pos = i;
            }
        }
        return pos;
    }
    
    private int removePeerListMidia(Peer p){
        int pos = -1;
        for(int i=0; i<peersMediaList.size(); i++){
            if (peersMediaList.get(i).getPeer().getAddr().getHostAddress().equals(p.getAddr().getHostAddress()) && peersMediaList.get(i).getPeer().getPort() == p.getPort()){
                pos = i;
            }
        }
        return pos;
    }
    
    private void clear(){
        peersMediaList.clear();
        midia = null;
        peersListSize = 0;
        count = 0;
        midiaReq = 0;
        if (fileNumbers != null){
            fileNumbers.clear();
        }
        fileSize = 0;
        fileBytes = null;
        fileCount = 0;
    }
    
    private class RunLaterMidia{
        
        private final Media mFile;
        private final int size;
        
        public RunLaterMidia(Media mFile, int size){
            this.mFile = mFile;
            this.size = size;
        }
        
        public void callLater(){
            Platform.runLater(new Runnable() {
                @Override 
                public void run() { 
                   mplayer.addMediaForStream(mFile, size);
                }
            });
        }
        
    }
    
}
