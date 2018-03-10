/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.stage.FileChooser;
import p2pso.P2PSO;
import static p2pso.P2PSO.SMANAGER;
import player.MPlayer;
import view.Message;

/**
 * FXML Controller class
 *
 * @author andre
 */
public class BuscaPlayController implements Initializable {
    
    @FXML
    private TextArea txtBusca;
    
    @FXML
    private TextField txtLider;

    @FXML
    private TextField txtPeer;

    @FXML
    private TextField txtOutrosPeer;

    @FXML
    private Button btnBuscar;
    
    @FXML
    private Button btnAbrir;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        P2PSO.SMANAGER.setController(this);
        txtLider.setText("Aguardando...");
        txtLider.setEditable(false);
        
        txtPeer.setText(Integer.toString(P2PSO.myPort));
        txtPeer.setEditable(false);
        
        txtOutrosPeer.setText(concatPeersPort());
        txtOutrosPeer.setEditable(false);
        
        btnBuscar.setOnMouseClicked((MouseEvent e)-> { 
            if (!txtBusca.getText().equals("")){
                P2PSO.server.getMidia(txtBusca.getText());
            } else{
                new Message().warningMessage("É necessário informar o nome da mídia a ser buscada.");
            }
        });
    
        btnAbrir.setOnMouseClicked((MouseEvent e)-> { 
            FileChooser fileChooser = new FileChooser();
            String workingDir = System.getProperty("user.dir");
            File path = new File(workingDir + "/midia/");
            if(!path.canRead()){
                path = new File("c:/");
            }
            
            fileChooser.setInitialDirectory(path);
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Mp4 file please", "*.mp4");

            fileChooser.getExtensionFilters().add(filter);
            
            List<File> files = fileChooser.showOpenMultipleDialog(null);
            if (files != null){
                MPlayer mplayer = new MPlayer(SMANAGER.getScreen());
                for(int i = 0; i < files.size(); i++){
                     mplayer.addMedia(new Media(files.get(i).toURI().toString()));
                }
            
                Scene scene = new Scene(mplayer, P2PSO.WIDTH, P2PSO.HEIGHT);
                SMANAGER.push(scene);
            }
                    
        });
          
    }
    
    private String concatPeersPort(){
        String text = null;
        for(int i=0; i<P2PSO.peersPort.size(); i++){
            if (i != P2PSO.peersPort.size() - 1){
                if (text == null){
                    text = Integer.toString(P2PSO.peersPort.get(i)) + "-";
                } else{
                    text = text + Integer.toString(P2PSO.peersPort.get(i)) + "-";
                }
            } else{
                if (text == null){
                    text = Integer.toString(P2PSO.peersPort.get(i));
                } else{
                    text = text + Integer.toString(P2PSO.peersPort.get(i));
                }
            }
        }
        return text;
    }
    
    public void setLeader(boolean leader){
        txtLider.setText(Boolean.toString(leader));
    }
    
}
