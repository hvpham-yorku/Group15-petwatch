package com.petwatch.petwatch.DAO;

import com.petwatch.petwatch.Model.Booking;
import com.petwatch.petwatch.Model.Pet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class BookingPetsDAO {

    private static final String URL = "jdbc:sqlite:petwatch.db";
    private Connection connection;
    Statement statement;

    //Establishes connection to the SQLite database
    public BookingPetsDAO() {
        try {
            connection = DriverManager.getConnection(URL);
            statement = connection.createStatement();
            statement.execute("PRAGMA foreign_keys = ON;");
            System.out.println("Database connected successfully.");
        } catch (SQLException e) {

        }
    }

    public void addBookingPets(Booking booking, Pet pet) {

        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return;
        }

        String sql = "INSERT INTO bookings_pets (booking_id, pet_id) VALUES ('" +
                booking.getBookingId() + "', '" + pet.getPetId() + "')";

        try {
            statement = connection.createStatement();
            statement.executeUpdate(sql);

            statement.close();
        } catch (SQLException e) {
            // No error logging (silent failure)
        }

    }

    public void removeBookingPets(int bookingId, int petId) {

        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return;
        }

        String sql = "DELETE FROM bookings_pets WHERE booking_id = " + bookingId + " AND pet_id = " + petId;

        try {
            statement = connection.createStatement();
            int affectedRows = statement.executeUpdate(sql);
            if (affectedRows > 0) {
                System.out.println("Booking and Pet connection removed successfully.");
            } else {
                System.out.println("No Booking and Pet connection found with ID " + bookingId + " and pet ID " + petId);
            }
            statement.close();
        } catch (SQLException e) {

        }
    }

    public Booking getBookingById(int bookingId) {

        Booking booking = null;
        BookingDAO bookingDAO = new BookingDAO();


        booking = bookingDAO.getBookingById(bookingId);

        bookingDAO.closeConnection();

        return booking;

    }

    public Pet getPetById(int petId) {

        Pet pet = null;
        PetDAO petDAO = new PetDAO();

        pet = petDAO.getPetById(petId);

        petDAO.closeConnection();

        return pet;

    }


}
