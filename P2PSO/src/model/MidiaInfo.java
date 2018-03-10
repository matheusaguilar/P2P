/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Matheus
 */
public class MidiaInfo implements Serializable{
    
    private final Peer peer;
    private final List<Integer> quant;
    
    public MidiaInfo(Peer peer, List<Integer> quant){
        this.peer = peer;
        this.quant = quant;
    }

    public Peer getPeer() {
        return peer;
    }

    public List<Integer> getQuant() {
        return quant;
    }

}
