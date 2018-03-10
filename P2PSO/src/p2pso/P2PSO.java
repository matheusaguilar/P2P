/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2pso;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import server.Server;
import view.Message;
import controller.SceneManager;

/**
 *
 * @author Matheus
 */
public class P2PSO extends Application {
    
    public static SceneManager SMANAGER;
    public static final int WIDTH = 960;
    public static final int HEIGHT = 560;
    public static int myPort;
    public static List<Integer> peersPort;
    public static Server server;
    
    @Override
    public void start(Stage primaryStage) { 
        peersPort = new LinkedList<>();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
        SMANAGER = new SceneManager(primaryStage);
        
        try{
            Parent root = FXMLLoader.load(getClass().getResource("/view/InfoPeer.fxml")); // Carrega FXML
            Scene infoPeer = new Scene(root);
            SMANAGER.push(infoPeer);
            SMANAGER.show(); 
        } catch (IOException ex){
            new Message().errorMessage(ex.getMessage());
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    public static void setMyPort(Integer port){
        myPort = port;
    }
    
    public static void addPeer(Integer port){
        peersPort.add(port);
    }
   
}
