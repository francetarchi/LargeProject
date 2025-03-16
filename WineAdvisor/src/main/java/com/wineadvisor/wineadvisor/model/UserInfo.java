package com.wineadvisor.wineadvisor.model;

public class UserInfo {
    private String username;
    private String thumbnail;

    public UserInfo() {}
    
    public UserInfo(String username, String thumbnail) {
        this.username = username;
        this.thumbnail = thumbnail;
    }

    public String getUsername() {
        return username;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
