package com.petwatch.petwatch.DAO;
import com.petwatch.petwatch.Model.Pet;
import com.petwatch.petwatch.Model.User;

import java.sql.*;

public class PetDAO {

    private static final String URL = "jdbc:sqlite:petwatch.db";
    private Connection connection;
    Statement statement;

    /**
     * Establishes the database connection
     */
    public PetDAO() {
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
     * Adds a Pet to the Pets table in the database
     *
     * @param pet
     * @return petId
     */
    public int addPet(Pet pet) {

        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return -1;
        }

        String sql = "INSERT INTO pets (owner_id, name, type, age) VALUES (" +
                pet.getPetOwnerId() + ", '" + pet.getPetName() + "', '" + pet.getType().name() + "', " + pet.getPetAge() + ")";

        int petId = -1;

        try {
            statement = connection.createStatement();
            int affectedRows = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

            if (affectedRows > 0) {
                ResultSet rs = statement.getGeneratedKeys();
                if (rs.next()) {
                    petId = rs.getInt(1);
                    pet.setPetId(petId);  // Assign generated ID
                    System.out.println("Pet added successfully with ID: " + pet.getPetId());

                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding pet: " + e.getMessage());
            System.err.println("SQL: " + sql);
            e.printStackTrace();
        }

        return petId;

    }

    /**
     * Removes Pet from the database using Id
     *
     * @param petId
     */
    public void removePet(int petId) {

        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return;
        }

        String sql = "DELETE FROM pets WHERE id = " + petId;

        try {
            statement = connection.createStatement();
            int affectedRows = statement.executeUpdate(sql);
            if (affectedRows > 0) {
                System.out.println("Pet removed successfully.");
            } else {
                System.out.println("No Pets found with ID " + petId);
            }
            statement.close();
        } catch (SQLException e) {
            //e.printStackTrace();
        }
    }

    /**
     * Searches the table in the database for a specific user matching the ID
     *
     * @param petId
     * @return Pet
     */
    public Pet getPetById(int petId) {

        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return null;
        }

        String sql = "SELECT * FROM pets WHERE id = " + petId;
        Pet pet = null;

        try {

            statement = connection.createStatement();

            ResultSet rs = statement.executeQuery(sql);


            if (rs.next()) {
                pet = new Pet(
                        rs.getString("name"),
                        rs.getInt("age"),
                        Pet.PetType.valueOf(rs.getString("type")),
                        rs.getInt("owner_id")
                );
                pet.setPetId(rs.getInt("id")); // Set the actual pet ID
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving Pet: " + e.getMessage());
        }

        return pet; // Returns null if not found
    }

    /**
     * Retrieves all pets belonging to a specific owner
     *
     * @param ownerId
     * @return List<Pet>
     */
    public java.util.List<Pet> getPetsByOwnerId(int ownerId) {
        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return java.util.Collections.emptyList();
        }

        String sql = "SELECT * FROM pets WHERE owner_id = " + ownerId;
        java.util.List<Pet> pets = new java.util.ArrayList<>();

        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                Pet pet = new Pet(
                    rs.getString("name"),
                    rs.getInt("age"),
                    Pet.PetType.valueOf(rs.getString("type")),
                    rs.getInt("owner_id")
                );
                pet.setPetId(rs.getInt("id")); // Set the actual pet ID
                pets.add(pet);
            }

            statement.close();
            System.out.println("Found " + pets.size() + " pets for owner ID: " + ownerId);
        } catch (SQLException e) {
            System.err.println("Error retrieving pets for owner: " + e.getMessage());
        }

        return pets;
    }

    /**
     * Updates an existing Pet in the database
     *
     * @param pet
     * @return boolean indicating success
     */
    public boolean updatePet(Pet pet) {
        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return false;
        }

        String sql = "UPDATE pets SET " +
                "name = '" + pet.getPetName() + "', " +
                "age = " + pet.getPetAge() + ", " +
                "type = '" + pet.getType().name() + "', " +
                "owner_id = " + pet.getPetOwnerId() + " " +
                "WHERE id = " + pet.getPetId();

        try {
            statement = connection.createStatement();
            int affectedRows = statement.executeUpdate(sql);
            
            boolean success = affectedRows > 0;
            if (success) {
                System.out.println("Pet updated successfully with ID: " + pet.getPetId());
            } else {
                System.out.println("No Pet found with ID " + pet.getPetId());
            }
            
            statement.close();
            return success;
        } catch (SQLException e) {
            System.err.println("Error updating Pet: " + e.getMessage());
            return false;
        }
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