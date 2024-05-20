package com.example.Capstone;

public class ListItem {
    private String name;
    private String address;
    private String progress;
    private String text;
    private String time;
    public String getName() {
        return name;
    }
    public String setName(String name) {
        this.name = name;
        return name;
    }
    public String getAddress() {
        return address;
    }
    public String setAddress(String address) {
        this.address = address;
        return address;
    }

    public String getProgress() {
        return progress;
    }
    public String setProgress(String progress) {
        this.progress = progress;
        return progress;
    }

    public String getText() {
        return text;
    }

    public String getTime() {
        return time;
    }


    /*public String setText(String text) {
        this.text = text;
        return text;
    }*/


    ListItem(String name, String address, String progress, String text, String time) {
        this.name = name;
        this.address = address;
        this.progress = progress;
        this.text = text;
        this.time = time;
    }
}
