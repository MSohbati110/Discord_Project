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

/**
 * Client class has fields form saving client data and has method to
 * connect clients to server. After each change in clients data, new data will save.
 * @author Mostafa Sohbati & Shahriar Mirnajafi
 * @version 1.0
 */
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
    private File profile = null;
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
    //send file to server
    private void sendFile (Socket socket, String path){
        int bytes = 0;
        try {
            File file = new File(path);
            FileInputStream fin = new FileInputStream(file);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeLong(file.length());
            byte[] buffer = new byte[4 * 1024];
            while ((bytes = fin.read(buffer)) != -1){
                dos.write(buffer, 0, bytes);
                dos.flush();
            }
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //receive file from server
    private void receiveFile (Socket socket, String fileName){
        int bytes = 0;
        try {
            FileOutputStream fout = new FileOutputStream(fileName);
            DataInputStream din = new DataInputStream(socket.getInputStream());
            long size = din.readLong();
            byte[] buffer = new byte[4 * 1024];
            while (size > 0 && (bytes = din.read(buffer, 0, (int)Math.min(buffer.length, size))) != 0){
                fout.write(buffer,0, bytes);
                size -= bytes;
            }
            fout.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    //get status of friends
    private void getFriendsStatus (ObjectOutputStream out) {
        for (String friend : friendsList.keySet()) {
            if (friendsList.get(friend)){
                //friendsStatus.put(friend, null);
                try {
                    out.writeObject(new Message(username, friend, "/getstatus"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private  void printFriendList () {
        int counter = 1;
        for (String friend : friendsList.keySet()) {
            if (friendsList.get(friend)) {

                System.out.println(ANSI_YELLOW + counter + "- " + friend + " - Status : " + friendsStatus.get(friend) + ANSI_RESET);
                counter++;
            }
        }
        if (counter == 1) {
            System.out.println(ANSI_RED + "you have no friends!" + ANSI_RESET);
        }
    }
    // saving data in file
    private void saving () {
        try {
            FileOutputStream fout = new FileOutputStream(username+".txt");
            ObjectOutputStream outf = new ObjectOutputStream(fout);
            data.add(friendsList);
            data.add(privateChats);
            data.add(groups);
            data.add(friendsStatus);
            data.add(profile);
            outf.writeObject(data);
            fout.close();
            outf.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    // starting the client

    /**
     * This method connect client to server and make to ObjectStream, one for
     * sending and other for receiving data. If there is any previous data, they will
     * read in here.
     */
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
            friendsStatus = (HashMap<String, String>) data.get(3);
            profile = (File) data.get(4);

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
                        else if (text.split(" ")[0].equals("/setprofile")){
                            out.writeObject(new Message(username, "", "/profile"));
                            String path;
                            System.out.println(ANSI_YELLOW + "Enter your photo address :" + ANSI_RESET);
                            path = scanner.nextLine();
                            profile = new File(path);
                            sendFile(socket, path);
                        }
                        else if (text.split(" ")[0].equals("/friend") && text.split(" ").length == 2) {
                            if (!username.equals(text.split(" ")[1])) {
                                out.writeObject(new Message(username,text.split(" ")[1],"/friend"));
                            }
                            else {
                                System.out.println(ANSI_RED + "No No No" + ANSI_RESET);
                            }
                        }
                        else if (text.split(" ")[0].equals("/friendrequest") && text.split(" ").length == 1) {
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
                        else if (text.split(" ")[0].equals("/friendaccept") && text.split(" ").length == 2) {
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
                        else if (text.split(" ")[0].equals("/friendreject") && text.split(" ").length == 2) {
                            if (friendsList.containsKey(text.split(" ")[1]) && !friendsList.get(text.split(" ")[1])) {
                                friendsList.remove(text.split(" ")[1]);
                                saving();
                                System.out.println(ANSI_BLUE + "friend request rejected" + ANSI_RESET);
                            }
                            else {
                                System.out.println(ANSI_RED + "there is no friend request from this username" + ANSI_RESET);
                            }
                        }
                        else if (text.split(" ")[0].equals("/friendlist") && text.split(" ").length == 1) {
                            getFriendsStatus(out);
                            printFriendList();
                        }
                        else if (text.split(" ")[0].equals("/server") && text.split(" ").length == 2) {
                            out.writeObject(new Message(username,text.split(" ")[1],"/server"));
                        }
                        else if (text.split(" ")[0].equals("/setstatus")){
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
                                        status = "Do-Not-Disturb";
                                        condition = false;
                                        break;
                                    case "4":
                                        status = "Invisible";
                                        condition = false;
                                        break;
                                    case "-1":
                                        status = "None";
                                        condition = false;
                                        break;
                                    default:
                                        System.out.println(ANSI_RED + "Wrong input" + ANSI_RESET);
                                        break;
                                }
                            }
                            out.writeObject(new Message(username, status, "/setstatus"));
                        }
                        else if (text.split(" ")[0].equals("/sendfile")){
                            String fileName = text.split(" ")[1];
                            out.writeObject(new Message(username, fileName, "/sendfile"));
                            sendFile(socket, fileName);
                        }
                        else if (text.split(" ")[0].equals("/receivefile")){
                            String fileName;
                            System.out.println(ANSI_YELLOW + "Enter file name with its format(example: file.format): ");
                            fileName = scanner.nextLine();

                            out.writeObject(new Message(username, fileName, "/receivefile"));
                        }
                        else if (!isGroup) {
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
                            else if (text.split(" ")[0].equals("/chatoff") && text.split(" ").length == 1) {
                                isPrivateChat = false;
                                privateChatUser = "";
                            }
                            else if (text.split(" ")[0].equals("/newserver") && text.split(" ").length == 1) {
                                Group group = new Group(username);
                                group.changeName();
                                isGroup = true;
                                saving();
                                out.writeObject(new Message(username,group.getName(),"/newserver"));
                            }
                        }
                        else if (isGroup) {
                            if (text.split(" ")[0].equals("/exitserver") && text.split(" ").length == 1) {
                                System.out.println(ANSI_BLUE + "exit the server successfully" + ANSI_RESET);
                                isGroup = false;
                                theGroup = -1;
                            }
                            else if (text.split(" ")[0].equals("/changeservername") && text.split(" ").length == 1) {
                                Group group = new Group(username);
                                group.changeName();
                                saving();
                                out.writeObject(new Message(String.valueOf(theGroup),group.getName(),"/changeservername"));
                            }
                            else if (text.split(" ")[0].equals("/addmember") && text.split(" ").length == 2) {
                                if (friendsList.containsKey(text.split(" ")[1])) {
                                    out.writeObject(new Message(username,theGroup + "-" + text.split(" ")[1],"/addmember"));
                                }
                                else {
                                    System.out.println(ANSI_RED + "you have no friend with this username" + ANSI_RESET);
                                }
                            }
                            else if (text.split(" ")[0].equals("/removemember") && text.split(" ").length == 2) {
                                out.writeObject(new Message(username, theGroup + "-" + text.split(" ")[1],"/removemember"));
                            }
                            else if (text.split(" ")[0].equals("/status") && text.split(" ").length == 2) {
                                out.writeObject(new Message(username, theGroup + "-" + text.split(" ")[1],"/status"));
                            }
                            else if (text.split(" ")[0].equals("/newchannel") && text.split(" ").length == 1) {
                                Channel channel = new Channel();
                                channel.changeName();
                                out.writeObject(new Message(username,theGroup + "-" + channel.getName(),"/newchannel"));
                            }
                            else if (text.split(" ")[0].equals("/channel") && text.split(" ").length == 2) {
                                out.writeObject(new Message(username, theGroup + "-" + text.split(" ")[1],"/channel"));
                            }
                            if (isChannel) {
                                if (text.split(" ")[0].equals("/exitchannel") && text.split(" ").length == 1) {
                                    isChannel = false;
                                    theChannel = -1;
                                    System.out.println(ANSI_BLUE + "exit the channel successfully" + ANSI_RESET);
                                }
                                else if (text.split(" ")[0].equals("/pin") && text.split(" ").length == 2){
                                    out.writeObject(new Message(username, text.split(" ")[1] + " " + theGroup + " " + theChannel, "/pin"));
                                    System.out.println(ANSI_BLUE + "you pined message" + ANSI_RESET);
                                }
                                else if (text.split(" ")[0].equals("/showpins")){
                                    out.writeObject(new Message(username, theGroup + " " + theChannel, "/showpins"));
                                }
                                else if (text.split(" ")[0].equals("/react") && text.split(" ").length == 2){
                                    Integer reaction = 0;
                                    boolean condition = true;
                                    System.out.println(ANSI_YELLOW + "Choose your react(-1 for cancel):\n1.like\n2.unlike\n3.laugh");
                                    while (condition){
                                        reaction = scanner.nextInt();
                                        switch (reaction){
                                            case 1:
                                            case 2:
                                            case 3:
                                            case -1:
                                                condition = false;
                                                break;
                                            default:
                                                System.out.println(ANSI_RED + "wrong input" + ANSI_RESET);
                                        }
                                    }
                                    if (reaction != -1){
                                        System.out.println(ANSI_YELLOW + "your react saved" + ANSI_RESET);
                                        out.writeObject(new Message(username, theGroup + " " + theChannel + " " + reaction + " " + text.split(" ")[1], "/react"));
                                    }
                                }
                                else if (text.split(" ")[0].equals("/showreacts") && text.split(" ").length == 2){
                                    out.writeObject(new Message(username, theGroup + " " + theChannel + " " + text.split(" ")[1], "/showreacts"));
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
                    else if (message.getType().equals("")) {
                        System.out.println(ANSI_BLUE + message.getText() + ANSI_RESET);
                    }
                    else if (message.getType().equals("/friend")) {
                        friendsList.put(message.getOwner(),false);
                        saving();
                    }
                    else if (message.getType().equals("/friendaccept")) {
                        friendsList.put(message.getOwner(),true);
                        saving();
                    }
                    else if (message.getType().equals("/getstatus")) {
                        friendsStatus.put(message.getText().split(" ")[0], message.getText().split(" ")[1]);
                        saving();
                        friendsStatus = (HashMap<String, String>) data.get(3);
                    }
                    else if (message.getType().equals("/chat")) {
                        if (!privateChats.containsKey(message.getOwner())) {
                            privateChats.put(message.getOwner(),new ArrayList<>());
                            saving();
                        }
                    }
                    else if (message.getType().equals("pchat")) {
                        ArrayList<String> chats = privateChats.get(message.getOwner());
                        chats.add(message.getOwner() + ": " + message.getText());
                        saving();
                        if (privateChatUser.equals(message.getOwner())) {
                            System.out.println(message.getOwner() + ": " + message.getText());
                        }
                    }
                    else if (message.getType().equals("/newserver")) {
                        theGroup = Integer.parseInt(message.getText());
                        groups.put(Integer.parseInt(message.getText()),true);
                    }
                    else if (message.getType().equals("groupjoin")) {
                        Group group = (Group) in.readObject();
                        groups.put(group.getId(),true);
                        System.out.println(ANSI_BLUE + "you have been added to the " + group.getName() + " server. WELCOME!" + ANSI_RESET);
                    }
                    else if (message.getType().equals("groupremove")) {
                        ArrayList<Integer> groups1 = new ArrayList<Integer>(groups.keySet());
                        groups.remove(groups1.indexOf(Integer.parseInt(message.getText())));
                        System.out.println(ANSI_RED + "you have been removed from the " + message.getOwner() +" server" + ANSI_RESET);
                    }
                    else if (message.getType().equals("/server")) {
                        isGroup = true;
                        theGroup = Integer.parseInt(message.getText());
                    }
                    else if (message.getType().equals("/newchannel")) {
                        isChannel = true;
                        theChannel = Integer.parseInt(message.getText());
                    }
                    else if (message.getType().equals("/channel")) {
                        isChannel = true;
                        theChannel = Integer.parseInt(message.getText());
                    }
                    else if (message.getType().split("-")[0].equals("chatroom")) {
                        if (isGroup && theGroup == Integer.parseInt(message.getType().split("-")[1])) {
                            if (isChannel && theChannel == Integer.parseInt(message.getType().split("-")[2])) {
                                if (!username.equals(message.getOwner())) {
                                    System.out.println("[" + message.getOwner() + "] : " + message.getText());
                                }
                            }
                        }
                    }
                    else if (message.getType().equals("channelchats")) {
                        if (!username.equals(message.getOwner())) {
                            System.out.println("[" + message.getOwner() + "] : " + message.getText());
                        }
                        else {
                            System.out.println(ANSI_GREEN + "[" + message.getOwner() + "] : " + message.getText() + ANSI_RESET);
                        }
                    }
                    else if (message.getType().equals("/showpins")){
                        System.out.println(message.getText());
                    }
                    else if (message.getType().equals("/showreacts")){
                        System.out.println(message.getText());
                    }
                    else if (message.getType().equals("/sendfile")){
                        System.out.println(ANSI_BLUE + "File sent successfully" + ANSI_RESET);
                    }
                    else if (message.getType().equals("/receivefile")){
                        receiveFile(socket, message.getText());
                        System.out.println(ANSI_BLUE + "File received successfully" + ANSI_RESET);
                    }
                    else if (message.getType().equals("/setstatus")){
                        System.out.println(ANSI_BLUE + message.getText() + ANSI_RESET);
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
