
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import p2pso.P2PSO;
import static p2pso.P2PSO.SMANAGER;
import server.Server;
import view.Message;

/**
 * FXML Controller class
 *
 * @author andre
 */
public class InfoPeerController implements Initializable {
    
    
    @FXML
    private Button btnAvançar;

    @FXML
    private TextField txPeer;

    @FXML
    private TextField txOutrosPeer;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        btnAvançar.setOnMouseClicked((MouseEvent e)-> {
                if(txPeer.getText().equals("") || txOutrosPeer.getText().equals("")){
                    new Message().errorMessage("Erro ao informar os Peers.", "O Erro aconteceu devido a possiveis informações invalidas.");
                }else{
                    Parent root;
                    try {
                        P2PSO.setMyPort(Integer.parseInt(txPeer.getText()));
                        splitPeers(txOutrosPeer.getText());
                        
                        P2PSO.server = new Server(P2PSO.myPort, P2PSO.peersPort);
                        P2PSO.server.startReceiver();
                        P2PSO.server.startElection();
                        
                        root = FXMLLoader.load(getClass().getResource("/view/BuscaPlay.fxml")); // Carrega FXML
                        Scene menu = new Scene(root);
                        SMANAGER.push(menu);
                    } catch (IOException | NumberFormatException ex) {
                        new Message().errorMessage(ex.getMessage());
                    }
                }
            });        
    }    
    
    private void splitPeers(String text) throws NumberFormatException{
        String peer;
        int aux = 0;
        for(int i=0; i<text.length(); i++){
            if (text.charAt(i) == '-'){
                peer = text.substring(aux, i);
                aux = i + 1;
                P2PSO.addPeer(Integer.parseInt(peer));
            }
        }
        peer = text.substring(aux);
        P2PSO.addPeer(Integer.parseInt(peer));
    }

}
