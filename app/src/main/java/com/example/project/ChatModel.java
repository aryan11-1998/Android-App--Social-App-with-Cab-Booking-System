package com.example.project;

public class ChatModel {
    public String date,time,type,message,from;
    public ChatModel(){

    }

    public ChatModel(String date, String time, String type, String message, String from) {
        this.date = date;
        this.time = time;
        this.type = type;
        this.message = message;
        this.from = from;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
