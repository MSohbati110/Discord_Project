package com.company;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

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
    private int phoneNumber = -1;
    private boolean sign = true;
    private HashMap<String,Boolean> friendsList = new HashMap<>();

    // sign up method
    private void signUp () {
        Scanner scanner = new Scanner(System.in);
        System.out.println(ANSI_YELLOW + "Enter your username : " + ANSI_RESET);
        username = scanner.nextLine();
        System.out.println(ANSI_YELLOW + "Enter your password : " + ANSI_RESET);
        passWord = scanner.nextLine();
        System.out.println(ANSI_YELLOW + "Enter your email : " + ANSI_RESET);
        email = scanner.nextLine();
        System.out.println(ANSI_YELLOW + "Enter your phoneNumber : (Enter -1 to pass)" + ANSI_RESET);
        phoneNumber = scanner.nextInt();
    }
    // sign in method
    private void signIn () {
        Scanner scanner = new Scanner(System.in);
        System.out.println(ANSI_YELLOW + "Enter your username : " + ANSI_RESET);
        username = scanner.nextLine();
        System.out.println(ANSI_YELLOW + "Enter your password : " + ANSI_RESET);
        passWord = scanner.nextLine();
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
            // Listener
            Thread thread = new Thread(new Listener(in,socket));
            thread.start();

            // writer
            while (connection) {
                String text = scanner.nextLine();
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
                    }
                }catch (StringIndexOutOfBoundsException e) {

                }

                out.writeObject(new Message(username,text,""));
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
                    if (message.getType().equals("/friend")) {
                        friendsList.put(message.getOwner(),false);
                    }
                    if (message.getType().equals("error")) {
                        System.out.println(ANSI_RED + message.getText() + ANSI_RESET);
                    }
                    if (message.getType().equals("")) {
                        System.out.println(ANSI_BLUE + message.getText() + ANSI_RESET);
                    }
                    if (message.getType().equals("/friendaccept")) {
                        friendsList.put(message.getOwner(),true);
                    }
                }
                catch (IOException | ClassNotFoundException e) {
                    if (connection) {
                        System.out.println(ANSI_RED + "The server shut down (Enter to close the program)" + ANSI_RESET);
                        connection = false;
                    }
                }
            }
        }
    }
}
