package com.example.chatapp.model;

public class EncryptedMessage {

    private String sender;
    private String receiver;
    private String encryptionType;
    private String encryptedMessage;

    EncryptedMessage() {}

    public EncryptedMessage(String sender, String receiver, String encryptionType, String encryptedMessage) {
        this.sender = sender;
        this.receiver = receiver;
        this.encryptionType = encryptionType;
        this.encryptedMessage = encryptedMessage;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(String encryptionType) {
        this.encryptionType = encryptionType;
    }

    public String getEncryptedMessage() {
        return encryptedMessage;
    }

    public void setEncryptedMessage(String encryptedMessage) {
        this.encryptedMessage = encryptedMessage;
    }
}
