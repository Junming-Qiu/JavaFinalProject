import java.io.*;
import java.util.*;
import java.net.*;

import java.util.random.*;
import java.util.stream.IntStream;

public class Server{ 
    // Game configurations
    static int port;
    static int gridDim;
    static double coinRatio;

    public static void runServer(int port){
        try{
            ServerSocket ss = new ServerSocket(port);

            // Accept player 1
            Socket player1 = ss.accept();
            System.out.println("Player 1 Connected");
            // Accept player 2
            Socket player2 = ss.accept();
            System.out.println("Player 2 Connected");

            // // Both player outputs
            // PrintStream p1Out = new PrintStream(player1.getOutputStream());
            // PrintStream p2Out = new PrintStream(player2.getOutputStream());

            // Initialize new grid
            GridItem.resetBoard(gridDim, coinRatio);
            GridItem.printGrid();
            
            // Start Listeners for both players
            PlayerListener p1 = new PlayerListener(player1, player2, 1);
            PlayerListener p2 = new PlayerListener(player2, player1, 2);
            p1.start();
            p2.start();

            try{
                p1.join();
                p2.join();
                ss.close();
            } catch (Exception e){
                System.out.println(e);
            }


        } catch (IOException ex){
            System.out.println("Unable to bind to port " + port);
        }
    }

    public static void main(String[] args) {
        // Start up server
        port = 5190;
        gridDim = 10;
        coinRatio = 0.2;
        System.out.println("Server Starting...");
        runServer(port);
    }
}

class PlayerListener extends Thread{
    Socket playerSocket;
    Socket otherSocket;
    int playerID;
    Scanner listener;
    PrintStream meOut;
    PrintStream otherOut;

    PlayerListener(Socket newPlayerSocket, Socket newOtherSocket, int newPlayerID){
        // Save both player sockets
        playerSocket = newPlayerSocket;
        otherSocket = newOtherSocket;
        playerID = newPlayerID;

        // Set up listener
        try{
            listener = new Scanner(playerSocket.getInputStream());
        } catch (Exception e){
            System.out.println(playerSocket.getInetAddress() + " error" + e.getMessage());
        }

        // Set up printStreams
        try{
            // Both player outputs
            meOut = new PrintStream(playerSocket.getOutputStream());
            otherOut = new PrintStream(otherSocket.getOutputStream());

            //meOut.println("I accepted you");
        } catch (IOException ex){
            System.out.println(ex); 
        }
    }

    @Override
    public void run(){       
        String command;
        System.out.println("Starting ProcessPlayer");
        while (listener.hasNext()){
            if (GridItem.hasCoin()){
                System.out.println("Player " + playerID);
                command = listener.nextLine();
                System.out.println(command);
                moveUpdate(command);
            } else {
                System.out.println("Game Done!");
                break;
            } 
        }
    }

    public synchronized void moveUpdate(String command){
        GridItem.moveMe(playerID, command);
        String state = GridItem.getState();
        meOut.println(state);
        otherOut.println(state);
        GridItem.printGrid();
    }
}

// Threadsafe wrapper for grid
class GridItem{
    // Set up constants for grid state
    final static int EMPTY = 0;
    final static int PLAYER1 = 1;
    final static int PLAYER2 = 2;
    final static int COIN = 3;

    // Raw data structure for grid
    private static int[][] grid;
    private static int gridDim;

    public static synchronized void resetBoard(int newGridDim, Double coinRatio){
        gridDim = newGridDim;

        grid = new int[gridDim][gridDim];
        Random rand = new Random();
        
        // First pass, disperse coins according to ratio
        for (int i=0; i<gridDim; i++){
            for (int j=0; j<gridDim; j++){
                int coinVal = rand.nextInt(100);
                boolean makeCoin = coinVal <= (100 * coinRatio);

                if (makeCoin){
                    grid[i][j] = GridItem.COIN;
                } else {
                    grid[i][j] = GridItem.EMPTY;
                }
            }
        }

        // Place players on opposite corners
        grid[0][0] = GridItem.PLAYER1;
        grid[gridDim-1][gridDim-1] = GridItem.PLAYER2;
    }

    // Debug function to visualize grid without UI
    public static synchronized void printGrid(){
        for (int i=0; i<grid.length; i++){
            for (int j=0; j<grid.length; j++){
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
    }

    // Serialized return of grid
    public static synchronized String getState(){        
        String output = "";
        for (int i=0; i<grid.length; i++){
            for (int j=0; j<grid.length; j++){
                output = output + (grid[i][j]) + "C";
            }
            output = output.substring(0, output.length()-1) + "R";
        } 

        output = output.substring(0, output.length()-1); 

        return output;
    }

    // Checks if spot to x direction of player has a coin, respecting boundaries of grid
    public static synchronized boolean hasCoin(){
        for (int i=0; i<grid.length; i++){
            for (int j=0; j<grid.length; j++){
                if (grid[i][j] == COIN){
                   return true;
                }
            }
        }  

        return false;
    }

    // Moves me up, down, left, or right and collects coin if there is any
    // Respects grid boundary
    public static synchronized void moveMe(int playerNum, String direction){
        // Find where player is
        int[] position = new int[2];
        for (int i=0; i<grid.length; i++){
            for (int j=0; j<grid.length; j++){
                if (grid[i][j] == playerNum){
                    position[0] = i;
                    position[1] = j;
                }
            }
        } 

        // Movement logic
        if (direction.equals("LEFT")){
            if (position[1] == 0) return;

            grid[position[0]][position[1]] = EMPTY;
            grid[position[0]][position[1] - 1] = playerNum;

        } else if (direction.equals("RIGHT")){
            if (position[1] == grid[0].length - 1) return;

            grid[position[0]][position[1]] = EMPTY;
            grid[position[0]][position[1] + 1] = playerNum;

        } else if (direction.equals("UP")){
            if (position[0] == 0) return;

            grid[position[0]][position[1]] = EMPTY;
            grid[position[0] - 1][position[1]] = playerNum;

        } else if (direction.equals("DOWN")) {
            if (position[0] == grid.length - 1) return;

            grid[position[0]][position[1]] = EMPTY;
            grid[position[0] + 1][position[1]] = playerNum;
        }
            
    }
}