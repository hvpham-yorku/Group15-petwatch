package com.petwatch.petwatch.Model;

public class PetSitter {
    private int petSitterId;
    private int userId;
    private String name;
    private String experience;
    private String availability;
    private double rating;
    private String city;
    private String bio;
    private String phone;

    public PetSitter(int userId, String name, String experience, String availability) {
        this.userId = userId;
        this.name = name;
        this.experience = experience;
        this.availability = availability;
        this.rating = 0;
        this.city = "";
        this.bio = "";
        this.phone = "";
    }

    public PetSitter(int userId, String name, String experience, String availability,
                    String city, String bio, String phone) {
        this.userId = userId;
        this.name = name;
        this.experience = experience;
        this.availability = availability;
        this.rating = 0;
        this.city = city;
        this.bio = bio;
        this.phone = phone;
    }

    // ✅ Constructor for retrieving from the database (ID is provided)
    public PetSitter(int id, int userId, String name, String experience, String availability, double rating) {
        this.petSitterId = id;
        this.userId = userId;
        this.name = name;
        this.experience = experience;
        this.availability = availability;
        this.rating = rating;
        this.city = "";
        this.bio = "";
        this.phone = "";
    }
    
    // Constructor for retrieving from the database with all fields
    public PetSitter(int id, int userId, String name, String experience, String availability, double rating,
                     String city, String bio, String phone) {
        this.petSitterId = id;
        this.userId = userId;
        this.name = name;
        this.experience = experience;
        this.availability = availability;
        this.rating = rating;
        this.city = city;
        this.bio = bio;
        this.phone = phone;
    }

    public int getPetSitterId() {
        return petSitterId;
    }

    public void setPetSitterId(int petSitterId) {
        this.petSitterId = petSitterId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
    
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
