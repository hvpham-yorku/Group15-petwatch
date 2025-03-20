package com.petwatch.petwatch.DAO;
import com.petwatch.petwatch.Model.*;
import com.petwatch.petwatch.DAO.PetOwnerDAO;
import com.petwatch.petwatch.DAO.PetSitterDAO;

import java.sql.*;

public class BookingDAO {

    private static final String URL = "jdbc:sqlite:petwatch.db";
    private Connection connection;
    Statement statement;

    //Establishes connection to the SQLite database
    public BookingDAO() {
        try {
            connection = DriverManager.getConnection(URL);
            statement = connection.createStatement();
            statement.execute("PRAGMA foreign_keys = ON;");
            System.out.println("Database connected successfully.");
        } catch (SQLException e) {

        }
    }

    /**
     * Adds a Booking to the Booking table in the database
     *
     * @param booking
     * @return bookingId
     */
    public int addBooking(Booking booking) {

        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return -1;
        }

        String sql = "INSERT INTO bookings (owner_id, sitter_id, status) VALUES ('" +
                booking.getPetOwner().getPetOwnerId() + "', '" + booking.getPetSitter().getPetSitterId() + "', '" + booking.getStatus() + "')";

        try {
            statement = connection.createStatement();
            int affectedRows = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

            // Retrieve the generated ID and assign it to the booking object
            if (affectedRows > 0) {
                ResultSet rs = statement.getGeneratedKeys();

                if (rs.next()) {
                    booking.setBookingId(rs.getInt(1));  // Assign generated ID
                    System.out.println("Booking added successfully with ID: " + booking.getBookingId());
                }
            }
            statement.close();
        } catch (SQLException e) {
            // No error logging (silent failure)
        }

        return booking.getBookingId();

    }

    /**
     * Removes Booking from the database using Id
     *
     * @param bookingId
     */
    public void removeBooking(int bookingId) {

        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return;
        }

        String sql = "DELETE FROM bookings WHERE id = " + bookingId;

        try {
            statement = connection.createStatement();
            int affectedRows = statement.executeUpdate(sql);
            if (affectedRows > 0) {
                System.out.println("Booking removed successfully.");
            } else {
                System.out.println("No Booking found with ID " + bookingId);
            }
            statement.close();
        } catch (SQLException e) {

        }
    }

    /**
     * Searches the table in the database for a specific booking matching the ID
     *
     * @param bookingId
     * @return Booking
     */
    public Booking getBookingById(int bookingId) {

        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return null;
        }

        String sql = "SELECT * FROM users WHERE id = " + bookingId;
        Booking booking = null;

        try {

            statement = connection.createStatement();

            ResultSet rs = statement.executeQuery(sql);

            int ownerId = rs.getInt("owner_id");
            int sitterId = rs.getInt("sitter_id");

            PetOwner petOwner = new PetOwnerDAO().getPetOwnerById(ownerId);
            PetSitter petSitter = new PetSitterDAO().getPetSitterById(sitterId);


            if (rs.next()) {

                booking = new Booking(petOwner, petSitter, Booking.Status.valueOf(rs.getString("status")));

            }

            statement.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving User: " + e.getMessage());
        }

        return booking; // Returns null if not found
    }

    // closes the connection when we are done with it
    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {

        }
    }

}
