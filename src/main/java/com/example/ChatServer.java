package com.example;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.util.*;

public class ChatServer extends WebSocketServer {
    private Map<WebSocket, String> users;
    private Map<String, String> userStatuses;

    public ChatServer(int port) {
        super(new InetSocketAddress(port));
        users = new HashMap<>();
        userStatuses = new HashMap<>();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        // Connection opened but waiting for JOIN message to associate username
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String username = users.get(conn);
        if (username != null) {
            // Remove user from all mappings
            users.remove(conn);
            userStatuses.remove(username);
            
            // Broadcast leave message
            broadcast("LEAVE:" + username);
            
            // Send updated user list
            broadcastUserList();
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        String[] parts = message.split(":", 3);
        if (parts.length >= 2) {
            switch (parts[0]) {
                case "JOIN":
                    handleJoin(conn, parts[1]);
                    break;
                case "MESSAGE":
                    handleMessage(parts[1], parts[2]);
                    break;
                case "STATUS":
                    handleStatus(parts[1], parts[2]);
                    break;
            }
        }
    }

    private void handleJoin(WebSocket conn, String username) {
        // Store user connection
        users.put(conn, username);
        userStatuses.put(username, "Online");
        
        // Broadcast join message
        broadcast("JOIN:" + username);
        
        // Send current user list to new user
        String userListMessage = "USERLIST:" + String.join(",", users.values());
        conn.send(userListMessage);
        
        // Send welcome message
        broadcast("MESSAGE:System:Welcome " + username + " to the chat!");
        
        // Broadcast updated user list
        broadcastUserList();
    }

    private void handleMessage(String username, String message) {
        broadcast("MESSAGE:" + username + ":" + message);
    }

    private void handleStatus(String username, String status) {
        userStatuses.put(username, status);
        broadcast("STATUS:" + username + ":" + status);
    }

    private void broadcastUserList() {
        StringBuilder userList = new StringBuilder();
        for (String username : users.values()) {
            String status = userStatuses.getOrDefault(username, "Online");
            userList.append(username).append("(").append(status).append("),");
        }
        if (userList.length() > 0) {
            userList.setLength(userList.length() - 1); // Remove last comma
        }
        broadcast("USERLIST:" + userList.toString());
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Error occurred on connection " + conn + ": " + ex);
        if (conn != null) {
            users.remove(conn);
            broadcastUserList();
        }
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started on port " + getPort());
    }

    // Helper method to broadcast to all connected users
    @Override
    public void broadcast(String message) {
        System.out.println("Broadcasting: " + message); // Helpful for debugging
        super.broadcast(message);
    }
}