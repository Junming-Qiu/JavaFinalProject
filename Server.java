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
    static int timerSecs;
    static ServerSocket ss;

    public static void runServer(int port){
        try{
            // Accept player 1
            Socket player1 = ss.accept();
            System.out.println("Player 1 Connected");
            // Accept player 2
            Socket player2 = ss.accept();
            System.out.println("Player 2 Connected");

            // Both player outputs
            PrintStream p1Out = new PrintStream(player1.getOutputStream());
            PrintStream p2Out = new PrintStream(player2.getOutputStream());

            PlayerListener p1 = new PlayerListener(player1, player2, 1);
            PlayerListener p2 = new PlayerListener(player2, player1, 2);

            // Start timer
            Timer t = new Timer(timerSecs * 1000, p1Out, p2Out, p1, p2);
            System.out.println(Timer.getTime());

            t.start();

            // Initialize new grid
            GridItem.resetBoard(gridDim, coinRatio);
            GridItem.printGrid();
            p1Out.println(GridItem.getState());
            p2Out.println(GridItem.getState());

            // Start Listeners for both players
            p1.start();
            p2.start();
            
            try{
                p1.join();
                p2.join();
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
        timerSecs = 30;

        try{
            ss = new ServerSocket(port);
        } catch (Exception ex){
            System.out.println(ex);
        }


        while (true) {
            System.out.println("Server Starting...");
            runServer(port);
        }
        
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

            System.out.println("Player " + playerID);
            command = listener.nextLine();
            if (command.equals("STOP")) break;


            System.out.println(command);
            moveUpdate(command);

            // Win game if cleared
            if (!GridItem.hasCoin()) {
                meOut.println("DONE=1");
                otherOut.println("DONE=1");
                System.out.println("Game Done!");
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

        output = "STATE=" + Timer.getTime() + "T" + output; 

        return output;
    }

    // Checks if board has coins
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
            // Can't move into wall
            if (position[1] == 0) return;

            // Can't move into another player
            if (grid[position[0]][position[1] - 1] == GridItem.PLAYER1) return;
            if (grid[position[0]][position[1] - 1] == GridItem.PLAYER2) return; 
            
            grid[position[0]][position[1]] = EMPTY;
            grid[position[0]][position[1] - 1] = playerNum;

        } else if (direction.equals("RIGHT")){
            if (position[1] == grid[0].length - 1) return;
            if (grid[position[0]][position[1] + 1] == GridItem.PLAYER1) return;
            if (grid[position[0]][position[1] + 1] == GridItem.PLAYER2) return; 

            grid[position[0]][position[1]] = EMPTY;
            grid[position[0]][position[1] + 1] = playerNum;

        } else if (direction.equals("UP")){
            if (position[0] == 0) return;
            if (grid[position[0] - 1][position[1]] == GridItem.PLAYER1) return;
            if (grid[position[0] - 1][position[1]] == GridItem.PLAYER2) return; 

            grid[position[0]][position[1]] = EMPTY;
            grid[position[0] - 1][position[1]] = playerNum;

        } else if (direction.equals("DOWN")) {
            if (position[0] == grid.length - 1) return;
            if (grid[position[0] + 1][position[1]] == GridItem.PLAYER1) return;
            if (grid[position[0] + 1][position[1]] == GridItem.PLAYER2) return;  

            grid[position[0]][position[1]] = EMPTY;
            grid[position[0] + 1][position[1]] = playerNum;
        }
            
    }
}

class Timer extends Thread{
    private static int ms;
    PrintStream p1Out;
    PrintStream p2Out;
    PlayerListener p1;
    PlayerListener p2;


    Timer(int newMS, PrintStream newP1Out, PrintStream newP2Out, PlayerListener newP1, PlayerListener newP2){
        ms = newMS;
        p1Out = newP1Out;
        p2Out = newP2Out;
        p1 = newP1;
        p2 = newP2;
    }

    public void run(){
        while (ms > 0){
            try{
                sleep(1000);
                ms -= 1000;
                p1Out.println("TIME=" + (ms / 1000));
                p2Out.println("TIME=" + (ms / 1000));

            } catch (Exception ex){
                System.out.println(ex);
            }
        }   

        // Fail Game
        p1Out.println("DONE=0");
        p2Out.println("DONE=0");
    }

    // Returns time in seconds
    public static synchronized int getTime(){
        return ms / 1000;
    }
}