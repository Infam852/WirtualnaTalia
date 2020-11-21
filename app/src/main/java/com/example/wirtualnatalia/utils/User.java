package com.example.wirtualnatalia.utils;

public class User {
    private String nickname;
    public String getNickname() {return nickname;}
    public void setNickname(String data) {this.nickname = data;}

    private static final User holder = new User();
    public static User getInstance() {return holder;}
}
