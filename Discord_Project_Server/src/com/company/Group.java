package com.company;

import java.io.Serializable;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Group implements Serializable {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private int id;
    private String name;
    private String owner;
    private ArrayList<String> members = new ArrayList<>();
    private HashMap<Integer,Channel> channels = new HashMap<>();
    private int channelId = 0;

    // constructor
    public Group(String owner) {
        this.owner = owner;
        members.add(owner);
    }
    // getter
    public String getName() {
        return name;
    }
    public int getId() {
        return id;
    }
    public ArrayList<String> getMembers() {
        return members;
    }

    public HashMap<Integer,Channel> getChannels() {
        return channels;
    }

    // setter
    public void setName(String name) {
        this.name = name;
    }
    public void setId(int id) {
        this.id = id;
    }

    // changing the name of the server
    public void changeName() {
        Scanner scanner = new Scanner(System.in);
        System.out.println(ANSI_YELLOW + "Choose a name for the server :" + ANSI_RESET);
        name = scanner.nextLine();
        System.out.println(ANSI_BLUE + "server name set successfully" + ANSI_RESET);
    }
    // adding a member to the server
    public void addMember (String member) {
        members.add(member);
    }
    // removing a member from the server
    public void removeMember (String member) {
        members.remove(member);
    }
    // check if someone is a member or not
    public boolean isMember (String name) {
        if (members.contains(name)) {
            return true;
        }
        return false;
    }
    // check if that channel exists or not
    public boolean isChannel (String name) {
        for (Channel channel : channels.values()) {
            if (channel.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    // making a new channel
    public int newChannel (String name) {
        Channel channel = new Channel();
        channel.setName(name);
        channel.setId(channelId);
        channels.put(channelId,channel);
        channelId++;
        return channelId-1;
    }
    // returning a channel id
    public int channelID (String name) {
        for (Channel channel : channels.values()) {
            if (channel.getName().equals(name)) {
                return channel.getId();
            }
        }
        return -1;
    }
    // return a channel
    public Channel channel (int id) {
        for (Channel channel1 : channels.values()) {
            if (channel1.getId() == id) {
                return channel1;
            }
        }
        return null;
    }
    // adding a chat to a channel
    public void addChat (int channelId, String username, String chat) {
        channels.get(channelId).addChat(username,chat);
    }

    //pin a message
    public void pinMessage (int channelId, int index){
        channels.get(channelId).pinMessage(index);
    }

    public String getPinedMessages (int channelId){
        return channels.get(channelId).getPinMessages();
    }
}
