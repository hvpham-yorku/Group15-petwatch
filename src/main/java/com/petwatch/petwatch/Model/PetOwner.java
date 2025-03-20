package com.petwatch.petwatch.Model;

public class PetOwner {
    private int petOwnerId;
    private int userId;
    private String name;
    private String phone;
    private String address;

    public PetOwner(String address, String phone, String name, int userId) {
        this.address = address;
        this.phone = phone;
        this.name = name;
        this.userId = userId;
    }

    public PetOwner(int id, String address, String phone, String name, int userId) {
        this.petOwnerId = id;
        this.address = address;
        this.phone = phone;
        this.name = name;
        this.userId = userId;
    }

    public int getPetOwnerId() {
        return petOwnerId;
    }

    public void setPetOwnerId(int petOwnerId) {
        this.petOwnerId = petOwnerId;
    }

    public int getUserId() {
        return userId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
