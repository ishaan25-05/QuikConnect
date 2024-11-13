package com.example;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

// WebSocket imports
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class EnhancedChatGUI extends JFrame {
    private JTextPane chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private JComboBox<String> statusComboBox;
    private ChatClient client;
    private String username;
    private Color primaryColor = new Color(64, 128, 128);
    private Color secondaryColor = new Color(230, 240, 240);
    private Font defaultFont = new Font("Segoe UI", Font.PLAIN, 14);
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public EnhancedChatGUI() {
        // Get username
        username = JOptionPane.showInputDialog(this, "Enter your username:", "Login", JOptionPane.QUESTION_MESSAGE);
        if (username == null || username.trim().isEmpty()) {
            username = "User" + System.currentTimeMillis() % 1000;
        }

        setupFrame();
        setupComponents();
        setupLayout();
        setupListeners();
        connectToServer();
        
        setVisible(true);
    }

    private void setupFrame() {
        setTitle("Chat Application - " + username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);
        
        // Set custom look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupComponents() {
        // Chat Area
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setFont(defaultFont);
        chatArea.setBackground(Color.WHITE);
        
        // Message Input
        messageField = new JTextField();
        messageField.setFont(defaultFont);
        messageField.setBorder(new CompoundBorder(
            messageField.getBorder(),
            new EmptyBorder(5, 5, 5, 5)
        ));

        // Send Button
        sendButton = new JButton("Send");
        sendButton.setFont(defaultFont);
        sendButton.setBackground(primaryColor);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        
        // User List
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setFont(defaultFont);
        userList.setFixedCellHeight(30);
        userList.setBackground(secondaryColor);
        userList.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // Status Combo Box
        String[] statuses = {"Online", "Away", "Busy", "Offline"};
        statusComboBox = new JComboBox<>(statuses);
        statusComboBox.setFont(defaultFont);
        statusComboBox.setBackground(Color.WHITE);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Main content panel with chat area
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Chat panel (chat area + input panel)
        JPanel chatPanel = new JPanel(new BorderLayout(5, 5));
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        
        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);
        
        contentPanel.add(chatPanel, BorderLayout.CENTER);
        
        // Right panel (user list + status)
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setPreferredSize(new Dimension(200, 0));
        
        // User list panel
        JPanel userListPanel = new JPanel(new BorderLayout());
        JLabel usersLabel = new JLabel(" Online Users", SwingConstants.LEFT);
        usersLabel.setFont(new Font(defaultFont.getName(), Font.BOLD, defaultFont.getSize()));
        usersLabel.setForeground(primaryColor);
        usersLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        userListPanel.add(usersLabel, BorderLayout.NORTH);
        userListPanel.add(new JScrollPane(userList), BorderLayout.CENTER);
        
        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        statusPanel.add(statusComboBox, BorderLayout.CENTER);
        
        rightPanel.add(userListPanel, BorderLayout.CENTER);
        rightPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(contentPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private void setupListeners() {
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        
        // Window listener for cleanup
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client != null) {
                    client.close();
                }
            }
        });
        
        // Status change listener
        statusComboBox.addActionListener(e -> {
            String status = (String) statusComboBox.getSelectedItem();
            if (client != null) {
                client.send("STATUS:" + username + ":" + status);
            }
        });
    }

    private void connectToServer() {
        try {
            client = new ChatClient(new URI("ws://localhost:8887"), this);
            client.connect();
        } catch (URISyntaxException e) {
            appendMessage("System", "Error connecting to server: " + e.getMessage());
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && client != null && client.isOpen()) {
            client.send("MESSAGE:" + username + ":" + message);
            messageField.setText("");
        }
    }

    public void appendMessage(String sender, String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                var doc = chatArea.getStyledDocument();
                String timestamp = LocalDateTime.now().format(timeFormatter);
                // String formattedMessage = String.format("[%s] %s: %s\n", timestamp, sender, message);
                
                // Style for timestamp
                var timestampStyle = chatArea.addStyle("Timestamp", null);
                StyleConstants.setForeground(timestampStyle, Color.GRAY);
                StyleConstants.setFontSize(timestampStyle, defaultFont.getSize() - 2);
                
                // Style for sender name
                var senderStyle = chatArea.addStyle("Sender", null);
                StyleConstants.setForeground(senderStyle, primaryColor);
                StyleConstants.setBold(senderStyle, true);
                
                // Style for message
                var messageStyle = chatArea.addStyle("Message", null);
                StyleConstants.setForeground(messageStyle, Color.BLACK);
                
                // Insert formatted message with styles
                doc.insertString(doc.getLength(), "[" + timestamp + "] ", timestampStyle);
                doc.insertString(doc.getLength(), sender + ": ", senderStyle);
                doc.insertString(doc.getLength(), message + "\n", messageStyle);
                
                chatArea.setCaretPosition(doc.getLength());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void updateUserList(Set<String> users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : users) {
                userListModel.addElement(user);
            }
        });
    }

    // Updated ChatClient class to handle the enhanced features
    private class ChatClient extends WebSocketClient {
        private final EnhancedChatGUI gui;
        private final Set<String> users = new HashSet<>();

        public ChatClient(URI serverUri, EnhancedChatGUI gui) {
            super(serverUri);
            this.gui = gui;
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            gui.appendMessage("System", "Connected to server");
            send("JOIN:" + username);
        }

        @Override
        public void onMessage(String message) {
            String[] parts = message.split(":", 3);
            if (parts.length >= 2) {
                switch (parts[0]) {
                    case "MESSAGE":
                        gui.appendMessage(parts[1], parts[2]);
                        break;
                    case "JOIN":
                        users.add(parts[1]);
                        gui.updateUserList(users);
                        gui.appendMessage("System", parts[1] + " joined the chat");
                        break;
                    case "LEAVE":
                        users.remove(parts[1]);
                        gui.updateUserList(users);
                        gui.appendMessage("System", parts[1] + " left the chat");
                        break;
                    case "STATUS":
                        gui.appendMessage("System", parts[1] + " is now " + parts[2]);
                        break;
                }
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            gui.appendMessage("System", "Disconnected from server");
        }

        @Override
        public void onError(Exception ex) {
            gui.appendMessage("System", "Error: " + ex.getMessage());
        }
    }
}