package com.company;

import java.io.*;
import java.net.Socket;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Client {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private String ip = "127.0.0.1";
    private int port = 6000;
    private boolean connection = true;
    private String username = "";
    private String passWord = "";
    private String email = "";
    private String phoneNumber = "";

    private String status = "";
    private boolean sign = true;
    private HashMap<String,Boolean> friendsList = new HashMap<>();

    private  HashMap<String, String> friendsStatus = new HashMap<>();
    private HashMap<String, ArrayList<String>> privateChats = new HashMap<>();
    private boolean isPrivateChat = false;
    private String privateChatUser = "";
    private HashMap<Integer,Boolean> groups = new HashMap<>();
    private boolean isGroup = false;
    private int theGroup = -1;
    private boolean isChannel = false;
    private int theChannel = -1;
    private ArrayList<Object> data = new ArrayList<>();

    // sign up method
//    private void signUp () {
//        boolean condition;
//        Scanner scanner = new Scanner(System.in);
//        System.out.println(ANSI_YELLOW + "Enter your username : " + ANSI_RESET);
//
//        do {
//            username = scanner.nextLine();
//            if (!(Pattern.matches("[a-zA-Z0-9]*", username))) {
//                System.out.println(ANSI_RED + "username should have only these characters : a-z, A-Z, 0-9." + ANSI_RESET);
//                condition = true;
//            } else if (username.length() < 6) {
//                System.out.println(ANSI_RED + "username should have at least 6 characters" + ANSI_RESET);
//                condition = true;
//            }
//            else {
//                condition = false;
//            }
//        } while (condition);
//        System.out.println(ANSI_YELLOW + "Enter your password : " + ANSI_RESET);
//
//        do {
//            passWord = scanner.nextLine();
//            if (passWord.contains(" ") || passWord.length() < 8) {
//                System.out.println(ANSI_RED + "password should be at least 8 characters and don't contains space." + ANSI_RESET);
//                condition = true;
//            } else {
//                condition = false;
//            }
//        } while (condition);
//        System.out.println(ANSI_YELLOW + "Enter your email : " + ANSI_RESET);
//
//        do {
//            email = scanner.nextLine();
//            if (!Pattern.matches("^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$", email)) {
//                System.out.println(ANSI_RED + "invalid email address." + ANSI_RESET);
//                condition = true;
//            } else {
//                condition = false;
//            }
//        } while (condition);
//        System.out.println(ANSI_YELLOW + "Enter your phoneNumber : (Enter -1 to pass)" + ANSI_RESET);
//
//        do {
//            phoneNumber = scanner.nextLine();
//            if (phoneNumber.equals("-1")) {
//                condition = false;
//            } else if (!Pattern.matches("[0-9]*", phoneNumber)) {
//                System.out.println(ANSI_RED + "phonenumber is invalid" + ANSI_RESET);
//                condition = true;
//            } else if (phoneNumber.length() != 11) {
//                System.out.println(ANSI_RED + "phonenumber is invalid" + ANSI_RESET);
//                condition = true;
//            } else if (!phoneNumber.substring(0,2).equals("09")) {
//                System.out.println(ANSI_RED + "phonenumber is invalid" + ANSI_RESET);
//                condition = true;
//            }
//            else {
//                condition = false;
//            }
//        } while (condition);
//    }
    private void signUp () {
        Scanner scanner = new Scanner(System.in);
        System.out.println(ANSI_YELLOW + "Enter your username : " + ANSI_RESET);
        username = scanner.nextLine();
        System.out.println(ANSI_YELLOW + "Enter your password : " + ANSI_RESET);
        passWord = scanner.nextLine();
        System.out.println(ANSI_YELLOW + "Enter your email : " + ANSI_RESET);
        email = scanner.nextLine();
        System.out.println(ANSI_YELLOW + "Enter your phoneNumber : (Enter -1 to pass)" + ANSI_RESET);
        phoneNumber = scanner.nextLine();
    }
    // sign in method
    private void signIn () {
        Scanner scanner = new Scanner(System.in);
        System.out.println(ANSI_YELLOW + "Enter your username : " + ANSI_RESET);
        username = scanner.nextLine();
        System.out.println(ANSI_YELLOW + "Enter your password : " + ANSI_RESET);
        passWord = scanner.nextLine();
    }
    // saving data in file
    private void saving () {
        try {
            FileOutputStream fout = new FileOutputStream(username+".txt");
            ObjectOutputStream outf = new ObjectOutputStream(fout);
            data.add(friendsList);
            data.add(privateChats);
            data.add(groups);
            outf.writeObject(data);
            fout.close();
            outf.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    // starting the client
    public void startClient () {
        try {
            // joining the server
            Socket socket = new Socket(ip,port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // sign up or sign in
            Scanner scanner = new Scanner(System.in);
            while (sign) {
                System.out.println(ANSI_YELLOW + "1- sign up" + ANSI_RESET);
                System.out.println(ANSI_YELLOW + "2- sign in" + ANSI_RESET);
                System.out.println(ANSI_YELLOW + "3- exit" + ANSI_RESET);
                int input = scanner.nextInt();
                while (input != 1 && input != 2 && input != 3) {
                    System.out.println(ANSI_RED + "invalid answer!" + ANSI_RESET);
                    input = scanner.nextInt();
                }
                if (input == 1) { // sign up
                    signUp();
                    out.writeObject(new Message(username, passWord + "-" + email + "-" + phoneNumber, "sign up"));
                    Message message = (Message) in.readObject();
                    if (message.getType().equals("error")) {
                        System.out.println(ANSI_RED + message.getText() + ANSI_RESET);
                    }
                    if (message.getType().equals("")) {
                        System.out.println(ANSI_BLUE + message.getText() + ANSI_RESET);
                        saving();
                        sign = false;
                    }
                }
                if (input == 2) { // sign in
                    signIn();
                    out.writeObject(new Message(username, passWord, "sign in"));
                    Message message = (Message) in.readObject();
                    if (message.getType().equals("error")) {
                        System.out.println(ANSI_RED + message.getText() + ANSI_RESET);
                    }
                    if (message.getType().equals("")) {
                        System.out.println(ANSI_BLUE + message.getText() + ANSI_RESET);
                        sign = false;
                    }
                }
                if (input == 3) { // exit
                    connection = false;
                    sign = false;
                }
            }

            // reading info from file
            FileInputStream fin = new FileInputStream(username+".txt");
            ObjectInputStream inf = new ObjectInputStream(fin);
            data = (ArrayList<Object>) inf.readObject();
            friendsList = (HashMap<String, Boolean>) data.get(0);
            privateChats = (HashMap<String, ArrayList<String>>) data.get(1);
            groups = (HashMap<Integer, Boolean>) data.get(2);

            // Listener
            Thread thread = new Thread(new Listener(in,socket));
            thread.start();

            // writer
            while (connection) {
                String text = "";
                if (scanner.hasNextLine()) {
                    text = scanner.nextLine();
                }
                try {
                    if (text.charAt(0) == '/') {
                        if (text.split(" ")[0].equals("/exit")) {
                            connection = false;
                            socket.close();
                        }
                        if (text.split(" ")[0].equals("/friend") && text.split(" ").length == 2) {
                            if (!username.equals(text.split(" ")[1])) {
                                out.writeObject(new Message(username,text.split(" ")[1],"/friend"));
                            }
                            else {
                                System.out.println(ANSI_RED + "No No No" + ANSI_RESET);
                            }
                        }
                        if (text.split(" ")[0].equals("/friendrequest") && text.split(" ").length == 1) {
                            int counter = 1;
                            for (String username : friendsList.keySet()) {
                                if (friendsList.get(username).equals(false)) {
                                    System.out.println(ANSI_YELLOW + counter + "- " + username + ANSI_RESET);
                                    counter++;
                                }
                            }
                            if (counter == 1) {
                                System.out.println(ANSI_RED + "you have no friend request!" + ANSI_RESET);
                            }
                        }
                        if (text.split(" ")[0].equals("/friendaccept") && text.split(" ").length == 2) {
                            if (friendsList.containsKey(text.split(" ")[1]) && !friendsList.get(text.split(" ")[1])) {
                                friendsList.replace(text.split(" ")[1],true);
                                saving();
                                out.writeObject(new Message(username,text.split(" ")[1],"/friendaccept"));
                                System.out.println(ANSI_BLUE + "friend request accepted" + ANSI_RESET);

                            }
                            else {
                                System.out.println(ANSI_RED + "there is no friend request from this username" + ANSI_RESET);
                            }
                        }
                        if (text.split(" ")[0].equals("/friendreject") && text.split(" ").length == 2) {
                            if (friendsList.containsKey(text.split(" ")[1]) && !friendsList.get(text.split(" ")[1])) {
                                friendsList.remove(text.split(" ")[1]);
                                saving();
                                System.out.println(ANSI_BLUE + "friend request rejected" + ANSI_RESET);
                            }
                            else {
                                System.out.println(ANSI_RED + "there is no friend request from this username" + ANSI_RESET);
                            }
                        }
                        if (text.split(" ")[0].equals("/friendlist") && text.split(" ").length == 1) {
                            int counter = 1;
                            for (String friend : friendsList.keySet()) {
                                if (friendsList.get(friend)) {
                                    System.out.println(ANSI_YELLOW + counter + "- " + friend + ANSI_RESET);
                                    counter++;
                                }
                            }
                            if (counter == 1) {
                                System.out.println(ANSI_RED + "you have no friends!" + ANSI_RESET);
                            }
                        }
                        if (text.split(" ")[0].equals("/server") && text.split(" ").length == 2) {
                            out.writeObject(new Message(username,text.split(" ")[1],"/server"));
                        }
                        if (text.split(" ")[0].equals("/setstatus") && text.split("").length == 2){
                            System.out.println(ANSI_YELLOW + "Set your status : \n1. Online \n2. Idle \n3. Do Not Distrub \n4. Invisible \n-1. None" + ANSI_RESET);
                            boolean condition = true;
                            while (condition) {
                                status = scanner.nextLine();
                                switch (status) {
                                    case "1":
                                        status = "Online";
                                        condition = false;
                                        break;
                                    case "2":
                                        status = "Idle";
                                        condition = false;
                                        break;
                                    case "3":
                                        status = "Do Not Disturb";
                                        condition = false;
                                        break;
                                    case "4":
                                        status = "Invisible";
                                        condition = false;
                                        break;
                                    case "-1":
                                        status = "";
                                        condition = false;
                                        break;
                                    default:
                                        System.out.println(ANSI_RED + "Wrong input" + ANSI_RESET);
                                        break;
                                }
                            }
                            if (!status.equals("")){
                                out.writeObject(new Message(username, text.split(" ")[1], "/setstatus"));
                            }
                        }
                        if (!isGroup) {
                            if (text.split(" ")[0].equals("/chat") && text.split(" ").length == 2) {
                                if (friendsList.containsKey(text.split(" ")[1])) {// && !privateChatUser.equals(text.split(" ")[1])
                                    isPrivateChat = true;
                                    privateChatUser = text.split(" ")[1];
                                    if (privateChats.containsKey(text.split(" ")[1])) {
                                        ArrayList<String> chats = privateChats.get(text.split(" ")[1]);
                                        for (String chat : chats) {
                                            System.out.println(chat);
                                        }
                                    }
                                    else {
                                        privateChats.put(text.split(" ")[1],new ArrayList<>());
                                        saving();
                                    }
                                    out.writeObject(new Message(username,text.split(" ")[1],"/chat"));
                                }
                                else {
                                    System.out.println(ANSI_RED + "you have no friend with this username" + ANSI_RESET);
                                }
                            }
                            if (text.split(" ")[0].equals("/chatoff") && text.split(" ").length == 1) {
                                isPrivateChat = false;
                                privateChatUser = "";
                            }
                            if (text.split(" ")[0].equals("/newserver") && text.split(" ").length == 1) {
                                Group group = new Group(username);
                                group.changeName();
                                isGroup = true;
                                saving();
                                out.writeObject(new Message(username,group.getName(),"/newserver"));
                            }
                        }
                        if (isGroup) {
                            if (text.split(" ")[0].equals("/exitserver") && text.split(" ").length == 1) {
                                System.out.println(ANSI_BLUE + "exit the server successfully" + ANSI_RESET);
                                isGroup = false;
                                theGroup = -1;
                            }
                            if (text.split(" ")[0].equals("/changeservername") && text.split(" ").length == 1) {
                                Group group = new Group(username);
                                group.changeName();
                                saving();
                                out.writeObject(new Message(String.valueOf(theGroup),group.getName(),"/changeservername"));
                            }
                            if (text.split(" ")[0].equals("/addmember") && text.split(" ").length == 2) {
                                if (friendsList.containsKey(text.split(" ")[1])) {
                                    out.writeObject(new Message(username,theGroup + "-" + text.split(" ")[1],"/addmember"));
                                }
                                else {
                                    System.out.println(ANSI_RED + "you have no friend with this username" + ANSI_RESET);
                                }
                            }
                            if (text.split(" ")[0].equals("/removemember") && text.split(" ").length == 2) {
                                out.writeObject(new Message(username, theGroup + "-" + text.split(" ")[1],"/removemember"));
                            }
                            if (text.split(" ")[0].equals("/status") && text.split(" ").length == 2) {
                                out.writeObject(new Message(username, theGroup + "-" + text.split(" ")[1],"/status"));
                            }
                            if (text.split(" ")[0].equals("/newchannel") && text.split(" ").length == 1) {
                                Channel channel = new Channel();
                                channel.changeName();
                                out.writeObject(new Message(username,theGroup + "-" + channel.getName(),"/newchannel"));
                            }
                            if (text.split(" ")[0].equals("/channel") && text.split(" ").length == 2) {
                                out.writeObject(new Message(username, theGroup + "-" + text.split(" ")[1],"/channel"));
                            }
                            if (isChannel) {
                                if (text.split(" ")[0].equals("/exitchannel") && text.split(" ").length == 1) {
                                    isChannel = false;
                                    theChannel = -1;
                                    System.out.println(ANSI_BLUE + "exit the channel successfully" + ANSI_RESET);
                                }
                            }
                        }
                    }
                    else {
                        if (isPrivateChat) {
                            ArrayList<String> chats = privateChats.get(privateChatUser);
                            chats.add("me: " + text);
                            saving();
                            out.writeObject(new Message(username,text,"pchat-"+privateChatUser));
                        }
                        if (isChannel) {
                            out.writeObject(new Message(username,text,"chatroom-" + theGroup + "-" + theChannel));
                        }
                    }
                }
                catch (StringIndexOutOfBoundsException e) {

                }
            }

        }
        catch (IOException | ClassNotFoundException e) {
            if (connection) {
                System.out.println(ANSI_RED + "Server is down" + ANSI_RESET);
            }
        }
    }
    // Listener thread
    private class Listener implements Runnable{
        private Socket socket;
        private ObjectInputStream in;

        // constructor
        public Listener(ObjectInputStream in, Socket socket) {
            this.in = in;
            this.socket = socket;
        }
        @Override
        public void run() {
            while (connection) {
                try {
                    Message message = (Message) in.readObject();

                    if (message.getType().equals("error")) {
                        System.out.println(ANSI_RED + message.getText() + ANSI_RESET);
                    }
                    if (message.getType().equals("")) {
                        System.out.println(ANSI_BLUE + message.getText() + ANSI_RESET);
                    }
                    if (message.getType().equals("/friend")) {
                        friendsList.put(message.getOwner(),false);
                        saving();
                    }
                    if (message.getType().equals("/friendaccept")) {
                        friendsList.put(message.getOwner(),true);
                        saving();
                    }
                    if (message.getType().equals("/friendstatus")) {
                        friendsStatus.put(message.getOwner(), message.getText());
                    }
                    if (message.getType().equals("/chat")) {
                        if (!privateChats.containsKey(message.getOwner())) {
                            privateChats.put(message.getOwner(),new ArrayList<>());
                            saving();
                        }
                    }
                    if (message.getType().equals("pchat")) {
                        ArrayList<String> chats = privateChats.get(message.getOwner());
                        chats.add(message.getOwner() + ": " + message.getText());
                        saving();
                        if (privateChatUser.equals(message.getOwner())) {
                            System.out.println(message.getOwner() + ": " + message.getText());
                        }
                    }
                    if (message.getType().equals("/newserver")) {
                        theGroup = Integer.parseInt(message.getText());
                        groups.put(Integer.parseInt(message.getText()),true);
                    }
                    if (message.getType().equals("groupjoin")) {
                        Group group = (Group) in.readObject();
                        groups.put(group.getId(),true);
                        System.out.println(ANSI_BLUE + "you have been added to the " + group.getName() + " server. WELCOME!" + ANSI_RESET);
                    }
                    if (message.getType().equals("groupremove")) {
                        ArrayList<Integer> groups1 = new ArrayList<Integer>(groups.keySet());
                        groups.remove(groups1.indexOf(Integer.parseInt(message.getText())));
                        System.out.println(ANSI_RED + "you have been removed from the " + message.getOwner() +" server" + ANSI_RESET);
                    }
                    if (message.getType().equals("/server")) {
                        isGroup = true;
                        theGroup = Integer.parseInt(message.getText());
                    }
                    if (message.getType().equals("/newchannel")) {
                        isChannel = true;
                        theChannel = Integer.parseInt(message.getText());
                    }
                    if (message.getType().equals("/channel")) {
                        isChannel = true;
                        theChannel = Integer.parseInt(message.getText());
                    }
                    if (message.getType().split("-")[0].equals("chatroom")) {
                        if (isGroup && theGroup == Integer.parseInt(message.getType().split("-")[1])) {
                            if (isChannel && theChannel == Integer.parseInt(message.getType().split("-")[2])) {
                                if (!username.equals(message.getOwner())) {
                                    System.out.println("[" + message.getOwner() + "] : " + message.getText());
                                }
                            }
                        }
                    }
                    if (message.getType().equals("channelchats")) {
                        if (!username.equals(message.getOwner())) {
                            System.out.println("[" + message.getOwner() + "] : " + message.getText());
                        }
                        else {
                            System.out.println(ANSI_GREEN + "[" + message.getOwner() + "] : " + message.getText() + ANSI_RESET);
                        }
                    }

                }
                catch (IOException e) {
                    if (connection) {
                        System.out.println(ANSI_RED + "The server shut down (Enter to close the program)" + ANSI_RESET);
                        connection = false;
                    }
                }
                catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
