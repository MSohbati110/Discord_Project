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
    // show members
    public void show () {
        for (String name : members) {
            System.out.print(name + "  ,  ");
        }
        System.out.println();
    }
}
