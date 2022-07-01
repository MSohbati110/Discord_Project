package com.company;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * The Channel class represent a suitable space for clients to communicate with each others.
 * Send messages, react to sent messages and pin a message to make other clients' attention to that message.
 * Each channel has a specific name and id number. This class has some methods that most of them are
 * getter and setter method, except them, there is methods for pin a message react to a message,and show them.
 * This class has no-param constructor.
 * @author Mostafa Sohbati & Shahriar Mirnajafi
 * @version 1.0
 */
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

    /**
     * Gives id number of channel.
     * @return int id
     */
    public int getId() {
        return id;
    }

    /**
     * Gives name of channel.
     * @return String name
     */
    public String getName() {
        return name;
    }

    /**
     * Gives a list of chats that is for channel.
     * @return ArrayList<String> chats
     */
    public ArrayList<String> getChats() {
        return chats;
    }

    /**
     * Gives a list of users that they are in channel, that they send a chat.
     * @return ArrayList<String> chatsUser
     */
    public ArrayList<String> getChatsUser() {
        return chatsUser;
    }

    /**
     * Make all pin messages together and gives them in a String.
     * @return String pined messages
     */
    public String getPinMessages() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String pinMessage : pinMessages) {
            stringBuilder.append(pinMessage);
            stringBuilder.append("\n");
        }
        return  stringBuilder.toString();
    }

    // setter

    /**
     * Sets id number of channel by the number that passed.
     * @param id int
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the name of channel by the name that passed.
     * @param name String
     */
    public void setName(String name) {
        this.name = name;
    }
    // change the name of channel

    /**
     * This method asks you to enter a name for channel and change name of channel
     * with new name.
     */
    public void changeName () {
        Scanner scanner = new Scanner(System.in);
        System.out.println(ANSI_YELLOW + "Choose a name for the channel :" + ANSI_RESET);
        name = scanner.nextLine();
        System.out.println(ANSI_BLUE + "channel name set successfully" + ANSI_RESET);
    }
    // adding a chat

    /**
     * Gets name of client that send chat (username) and chat, then add them to
     * lists of channel.
     * @param username String
     * @param chat String
     */
    public void addChat (String username, String chat) {
        chats.add(chat);
        chatsUser.add(username);
    }
    // pin a message

    /**
     * Gets number of chat that you want to pin.
     * @param index int
     */
    public  void pinMessage (int index){
        pinMessages.add(chats.get(index));
    }
    //react to a message

    /**
     * Gets number of chat you want to react to, and number of your reaction.
     * @param react Integer
     * @param index Integer
     */
    public void reaction (Integer react, Integer index){
        ArrayList<Integer> reacts;
        if (reactions.containsKey(index)){
            reacts = reactions.get(index);
        }
        else {
            reacts = new ArrayList<>();
        }
        reacts.add(react);
        reactions.put(index, reacts);
    }

    /**
     * Gets number of chat that you want to see its reactions.
     * @param index Integer
     * @return String reactions
     */
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
        stringBuilder.append(" - Unlikes : ");
        stringBuilder.append(unlikes);
        stringBuilder.append(" - Laughs : ");
        stringBuilder.append(laughs);

        return stringBuilder.toString();
    }
}
