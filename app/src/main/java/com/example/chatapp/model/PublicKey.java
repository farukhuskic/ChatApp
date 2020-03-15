package com.example.chatapp.model;

public class PublicKey {

    private String publicKey;
    private String userId;
    private String n;

    public PublicKey(String publicKey, String userId, String n) {
        this.publicKey = publicKey;
        this.userId = userId;
        this.n = n;
    }

    public PublicKey() {
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }
}
