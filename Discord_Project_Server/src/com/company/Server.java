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
    private HashMap<String,Boolean> online = new HashMap<>();
    private HashMap<String[],Boolean> friendRequest = new HashMap<>();
    private HashMap<String[],Boolean> friendAccept = new HashMap<>();

    private HashMap<String, String> usersStatus = new HashMap<>();
    private HashMap<ArrayList<String>,ArrayList<String>> privateChats = new HashMap<>();
    private ArrayList<Group> groups = new ArrayList<>();
    private int groupId = 0;
    private HashMap<String,Group> groupJoins = new HashMap<>();
    private HashMap<String,Message> groupRemoves = new HashMap<>();
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
            groups = (ArrayList<Group>) data.get(5);
            groupJoins = (HashMap<String, Group>) data.get(6);
            groupRemoves = (HashMap<String, Message>) data.get(7);
            usersStatus = (HashMap<String, String>) data.get(8);
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
    private void gSendTo (String username, Group group, String type) {
        ClientHandler clientHandler = clients.get(username);
        clientHandler.gSendToClient(group,type);
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
        for (String user : groupJoins.keySet()) {
            if (user.equals(username)) {
                clientHandler.gSendToClient(groupJoins.get(user),"groupjoin");
                groupJoins.remove(user);
                saving();
            }
        }
        for (String user : groupRemoves.keySet()) {
            if (user.equals(username)) {
                clientHandler.sendToClient(groupRemoves.get(user),"groupremove");
                groupRemoves.remove(user);
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
            data.add(groups);
            data.add(groupJoins);
            data.add(groupRemoves);
            data.add(usersStatus);
            outf.writeObject(data);
            fout.close();
            outf.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    // clientHandler thread
    public class ClientHandler implements Runnable,Serializable{
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
                if (type.equals("groupremove")) {
                    groupRemoves.put(username,message);
                    saving();
                }
            }
        }
        public synchronized void gSendToClient (Group group, String type) {
            try {
                out.writeObject(new Message("server","",type));
                out.writeObject(group);
            } catch (IOException e) {
                if (type.equals("groupjoin")) {
                    groupJoins.put(username,group);
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
                            online.put(username,true);
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
                                online.put(username,true);
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
                    if (message.getType().equals("/setstatus")) {
                        usersStatus.put(message.getOwner(), message.getText());
                        saving();
                    }
                    if (message.getType().equals("/chat")) {
                        sendTo(message.getText(), new Message(message.getOwner(), "", "/chat"), "/chat");
                    }
                    if ((message.getType().split("-")[0].equals("pchat"))) {
                        sendTo(message.getType().split("-")[1],new Message(message.getOwner(),message.getText(),"pchat"),"pchat");
                    }
                    if (message.getType().equals("/newserver")) {
                        Group group = new Group(message.getOwner());
                        group.setId(groupId);
                        group.setName(message.getText());
                        groups.add(group);
                        saving();
                        sendToClient(new Message("server",String.valueOf(groupId),"/newserver"),"/newserver");
                        groupId++;
                    }
                    if (message.getType().equals("/changeservername")) {
                        groups.get(Integer.parseInt(message.getOwner())).setName(message.getText());
                        saving();
                    }
                    if (message.getType().equals("/addmember")) {
                        groups.get(Integer.parseInt(message.getText().split("-")[0])).addMember(message.getText().split("-")[1]);
                        saving();
                        gSendTo(message.getText().split("-")[1], groups.get(Integer.parseInt(message.getText().split("-")[0])), "groupjoin");
                    }
                    if (message.getType().equals("/removemember")) {
                        Group group = groups.get(Integer.parseInt(message.getText().split("-")[0]));
                        if (group.isMember(message.getText().split("-")[1])) {
                            group.removeMember(message.getText().split("-")[1]);
                            saving();
                            sendTo(message.getText().split("-")[1],new Message(group.getName(),String.valueOf(group.getId()),"groupremove"),"groupremove");
                        }
                        else {
                            sendToClient(new Message("server","there is no such member in this server!","error"),"error");
                        }
                    }
                    if (message.getType().equals("/status")) {
                        Group group = groups.get(Integer.parseInt(message.getText().split("-")[0]));
                        if (group.isMember(message.getText().split("-")[1])) {
                            if (online.containsKey(message.getText().split("-")[1])) {
                                sendToClient(new Message("server","Online",""),"");
                            }
                            else {
                                sendToClient(new Message("server","Offline","error"),"error");
                            }
                        }
                        else {
                            sendToClient(new Message("server","there is no such member in this server!","error"),"error");
                        }
                    }
                    if (message.getType().equals("/server")) {
                        boolean bool = false;
                        for (Group group : groups) {
                            if (group.getName().equals(message.getText())) {
                                if (group.isMember(message.getOwner())) {
                                    sendToClient(new Message("server",String.valueOf(group.getId()),"/server"),"/server");
                                }
                                else {
                                    sendToClient(new Message("server","you have no such server!","error"),"error");
                                }
                                bool = true;
                            }
                        }
                        if (!bool) {
                            sendToClient(new Message("server","you have no such server!","error"),"error");
                        }
                    }
                    if (message.getType().equals("/newchannel")) {
                        Group group = groups.get(Integer.parseInt(message.getText().split("-")[0]));
                        int channelId = group.newChannel(message.getText().split("-")[1]);
                        saving();
                        sendToClient(new Message("server",String.valueOf(channelId),"/newchannel"),"/newchannel");
                    }
                    if (message.getType().equals("/channel")) {
                        Group group = groups.get(Integer.parseInt(message.getText().split("-")[0]));
                        if (group.isChannel(message.getText().split("-")[1])) {
                            int channelID = group.channelID(message.getText().split("-")[1]);
                            sendToClient(new Message("server",String.valueOf(channelID),"/channel"),"/channel");
                            Channel channel = group.channel(channelID);
                            ArrayList<String> chats = channel.getChats();
                            ArrayList<String> chatsUser = channel.getChatsUser();
                            for (int i=0 ; i<chats.size() ; i++) {
                                sendToClient(new Message(chatsUser.get(i),chats.get(i),"channelchats"),"channelchats");
                            }
                        }
                        else {
                            sendToClient(new Message("server","there is no such channel in this server!","error"),"error");
                        }
                    }
                    if ((message.getType().split("-")[0].equals("chatroom"))) {
                        Group group = groups.get(Integer.parseInt(message.getType().split("-")[1]));
                        group.addChat(Integer.parseInt(message.getType().split("-")[2]),message.getOwner(),message.getText());
                        Channel channel = group.channel(Integer.parseInt(message.getType().split("-")[2]));
                        ArrayList<String> groupMembers = group.getMembers();
                        saving();
                        for (String member : groupMembers) {
                            sendTo(member,new Message(message.getOwner(),message.getText(),"chatroom-" + group.getId() + "-" + channel.getId()),"chatroom");
                        }
                    }

                }
            }
            catch (ClassNotFoundException | IOException e) {
                    try {
                        online.remove(username);
                        socket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
        }
    }
}