/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Matheus
 */
public class MidiaUtil {
    
    private final int bufferSize = 1024;

    public MidiaUtil() {
    }
    
    public MidiaInfo getAllFiles(Peer myPeer, String name){
        String workingDir = System.getProperty("user.dir");
        File folder = new File(workingDir + "/midia/");
        File[] listOfFiles = folder.listFiles();
        List<Integer> list = new LinkedList<>();

        if (name != null){
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    if (getFileExtension(listOfFiles[i].getName()).equals("mp4")){
                        if (isFileName(name, listOfFiles[i].getName())){
                            list.add(getFileNumber(listOfFiles[i].getName()));
                        }
                    }
                }
            }
        }
        
        return new MidiaInfo(myPeer, list);
    }
    
    public List<File> getAllFiles(String name){
        String workingDir = System.getProperty("user.dir");
        File folder = new File(workingDir + "/midia/");
        File[] listOfFiles = folder.listFiles();
        List<File> list = new LinkedList<>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                if (getFileExtension(listOfFiles[i].getName()).equals("mp4")){
                    if (isFileName(name, listOfFiles[i].getName())){
                        list.add(listOfFiles[i]);
                    }
                }
            }
        }
        
        return list;
    }
    
    public File getFileByNumber(String name, int number){
        String workingDir = System.getProperty("user.dir");
        File folder = new File(workingDir + "/midia/");
        File[] listOfFiles = folder.listFiles();
        
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                if (getFileExtension(listOfFiles[i].getName()).equals("mp4")){
                    if (isFileName(name, listOfFiles[i].getName())){
                        if (getFileNumber(listOfFiles[i].getName()) == number){
                            return listOfFiles[i];
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    public List<byte[]> getFileByNumberToBytes(String name, int number){
        String workingDir = System.getProperty("user.dir");
        File folder = new File(workingDir + "/midia/");
        File[] listOfFiles = folder.listFiles();
        List<byte[]> bytesList = new ArrayList<>();
        byte[] bytes;
        FileInputStream fis;
        BufferedInputStream bis;
        
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                if (getFileExtension(listOfFiles[i].getName()).equals("mp4")){
                    if (isFileName(name, listOfFiles[i].getName())){
                        if (getFileNumber(listOfFiles[i].getName()) == number){
                            try {
                                fis = new FileInputStream(listOfFiles[i]);
                                bis = new BufferedInputStream(fis);
                                bytes = new byte[bufferSize];
                                while(bis.read(bytes) != -1){
                                    bytesList.add(bytes);
                                    bytes = new byte[bufferSize];
                                }
                                bis.close();
                                fis.close();
                                return bytesList;
                            } catch (IOException ex) {
                                System.err.println(ex.getMessage());
                                return null;
                            }
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    public void saveFile(String name, int number, List<byte[]> bytes) throws FileNotFoundException, IOException{
        String workingDir = System.getProperty("user.dir");
        try (FileOutputStream fos = new FileOutputStream(workingDir + "/midia/" + name + "-" + number + ".mp4")) {
            for(byte[] bt : bytes){
                fos.write(bt);
            }
            fos.flush();
            fos.close();
        }
    }
    
    private String getFileExtension(String name){
        String extension;
        int lastDot = 0;
        for(int i=0; i<name.length(); i++){
            if (name.charAt(i) == '.'){
                lastDot = i;
            }
        }
        if (lastDot != 0){
            extension = name.substring(lastDot + 1);
        } else{
            extension = name;
        }
        return extension;
    }
    
    private boolean isFileName(String required, String name){
        String compare;
        int lastDash = 0;
        for (int i=0; i<name.length(); i++){
            if (name.charAt(i) == '-'){
                lastDash = i;
            }
        }
        if (lastDash != 0){
            compare = name.substring(0, lastDash);
        } else{
            compare = name;
        }
        return required.equals(compare);
    }
    
    private int getFileNumber(String name){
        String number;
        int numb;
        int lastDash = 0;
        int lastDot = 0;
        for (int i=0; i<name.length(); i++){
            if (name.charAt(i) == '-'){
                lastDash = i;
            } else if (name.charAt(i) == '.'){
                lastDot = i;
            }
        }
        if (lastDash != 0 && lastDot != 0){
            number = name.substring(lastDash + 1, lastDot);
            try{
                numb = Integer.parseInt(number);
                return numb;
            } catch(NumberFormatException ex){
                System.err.println(ex.getMessage());
                return -1;
            }
        } else{
            return -1;
        }
    }
    
}
