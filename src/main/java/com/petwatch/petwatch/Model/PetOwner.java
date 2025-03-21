package com.petwatch.petwatch.Model;

public class PetOwner {
    private int petOwnerId;
    private int userId;
    private String name;
    private String phone;
    private String address;
    private String country;
    private String state;
    private String city;
    private String postalCode;

    public PetOwner(String address, String phone, String name, int userId) {
        this.address = address;
        this.phone = phone;
        this.name = name;
        this.userId = userId;
        this.country = "";
        this.state = "";
        this.city = "";
        this.postalCode = "";
    }

    public PetOwner(String address, String phone, String name, int userId, 
                    String country, String state, String city, String postalCode) {
        this.address = address;
        this.phone = phone;
        this.name = name;
        this.userId = userId;
        this.country = country;
        this.state = state;
        this.city = city;
        this.postalCode = postalCode;
    }

    public PetOwner(int id, String address, String phone, String name, int userId) {
        this.petOwnerId = id;
        this.address = address;
        this.phone = phone;
        this.name = name;
        this.userId = userId;
        this.country = "";
        this.state = "";
        this.city = "";
        this.postalCode = "";
    }
    
    public PetOwner(int id, String address, String phone, String name, int userId,
                    String country, String state, String city, String postalCode) {
        this.petOwnerId = id;
        this.address = address;
        this.phone = phone;
        this.name = name;
        this.userId = userId;
        this.country = country;
        this.state = state;
        this.city = city;
        this.postalCode = postalCode;
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
    
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
