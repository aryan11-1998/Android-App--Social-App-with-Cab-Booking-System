package com.example.project;

public class CommentModel {
    public String comment, date,time,fullname;

    public CommentModel(){

    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public CommentModel(String comment, String date, String time, String fullname) {
        this.comment = comment;
        this.date = date;
        this.time = time;
        this.fullname = fullname;
    }
}
