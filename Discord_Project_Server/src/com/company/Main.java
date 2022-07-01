package com.company;

/**
 * In Main class our program starts.
 */
public class Main {
    /**
     * Program starts from here
     * @param args String[]
     */
    public static void main(String[] args) {
        Server server = new Server();
        server.saving();
        server.startServer();
    }
}
