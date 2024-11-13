package com.example;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

public class ChatClient extends WebSocketClient {
    private ChatGUI gui;

    public ChatClient(URI serverUri, ChatGUI gui) {
        super(serverUri);
        this.gui = gui;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        gui.appendMessage("Connected to server");
    }

    @Override
    public void onMessage(String message) {
        gui.appendMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        gui.appendMessage("Disconnected from server");
    }

    @Override
    public void onError(Exception ex) {
        gui.appendMessage("Error: " + ex.getMessage());
    }
}