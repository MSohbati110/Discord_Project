package com.company;

import java.io.Serializable;

/**
 * The Message class is for transporting messages. messages in Message class constructor
 * have this pattern : name of owner of message, message that you have, type of your message.
 * Type of the message use in the message of between client and server to give our orders.
 * This class only have one constructor and getter method, so that you can not change your message.
 * @author Mostafa Sohbati
 * @version  1.0
 */
public class Message implements Serializable {
    private String owner;
    private String text;
    private String type;

    // constructor
    /**
     * This is the constructor of Message class
     * @param owner person who wrote the message
     * @param text main message
     * @param type type of your message or type of your order
     */
    public Message(String owner, String text, String type) {
        this.owner = owner;
        this.text = text;
        this.type = type;
    }
    // getter

    /**
     * gets the owner value
     * @return String owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * gets the text value, text is main message
     * @return String text
     */
    public String getText() {
        return text;
    }

    /**
     * gets type of message
     * @return String type
     */
    public String getType() {
        return type;
    }

}
