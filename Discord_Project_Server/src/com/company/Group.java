package com.company;

import java.io.Serializable;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * The Group class represent a space for make a community between clients.
 * Owner and admins of group can make channels and other users can join channels they like.
 * You can change name of the group, add members, remove members. Each group has an id that is a number.
 * @author Mostafa Sohbati & Shahriar Mirnajafi
 * @version 1.0
 */
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

    /**
     * This is the constructor of Group class, you pass the name of owner of group.
     * @param owner String
     */
    public Group(String owner) {
        this.owner = owner;
        members.add(owner);
    }
    // getter

    /**
     * Gives the name of group.
     * @return String group name
     */
    public String getName() {
        return name;
    }

    /**
     * Gives id number of group.
     * @return int id number
     */
    public int getId() {
        return id;
    }

    /**
     * Gives list of member of group.
     * @return ArrayList<String> members
     */
    public ArrayList<String> getMembers() {
        return members;
    }

    /**
     * Gives channels of group with their id.
     * @return HashMap<Integer,Channel>
     */
    public HashMap<Integer,Channel> getChannels() {
        return channels;
    }

    // setter

    /**
     * Sets the name of group by the name that passed.
     * @param name String
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the id number of group by the if number that passed.
     * @param id int
     */
    public void setId(int id) {
        this.id = id;
    }

    // changing the name of the server

    /**
     * Changes the name of group with theme that you entered.
     */
    public void changeName() {
        Scanner scanner = new Scanner(System.in);
        System.out.println(ANSI_YELLOW + "Choose a name for the server :" + ANSI_RESET);
        name = scanner.nextLine();
        System.out.println(ANSI_BLUE + "server name set successfully" + ANSI_RESET);
    }
    // adding a member to the server

    /**
     * Gets member's name and add the member to group.
     * @param member String
     */
    public void addMember (String member) {
        members.add(member);
    }
    // removing a member from the server

    /**
     * Gets member's name and remove the member from group.
     * @param member String
     */
    public void removeMember (String member) {
        members.remove(member);
    }
    // check if someone is a member or not

    /**
     * Gets a name and checks if there is any member with this name.
     * @param name String
     * @return boolean
     */
    public boolean isMember (String name) {
        if (members.contains(name)) {
            return true;
        }
        return false;
    }
    // check if that channel exists or not

    /**
     * Gets a name and checks if there is any channel with this name.
     * @param name String
     * @return boolean
     */
    public boolean isChannel (String name) {
        for (Channel channel : channels.values()) {
            if (channel.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    // making a new channel

    /**
     * Gets the name of channel that you want to create and creates it, and returns
     * id number of channel.
     * @param name String
     * @return int
     */
    public int newChannel (String name) {
        Channel channel = new Channel();
        channel.setName(name);
        channel.setId(channelId);
        channels.put(channelId,channel);
        channelId++;
        return channelId-1;
    }
    // returning a channel id

    /**
     * Gets name of channel that you want to know its id and returns channel id number.
     * @param name String
     * @return int
     */
    public int channelID (String name) {
        for (Channel channel : channels.values()) {
            if (channel.getName().equals(name)) {
                return channel.getId();
            }
        }
        return -1;
    }
    // return a channel

    /**
     * Gets id number of channel and returns that channel.
     * @param id int
     * @return Channel
     */
    public Channel channel (int id) {
        for (Channel channel1 : channels.values()) {
            if (channel1.getId() == id) {
                return channel1;
            }
        }
        return null;
    }
    // adding a chat to a channel

    /**
     * Gets chat and information of channel (id number) and add this chat to channel.
     * @param channelId int
     * @param username String
     * @param chat Sting
     */
    public void addChat (int channelId, String username, String chat) {
        channels.get(channelId).addChat(username,chat);
    }

    //pin a message

    /**
     * Gets the number of chat that you want pin it, and gets id number of channel that
     * chat exists in there.
     * @param channelId int
     * @param index int
     */
    public void pinMessage (int channelId, int index){
        channels.get(channelId).pinMessage(index);
    }

    /**
     * Gets the id number of channel that you want see its pined messages.
     * @param channelId int
     * @return String
     */
    public String getPinedMessages (int channelId){
        return channels.get(channelId).getPinMessages();
    }

    //react to a message

    /**
     * Gets id number of channel that chat is in there, and gets number of chat you want to react
     * and your reaction.
     * @param channelId int
     * @param index Integer
     * @param react Integer
     */
    public void reaction (int channelId, Integer index, Integer react){
        channels.get(channelId).reaction(react, index);
    }

    /**
     * Gets id number of channel that chat is there, ind chat number.
     * returns String of reactions of that chat.
     * @param channelId int
     * @param index Integer
     * @return String
     */
    public String getReactions (int channelId, Integer index){
        return channels.get(channelId).getReactions(index);
    }
}
