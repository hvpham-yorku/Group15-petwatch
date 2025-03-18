package com.petwatch.petwatch.Model;

public class Pet {
    private int petId;
    private int petOwnerId;
    private String petName;
    private int petAge;
    private PetType type;


    public enum PetType {
        DOG, CAT, BIRD, OTHER
    }

    public Pet(String petName, int petAge, PetType petType, int petOwnerId) {
        this.petName = petName;
        this.petAge = petAge;
        this.type = type;
        this.petOwnerId = petOwnerId;
    }

    public int getPetId() {

        return petId;
    }

    public void setPetId(int anInt) {
    }


    public String getPetName() {

        return petName;
    }

    public void setPetName(String petName) {

        this.petName = petName;
    }

    public int getPetAge() {

        return petAge;
    }

    public void setPetAge(int petAge) {

        this.petAge = petAge;
    }

    public PetType getType() {

        return type;
    }

    public void setType(PetType type) {

        this.type = type;
    }

    public int getPetOwnerId() {

        return petOwnerId;
    }


}
