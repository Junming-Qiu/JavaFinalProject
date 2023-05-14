import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Client {
    public static void main(String[] args) {
        int port = 5190;

        try{
            Socket s = new Socket("127.0.0.1", port);
            PrintStream sout = new PrintStream(s.getOutputStream());
            Scanner sin = new Scanner(s.getInputStream());

            Scanner read = new Scanner(System.in);
            String message;

            new ClientL(sin, sout).start();

            while (true){
                message = read.nextLine();
                sout.println(message);
            }

        } catch (IOException ex){
            System.out.println("Connection Failed");
        }
    }
}

class ClientL extends Thread{
    Scanner sin;
    PrintStream sout;

    ClientL(Scanner newSin, PrintStream newSout){
        sin = newSin;
        sout = newSout;
    }

    public static int[][] parseMessage(String message){
        if (message == ""){
            return new int[][]{
                {0,1,0},
                {0,0,0},
                {0,0,2}
            };
        }
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
        String message = "";
        int[][] grid = parseMessage(message);

        JFrame jf = new JFrame("Epic 'Not a Mario Game' Game");
        jf.setSize(600, 600);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
        jf.setLayout(new BoxLayout(jf.getContentPane(), BoxLayout.Y_AXIS));

        // Direction buttons and their action listeners
        JButton up = new JButton("UP");
        up.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Print the message to the socket
                sout.println("UP");
            }
        });
        JButton dn = new JButton("DOWN");
        dn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Print the message to the socket
                sout.println("DOWN");
            }
        });
        JButton lt = new JButton("LEFT");
        lt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Print the message to the socket
                sout.println("LEFT");
            }
        });
        JButton rt = new JButton("RIGHT");
        rt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Print the message to the socket
                sout.println("RIGHT");
            }
        });

        FunPanel fp = new FunPanel(grid);
        jf.add(fp);
        JPanel jp = new JPanel();
        jp.setBackground(Color.CYAN);
        //jp.setLayout(new BorderLayout());
        jf.add(jp);
        /*
        fp.add(up, BorderLayout.NORTH);
        fp.add(dn, BorderLayout.SOUTH);
        fp.add(lt, BorderLayout.EAST);
        fp.add(rt, BorderLayout.WEST);
        */
        jp.add(up, BorderLayout.NORTH);
        jp.add(dn, BorderLayout.SOUTH);
        jp.add(lt, BorderLayout.EAST);
        jp.add(rt, BorderLayout.WEST);

        while (sin.hasNext()){
            message = sin.nextLine();
            System.out.println(message);
            // Read the state
            String state = message.split("=")[0];
            String msg = message.split("=")[1];
            if (state == "STATE") {
                grid = parseMessage(msg);
                printGrid(grid);
                fp.updateGrid(grid);
                fp.repaint();
            }
        }
    }
}

class FunPanel extends JPanel{
    int gridSize = 10;
    int[][] grid;

    FunPanel(int[][] g){
        super();
        //During construction, the height and width will still be 0.
        grid = g;
        gridSize = grid.length;
    }

    void updateGrid(int[][] newGrid){
        grid = newGrid;
        gridSize = newGrid.length;
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        // Grid color
        g.setColor(Color.BLUE);

        // Getting dimensions
        int width = getWidth();
        int height = getHeight();
        int cellW = width / gridSize;
        int cellH = height / gridSize;

        // Drawing vertical grid lines
        for (int x = 0; x <= width; x += cellW) {
            g.drawLine(x, 0, x, height);
        }
        // Drawing horizontal grid lines
        for (int y = 0; y <= height; y += cellH) {
            g.drawLine(0, y, width, y);
        }

        // Drawing grid items onto the grid
        for (int i = 0; i < gridSize; i++){
            for (int j = 0; j < gridSize; j++) {
                int item = grid[i][j];
                if (item == 0) {
                    //Empty
                    continue;
                } else if (item == 1) {
                    //Player 1
                    g.setColor(Color.RED);
                } else if (item == 2) {
                    //Player 2
                    g.setColor(Color.GREEN);
                } else {
                    //Coin
                    g.setColor(Color.YELLOW);
                }
                g.fillRect(i * cellW, j * cellH, cellW, cellH);
            }
        }
    }
}