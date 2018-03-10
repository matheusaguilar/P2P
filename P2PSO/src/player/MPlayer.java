/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package player;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import p2pso.P2PSO;

/**
 *
 * @author Matheus
 */
public class MPlayer extends StackPane{
    
    private final Stage stage;
    private MediaPlayer mediaplayer;
    private final MediaView mediaview;
    private final Button buttonback, buttonplay, buttonfullscreen;
    private final Slider slider;
    private final Label labelmedia;
    private boolean started = false, fullscreen = false;
    private final MediaFile mediafile;
    private int position = 0;
    private int streamSise;
    
    public MPlayer(Stage stage){
        //Define estado para chamar FullScreen:
        this.stage = stage;
        
        //Cor Player:
        this.setStyle("-fx-background-color: #000000;");
        
        //Manipulador de arquivos:
        mediafile = new MediaFile();
        
        //Video Player:
        mediaview = new MediaView();
        final DoubleProperty width = mediaview.fitWidthProperty();
        final DoubleProperty height = mediaview.fitHeightProperty();
        width.bind(Bindings.selectDouble(mediaview.sceneProperty(), "width"));
        height.bind(Bindings.selectDouble(mediaview.sceneProperty(), "height"));
        mediaview.setPreserveRatio(true);
        this.getChildren().add(mediaview);
       
        //Pane para adicionar os menus de controle:
        final BorderPane borderpane = new BorderPane();
        final HBox menubottom = new HBox();
        menubottom.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        menubottom.setPadding(new Insets(10, 20, 10, 20));
        menubottom.setSpacing(35);
        
        //Botao Voltar:
        buttonback = new Button("Back");
        buttonback.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        buttonback.setPrefSize(100, 30);
        
        buttonback.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                dispose();
                P2PSO.SMANAGER.pop();
            }
        });
        
        //Botao Play:
        buttonplay = new Button("||");
        buttonplay.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        buttonplay.setPrefSize(40, 30);
        buttonplay.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                Status status;
                if (mediaplayer != null){
                    status = mediaplayer.getStatus();
                    if (status == Status.PAUSED || status == Status.READY || status == Status.STOPPED) {
                        buttonplay.setText("||");
                        mediaplayer.play();
                    } else {
                        buttonplay.setText(">");
                        mediaplayer.pause();
                    }
                } else{
                    nextMedia();
                }
            }
        });
        
        //Slider:
        slider = new Slider(1, 1, 1);
        slider.setMinWidth(100);
        slider.setMaxWidth(200);
        slider.setPadding(new Insets(10, 0, 0, 0));
        slider.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                if (slider.isValueChanging()) {
                    if ((int)slider.getValue() != position){
                        position = (int)slider.getValue();
                        buttonplay.setText("||");
                        labelmedia.setText(getLabelCountText());
                        if (mediaplayer != null){
                            mediaplayer.dispose();
                        }
                        mediafile.setPosition(position - 1);
                        mediaplayer = new MediaPlayer(mediafile.hasNext());
                        mediaview.setMediaPlayer(mediaplayer);
                        mediaplayer.play();
                        mediaplayer.setOnEndOfMedia(new Runnable() {
                            public void run() {
                                if (!nextMedia()){
                                    buttonplay.setText(">");
                                }
                            }
                        });
                    }
                }
            }
        });
        
        //Label para contar medias:
        labelmedia = new Label(getLabelCountText());
        labelmedia.setPadding(new Insets(5, 0, 0, 0));
        labelmedia.setStyle("-fx-background-color: #ffffff;");
        labelmedia.setFont(Font.font("Arial", FontWeight.THIN, 18));
        labelmedia.setPrefSize(60, 30);
        
        //Botao FullScreen:
        buttonfullscreen = new Button("[ ]");
        buttonfullscreen.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        buttonfullscreen.setPrefSize(50, 30);
        buttonfullscreen.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                if (fullscreen){
                    fullscreen = false;
                } else{
                    fullscreen = true;
                }
                stage.setFullScreen(fullscreen);
            }
        });
        
        //Label para espacamento:
        Label spacer = new Label();
        spacer.setPrefSize(150, 30);
        Label spacer1 = new Label();
        spacer1.setPrefSize(170, 30);
        
        //Adiciona elementos ao BorderPane:
        menubottom.getChildren().add(buttonback);
        menubottom.getChildren().add(spacer);
        menubottom.getChildren().add(buttonplay);
        menubottom.getChildren().add(slider);
        menubottom.getChildren().add(labelmedia);
        menubottom.getChildren().add(spacer1);
        menubottom.getChildren().add(buttonfullscreen);
        borderpane.setBottom(menubottom);
        
        //Adiciona BorderPane ao StackPane:
        this.getChildren().add(borderpane);
    }
    
    public void addMediaForStream(Media media, int streamSize){
        mediafile.addMedia(media);
        streamSise = streamSize;
        slider.setMax(mediafile.getMediasCount());
        labelmedia.setText(getLabelCountText());
        if (mediaplayer != null){
            if (!(mediaplayer.getStatus().equals(Status.UNKNOWN) || mediaplayer.getStatus().equals(Status.READY) || mediaplayer.getStatus().equals(Status.PLAYING))){
                nextMediaForStream();
            }
        } else{
            nextMediaForStream();
        }
    }
    
    private boolean nextMediaForStream(){
        boolean has = false;
      
        if (mediaplayer != null){
            mediaplayer.dispose();
        }
        Media media = mediafile.hasNext();
        if (media != null){
            has = true;
            position++;
            slider.setValue(position);
            labelmedia.setText(getLabelCountText());
            buttonplay.setText("||");
            addMediaToPlayerForStream(media);
        }
       
        return has;
    }
    
    private void addMediaToPlayerForStream(Media media){
        mediaplayer = new MediaPlayer(media);
        mediaview.setMediaPlayer(mediaplayer);
        mediaplayer.play();
        mediaplayer.setOnEndOfMedia(new Runnable() {
            public void run() {
                if (!nextMediaForStream()){
                    buttonplay.setText(">");
                    if (position == streamSise){
                        mediaplayer = null;
                        position = 0;
                        slider.setValue(position);
                        mediafile.resetMedias();
                    }
                }
            }
        });
    }
    
    
    public void addMedia(Media media){
        mediafile.addMedia(media);
        if (!started){
            started = true;
            nextMedia();
        } else{
            slider.setMax(mediafile.getMediasCount());
            labelmedia.setText(getLabelCountText());
        }
    }
    
    public void dispose(){
        if (mediaplayer != null){
            mediaplayer.dispose();
        }
    }
    
    private boolean nextMedia(){
        boolean has = false;
        if (mediaplayer != null){
            mediaplayer.dispose();
        }
        Media media = mediafile.hasNext();
        if (media != null){
            has = true;
            position++;
            slider.setValue(position);
            labelmedia.setText(getLabelCountText());
            buttonplay.setText("||");
            addMediaToPlayer(media);
        } else{
            mediaplayer = null;
            position = 0;
            slider.setValue(position);
            mediafile.resetMedias();
        }
        return has;
    }
    
    private void addMediaToPlayer(Media media){
        mediaplayer = new MediaPlayer(media);
        mediaview.setMediaPlayer(mediaplayer);
        mediaplayer.play();
        mediaplayer.setOnEndOfMedia(new Runnable() {
            public void run() {
                if (!nextMedia()){
                    buttonplay.setText(">");
                }
            }
        });
    }
    
    private String getLabelCountText(){
        return " " + position + "/" + mediafile.getMediasCount();
    }
    
}