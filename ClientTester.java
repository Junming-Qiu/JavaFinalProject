import java.io.*;
import java.util.*;
import java.net.*;
public class ClientTester {
    public static void main(String[] args) {
        int port = 5190;

        try{
            Socket s = new Socket("127.0.0.1", port);
            PrintStream sout = new PrintStream(s.getOutputStream());
            Scanner sin = new Scanner(s.getInputStream());

            Scanner read = new Scanner(System.in);
            String message;

            new Listener(sin, sout).start();

            while (true){
                message = read.nextLine();
                sout.println(message);
            }

        } catch (IOException ex){
            System.out.println("Connection Failed");
        }
    }
}

class Listener extends Thread{
    Scanner listen;
    PrintStream sout;

    Listener(Scanner newListen, PrintStream newSout){
        listen = newListen;
        sout = newSout;

    }

    public int[][] parseMessage(String message){
        String[] rows = message.split("R");
        //System.out.println(rows.length);
        int[][] board = new int[rows.length][rows[0].split("C").length];

        for (int i=0; i<rows.length; i++){
            String[] columns = rows[i].split("C");
            for (int j=0; j<columns.length; j++){
                board[i][j] = Integer.parseInt(columns[j]);
            }
        }
        return board;
    }

    // Debug function to visualize grid without UI
    public static void printGrid(int[][] grid){
        for (int i=0; i<grid.length; i++){
            for (int j=0; j<grid.length; j++){
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
    }

    @Override
    public void run(){
        String message;
        int[][] grid;

        while (listen.hasNext()){
            message = listen.nextLine();
            //System.out.println(message);
            String state = message.split("=")[0];
            message = message.split("=")[1];
            System.out.println(state);

            if (state.equals("STATE")){
                String time = message.split("T")[0];
                grid = parseMessage(message.split("T")[1]);
                System.out.println(time);
                printGrid(grid);
            } else if (state.equals("DONE")){
                if (message.equals("1")){
                    System.out.println("You Won!");
                } else if (message.equals("0")){
                    System.out.println("You Lost!");
                }
                sout.println("STOP");
                break;
            } else if (state.equals("TIME")){
                System.out.println("Time = " + message);
            }
        
        }
    }

}