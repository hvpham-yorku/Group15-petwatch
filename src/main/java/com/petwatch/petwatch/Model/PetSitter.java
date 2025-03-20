package com.petwatch.petwatch.Model;

public class PetSitter {
    private int petSitterId;
    private int userId;
    private String name;
    private String experience;
    private String availability;
    private double rating;

    public PetSitter(int userId, String name, String experience, String availability) {
        this.userId = userId;
        this.name = name;
        this.experience = experience;
        this.availability = availability;
        this.rating = 0;
    }

    // ✅ Constructor for retrieving from the database (ID is provided)
    public PetSitter(int id, int userId, String name, String experience, String availability, double rating) {
        this.petSitterId = id;
        this.userId = userId;
        this.name = name;
        this.experience = experience;
        this.availability = availability;
        this.rating = rating;
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
}
