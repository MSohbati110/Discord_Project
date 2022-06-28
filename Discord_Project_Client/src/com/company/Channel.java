package com.company;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Channel implements Serializable {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private int id;
    private String name;
    private ArrayList<String> chats = new ArrayList<>();
    private HashMap<Integer,ArrayList<Integer>> reactions = new HashMap<>();
    private  ArrayList<String> pinMessages = new ArrayList<>();
    private ArrayList<String> chatsUser = new ArrayList<>();

    // getter
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public ArrayList<String> getChats() {
        return chats;
    }
    public ArrayList<String> getChatsUser() {
        return chatsUser;
    }
    public String getPinMessages() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String pinMessage : pinMessages) {
            stringBuilder.append(pinMessage);
            stringBuilder.append("\n");
        }
        return  stringBuilder.toString();
    }

    // setter
    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    // change the name of channel
    public void changeName () {
        Scanner scanner = new Scanner(System.in);
        System.out.println(ANSI_YELLOW + "Choose a name for the channel :" + ANSI_RESET);
        name = scanner.nextLine();
        System.out.println(ANSI_BLUE + "channel name set successfully" + ANSI_RESET);
    }
    // adding a chat
    public void addChat (String username, String chat) {
        chats.add(chat);
        chatsUser.add(username);
    }
    // pin a message
    public  void pinMessage (int index){
        pinMessages.add(chats.get(index));
    }
    //react to a message
    public void reaction (Integer react, Integer index){
        if (reactions.containsKey(index)){
            ArrayList<Integer> reacts = reactions.get(index);
            reacts.add(react);
            reactions.put(index, reacts);
        }
        else {
            ArrayList<Integer> reacts = new ArrayList<>();
            reacts.add(react);
            reactions.put(index, reacts);
        }
    }

    public String getReactions (Integer index){
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<Integer> reacts = reactions.get(index);
        int likes = 0, unlikes = 0, laughs = 0;
        stringBuilder.append(index).append(" : ");
        for (Integer react : reacts) {
            if (react.equals(1)){
                likes++;
            } else if (react.equals(2)) {
                unlikes++;
            } else if (react.equals(3)) {
                laughs++;
            }
        }
        stringBuilder.append("Likes : ");
        stringBuilder.append(likes);
        stringBuilder.append(" Unlikes : ");
        stringBuilder.append(unlikes);
        stringBuilder.append(" Laughs : ");
        stringBuilder.append(laughs);

        return stringBuilder.toString();
    }
}
