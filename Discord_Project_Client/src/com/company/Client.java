package com.company;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.regex.Pattern;

public class Client {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private String ip = "127.0.0.1";
    private int port = 6000;
    private boolean connection = true;
    private String username = "";
    private String passWord = "";
    private String email = "";
    private String phoneNumber = "";
    private boolean sign = true;
    private HashMap<String,Boolean> friendsList = new HashMap<>();
    private HashMap<String, ArrayList<String>> privateChats = new HashMap<>();
    private boolean isPrivateChat = false;
    private String privateChatUser = "";
    private ArrayList<Object> data = new ArrayList<>();

    // sign up method
    private void signUp () {
        boolean condition;
        Scanner scanner = new Scanner(System.in);
        System.out.println(ANSI_YELLOW + "Enter your username : " + ANSI_RESET);
        
        do {
            username = scanner.nextLine();
            if (!(Pattern.matches("[a-zA-Z0-9]*", username))) {
                System.out.println(ANSI_RED + "username should have only these characters : a-z, A-Z, 0-9." + ANSI_RESET);
                condition = true;
            } else if (username.length() < 6) {
                System.out.println(ANSI_RED + "username should have at least 6 characters" + ANSI_RESET);
                condition = true;
            }
            else {
                condition = false;
            }
        } while (condition);
        System.out.println(ANSI_YELLOW + "Enter your password : " + ANSI_RESET);

        do {
            passWord = scanner.nextLine();
            if (passWord.contains(" ") || passWord.length() < 8) {
                System.out.println(ANSI_RED + "password should be at least 8 characters and don't contains space." + ANSI_RESET);
                condition = true;
            } else {
                condition = false;
            }
        } while (condition);
        System.out.println(ANSI_YELLOW + "Enter your email : " + ANSI_RESET);
        
        do {
            email = scanner.nextLine();
            if (!Pattern.matches("^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$", email)) {
                System.out.println(ANSI_RED + "invalid email address." + ANSI_RESET);
                condition = true;
            } else {
                condition = false;
            }
        } while (condition);
        System.out.println(ANSI_YELLOW + "Enter your phoneNumber : (Enter -1 to pass)" + ANSI_RESET);
        
        do {
            phoneNumber = scanner.nextLine();
            if (phoneNumber.equals("-1")) {
                condition = false;
            } else if (!Pattern.matches("[0-9]*", phoneNumber)) {
                System.out.println(ANSI_RED + "phonenumber is invalid" + ANSI_RESET);
                condition = true;
            } else if (phoneNumber.length() != 11) {
                System.out.println(ANSI_RED + "phonenumber is invalid" + ANSI_RESET);
                condition = true;
            } else if (!phoneNumber.substring(0,2).equals("09")) {
                System.out.println(ANSI_RED + "phonenumber is invalid" + ANSI_RESET);
                condition = true;
            }
            else {
                condition = false;
            }
        } while (condition);
        scanner.close();
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
                    }
                    else {
                        if (isPrivateChat) {
                            ArrayList<String> chats = privateChats.get(privateChatUser);
                            chats.add("me: " + text);
                            saving();
                            out.writeObject(new Message(username,text,"pchat-"+privateChatUser));
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
