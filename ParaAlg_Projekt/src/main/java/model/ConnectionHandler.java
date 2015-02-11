/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tim Pontzen
 */
public class ConnectionHandler {
    private ServerSocket serverSocket;
    private Socket socket;
    private int port;
    
    
    
    public ConnectionHandler(int port){
        this.port=port;
        try {
            serverSocket= new ServerSocket(port);
            socket= serverSocket.accept();
            Listener lis=new Listener(socket);
            lis.listen();
        } catch (IOException ex) {
            System.out.println("Connection Error: "+ex.getMessage());
        }
            
        
    }
}
