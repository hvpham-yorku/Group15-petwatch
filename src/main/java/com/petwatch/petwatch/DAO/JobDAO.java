package com.petwatch.petwatch.DAO;

import com.petwatch.petwatch.Model.Job;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JobDAO {
    private Connection conn;
    private final String url = "jdbc:sqlite:petwatch.db";
    private static final int MAX_RETRIES = 5;
    private static final String SQLITE_BUSY = "SQLITE_BUSY";
    private final Random random = new Random();
    
    // Constructor - we won't establish connection in constructor to prevent connection leaks
    public JobDAO() {
        createJobsTableIfNotExists();
        System.out.println("JobDAO initialized");
    }
    
    // Get a fresh database connection
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }
    
    // Create the jobs table if it doesn't exist
    private void createJobsTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS jobs (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "pet_owner_id INTEGER NOT NULL, " +
                     "pet_sitter_id INTEGER, " +
                     "pet_type TEXT NOT NULL, " +
                     "priority TEXT NOT NULL, " +
                     "start_date TEXT NOT NULL, " +
                     "end_date TEXT NOT NULL, " +
                     "description TEXT NOT NULL, " +
                     "notes TEXT, " +
                     "pay_rate REAL NOT NULL, " +
                     "payment_method TEXT NOT NULL, " +
                     "status TEXT NOT NULL, " +
                     "FOREIGN KEY (pet_owner_id) REFERENCES pet_owners(id))";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            
            // Check if pet_sitter_id column exists, add it if not
            try {
                stmt.execute("SELECT pet_sitter_id FROM jobs LIMIT 1");
            } catch (SQLException e) {
                // Column doesn't exist, add it
                if (e.getMessage().contains("no such column")) {
                    stmt.execute("ALTER TABLE jobs ADD COLUMN pet_sitter_id INTEGER");
                    System.out.println("Added pet_sitter_id column to jobs table");
                }
            }
            
            System.out.println("Jobs table check/creation completed");
        } catch (SQLException e) {
            System.out.println("Error creating jobs table: " + e.getMessage());
        }
    }
    
    // Add a new job to the database with retry mechanism
    public int addJob(Job job) {
        int retries = 0;
        int jobId = -1;
        
        while (retries < MAX_RETRIES && jobId == -1) {
            String sql = "INSERT INTO jobs (pet_owner_id, pet_sitter_id, pet_type, priority, start_date, end_date, " +
                         "description, notes, pay_rate, payment_method, status) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
                // Set pragma to reduce locking
                try (Statement pragmaStmt = conn.createStatement()) {
                    pragmaStmt.execute("PRAGMA busy_timeout = 5000;");  // 5 second timeout
                }
                
                pstmt.setInt(1, job.getPetOwnerId());
                if (job.getPetSitterId() != null) {
                    pstmt.setInt(2, job.getPetSitterId());
                } else {
                    pstmt.setNull(2, java.sql.Types.INTEGER);
                }
                pstmt.setString(3, job.getPetType());
                pstmt.setString(4, job.getPriority());
                pstmt.setString(5, new java.sql.Date(job.getStartDate().getTime()).toString());
                pstmt.setString(6, new java.sql.Date(job.getEndDate().getTime()).toString());
                pstmt.setString(7, job.getDescription());
                pstmt.setString(8, job.getNotes());
                pstmt.setDouble(9, job.getPayRate());
                pstmt.setString(10, job.getPaymentMethod());
                pstmt.setString(11, job.getStatus());
                
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            jobId = rs.getInt(1);
                            return jobId;  // Success! Return and exit the method
                        }
                    }
                }
            } catch (SQLException e) {
                // Check if this is a database lock error
                if (e.getMessage().contains(SQLITE_BUSY)) {
                    retries++;
                    System.out.println("Database locked, retry attempt " + retries + " of " + MAX_RETRIES);
                    
                    if (retries < MAX_RETRIES) {
                        // Exponential backoff with jitter
                        long backoffMs = (long) Math.min(1000 * Math.pow(2, retries), 8000) 
                                       + random.nextInt(1000);
                        try {
                            System.out.println("Waiting " + backoffMs + "ms before retry");
                            Thread.sleep(backoffMs);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;  // Exit the retry loop if interrupted
                        }
                    }
                } else {
                    // Different error, just log and return error
                    System.out.println("Error adding job: " + e.getMessage());
                    return -1;
                }
            }
        }
        
        if (retries == MAX_RETRIES) {
            System.out.println("Failed to add job after " + MAX_RETRIES + " attempts due to database locks");
        }
        
        return jobId;
    }
    
    // Get all jobs for a specific pet owner
    public List<Job> getJobsByPetOwnerId(int petOwnerId) {
        List<Job> jobs = new ArrayList<>();
        
        String sql = "SELECT * FROM jobs WHERE pet_owner_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, petOwnerId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Job job = new Job();
                    job.setId(rs.getInt("id"));
                    job.setPetOwnerId(rs.getInt("pet_owner_id"));
                    
                    // Handle petSitterId (might be NULL)
                    int petSitterId = rs.getInt("pet_sitter_id");
                    if (!rs.wasNull()) {
                        job.setPetSitterId(petSitterId);
                    }
                    
                    job.setPetType(rs.getString("pet_type"));
                    job.setPriority(rs.getString("priority"));
                    job.setStartDate(java.sql.Date.valueOf(rs.getString("start_date")));
                    job.setEndDate(java.sql.Date.valueOf(rs.getString("end_date")));
                    job.setDescription(rs.getString("description"));
                    job.setNotes(rs.getString("notes"));
                    job.setPayRate(rs.getDouble("pay_rate"));
                    job.setPaymentMethod(rs.getString("payment_method"));
                    job.setStatus(rs.getString("status"));
                    
                    jobs.add(job);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting jobs for pet owner: " + e.getMessage());
        }
        
        return jobs;
    }
    
    // Get all open jobs
    public List<Job> getOpenJobs() {
        List<Job> jobs = new ArrayList<>();
        int retries = 0;
        boolean success = false;
        
        System.out.println("JobDAO: Starting getOpenJobs method");
        
        while (retries < MAX_RETRIES && !success) {
            String sql = "SELECT * FROM jobs WHERE status = 'Open'";
            
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // Set pragma to reduce locking
                try (Statement pragmaStmt = conn.createStatement()) {
                    pragmaStmt.execute("PRAGMA busy_timeout = 5000;");  // 5 second timeout
                }
                
                System.out.println("JobDAO: Executing query for open jobs: " + sql);
                
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    int count = 0;
                    while (rs.next()) {
                        Job job = new Job();
                        job.setId(rs.getInt("id"));
                        job.setPetOwnerId(rs.getInt("pet_owner_id"));
                        
                        // Handle petSitterId (might be NULL)
                        int petSitterId = rs.getInt("pet_sitter_id");
                        if (!rs.wasNull()) {
                            job.setPetSitterId(petSitterId);
                        }
                        
                        job.setPetType(rs.getString("pet_type"));
                        job.setPriority(rs.getString("priority"));
                        job.setStartDate(java.sql.Date.valueOf(rs.getString("start_date")));
                        job.setEndDate(java.sql.Date.valueOf(rs.getString("end_date")));
                        job.setDescription(rs.getString("description"));
                        job.setNotes(rs.getString("notes"));
                        job.setPayRate(rs.getDouble("pay_rate"));
                        job.setPaymentMethod(rs.getString("payment_method"));
                        job.setStatus(rs.getString("status"));
                        
                        System.out.println("JobDAO: Found open job with ID: " + job.getId() + ", type: " + job.getPetType());
                        jobs.add(job);
                        count++;
                    }
                    System.out.println("JobDAO: Query returned " + count + " open jobs");
                }
                success = true;  // Successfully retrieved jobs
            } catch (SQLException e) {
                // Check if this is a database lock error
                if (e.getMessage().contains(SQLITE_BUSY)) {
                    retries++;
                    System.out.println("Database locked during getOpenJobs, retry attempt " + retries + " of " + MAX_RETRIES);
                    
                    if (retries < MAX_RETRIES) {
                        // Exponential backoff with jitter
                        long backoffMs = (long) Math.min(1000 * Math.pow(2, retries), 8000) 
                                       + random.nextInt(1000);
                        try {
                            System.out.println("Waiting " + backoffMs + "ms before retry");
                            Thread.sleep(backoffMs);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;  // Exit the retry loop if interrupted
                        }
                    }
                } else {
                    // Different error, just log and continue
                    System.out.println("Error getting open jobs: " + e.getMessage());
                    break;
                }
            }
        }
        
        if (retries == MAX_RETRIES) {
            System.out.println("Failed to get open jobs after " + MAX_RETRIES + " attempts due to database locks");
        }
        
        System.out.println("JobDAO: Returning " + jobs.size() + " open jobs");
        return jobs;
    }
    
    // Get a job by ID
    public Job getJobById(int id) {
        String sql = "SELECT * FROM jobs WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Job job = new Job();
                    job.setId(rs.getInt("id"));
                    job.setPetOwnerId(rs.getInt("pet_owner_id"));
                    
                    // Handle petSitterId (might be NULL)
                    int petSitterId = rs.getInt("pet_sitter_id");
                    if (!rs.wasNull()) {
                        job.setPetSitterId(petSitterId);
                    }
                    
                    job.setPetType(rs.getString("pet_type"));
                    job.setPriority(rs.getString("priority"));
                    job.setStartDate(java.sql.Date.valueOf(rs.getString("start_date")));
                    job.setEndDate(java.sql.Date.valueOf(rs.getString("end_date")));
                    job.setDescription(rs.getString("description"));
                    job.setNotes(rs.getString("notes"));
                    job.setPayRate(rs.getDouble("pay_rate"));
                    job.setPaymentMethod(rs.getString("payment_method"));
                    job.setStatus(rs.getString("status"));
                    
                    return job;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting job by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    // Update a job
    public boolean updateJob(Job job) {
        int retries = 0;
        boolean success = false;
        
        while (retries < MAX_RETRIES && !success) {
            String sql = "UPDATE jobs SET pet_type = ?, priority = ?, start_date = ?, end_date = ?, " +
                         "description = ?, notes = ?, pay_rate = ?, payment_method = ?, status = ?, pet_sitter_id = ? " +
                         "WHERE id = ?";
            
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                // Set pragma to reduce locking
                try (Statement pragmaStmt = conn.createStatement()) {
                    pragmaStmt.execute("PRAGMA busy_timeout = 5000;");  // 5 second timeout
                }
                
                pstmt.setString(1, job.getPetType());
                pstmt.setString(2, job.getPriority());
                pstmt.setString(3, new java.sql.Date(job.getStartDate().getTime()).toString());
                pstmt.setString(4, new java.sql.Date(job.getEndDate().getTime()).toString());
                pstmt.setString(5, job.getDescription());
                pstmt.setString(6, job.getNotes());
                pstmt.setDouble(7, job.getPayRate());
                pstmt.setString(8, job.getPaymentMethod());
                pstmt.setString(9, job.getStatus());
                
                // Handle null pet sitter ID
                if (job.getPetSitterId() != null) {
                    pstmt.setInt(10, job.getPetSitterId());
                } else {
                    pstmt.setNull(10, java.sql.Types.INTEGER);
                }
                
                pstmt.setInt(11, job.getId());
                
                int affectedRows = pstmt.executeUpdate();
                success = affectedRows > 0;
                if (success) {
                    return true;  // Success! Return and exit the method
                }
            } catch (SQLException e) {
                // Check if this is a database lock error
                if (e.getMessage().contains(SQLITE_BUSY)) {
                    retries++;
                    System.out.println("Database locked, retry attempt " + retries + " of " + MAX_RETRIES);
                    
                    if (retries < MAX_RETRIES) {
                        // Exponential backoff with jitter
                        long backoffMs = (long) Math.min(1000 * Math.pow(2, retries), 8000) 
                                       + random.nextInt(1000);
                        try {
                            System.out.println("Waiting " + backoffMs + "ms before retry");
                            Thread.sleep(backoffMs);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;  // Exit the retry loop if interrupted
                        }
                    }
                } else {
                    // Different error, just log and return error
                    System.out.println("Error updating job: " + e.getMessage());
                    return false;
                }
            }
        }
        
        if (retries == MAX_RETRIES) {
            System.out.println("Failed to update job after " + MAX_RETRIES + " attempts due to database locks");
        }
        
        return success;
    }
    
    // Accept a job as a pet sitter
    public boolean acceptJob(int jobId, int petSitterId) {
        Job job = getJobById(jobId);
        if (job == null) {
            System.out.println("Job not found with ID: " + jobId);
            return false;
        }
        
        // Check if job is already accepted or not open
        if (!"Open".equals(job.getStatus())) {
            System.out.println("Job is not available for acceptance. Current status: " + job.getStatus());
            return false;
        }
        
        // Update job with sitter ID and change status
        job.setPetSitterId(petSitterId);
        job.setStatus("Accepted");
        
        return updateJob(job);
    }
    
    // Decline a job as a pet sitter
    public boolean declineJob(int jobId, int petSitterId) {
        Job job = getJobById(jobId);
        if (job == null) {
            System.out.println("Job not found with ID: " + jobId);
            return false;
        }
        
        // Only decline if this job is assigned to this sitter
        Integer currentSitterId = job.getPetSitterId();
        if (currentSitterId != null && currentSitterId == petSitterId && "Accepted".equals(job.getStatus())) {
            // Set the job to cancelled status and remove sitter ID
            job.setPetSitterId(null);
            job.setStatus("Cancelled");
            return updateJob(job);
        } else if ("Open".equals(job.getStatus())) {
            // Job is already open, nothing to decline
            System.out.println("Job is already open, nothing to decline");
            return true;
        } else {
            System.out.println("Job cannot be declined by this sitter or is not in the right state");
            return false;
        }
    }
    
    // Get jobs by pet sitter ID
    public List<Job> getJobsByPetSitterId(int petSitterId) {
        List<Job> jobs = new ArrayList<>();
        
        String sql = "SELECT * FROM jobs WHERE pet_sitter_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, petSitterId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Job job = new Job();
                    job.setId(rs.getInt("id"));
                    job.setPetOwnerId(rs.getInt("pet_owner_id"));
                    job.setPetSitterId(petSitterId);
                    job.setPetType(rs.getString("pet_type"));
                    job.setPriority(rs.getString("priority"));
                    job.setStartDate(java.sql.Date.valueOf(rs.getString("start_date")));
                    job.setEndDate(java.sql.Date.valueOf(rs.getString("end_date")));
                    job.setDescription(rs.getString("description"));
                    job.setNotes(rs.getString("notes"));
                    job.setPayRate(rs.getDouble("pay_rate"));
                    job.setPaymentMethod(rs.getString("payment_method"));
                    job.setStatus(rs.getString("status"));
                    
                    jobs.add(job);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting jobs for pet sitter: " + e.getMessage());
        }
        
        return jobs;
    }
    
    // Delete a job
    public boolean deleteJob(int id) {
        int retries = 0;
        boolean success = false;
        
        while (retries < MAX_RETRIES && !success) {
            String sql = "DELETE FROM jobs WHERE id = ?";
            
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                // Set pragma to reduce locking
                try (Statement pragmaStmt = conn.createStatement()) {
                    pragmaStmt.execute("PRAGMA busy_timeout = 5000;");  // 5 second timeout
                }
                
                pstmt.setInt(1, id);
                
                int affectedRows = pstmt.executeUpdate();
                success = affectedRows > 0;
                if (success) {
                    return true;  // Success! Return and exit the method
                }
            } catch (SQLException e) {
                // Check if this is a database lock error
                if (e.getMessage().contains(SQLITE_BUSY)) {
                    retries++;
                    System.out.println("Database locked, retry attempt " + retries + " of " + MAX_RETRIES);
                    
                    if (retries < MAX_RETRIES) {
                        // Exponential backoff with jitter
                        long backoffMs = (long) Math.min(1000 * Math.pow(2, retries), 8000) 
                                       + random.nextInt(1000);
                        try {
                            System.out.println("Waiting " + backoffMs + "ms before retry");
                            Thread.sleep(backoffMs);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;  // Exit the retry loop if interrupted
                        }
                    }
                } else {
                    // Different error, just log and return error
                    System.out.println("Error deleting job: " + e.getMessage());
                    return false;
                }
            }
        }
        
        if (retries == MAX_RETRIES) {
            System.out.println("Failed to delete job after " + MAX_RETRIES + " attempts due to database locks");
        }
        
        return success;
    }
    
    // Close the database connection
    public void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("JobDAO database connection closed.");
            }
        } catch (SQLException e) {
            System.out.println("Error closing database connection: " + e.getMessage());
        }
    }
} 