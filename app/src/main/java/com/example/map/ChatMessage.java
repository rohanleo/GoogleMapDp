package com.example.map;

public class ChatMessage {
    private String sender;
    private String message;
    public ChatMessage(){}

    public ChatMessage(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String name) {
        this.sender = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}