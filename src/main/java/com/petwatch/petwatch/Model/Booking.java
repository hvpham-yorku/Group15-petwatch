package com.petwatch.petwatch.Model;

import java.util.ArrayList;
import java.util.List;

public class Booking {
    private int bookingId;
    private PetOwner petOwner;
    private PetSitter petSitter;
    private List<Pet> pets = new ArrayList<Pet>();


    public Booking(PetOwner petOwner, PetSitter petSitter, List<Pet> pets) {
        this.petOwner = petOwner;
        this.petSitter = petSitter;
        this.pets = pets;
    }


    public int getBookingId() {

        return bookingId;
    }

    public PetOwner getPetOwner() {

        return petOwner;
    }

    public void setPetOwner(PetOwner petOwner) {

        this.petOwner = petOwner;
    }

    public PetSitter getPetSitter() {

        return petSitter;
    }

    public void setPetSitter(PetSitter petSitter) {

        this.petSitter = petSitter;
    }

    public List<Pet> getPets() {

        return pets;
    }

    public void setPets(List<Pet> pets) {

        this.pets = pets;
    }
}
