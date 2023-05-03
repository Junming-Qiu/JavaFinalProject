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

    @Override
    public void run(){
        String message;
        while (listen.hasNext()){
            message = listen.nextLine();
            System.out.println(message);
        }

    }

}