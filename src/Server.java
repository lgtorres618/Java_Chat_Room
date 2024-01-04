

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;


    public Server() {
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(15001);   //9999 is already in use, try somethign like 16001
            pool = Executors.newCachedThreadPool();
            while (!done) {

                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            // TODO: handle
            shutdown();
        }
    }

    public void shutdown() {
        try {
            done = true;
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler ch : connections) {
                ch.shutdown();
            }
        } catch (IOException e) {
            //ignore
        }
    }

    class ConnectionHandler implements Runnable {

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;

        public void broadcast(String message) {
            for (ConnectionHandler ch : connections) {    //Sending messages to all connected clients
                if (ch != null) {
                    ch.sendMessage(message);

                }
            }
        }

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println(" Please enter a nickname:  ");
                nickname = in.readLine();
                System.out.println(nickname + " connected!");
                broadcast(nickname + " joined the chat! ");
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/lemon")) {
                        // TODO: handle nickname
                        String[] messageSplit = message.split("", 2);
                        if (messageSplit.length == 2) {
                            broadcast(nickname + "renamed themeselves to" + messageSplit);
                            System.out.println(nickname + "renamed themeselves to" + messageSplit);
                            nickname = messageSplit[1];
                            out.println("Succesfully changed nickname to " + nickname);

                        }
                    } else if (message.startsWith("/quit")) {
                        broadcast(nickname + "left the chat!");
                        shutdown();


                    }else if (message.startsWith("/private")) {
                        broadcast(nickname + "whispers");



                    }
                    else {
                        broadcast(nickname + ":" + message);
                    }
                }


                // edge cases is not in the video if else with string functions if nickname is not null and if is no
                // pass input reader stream and you put the input stream from the client
            } catch (IOException e) {
                shutdown();
            }

        }

        public void sendMessage(String message) {

            out.println(message);

        }

        public void sendPrivateMessage(String recipientName, String message){
            for(ConnectionHandler ch: connections){
                if( ch != null && ch.nickname.equals(recipientName)){
                    out.println(message);
                }
            }
        }

        public void shutdown() {
            try {

                in.close();
                out.close();

                if (client!= null && !client.isClosed()) {
                    client.close();
                }

            } catch (IOException e) {

            }
        }
    }
}




