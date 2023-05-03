import java.io.*;
import java.util.*;
import java.net.*;

public class Server{ 
    public static void runServer(int port, int gridDim){
        try{
            ServerSocket ss = new ServerSocket(port);

            // Accept player 1
            Socket player1 = ss.accept();
            System.out.println("Player 1 Connected");
            // Accept player 2
            Socket player2 = ss.accept();
            System.out.println("Player 2 Connected");

            // Both player outputs
            PrintStream p1Out = new PrintStream(player1.getOutputStream());
            PrintStream p2Out = new PrintStream(player2.getOutputStream());

            // PlayerListener settings
            PlayerListener.grid = new int[gridDim][gridDim]; 

            // Start Listeners for both players
            new PlayerListener(player1, player2, 1).start();
            new PlayerListener(player2, player1, 2).start();

            p1Out.println("I accepted you");
            p2Out.println("I accepted you");

        } catch (IOException ex){
            System.out.println("Unable to bind to port " + port);
        }
    }

    public static void main(String[] args) {
        // Start up server
        int port = 5190;
        int gridDim = 10;
        System.out.println("Server Starting...");
        runServer(port, gridDim);
    }
}

class PlayerListener extends Thread{
    Socket playerSocket;
    Socket otherSocket;
    int playerID;
    Scanner listener;
    static int[][] grid;

    // Set up constants for grid state
    final static int EMPTY = 0;
    final static int PLAYER1 = 1;
    final static int PLAYER2 = 2;
    final static int COIN = 3;


    PlayerListener(Socket newPlayerSocket, Socket newOtherSocket, int newPlayerID){
        // Save both player sockets
        playerSocket = newPlayerSocket;
        otherSocket = newOtherSocket;

        // Set up listener
        try{
            listener = new Scanner(playerSocket.getInputStream());
        } catch (Exception e){
            System.out.println(playerSocket.getInetAddress() + " error" + e.getMessage());
        }

        // Init grid to all 0
        resetGrid();
    }

    public void resetGrid(){
        for (int i=0; i<grid.length; i++){
            for (int j=0; j<grid.length; j++){
                grid[i][j] = 0;
            }
        }
    }

    @Override
    public void run(){
        String command;
        System.out.println("Starting ProcessPlayer");
        while (listener.hasNext()){
            command = listener.nextLine();
            System.out.println(command);
        }
    }
}