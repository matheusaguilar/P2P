/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.util.Stack;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Matheus
 */
public class SceneManager{
    
    private final Stage screen;
    private final Stack<Scene> scenes;
    private BuscaPlayController bcontroller;
    
    public SceneManager(Stage stage){
        screen = stage;
        screen.setTitle("P2P");
        scenes = new Stack<>();
    }
    
    public void show(){
        screen.show();
    }
    
    public void push(Scene scene){
        scenes.push(scene);
        updateScene();
    }
    
    public void pop(){
        scenes.pop();
        updateScene();
    }
    
    private void updateScene(){
        screen.setScene(scenes.peek());
    }

    public Stage getScreen() {
        return screen;
    }
    
    public void setController(BuscaPlayController bcontroller){
        this.bcontroller = bcontroller;
    }
    
    public void setLeader(boolean leader){
        bcontroller.setLeader(leader);
    }

}
