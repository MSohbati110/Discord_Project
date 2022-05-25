package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    private final int port = 6000;
    private HashMap<String,ClientHandler> clients = new HashMap();
    private HashMap<String,String[]> users = new HashMap<>();
    // starting the server
    public void startServer () {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                Thread thread = new Thread(new ClientHandler(socket));
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // sending a message to a specific client
    public void sentTo (String username, Message message) {
        ClientHandler clientHandler = clients.get(username);
        clientHandler.sendToClient(message);
    }
    // clientHandler thread
    private class ClientHandler implements Runnable{
        private Socket socket;
        private ObjectInput in;
        private ObjectOutput out;
        private String username;
        // constructor
        public ClientHandler(Socket socket) {
            try {
                this.socket = socket;
                username = "";
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // send a message to the client
        public synchronized void sendToClient (Message message) {
            try {
                out.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            while (true) {
                try {
                    Message message = (Message) in.readObject();
                    if (message.getType().equals("sign up")) {
                        if (users.containsKey(message.getOwner())) {
                            sendToClient(new Message("server","username already exists","error"));
                        }
                        else {
                            sendToClient(new Message("server","username sign up successfully",""));
                            username = message.getOwner();
                            String[] inputs = message.getText().split("-");
                            clients.put(username,this);
                            users.put(username,inputs);
                        }
                    }
                    if (message.getType().equals("sign in")) {
                        if (!users.containsKey(message.getOwner())) {
                            sendToClient(new Message("server","username or password is wrong","error"));
                        }
                        else {
                            if (!users.get(message.getOwner())[0].equals(message.getText())) {
                                sendToClient(new Message("server","username or password is wrong","error"));
                            }
                            else {
                                sendToClient(new Message("server","username sign in successfully",""));
                                username = message.getOwner();
                                clients.replace(username,this);
                            }
                        }
                    }
                    if (message.getType().equals("/friend")) {
                        if (users.containsKey(message.getText())) {
                            sendToClient(new Message("server","friend request sent",""));
                            sentTo(message.getText(),new Message(message.getOwner(),"","/friend"));
                        }
                        else {
                            sendToClient(new Message("server","username doesn't exist","error"));
                        }
                    }
                    if (message.getType().equals("/friendaccept")) {
                        sentTo(message.getText(),new Message(message.getOwner(),"","/friendaccept"));
                    }
                } catch (ClassNotFoundException | IOException e) {

                }
            }
        }
    }
}