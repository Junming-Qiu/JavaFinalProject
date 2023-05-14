import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Client {
    static String host;
    static int port;
    public static void main(String[] args) {
        port = 5190;
        host = "127.0.0.1";

        // Window for connecting to host
        JFrame jf = new JFrame("\"Epic 'Not a Mario Game' Game Client");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setSize(1000, 400);
        jf.setLayout(new BoxLayout(jf.getContentPane(), BoxLayout.Y_AXIS));
        jf.setVisible(true);
        JPanel jp = new JPanel();
        jp.setBackground(Color.LIGHT_GRAY);
        jp.setLayout(new FlowLayout());
        jf.add(jp);
        //Set up text GUI stuff
        JLabel inst = new JLabel("Input the hostname of the server to connect to below. Default is localhost");
        JTextField messageField = new JTextField(20);
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String conn = messageField.getText();
                System.out.println("Conn: " + conn);
                if (conn.isEmpty()) {
                    conn = host;
                }
                System.out.println("Connecting to: " + conn);
                try{
                    Socket s = new Socket(conn, port);
                    PrintStream sout = new PrintStream(s.getOutputStream());
                    Scanner sin = new Scanner(s.getInputStream());
                    jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    jf.setVisible(false);
                    // Start up a client handler
                    new ClientL(sin, sout).start();
                } catch (IOException ex){
                    System.out.println("Connection Failed");
                    inst.setText("Connection Failed. Please retry valid hostname.");
                }
            }
        });
        jp.add(inst);
        jp.add(messageField);
        jp.add(sendButton);
        // Game instructions
        JPanel jp2 = new JPanel();
        jp2.setBackground(Color.GRAY);
        JTextArea messageArea = new JTextArea(6, 55);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true); // Enable line wrapping
        messageArea.setWrapStyleWord(true); // Wrap at word boundaries
        messageArea.append("Instructions\n");
        messageArea.append("Use the onscreen buttons to move.\n");
        messageArea.append("Map Key: Green is Lario, Red is Muigi, Yellow are coins\n");
        messageArea.append("This is a real time game!\n");
        messageArea.append("Work together with your non-copyrighted partner\n");
        messageArea.append("to collect all the coins on the map!\n");
        messageArea.append("Good luck!\n");
        jf.add(jp2);
        jp2.add(messageArea);
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
        if (message.equals("")){
            //Default
            return new int[][]{
                {0,1,0},
                {0,0,0},
                {0,0,2}
            };
        }
        String[] rows = message.split("R");
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

        // Grid
        FunPanel fp = new FunPanel(grid);
        jf.add(fp);
        // Controls
        JPanel jp = new JPanel();
        jp.setBackground(Color.CYAN);
        JLabel crtl = new JLabel("Controls! Figure out who you are by just moving!");
        jp.add(crtl);
        jf.add(jp);
        jp.add(up);
        jp.add(dn);
        jp.add(lt);
        jp.add(rt);
        // Results + Info section
        JPanel jp2 = new JPanel();
        JLabel result = new JLabel("Result");
        JLabel timer = new JLabel("WAITING for other player to join");
        JButton stop = new JButton("STOP AND RESTART GAME");
        
        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sout.println("STOP");
                System.out.println("Connecting to: " + Client.host);
                try{
                    Socket s = new Socket(Client.host, Client.port);
                    PrintStream sout = new PrintStream(s.getOutputStream());
                    Scanner sin = new Scanner(s.getInputStream());
                    jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    jf.setVisible(false);

                    // Start up a client handler
                    new ClientL(sin, sout).start();
                } catch (IOException ex){
                    System.out.println("Connection Failed");
                }

            }
        });
        jp2.setBackground(Color.PINK);
        jf.add(jp2);
        jp2.add(result);
        jp2.add(stop);
        jp2.add(timer);

        while (sin.hasNext()){
            message = sin.nextLine();
            // Read the state
            String state = message.split("=")[0];
            message = message.split("=")[1];
            System.out.println(message);
            if (state.equals("STATE")) {
                int time = Integer.parseInt(message.split("T")[0]);
                if (time > fp.time){
                    // Then this is an outdated message. Ignore it
                    System.out.println("Outdated message received. Curr: " + fp.time + " Received: " + time);
                    continue;
                }
                grid = parseMessage(message.split("T")[1]);
                printGrid(grid);
                System.out.println("Before update: " + fp.time);
                fp.update(grid, time);
                System.out.println("After update: " + fp.time);
                // Repaint the window
                fp.repaint();
            } else if (state.equals("DONE")){
                // Server has told us the game is done.
                if (message.equals("1")){
                    System.out.println("You Won!");
                    result.setText("You Won!");
                } else if (message.equals("0")){
                    System.out.println("You Lost!");
                    result.setText("You Lost!");
                }
                // Prompt the next game by responding to server via result Action Listener
                stop.setVisible(true);
                break;
            } else if (state.equals("TIME")){
                timer.setText("Time Remaining: " + message);
            }
        }
    }
}

class FunPanel extends JPanel{
    int gridSize;
    int[][] grid;
    int time;

    FunPanel(int[][] g){
        super();
        //During construction, the height and width will still be 0.
        grid = g;
        gridSize = grid.length;
        time = Integer.MAX_VALUE;
    }

    void update(int[][] newGrid, int newTime){
        grid = newGrid;
        gridSize = newGrid.length;
        time = newTime;
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
                g.fillRect(j * cellW, i * cellH, cellW, cellH);
            }
        }
    }
}