package com.example.firebasestuff.Model;

import java.io.Serializable;

public class Location implements Serializable {
    private String name;
    private String address;
    private String photo;

    public Location(String name, String address, String photo) {
        this.name = name;
        this.address = address;
        this.photo = photo;
    }

    public Location() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoto() {
        return photo;
    }

    @Override
    public String toString() {
        return "--> " + name + " <--> " + address + "\n";
    }
}
