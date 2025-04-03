package com.petwatch.petwatch.DAO;
import com.petwatch.petwatch.Model.PetSitter;
import java.sql.*;

public class PetSitterDAO {

    private static final String URL = "jdbc:sqlite:petwatch.db";
    private Connection connection;
    Statement statement;

    /**
     * Establishes the database connection
     */
    public PetSitterDAO() {
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
     * Adds a PetSitter to the User table in the database
     *
     * @param petSitter
     * @return userId
     */
    public int addPetSitter(PetSitter petSitter) {

        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return -1;
        }

        String sql = "INSERT INTO pet_sitters (user_id, name, experience, availability, city, bio, phone) VALUES (" +
                petSitter.getUserId() + ", '" +
                petSitter.getName() + "', '" +
                petSitter.getExperience() + "', '" +
                petSitter.getAvailability() + "', '" +
                petSitter.getCity() + "', '" +
                petSitter.getBio() + "', '" +
                petSitter.getPhone() + "')";

        try {
            statement = connection.createStatement();
            int affectedRows = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

            // Retrieve the generated ID and assign it to the user object
            if (affectedRows > 0) {
                ResultSet rs = statement.getGeneratedKeys();

                if (rs.next()) {
                    petSitter.setPetSitterId(rs.getInt(1));  // Assign generated ID
                    System.out.println("Pet Sitter added successfully with ID: " + petSitter.getPetSitterId());
                }

            }
            statement.close();
        } catch (SQLException e) {
            System.err.println("Error adding pet sitter: " + e.getMessage());
            System.err.println("SQL: " + sql);
            e.printStackTrace();
        }

        return petSitter.getPetSitterId();

    }

    /**
     * Removes PetSitter from the database using Id
     *
     * @param petSitterId
     */
    public void removePetSitter(int petSitterId) {

        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return;
        }

        String sql = "DELETE FROM pet_sitters WHERE id = " + petSitterId;

        try {
            statement = connection.createStatement();
            int affectedRows = statement.executeUpdate(sql);
            if (affectedRows > 0) {
                System.out.println("Pet Sitter removed successfully.");
            } else {
                System.out.println("No Pet Sitters found with ID " + petSitterId);
            }
            statement.close();
        } catch (SQLException e) {
            //e.printStackTrace();
        }
    }

    /**
     * Searches the table in the database for a specific user matching the ID
     *
     * @param petSitterId
     * @return User
     */
    public PetSitter getPetSitterById(int petSitterId) {

        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return null;
        }


        String sql = "SELECT * FROM pet_sitters WHERE id = " + petSitterId;
        PetSitter petSitter = null;

        try {

            statement = connection.createStatement();

            ResultSet rs = statement.executeQuery(sql);


            if (rs.next()) {
                petSitter = new PetSitter(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("experience"),
                        rs.getString("availability"),
                        rs.getDouble("rating"),
                        rs.getString("city"),
                        rs.getString("bio"),
                        rs.getString("phone")
                );
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving PetSitter: " + e.getMessage());
        }

        return petSitter; // Returns null if not found
    }

    /**
     * Gets a PetSitter record by user ID
     *
     * @param userId The user ID to search for
     * @return PetSitter object or null if not found
     */
    public PetSitter getPetSitterByUserId(int userId) {
        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return null;
        }

        String sql = "SELECT * FROM pet_sitters WHERE user_id = " + userId;
        PetSitter petSitter = null;

        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            if (rs.next()) {
                petSitter = new PetSitter(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("experience"),
                        rs.getString("availability"),
                        rs.getDouble("rating"),
                        rs.getString("city"),
                        rs.getString("bio"),
                        rs.getString("phone")
                );
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving PetSitter by user ID: " + e.getMessage());
        }

        return petSitter;
    }

    public void updatePetSitter(PetSitter petSitter) {
        if (connection == null) {
            System.err.println("Error: Database connection is not available.");
            return;
        }

        String sql = "UPDATE pet_sitters SET bio=" + petSitter.getBio() + "WHERE user_id = " +  petSitter.getUserId();

        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            statement.close();
        } catch (SQLException e) {
            System.err.println("Error updating PetSitter by user ID: " + e.getMessage());
        }
        return;
    }

    public void updateName(int petSitterId, String newName) {
        String sql = "UPDATE pet_sitters SET name = '" + newName + "' WHERE id = " + petSitterId;
        try {
            int rowsUpdated = statement.executeUpdate(sql);
            if (rowsUpdated == 0) {
                throw new SQLException("Failed to update: No record found for ID = " + petSitterId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateExperience(int petSitterId, int experience) {
        String sql = "UPDATE pet_sitters SET experience = " + experience + " WHERE id = " + petSitterId;
        try {
            int rowsUpdated = statement.executeUpdate(sql);
            if (rowsUpdated == 0) {
                throw new SQLException("Failed to update: No record found for ID = " + petSitterId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateAvailability(int petSitterId, boolean availability) {
        String sql = "UPDATE pet_sitters SET availability = " + (availability ? 1 : 0) + " WHERE id = " + petSitterId;
        try {
            int rowsUpdated = statement.executeUpdate(sql);
            if (rowsUpdated == 0) {
                throw new SQLException("Failed to update: No record found for ID = " + petSitterId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateRating(int petSitterId, double rating) {
        String sql = "UPDATE pet_sitters SET rating = " + rating + " WHERE id = " + petSitterId;
        try {
            int rowsUpdated = statement.executeUpdate(sql);
            if (rowsUpdated == 0) {
                throw new SQLException("Failed to update: No record found for ID = " + petSitterId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCity(int petSitterId, String city) {
        String sql = "UPDATE pet_sitters SET city = '" + city + "' WHERE id = " + petSitterId;
        try {
            int rowsUpdated = statement.executeUpdate(sql);
            if (rowsUpdated == 0) {
                throw new SQLException("Failed to update: No record found for ID = " + petSitterId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateBio(int petSitterId, String bio) {
        String sql = "UPDATE pet_sitters SET bio = '" + bio + "' WHERE id = " + petSitterId;
        try {
            int rowsUpdated = statement.executeUpdate(sql);
            if (rowsUpdated == 0) {
                throw new SQLException("Failed to update: No record found for ID = " + petSitterId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePhone(int petSitterId, String phone) {
        String sql = "UPDATE pet_sitters SET phone = '" + phone + "' WHERE id = " + petSitterId;
        try {
            int rowsUpdated = statement.executeUpdate(sql);
            if (rowsUpdated == 0) {
                throw new SQLException("Failed to update: No record found for ID = " + petSitterId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
