package com.example;


import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Start the server
        ChatServer server = new ChatServer(8887);
        server.start();
        System.out.println("Server started on port 8887");

        // Start the GUI client
        SwingUtilities.invokeLater(() -> {
            // You can start multiple instances to test the chat
            new EnhancedChatGUI();
        });
    }
}