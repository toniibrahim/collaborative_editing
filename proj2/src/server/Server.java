package server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import controller.Controller;



public class Server {
    private final ServerSocket serverSocket;
    /** True if the server should disconnect a client after a BOOM message. */
    private int numPlayers;
	private final Controller controller;

    /**
     * Make a MinesweeperServer that listens for connections on port.
     * @param port port number, requires 0 <= port <= 65535.
     * @param board 
     */
    public Server(int port, Controller c) throws IOException {
        serverSocket = new ServerSocket(port);
        this.numPlayers=0;
        this.controller=c;
    }

    
    public synchronized void increaseNumPlayers(){
        this.numPlayers++;
    }
    
    public synchronized void decreaseNumPlayers(){
        this.numPlayers--;
    }
    
    public synchronized int getNumPlayers() {
        return numPlayers;
    }
    
    public void serve() throws IOException {
        while (true) {
            // block until a client connects
            Socket socket = serverSocket.accept();

            // handle the client
            serverThread thread = new serverThread(this,socket, controller);
            thread.start();  
        }
    }
    
    public static void runServer(int port, Controller c)
            throws IOException
    {

        Server server = new Server(port, c);
        server.serve();
    }
    
    
	public static void main(final String[] args) {
		Controller c=new Controller();
        final int port=4441;
        try {
            runServer(port,c);
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}
}