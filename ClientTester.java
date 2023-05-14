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

            new Listener(sin).start();

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

    Listener(Scanner newListen){
        listen = newListen;
    }

    public int[][] parseMessage(String message){
        String[] rows = message.split("R");
        System.out.println(rows.length);
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

            if (state == "STATE"){
                grid = parseMessage(message);
                printGrid(grid);
            }
        
        }
    }

}