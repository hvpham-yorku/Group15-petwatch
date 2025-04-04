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

        String sql = "INSERT INTO pet_owners (user_id, name, phone, address, country, state, city, postal_code) VALUES (" +
                petOwner.getUserId() + ", '" +
                petOwner.getName() + "', '" +
                petOwner.getPhone() + "', '" +
                petOwner.getAddress() + "', '" +
                petOwner.getCountry() + "', '" +
                petOwner.getState() + "', '" +
                petOwner.getCity() + "', '" +
                petOwner.getPostalCode() + "')";
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
            System.err.println("Error adding pet owner: " + e.getMessage());
            System.err.println("SQL: " + sql);
            e.printStackTrace();
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
                        rs.getInt("id"),
                        rs.getString("address"),
                        rs.getString("phone"),
                        rs.getString("name"),
                        rs.getInt("user_id"),
                        rs.getString("country"),
                        rs.getString("state"),
                        rs.getString("city"),
                        rs.getString("postal_code")
                );
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving PetOwner: " + e.getMessage());
        }

        return petOwner; // Returns null if not found
    }

    /**
     * Gets a PetOwner record by user ID
     *
     * @param userId The user ID to search for
     * @return PetOwner object or null if not found
     */
    public PetOwner getPetOwnerByUserId(int userId) {
        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return null;
        }

        String sql = "SELECT * FROM pet_owners WHERE user_id = " + userId;
        PetOwner petOwner = null;

        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            if (rs.next()) {
                petOwner = new PetOwner(
                        rs.getInt("id"),
                        rs.getString("address"),
                        rs.getString("phone"),
                        rs.getString("name"),
                        rs.getInt("user_id"),
                        rs.getString("country"),
                        rs.getString("state"),
                        rs.getString("city"),
                        rs.getString("postal_code")
                );
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving PetOwner by user ID: " + e.getMessage());
        }

        return petOwner;
    }

    public void updateAddress(int petOwnerId, String newAddress) {
        String sql = "UPDATE pet_owners SET address = '" + newAddress + "' WHERE id = " + petOwnerId;
        try {
            int rowsUpdated = statement.executeUpdate(sql);
            if (rowsUpdated == 0) {
                throw new SQLException("Failed to update: No record found for ID = " + petOwnerId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePhone(int petOwnerId, String newPhone) {
        String sql = "UPDATE pet_owners SET phone = '" + newPhone + "' WHERE id = " + petOwnerId;
        try {
            int rowsUpdated = statement.executeUpdate(sql);
            if (rowsUpdated == 0) {
                throw new SQLException("Failed to update: No record found for ID = " + petOwnerId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateName(int petOwnerId, String newName) {
        String sql = "UPDATE pet_owners SET name = '" + newName + "' WHERE id = " + petOwnerId;
        try {
            int rowsUpdated = statement.executeUpdate(sql);
            if (rowsUpdated == 0) {
                throw new SQLException("Failed to update: No record found for ID = " + petOwnerId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCity(int petOwnerId, String newCity) {
        String sql = "UPDATE pet_owners SET city = '" + newCity + "' WHERE id = " + petOwnerId;
        try {
            int rowsUpdated = statement.executeUpdate(sql);
            if (rowsUpdated == 0) {
                throw new SQLException("Failed to update: No record found for ID = " + petOwnerId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateState(int petOwnerId, String newState) {
        String sql = "UPDATE pet_owners SET state = '" + newState + "' WHERE id = " + petOwnerId;
        try {
            int rowsUpdated = statement.executeUpdate(sql);
            if (rowsUpdated == 0) {
                throw new SQLException("Failed to update: No record found for ID = " + petOwnerId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateZipCode(int petOwnerId, String newZipCode) {
        String sql = "UPDATE pet_owners SET zipCode = '" + newZipCode + "' WHERE id = " + petOwnerId;
        try {
            int rowsUpdated = statement.executeUpdate(sql);
            if (rowsUpdated == 0) {
                throw new SQLException("Failed to update: No record found for ID = " + petOwnerId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCountry(int petOwnerId, String newCountry) {
        String sql = "UPDATE pet_owners SET country = '" + newCountry + "' WHERE id = " + petOwnerId;
        try {
            int rowsUpdated = statement.executeUpdate(sql);
            if (rowsUpdated == 0) {
                throw new SQLException("Failed to update: No record found for ID = " + petOwnerId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates a pet owner's details in the database
     * 
     * @param petOwner The updated pet owner object
     * @return true if update was successful, false otherwise
     */
    public boolean updatePetOwner(PetOwner petOwner) {
        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return false;
        }
        
        String sql = "UPDATE pet_owners SET " +
                    "name = '" + petOwner.getName() + "', " +
                    "phone = '" + petOwner.getPhone() + "', " +
                    "address = '" + petOwner.getAddress() + "', " +
                    "country = '" + petOwner.getCountry() + "', " +
                    "state = '" + petOwner.getState() + "', " +
                    "city = '" + petOwner.getCity() + "', " +
                    "postal_code = '" + petOwner.getPostalCode() + "' " +
                    "WHERE id = " + petOwner.getPetOwnerId();
        
        try {
            statement = connection.createStatement();
            int rowsUpdated = statement.executeUpdate(sql);
            statement.close();
            
            if (rowsUpdated > 0) {
                System.out.println("Pet owner updated successfully with ID: " + petOwner.getPetOwnerId());
                return true;
            } else {
                System.out.println("No pet owner found with ID: " + petOwner.getPetOwnerId());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error updating pet owner: " + e.getMessage());
            System.err.println("SQL: " + sql);
            e.printStackTrace();
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
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }




}
