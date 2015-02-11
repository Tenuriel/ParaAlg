/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Tim Pontzen
 */
public class Sender {

    /**
     * IP of maschiene hosting the IndexHandler jar
     */
    private String ip;
    /**
     * Port to connect to
     */
    private int port;
    /**
     * connecting socket
     */
    private Socket socket;
    
    
    public static void main(String[] args){
        Sender s= new Sender(5000, "localhost");
        s.connect();
        s.transaction(null);
    }
    
    public Sender() {
        ip = JOptionPane.showInputDialog("Enter IP of IndexHandler");
        port = Integer.valueOf(JOptionPane.showInputDialog("Enter Port"));
    }

    public Sender(int port, String ip) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * connects to the other application. returns false if unsuccessfull
     *
     * @return
     */
    public boolean connect() {
        try {
            socket = new Socket(ip, port);

        } catch (IOException ex) {
            return false;
        } catch (IllegalArgumentException ex) {
            return false;
        }
        return true;
    }

    /**
     * Sends the data and returns the processd data.
     * Tgis method will block until it recieves the data.
     * @param input
     * @return 
     */
    public List<Pair<String, Double>> transaction(HashMap<String, Double>[] input) {
        if (!socket.isClosed()) {
            try {
                ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());

                os.writeObject(input);
                List<Pair<String, Double>> ret=(List<Pair<String, Double>>) is.readObject();
//                socket.close();
                return ret;
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            } catch (ClassNotFoundException ex) {
                System.out.println(ex.getMessage());
            }

        }
        return new ArrayList<Pair<String, Double>>();
    }
}
