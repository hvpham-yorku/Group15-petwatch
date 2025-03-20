package com.petwatch.petwatch.DAO;
import com.petwatch.petwatch.Model.PetOwner;
import java.sql.*;

public class PetOwnerDAO {

    private static final String URL = "jdbc:sqlite:petwatch.db";
    private Connection connection;
    Statement statement;

    //Establishes connection to the SQLite database
    public PetOwnerDAO() {
        try {
            connection = DriverManager.getConnection(URL);
            statement = connection.createStatement();
            statement.execute("PRAGMA foreign_keys = ON;");

            System.out.println("Database connected successfully.");
        } catch (SQLException e) {
            //e.printStackTrace();
        }
    }

    /**
     * Adds a PetOwner to the User table in the database
     *
     * @param petOwner
     * @return userId
     */
    public int addPetOwner(PetOwner petOwner) {

        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return -1;
        }

        String sql = "INSERT INTO pet_owners (user_id, name, phone, address) VALUES (" +
                petOwner.getUserId() + ", '" + petOwner.getName() + "', '" + petOwner.getPhone() + "', '" + petOwner.getAddress() + "')";
        try {
            statement = connection.createStatement();
            int affectedRows = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

            // Retrieve the generated ID and assign it to the user object
            if (affectedRows > 0) {
                ResultSet rs = statement.getGeneratedKeys();

                if (rs.next()) {
                    petOwner.setPetOwnerId(rs.getInt(1));  // Assign generated ID
                    System.out.println("Pet Owner added successfully with ID: " + petOwner.getPetOwnerId());
                }

            }
            statement.close();
        } catch (SQLException e) {
            //e.printStackTrace();
        }

        return petOwner.getPetOwnerId();

    }

    /**
     * Removes PetSitter from the database using Id
     *
     * @param petOwnerId
     */
    public void removePetOwner(int petOwnerId) {

        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return;
        }

        String sql = "DELETE FROM pet_owners WHERE id = " + petOwnerId;

        try {
            statement = connection.createStatement();
            int affectedRows = statement.executeUpdate(sql);
            if (affectedRows > 0) {
                System.out.println("Pet Owner removed successfully.");
            } else {
                System.out.println("No Pet Owner found with ID " + petOwnerId);
            }
            statement.close();
        } catch (SQLException e) {
            //e.printStackTrace();
        }
    }

    /**
     * Removes PetOwner from the database using Id
     *
     * @param petOwnerId
     */
    public PetOwner getPetOwnerById(int petOwnerId) {

        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return null;
        }


        String sql = "SELECT * FROM pet_owners WHERE id = " + petOwnerId;
        PetOwner petOwner = null;

        try {

            statement = connection.createStatement();

            ResultSet rs = statement.executeQuery(sql);


            if (rs.next()) {
                petOwner = new PetOwner(
                        rs.getString("address"),
                        rs.getString("phone"),
                        rs.getString("name"),
                        rs.getInt("user_id") // Linking to User
                );
                petOwner.setPetOwnerId(rs.getInt("id")); // Set the actual petOwner ID
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving PetOwner: " + e.getMessage());
        }

        return petOwner; // Returns null if not found
    }



    // closes the connection when we are done with it
    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            //e.printStackTrace();
        }
    }




}
