package com.example.Capstone;

import org.jetbrains.annotations.NotNull;

public class PostItem {


    private Integer id;
    private String name;
    private String address;
    private String text;
    private String time;
    private String report;
    private String result;
    private String information;
    @NotNull
    public static final String BASE_URL = "http://10.0.2.2:8000/posts/";


    public Integer getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getText() {
        return text;
    }
    public String getTime() {
        return time;
    }
    public String getReport() {
        return report;
    }
    public String getAddress(){
        return address;
    }
    public String getResult() {
        return result;
    }
    public String getInformation() {
        return information;
    }



    public void setAddress(String s){
        address = s;
    }
    public void setName(String s){
        name = s;
    }
    public void setText(String s){
        text = s;
    }
    public void setReport(String s){
        report = s;
    }
    public void setTime(String s){
        time = s;
    }
    public void setInformation(String s){
        information = s;
    }
}