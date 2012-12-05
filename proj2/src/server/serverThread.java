/**
 * 
 */
package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;

import model.EventPackage;
import controller.Controller;

/**
 * @author gyz
 *
 */
public class serverThread extends Thread{

	/**
	 * @param server
	 * @param socket
	 * @param controller
	 */
	
    private final Socket socket;
    private final Server server;
    private Controller controller;
    public ObjectOutputStream toClient;
    public ObjectInputStream fromClient;
    
    
    public serverThread(Server server, Socket socket, Controller c) {
        this.socket = socket;
        this.server = server;
        this.controller=c;
    }
    
    public Socket getSocket(){
        return socket;
    }
    
    /**
     * Run the server, listening for client connections and handling them.  
     * Never returns unless an exception is thrown.
     * @throws IOException if the main server socket is broken
     * (IOExceptions from individual clients do *not* terminate serve()).
     */
    //thread object need to have "run" method
    public void run(){

        try {
            initialize(socket);
            try {
                handleConnection(socket);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void initialize(Socket socket) throws IOException {

        try {
            toClient = new ObjectOutputStream(socket.getOutputStream());
            toClient.writeObject(controller);
            toClient.flush();
            fromClient = new ObjectInputStream(socket.getInputStream());

        } 
        catch (Exception e){
        	e.printStackTrace();
        }


    }
    
    private void updateServer(EventPackage eventPackage) throws BadLocationException{
        AbstractDocument d = server.controller.getModel().getDoc();
        if (eventPackage.eventType.equals("INSERT")) {
            d.insertString(eventPackage.offset, eventPackage.inserted,
                    new SimpleAttributeSet());
        } else if (eventPackage.eventType.equals("REMOVE")) {

            d.remove(eventPackage.offset, eventPackage.len);
        }
        System.out.println(d.getText(0, d.getLength()));
    }
    
    private void updateClient(EventPackage eventPackage) throws Exception{
        for (serverThread t:server.threadlist){
//            ObjectOutputStream tothisClient = new ObjectOutputStream(t.getSocket().getOutputStream());
//            tothisClient.writeObject(eventPackage);
//            tothisClient.flush();
            System.out.println(server.threadlist.size());
            if (!this.equals(t))
              t.toClient.writeObject(eventPackage);
              t.toClient.flush();
        }
    }
    
    private void handleConnection(Socket socket) throws IOException, Exception {
        while (true) {
            EventPackage eventPackage = (EventPackage) fromClient.readObject();
            System.out.println("received update from client");
            updateServer(eventPackage);
            updateClient(eventPackage);
            

        }
    }

}
