/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 *
 * @author Matheus
 */
public class Message {
    
    public void errorMessage(String error){
        if (!Platform.isFxApplicationThread()){
            Platform.runLater(new Runnable() {
                @Override 
                public void run() { 
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("Erro");
                    alert.setContentText(error);
                    alert.show();
                }
            });
        } else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro");
            alert.setContentText(error);
            alert.show();
        }
    }
    
    public void errorMessage(String header, String error){
        if (!Platform.isFxApplicationThread()){
            Platform.runLater(new Runnable() {
                @Override 
                public void run() { 
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText(header);
                    alert.setContentText(error);
                    alert.show();
                }
            });
        } else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(header);
            alert.setContentText(error);
            alert.show();
        }
    }
    
    public void warningMessage(String warning){
        if (!Platform.isFxApplicationThread()){
            Platform.runLater(new Runnable() {
                @Override 
                public void run() { 
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Warning");
                    alert.setHeaderText("Warning");
                    alert.setContentText(warning);
                    alert.show();
                }
            });
        } else{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Warning");
            alert.setContentText(warning);
            alert.show();
        }
    }
    
    public void warningMessage(String header, String warning){
        if (!Platform.isFxApplicationThread()){
            Platform.runLater(new Runnable() {
                @Override 
                public void run() { 
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Warning");
                    alert.setHeaderText(header);
                    alert.setContentText(warning);
                    alert.show();
                }
            });
        } else{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(header);
            alert.setContentText(warning);
            alert.show();
        }
    }
    
    public void infoMessage(String info){
        if (!Platform.isFxApplicationThread()){
            Platform.runLater(new Runnable() {
                @Override 
                public void run() { 
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Information");
                        alert.setHeaderText("Information");
                        alert.setContentText(info);
                        alert.show();
                }
            });
        } else{
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText("Information");
            alert.setContentText(info);
            alert.show();
        }
    }
    
     public void infoMessage(String header, String info){
        if (!Platform.isFxApplicationThread()){
            Platform.runLater(new Runnable() {
                @Override 
                public void run() { 
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information");
                    alert.setHeaderText(header);
                    alert.setContentText(info);
                    alert.show();
                }
            });
        } else{
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(header);
            alert.setContentText(info);
            alert.show();
        }
    }
    
}
