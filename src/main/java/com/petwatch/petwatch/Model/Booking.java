package com.petwatch.petwatch.Model;

import java.util.ArrayList;
import java.util.List;

public class Booking {
    private int bookingId;
    private PetOwner petOwner;
    private PetSitter petSitter;
    private Status status;

    public enum Status{
        PENDING, ACCEPTED, DECLINED
    }

    public Booking(PetOwner petOwner, PetSitter petSitter, Status status) {
        this.petOwner = petOwner;
        this.petSitter = petSitter;
        this.status = status;
    }


    public int getBookingId() {

        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
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

    public Status getStatus() {

        return status;
    }

    public void setStatus(Status status) {

        this.status = status;
    }

}
