/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package player;

import java.util.LinkedList;
import java.util.List;
import javafx.scene.media.Media;

/**
 *
 * @author Matheus
 */
public class MediaFile{
    
    private final List<Media> medias;
    private int position;
    
    public MediaFile(){
        medias = new LinkedList<>();
        position = 0;
    }
    
    public void addMedia(Media media){
        medias.add(media);
    }
    
    public Media hasNext(){
        Media media = null;
        if (!medias.isEmpty()){
            if (position < medias.size()){
                media = medias.get(position);
                position++;
            }
        }
        return media;
    }
    
    public Media getMedia(int i){
        return medias.get(i);
    }

    public void setPosition(int position) {
        this.position = position;
    }
    
    public int getMediasCount(){
        return medias.size();
    }
    
    public void resetMedias(){
        position = 0;
    }
    
}