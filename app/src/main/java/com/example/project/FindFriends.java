package com.example.project;

public class FindFriends {
    public String profileimage;
    public String fullname;
    public String about;

    public FindFriends(String profileimage, String fullname, String about) {
        this.profileimage = profileimage;
        this.fullname = fullname;
        this.about = about;
    }

    public FindFriends(){

    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}
