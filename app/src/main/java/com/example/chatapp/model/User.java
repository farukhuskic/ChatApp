package com.example.chatapp.model;

public class User {

    private String id;
    private String username;
    private String imageUrl;

    public User(String id, String username, String imageURL) {
        this.id = id;
        this.username = username;
        this.imageUrl = imageURL;
    }

    public User() { }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageUrl;
    }

    public void setImageURL(String imageURL) {
        this.imageUrl = imageURL;
    }
}
