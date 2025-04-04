package com.petwatch.petwatch.Model;

import java.util.Date;

public class Job {
    private int id;
    private int petOwnerId;
    private Integer petSitterId; // Can be null if not assigned
    private String petType;
    private String priority;
    private Date startDate;
    private Date endDate;
    private String description;
    private String notes;
    private double payRate;
    private String paymentMethod;
    private String status;

    // Default constructor
    public Job() {
    }

    // Constructor without ID (for creating new jobs)
    public Job(int petOwnerId, String petType, String priority, Date startDate, Date endDate, 
               String description, String notes, double payRate, String paymentMethod) {
        this.petOwnerId = petOwnerId;
        this.petSitterId = null; // Initially no sitter is assigned
        this.petType = petType;
        this.priority = priority;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.notes = notes;
        this.payRate = payRate;
        this.paymentMethod = paymentMethod;
        this.status = "Open"; // Default status for new jobs
    }

    // Full constructor
    public Job(int id, int petOwnerId, Integer petSitterId, String petType, String priority, Date startDate, Date endDate,
               String description, String notes, double payRate, String paymentMethod, String status) {
        this.id = id;
        this.petOwnerId = petOwnerId;
        this.petSitterId = petSitterId;
        this.petType = petType;
        this.priority = priority;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.notes = notes;
        this.payRate = payRate;
        this.paymentMethod = paymentMethod;
        this.status = status;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPetOwnerId() {
        return petOwnerId;
    }

    public void setPetOwnerId(int petOwnerId) {
        this.petOwnerId = petOwnerId;
    }
    
    public Integer getPetSitterId() {
        return petSitterId;
    }

    public void setPetSitterId(Integer petSitterId) {
        this.petSitterId = petSitterId;
    }

    public String getPetType() {
        return petType;
    }

    public void setPetType(String petType) {
        this.petType = petType;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public double getPayRate() {
        return payRate;
    }

    public void setPayRate(double payRate) {
        this.payRate = payRate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 