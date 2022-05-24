package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    private int port = 6000;
    private ArrayList<ClientHandler> clients = new ArrayList<>();
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
                            clients.add(this);
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
                                System.out.println("user sign in");
                            }
                        }
                    }
                } catch (ClassNotFoundException | IOException e) {

                }
            }
        }
    }
}