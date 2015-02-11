/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tim Pontzen
 */
public class Listener {

    private final Socket socket;
    private Thread executer;

    public Listener(Socket socket) {
        this.socket = socket;
    }

    public void listen() {
        executer = new Thread(() -> {
            ObjectInputStream is;
            try {
                is = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream os= new ObjectOutputStream(socket.getOutputStream());
                while (!executer.isInterrupted()) {
                    Object o=is.readObject();
                    if(o instanceof HashMap[]){
                        HashMap<String,Double>[] input = (HashMap<String,Double>[]) o;
                        
                        os.writeObject(LuceneHandler.processInput(input));
                        shutdown();
                    }
                }
            } catch (IOException | ClassNotFoundException | ClassCastException | InterruptedException ex) {
                System.out.println("Unexpected event while listening: "+ex.getMessage());
            }
        });
        executer.setDaemon(true);
        executer.start();

    }

    
    /**
     * clears up everything
     * @throws IOException socket had trouble closing
     * @throws InterruptedException socket is blocking
     */
    public void shutdown() throws IOException,InterruptedException {
//        executer.interrupt();
        socket.close();
    }

}
