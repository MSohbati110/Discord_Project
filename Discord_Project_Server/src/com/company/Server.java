package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Server implements Serializable{
    private final int port = 6000;
    private HashMap<String,ClientHandler> clients = new HashMap();
    private HashMap<String,String[]> users = new HashMap<>();
    private HashMap<String[],Boolean> friendRequest = new HashMap<>();
    private HashMap<String[],Boolean> friendAccept = new HashMap<>();
    private HashMap<ArrayList<String>,ArrayList<String>> privateChats = new HashMap<>();
    private ArrayList<Object> data = new ArrayList<>();

    // starting the server
    public void startServer () {
        try {
            // reading info from file
            FileInputStream fin = new FileInputStream("data.txt");
            ObjectInputStream inf = new ObjectInputStream(fin);
            data = (ArrayList<Object>) inf.readObject();
            clients = (HashMap<String, ClientHandler>) data.get(0);
            users = (HashMap<String, String[]>) data.get(1);
            friendRequest = (HashMap<String[], Boolean>) data.get(2);
            friendAccept = (HashMap<String[], Boolean>) data.get(3);
            privateChats = (HashMap<ArrayList<String>, ArrayList<String>>) data.get(4);
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                Thread thread = new Thread(new ClientHandler(socket));
                thread.start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {

        }
    }
    // sending a message to a specific client
    private void sendTo (String username, Message message, String type) {
        ClientHandler clientHandler = clients.get(username);
        clientHandler.sendToClient(message,type);
    }
    // sending any requests when offline
    private void offlineRequests (String username, ClientHandler clientHandler) {
        for (String[] user : friendRequest.keySet()) {
            if (user[1].equals(username)) {
                clientHandler.sendToClient(new Message(user[0],"","/friend"),"/friend");
                friendRequest.remove(user);
                saving();
            }
        }
        for (String[] user : friendAccept.keySet()) {
            if (user[1].equals(username)) {
                clientHandler.sendToClient(new Message(user[0],"","/friendaccept"),"/friendaccept");
                friendAccept.remove(user);
                saving();
            }
        }
        Iterator<ArrayList<String>> iterator = privateChats.keySet().iterator();
        while (iterator.hasNext()) {
            ArrayList<String> user = iterator.next();
            if (user.get(1).equals(username)) {
                clientHandler.sendToClient(new Message(user.get(0),"","/chat"),"/chat");
                for (String chat : privateChats.get(user)) {
                    clientHandler.sendToClient(new Message(user.get(0),chat,"pchat"),"pchat");
                }
                iterator.remove();
                saving();
            }
        }
    }
    // saving data in file
    public void saving () {
        try {
            FileOutputStream fout = new FileOutputStream("data.txt");
            ObjectOutputStream outf = new ObjectOutputStream(fout);
            data.add(clients);
            data.add(users);
            data.add(friendRequest);
            data.add(friendAccept);
            data.add(privateChats);
            outf.writeObject(data);
            fout.close();
            outf.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    // clientHandler thread
    private class ClientHandler implements Runnable,Serializable{
        private transient Socket socket;
        private transient ObjectInput in;
        private transient ObjectOutput out;
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
        public synchronized void sendToClient (Message message, String type) {
            try {
                out.writeObject(message);
            } catch (IOException e) {
                if (type.equals("/friend")) {
                    friendRequest.put(new String[]{message.getOwner(),username},true);
                    saving();
                }
                if (type.equals("/friendaccept")) {
                    friendAccept.put(new String[]{message.getOwner(),username},true);
                    saving();
                }
                if (type.equals("/chat")) {
                    ArrayList<String> users = new ArrayList<>();
                    users.add(message.getOwner());
                    users.add(username);
                    privateChats.put(users,new ArrayList<>());
                    saving();
                }
                if (type.equals("pchat")) {
                    ArrayList<String> users = new ArrayList<>();
                    users.add(message.getOwner());
                    users.add(username);
                    ArrayList<String> chats = privateChats.get(users);
                    chats.add(message.getText());
                    privateChats.put(users,chats);
                    saving();
                }
            }
        }
        @Override
        public void run() {
            try {
                if (out == null) {
                    out = new ObjectOutputStream(socket.getOutputStream());
                }
                if (in == null) {
                    in = new ObjectInputStream(socket.getInputStream());
                }
                while (true) {
                    Message message = (Message) in.readObject();
                    if (message.getType().equals("sign up")) {
                        if (users.containsKey(message.getOwner())) {
                            sendToClient(new Message("server", "username already exists", "error"), "");
                        }
                        else {
                            sendToClient(new Message("server", "username sign up successfully", ""), "");
                            username = message.getOwner();
                            String[] inputs = message.getText().split("-");
                            clients.put(username, this);
                            users.put(username, inputs);
                            saving();
                        }
                    }
                    if (message.getType().equals("sign in")) {
                        if (!users.containsKey(message.getOwner())) {
                            sendToClient(new Message("server", "username or password is wrong", "error"), "");
                        } else {
                            if (!users.get(message.getOwner())[0].equals(message.getText())) {
                                sendToClient(new Message("server", "username or password is wrong", "error"), "");
                            } else {
                                sendToClient(new Message("server", "username sign in successfully", ""), "");
                                username = message.getOwner();
                                offlineRequests(username, this);
                                clients.replace(username, this);
                            }
                        }
                    }
                    if (message.getType().equals("/friend")) {
                        if (users.containsKey(message.getText())) {
                            sendToClient(new Message("server", "friend request sent", ""), "");
                            sendTo(message.getText(), new Message(message.getOwner(), "", "/friend"), "/friend");
                        } else {
                            sendToClient(new Message("server", "username doesn't exist", "error"), "");
                        }
                    }
                    if (message.getType().equals("/friendaccept")) {
                        sendTo(message.getText(), new Message(message.getOwner(), "", "/friendaccept"), "/friendaccept");
                    }
                    if (message.getType().equals("/chat")) {
                        sendTo(message.getText(), new Message(message.getOwner(), "", "/chat"), "/chat");
                    }
                    if ((message.getType().split("-")[0].equals("pchat"))) {
                        sendTo(message.getType().split("-")[1],new Message(message.getOwner(),message.getText(),"pchat"),"pchat");
                    }
                }
            }
            catch (ClassNotFoundException | IOException e) {
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
        }
    }
}