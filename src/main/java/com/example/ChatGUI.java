package com.example;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;

public class ChatGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private ChatClient client;

    public ChatGUI() {
        setTitle("WebSocket Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 600);
        setLayout(new BorderLayout());

        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        sendButton = new JButton("Send");
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // Connect to WebSocket server
        try {
            client = new ChatClient(new URI("ws://localhost:8887"), this);
            client.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        // Add action listeners
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        setVisible(true);
    }

    private void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty() && client != null) {
            client.send(message);
            messageField.setText("");
        }
    }

    public void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatGUI());
    }
}