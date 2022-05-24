package com.company;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Client {
    private String ip = "127.0.0.1";
    private int port = 6000;
    private boolean connection = true;
    private String username = "";
    private String passWord = "";
    private String email = "";
    private int phoneNumber = -1;
    private boolean sign = true;

    // sign up method
    private void signUp () {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username : ");
        username = scanner.nextLine();
        System.out.println("Enter your password : ");
        passWord = scanner.nextLine();
        System.out.println("Enter your email : ");
        email = scanner.nextLine();
        System.out.println("Enter your phoneNumber : (Enter -1 to pass)");
        phoneNumber = scanner.nextInt();
    }
    // sign in method
    private void signIn () {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username : ");
        username = scanner.nextLine();
        System.out.println("Enter your password : ");
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
                System.out.println("1- sign up");
                System.out.println("2- sign in");
                System.out.println("3- exit");
                int input = scanner.nextInt();
                while (input != 1 && input != 2 && input != 3) {
                    System.out.println("invalid answer!");
                    input = scanner.nextInt();
                }
                if (input == 1) { // sign up
                    signUp();
                    out.writeObject(new Message(username, passWord + "-" + email + "-" + phoneNumber, "sign up"));
                    Message message = (Message) in.readObject();
                    if (message.getType().equals("error")) {
                        System.out.println(message.getText());
                    }
                    if (!message.getType().equals("error")) {
                        sign = false;
                    }
                }
                if (input == 2) { // sign in
                    signIn();
                    out.writeObject(new Message(username, passWord, "sign in"));
                    Message message = (Message) in.readObject();
                    if (message.getType().equals("error")) {
                        System.out.println(message.getText());
                    }
                    if (!message.getType().equals("error")) {
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
                out.writeObject(new Message(username,text,""));
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Server is down");
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
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("The server shut down (Enter to close the program)");
                    connection = false;
                }
            }
        }
    }
}
