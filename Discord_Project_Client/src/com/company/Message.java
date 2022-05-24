package com.company;

import java.io.Serializable;

public class Message implements Serializable {
    private String owner;
    private String text;
    private String type; // sign up . sign in . error
    // constructor
    public Message(String owner, String text, String type) {
        this.owner = owner;
        this.text = text;
        this.type = type;
    }
    // getter
    public String getOwner() {
        return owner;
    }
    public String getText() {
        return text;
    }
    public String getType() {
        return type;
    }

}
