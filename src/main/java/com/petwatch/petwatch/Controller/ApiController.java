package com.petwatch.petwatch.Controller;

import com.petwatch.petwatch.DAO.PetDAO;
import com.petwatch.petwatch.DAO.PetOwnerDAO;
import com.petwatch.petwatch.DAO.PetSitterDAO;
import com.petwatch.petwatch.DAO.UserDAO;
import com.petwatch.petwatch.DAO.JobDAO;
import com.petwatch.petwatch.Model.Pet;
import com.petwatch.petwatch.Model.PetOwner;
import com.petwatch.petwatch.Model.PetSitter;
import com.petwatch.petwatch.Model.User;
import com.petwatch.petwatch.Model.Job;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {
    
    // Use DAOs for database access instead of in-memory storage
    private final UserDAO userDAO;
    private final PetDAO petDAO;
    private final JobDAO jobDAO;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Constructor to initialize DAOs
    public ApiController() {
        this.userDAO = new UserDAO();
        this.petDAO = new PetDAO();
        this.jobDAO = new JobDAO();
        System.out.println("ApiController initialized with database access");
    }
    
    // Helper method for SecurityConfig
    public static User getUserByEmail(String email) {
        UserDAO tempUserDAO = new UserDAO();
        User user = tempUserDAO.getUserByEmail(email);
        tempUserDAO.closeConnection();
        return user;
    }
    
    @GetMapping("/check-auth")
    public ResponseEntity<Map<String, String>> checkAuthentication(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("status", "not authenticated"));
        }
        return ResponseEntity.ok(Map.of("status", "authenticated"));
    }
    
    @GetMapping("/current-user")
    public ResponseEntity<Map<String, String>> getCurrentUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        
        String email = principal.getName();
        User user = userDAO.getUserByEmail(email);
        String role = (user != null) ? user.getRole().toString() : "";
        
        return ResponseEntity.ok(Map.of(
            "email", email,
            "role", role
        ));
    }
    
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody Map<String, Object> userData) {
        try {
            // Create user from request data
            String email = (String) userData.get("email");
            String password = (String) userData.get("password");
            
            // Default to USER role when not specified (for owner signup)
            String roleStr = (String) userData.get("role");
            User.Role role;
            
            if (roleStr != null) {
                if ("employee".equalsIgnoreCase(roleStr)) {
                    role = User.Role.EMPLOYEE;
                    System.out.println("Setting role to EMPLOYEE based on roleStr: " + roleStr);
                } else {
                    role = User.Role.USER;
                    System.out.println("Setting role to USER based on roleStr: " + roleStr);
                }
            } else {
                role = User.Role.USER;
                System.out.println("No role specified, defaulting to USER");
            }
            
            System.out.println("Received signup data: " + userData);
            
            // Check if user already exists
            User existingUser = userDAO.getUserByEmail(email);
            if (existingUser != null) {
                System.out.println("User already exists: " + email);
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Email already registered"));
            }
            
            // Create our custom User model and save to database
            User newUser = new User(email, password, role);
            int userId = userDAO.addUser(newUser);
            
            if (userId <= 0) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Failed to create user in database"));
            }
            
            System.out.println("Registered user: " + email + " with role: " + role + " and ID: " + userId);
            
            // Handle additional profile data based on role
            if (role == User.Role.USER) {
                // This is a pet owner
                String firstName = (String) userData.get("firstName");
                String phone = (String) userData.get("phoneNumber");
                String address = (String) userData.get("address");
                String country = (String) userData.get("country");
                String state = (String) userData.get("state");
                String city = (String) userData.get("city");
                String postalCode = (String) userData.get("postalCode");
                
                // Use defaults for any missing values
                firstName = (firstName != null) ? firstName : email;
                phone = (phone != null) ? phone : "";
                address = (address != null) ? address : "";
                country = (country != null) ? country : "";
                state = (state != null) ? state : "";
                city = (city != null) ? city : "";
                postalCode = (postalCode != null) ? postalCode : "";
                
                // Create pet owner profile
                PetOwner petOwner = new PetOwner(address, phone, firstName, userId, country, state, city, postalCode);
                PetOwnerDAO petOwnerDAO = new PetOwnerDAO();
                int petOwnerId = petOwnerDAO.addPetOwner(petOwner);
                petOwnerDAO.closeConnection();
                
                if (petOwnerId <= 0) {
                    System.err.println("Warning: Failed to create pet owner profile for user ID: " + userId);
                } else {
                    System.out.println("Created pet owner profile with ID: " + petOwnerId + " for user ID: " + userId);
                }
            } else if (role == User.Role.EMPLOYEE) {
                // This is a pet sitter
                String firstName = (String) userData.get("firstName");
                String phone = (String) userData.get("phoneNumber");
                String city = (String) userData.get("city");
                String experience = (String) userData.get("experience");
                String bio = (String) userData.get("bio");
                
                // Use defaults for any missing values
                firstName = (firstName != null) ? firstName : email;
                phone = (phone != null) ? phone : "";
                city = (city != null) ? city : "";
                experience = (experience != null) ? experience : "0";
                bio = (bio != null) ? bio : "";
                
                // Create pet sitter profile
                PetSitter petSitter = new PetSitter(userId, firstName, experience, "Available", city, bio, phone);
                PetSitterDAO petSitterDAO = new PetSitterDAO();
                int petSitterId = petSitterDAO.addPetSitter(petSitter);
                petSitterDAO.closeConnection();
                
                if (petSitterId <= 0) {
                    System.err.println("Warning: Failed to create pet sitter profile for user ID: " + userId);
                } else {
                    System.out.println("Created pet sitter profile with ID: " + petSitterId + " for user ID: " + userId);
                }
            }
            
            return ResponseEntity.ok(Map.of("status", "success", "message", "User registered successfully"));
        } catch (Exception e) {
            System.err.println("Error in signup: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Registration failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/pets")
    public ResponseEntity<List<Map<String, Object>>> getPets(@RequestParam String email) {
        // Get user ID from email
        User user = userDAO.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
        
        System.out.println("Fetching pets for user ID: " + user.getId());
        
        // Get pet owner ID from user ID
        int petOwnerId = userDAO.getPetOwnerIdForUser(user.getId());
        if (petOwnerId == -1) {
            System.out.println("No pet owner found for user ID: " + user.getId());
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        System.out.println("Using pet owner ID: " + petOwnerId + " to fetch pets");
        
        // Get pets from database using PetDAO with the pet owner ID
        List<Pet> pets = petDAO.getPetsByOwnerId(petOwnerId);
        
        // Convert pets to format expected by frontend
        List<Map<String, Object>> response = new ArrayList<>();
        for (Pet pet : pets) {
            Map<String, Object> petData = new HashMap<>();
            petData.put("id", String.valueOf(pet.getPetId()));
            petData.put("type", pet.getType().toString());
            petData.put("name", pet.getPetName());
            petData.put("age", pet.getPetAge());
            
            System.out.println("  - Pet: " + pet.getType() + " with ID: " + pet.getPetId());
            response.add(petData);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/pets")
    public ResponseEntity<Map<String, Object>> savePet(
            @RequestParam String email,
            @RequestBody Map<String, Object> petData) {
        
        int petId = -1;
        Pet updatedPet = null;
        boolean success = false;
        
        try {
            // Get user ID from email
            User user = userDAO.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "User not found"
                ));
            }
            
            // Get pet owner ID from user ID
            int petOwnerId = userDAO.getPetOwnerIdForUser(user.getId());
            if (petOwnerId == -1) {
                System.out.println("No pet owner found for user ID: " + user.getId());
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error", 
                    "message", "Pet owner not found"
                ));
            }
            
            // Parse pet data
            String type = (String) petData.get("type");
            String name = (String) petData.get("name");
            Integer age = null;
            if (petData.get("age") != null) {
                age = ((Number) petData.get("age")).intValue();
            }
            
            Pet.PetType petType;
            try {
                petType = Pet.PetType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Invalid pet type"
                ));
            }
            
            // Check if this is an update (petData contains an ID)
            if (petData.containsKey("id") && petData.get("id") != null) {
                int id;
                if (petData.get("id") instanceof Number) {
                    id = ((Number) petData.get("id")).intValue();
                } else {
                    id = Integer.parseInt(petData.get("id").toString());
                }
                
                System.out.println("Received request to save pet: " + petData);
                
                // Update existing pet
                Pet existingPet = petDAO.getPetById(id);
                if (existingPet == null) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Pet not found"
                    ));
                }
                
                existingPet.setType(petType);
                existingPet.setPetName(name);
                if (age != null) {
                    existingPet.setPetAge(age);
                }
                
                success = petDAO.updatePet(existingPet);
                updatedPet = existingPet;
                
                if (success) {
                    System.out.println("Pet updated successfully with ID: " + id);
                    System.out.println("Updated pet with ID: " + id);
                } else {
                    return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Failed to update pet in database"
                    ));
                }
            } else {
                // Create new pet
                System.out.println("Creating new pet for pet owner ID: " + petOwnerId);
                updatedPet = new Pet(name, age != null ? age : 0, petType, petOwnerId);
                int newPetId = petDAO.addPet(updatedPet);
                
                if (newPetId > 0) {
                    success = true;
                    System.out.println("Created new pet with ID: " + newPetId);
                } else {
                    return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Failed to create pet in database"
                    ));
                }
            }
            
            // Return response with pet data
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Pet saved successfully");
            
            if (updatedPet != null) {
                Map<String, Object> petInfo = new HashMap<>();
                petInfo.put("id", String.valueOf(updatedPet.getPetId()));
                petInfo.put("type", updatedPet.getType().toString());
                petInfo.put("name", updatedPet.getPetName());
                petInfo.put("age", updatedPet.getPetAge());
                
                response.put("pet", petInfo);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error saving pet: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Server error: " + e.getMessage()
            ));
        }
    }
    
    @DeleteMapping("/pets/{petId}")
    public ResponseEntity<Map<String, String>> deletePet(
            @RequestParam String email,
            @PathVariable String petId) {
        
        try {
            int id = Integer.parseInt(petId);
            petDAO.removePet(id);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Pet deleted successfully"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Invalid pet ID"));
        }
    }
    
    // Job Endpoints
    @PostMapping("/jobs")
    public ResponseEntity<Map<String, Object>> createJob(
            @RequestParam String email,
            @RequestBody Map<String, Object> jobData) {
        JobDAO jobDAO = new JobDAO();
        try {
            // Get user ID from email
            User user = userDAO.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "User not found"));
            }
            
            // Get pet owner ID from user ID
            int petOwnerId = userDAO.getPetOwnerIdForUser(user.getId());
            if (petOwnerId == -1) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Pet owner not found"));
            }
            
            // Parse job data
            String petType = (String) jobData.get("petType");
            String priority = (String) jobData.get("priority");
            String startDateStr = (String) jobData.get("startDate");
            String endDateStr = (String) jobData.get("endDate");
            String description = (String) jobData.get("description");
            String notes = (String) jobData.get("notes");
            double payRate = Double.parseDouble(jobData.get("payRate").toString());
            String paymentMethod = (String) jobData.get("paymentMethod");
            
            // Convert date strings to Date objects
            Date startDate = java.sql.Date.valueOf(startDateStr);
            Date endDate = java.sql.Date.valueOf(endDateStr);
            
            // Create a new Job object
            Job job = new Job(petOwnerId, petType, priority, startDate, endDate, description, notes, payRate, paymentMethod);
            
            // Save to database
            int jobId = jobDAO.addJob(job);
            
            if (jobId == -1) {
                return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", "Failed to create job"
                ));
            }
            
            // Set the ID in the job object
            job.setId(jobId);
            
            // Return success response with job details
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Job created successfully",
                "job", Map.of(
                    "id", jobId,
                    "petType", petType,
                    "priority", priority,
                    "startDate", startDateStr,
                    "endDate", endDateStr,
                    "description", description,
                    "notes", notes != null ? notes : "",
                    "payRate", payRate,
                    "paymentMethod", paymentMethod,
                    "status", "Open"
                )
            ));
        } catch (Exception e) {
            System.err.println("Error creating job: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error creating job: " + e.getMessage()
            ));
        } finally {
            if (jobDAO != null) {
                jobDAO.closeConnection();
            }
        }
    }

    @GetMapping("/jobs/owner")
    public ResponseEntity<List<Map<String, Object>>> getOwnerJobs(@RequestParam String email) {
        JobDAO jobDAO = new JobDAO();
        try {
            // Get user ID from email
            User user = userDAO.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(Collections.emptyList());
            }
            
            // Get pet owner ID from user ID
            int petOwnerId = userDAO.getPetOwnerIdForUser(user.getId());
            if (petOwnerId == -1) {
                return ResponseEntity.status(404).body(Collections.emptyList());
            }
            
            // Get jobs for pet owner
            List<Job> jobs = jobDAO.getJobsByPetOwnerId(petOwnerId);
            
            // Convert jobs to JSON-friendly format
            List<Map<String, Object>> jobsData = new ArrayList<>();
            for (Job job : jobs) {
                Map<String, Object> jobData = new HashMap<>();
                jobData.put("id", job.getId());
                jobData.put("petType", job.getPetType());
                jobData.put("priority", job.getPriority());
                jobData.put("startDate", job.getStartDate().toString());
                jobData.put("endDate", job.getEndDate().toString());
                jobData.put("description", job.getDescription());
                jobData.put("notes", job.getNotes());
                jobData.put("payRate", job.getPayRate());
                jobData.put("paymentMethod", job.getPaymentMethod());
                jobData.put("status", job.getStatus());
                
                // Add sitter name for accepted jobs
                if ("Accepted".equals(job.getStatus()) && job.getPetSitterId() != null) {
                    PetSitterDAO petSitterDAO = new PetSitterDAO();
                    PetSitter sitter = petSitterDAO.getPetSitterById(job.getPetSitterId());
                    if (sitter != null) {
                        jobData.put("sitterName", sitter.getName());
                    } else {
                        jobData.put("sitterName", "Unknown Sitter");
                    }
                    petSitterDAO.closeConnection();
                }
                
                jobsData.add(jobData);
            }
            
            return ResponseEntity.ok(jobsData);
        } catch (Exception e) {
            System.err.println("Error getting jobs for owner: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.emptyList());
        } finally {
            if (jobDAO != null) {
                jobDAO.closeConnection();
            }
        }
    }

    @GetMapping("/jobs/open")
    public ResponseEntity<List<Map<String, Object>>> getOpenJobs() {
        JobDAO jobDAO = new JobDAO();
        try {
            System.out.println("API: Fetching all open jobs");
            
            // Get all open jobs
            List<Job> jobs = jobDAO.getOpenJobs();
            System.out.println("API: Found " + jobs.size() + " open jobs");
            
            // Convert jobs to JSON-friendly format
            List<Map<String, Object>> jobsData = new ArrayList<>();
            for (Job job : jobs) {
                Map<String, Object> jobData = new HashMap<>();
                jobData.put("id", job.getId());
                jobData.put("petType", job.getPetType());
                jobData.put("priority", job.getPriority());
                jobData.put("startDate", job.getStartDate().toString());
                jobData.put("endDate", job.getEndDate().toString());
                jobData.put("description", job.getDescription());
                jobData.put("notes", job.getNotes());
                jobData.put("payRate", job.getPayRate());
                jobData.put("paymentMethod", job.getPaymentMethod());
                jobData.put("status", job.getStatus());
                
                System.out.println("API: Adding job ID " + job.getId() + " to response");
                jobsData.add(jobData);
            }
            
            return ResponseEntity.ok(jobsData);
        } catch (Exception e) {
            System.err.println("Error getting open jobs: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.emptyList());
        } finally {
            if (jobDAO != null) {
                jobDAO.closeConnection();
            }
        }
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<Map<String, Object>> getJobById(@PathVariable int jobId) {
        JobDAO jobDAO = new JobDAO();
        try {
            // Get job by ID
            Job job = jobDAO.getJobById(jobId);
            
            if (job == null) {
                return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Job not found"));
            }
            
            // Convert job to JSON-friendly format
            Map<String, Object> jobData = new HashMap<>();
            jobData.put("id", job.getId());
            jobData.put("petType", job.getPetType());
            jobData.put("priority", job.getPriority());
            jobData.put("startDate", job.getStartDate().toString());
            jobData.put("endDate", job.getEndDate().toString());
            jobData.put("description", job.getDescription());
            jobData.put("notes", job.getNotes());
            jobData.put("payRate", job.getPayRate());
            jobData.put("paymentMethod", job.getPaymentMethod());
            jobData.put("status", job.getStatus());
            
            return ResponseEntity.ok(jobData);
        } catch (Exception e) {
            System.err.println("Error getting job by ID: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Error retrieving job"));
        } finally {
            if (jobDAO != null) {
                jobDAO.closeConnection();
            }
        }
    }

    @GetMapping("/jobs/sitter")
    public ResponseEntity<List<Map<String, Object>>> getSitterJobs(@RequestParam String email) {
        JobDAO jobDAO = new JobDAO();
        try {
            // Get user ID from email
            User user = userDAO.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(Collections.emptyList());
            }
            
            // Get pet sitter ID from user ID
            PetSitterDAO petSitterDAO = new PetSitterDAO();
            int petSitterId = petSitterDAO.getPetSitterIdByUserId(user.getId());
            petSitterDAO.closeConnection();
            
            if (petSitterId == -1) {
                return ResponseEntity.status(404).body(Collections.emptyList());
            }
            
            // Get jobs assigned to this pet sitter
            List<Job> jobs = jobDAO.getJobsByPetSitterId(petSitterId);
            
            // Convert jobs to JSON-friendly format
            List<Map<String, Object>> jobsData = new ArrayList<>();
            for (Job job : jobs) {
                Map<String, Object> jobData = new HashMap<>();
                jobData.put("id", job.getId());
                jobData.put("petType", job.getPetType());
                jobData.put("priority", job.getPriority());
                jobData.put("startDate", job.getStartDate().toString());
                jobData.put("endDate", job.getEndDate().toString());
                jobData.put("description", job.getDescription());
                jobData.put("notes", job.getNotes());
                jobData.put("payRate", job.getPayRate());
                jobData.put("paymentMethod", job.getPaymentMethod());
                jobData.put("status", job.getStatus());
                
                jobsData.add(jobData);
            }
            
            return ResponseEntity.ok(jobsData);
        } catch (Exception e) {
            System.err.println("Error getting jobs for sitter: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.emptyList());
        } finally {
            if (jobDAO != null) {
                jobDAO.closeConnection();
            }
        }
    }
    
    @PostMapping("/jobs/{jobId}/accept")
    public ResponseEntity<Map<String, Object>> acceptJob(
            @PathVariable int jobId,
            @RequestParam String email) {
        JobDAO jobDAO = new JobDAO();
        try {
            // Get user ID from email
            User user = userDAO.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "User not found"
                ));
            }
            
            // Get pet sitter ID from user ID
            PetSitterDAO petSitterDAO = new PetSitterDAO();
            int petSitterId = petSitterDAO.getPetSitterIdByUserId(user.getId());
            petSitterDAO.closeConnection();
            
            if (petSitterId == -1) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Pet sitter profile not found"
                ));
            }
            
            // Accept the job
            boolean success = jobDAO.acceptJob(jobId, petSitterId);
            
            if (!success) {
                return ResponseEntity.status(400).body(Map.of(
                    "status", "error",
                    "message", "Failed to accept job. Job may already be accepted or not available."
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Job accepted successfully"
            ));
        } catch (Exception e) {
            System.err.println("Error accepting job: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error accepting job: " + e.getMessage()
            ));
        } finally {
            if (jobDAO != null) {
                jobDAO.closeConnection();
            }
        }
    }
    
    @PostMapping("/jobs/{jobId}/decline")
    public ResponseEntity<Map<String, Object>> declineJob(
            @PathVariable int jobId,
            @RequestParam String email) {
        JobDAO jobDAO = new JobDAO();
        try {
            // Get user ID from email
            User user = userDAO.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "User not found"
                ));
            }
            
            // Get pet sitter ID from user ID
            PetSitterDAO petSitterDAO = new PetSitterDAO();
            int petSitterId = petSitterDAO.getPetSitterIdByUserId(user.getId());
            petSitterDAO.closeConnection();
            
            if (petSitterId == -1) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Pet sitter profile not found"
                ));
            }
            
            // Decline the job
            boolean success = jobDAO.declineJob(jobId, petSitterId);
            
            if (!success) {
                return ResponseEntity.status(400).body(Map.of(
                    "status", "error",
                    "message", "Failed to decline job. Job may not be assigned to you."
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Job declined successfully"
            ));
        } catch (Exception e) {
            System.err.println("Error declining job: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error declining job: " + e.getMessage()
            ));
        } finally {
            if (jobDAO != null) {
                jobDAO.closeConnection();
            }
        }
    }

    @PostMapping("/jobs/{jobId}/reject")
    public ResponseEntity<Map<String, Object>> rejectJob(
            @PathVariable int jobId,
            @RequestParam String email) {
        JobDAO jobDAO = new JobDAO();
        try {
            // Get user ID from email
            User user = userDAO.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "User not found"
                ));
            }
            
            // Get pet sitter ID from user ID
            PetSitterDAO petSitterDAO = new PetSitterDAO();
            int petSitterId = petSitterDAO.getPetSitterIdByUserId(user.getId());
            petSitterDAO.closeConnection();
            
            // Get the job
            Job job = jobDAO.getJobById(jobId);
            if (job == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Job not found"
                ));
            }
            
            // Mark job as rejected/declined
            job.setStatus("Declined");
            boolean success = jobDAO.updateJob(job);
            
            if (!success) {
                return ResponseEntity.status(400).body(Map.of(
                    "status", "error",
                    "message", "Failed to reject job."
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Job rejected successfully"
            ));
        } catch (Exception e) {
            System.err.println("Error rejecting job: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error rejecting job: " + e.getMessage()
            ));
        } finally {
            if (jobDAO != null) {
                jobDAO.closeConnection();
            }
        }
    }

    // Profile Endpoints
    
    @GetMapping("/profile/owner")
    public ResponseEntity<Map<String, Object>> getOwnerProfile(@RequestParam String email) {
        try {
            // Get user from email
            User user = userDAO.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "User not found"
                ));
            }
            
            // Get pet owner ID from user ID
            int petOwnerId = userDAO.getPetOwnerIdForUser(user.getId());
            if (petOwnerId == -1) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Pet owner profile not found"
                ));
            }
            
            // Get pet owner details
            PetOwnerDAO petOwnerDAO = new PetOwnerDAO();
            PetOwner petOwner = petOwnerDAO.getPetOwnerById(petOwnerId);
            
            if (petOwner == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Pet owner profile not found"
                ));
            }
            
            // Return pet owner details
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("name", petOwner.getName());
            profileData.put("phone", petOwner.getPhone());
            profileData.put("address", petOwner.getAddress());
            profileData.put("email", user.getEmail());
            profileData.put("country", petOwner.getCountry());
            profileData.put("state", petOwner.getState());
            profileData.put("city", petOwner.getCity());
            profileData.put("postalCode", petOwner.getPostalCode());
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "profile", profileData
            ));
        } catch (Exception e) {
            System.err.println("Error fetching owner profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error fetching owner profile: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/profile/owner")
    public ResponseEntity<Map<String, Object>> updateOwnerProfile(
            @RequestParam String email,
            @RequestBody Map<String, Object> profileData) {
        try {
            // Get user from email
            User user = userDAO.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "User not found"
                ));
            }
            
            // Get pet owner ID from user ID
            int petOwnerId = userDAO.getPetOwnerIdForUser(user.getId());
            if (petOwnerId == -1) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Pet owner profile not found"
                ));
            }
            
            // Get pet owner details
            PetOwnerDAO petOwnerDAO = new PetOwnerDAO();
            PetOwner petOwner = petOwnerDAO.getPetOwnerById(petOwnerId);
            
            if (petOwner == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Pet owner profile not found"
                ));
            }
            
            // Update pet owner details
            if (profileData.containsKey("name")) {
                petOwner.setName((String) profileData.get("name"));
            }
            if (profileData.containsKey("phone")) {
                petOwner.setPhone((String) profileData.get("phone"));
            }
            if (profileData.containsKey("address")) {
                petOwner.setAddress((String) profileData.get("address"));
            }
            if (profileData.containsKey("country")) {
                petOwner.setCountry((String) profileData.get("country"));
            }
            if (profileData.containsKey("state")) {
                petOwner.setState((String) profileData.get("state"));
            }
            if (profileData.containsKey("city")) {
                petOwner.setCity((String) profileData.get("city"));
            }
            if (profileData.containsKey("postalCode")) {
                petOwner.setPostalCode((String) profileData.get("postalCode"));
            }
            
            // Save updated pet owner to database
            petOwnerDAO.updatePetOwner(petOwner);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Pet owner profile updated successfully"
            ));
        } catch (Exception e) {
            System.err.println("Error updating owner profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error updating owner profile: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/profile/sitter")
    public ResponseEntity<Map<String, Object>> getSitterProfile(@RequestParam String email) {
        try {
            // Get user from email
            User user = userDAO.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "User not found"
                ));
            }
            
            // Get pet sitter details
            PetSitterDAO petSitterDAO = new PetSitterDAO();
            PetSitter petSitter = petSitterDAO.getPetSitterByUserId(user.getId());
            
            if (petSitter == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Pet sitter profile not found"
                ));
            }
            
            // Return pet sitter details
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("name", petSitter.getName());
            profileData.put("bio", petSitter.getBio());
            profileData.put("phone", petSitter.getPhone());
            profileData.put("email", user.getEmail());
            profileData.put("city", petSitter.getCity());
            profileData.put("experience", petSitter.getExperience());
            profileData.put("availability", petSitter.getAvailability());
            profileData.put("rating", petSitter.getRating());
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "profile", profileData
            ));
        } catch (Exception e) {
            System.err.println("Error fetching sitter profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error fetching sitter profile: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/profile/sitter")
    public ResponseEntity<Map<String, Object>> updateSitterProfile(
            @RequestParam String email,
            @RequestBody Map<String, Object> profileData) {
        try {
            // Get user from email
            User user = userDAO.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "User not found"
                ));
            }
            
            // Get pet sitter details
            PetSitterDAO petSitterDAO = new PetSitterDAO();
            PetSitter petSitter = petSitterDAO.getPetSitterByUserId(user.getId());
            
            if (petSitter == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Pet sitter profile not found"
                ));
            }
            
            // Update pet sitter details
            if (profileData.containsKey("name")) {
                petSitter.setName((String) profileData.get("name"));
            }
            if (profileData.containsKey("bio")) {
                petSitter.setBio((String) profileData.get("bio"));
            }
            if (profileData.containsKey("phone")) {
                petSitter.setPhone((String) profileData.get("phone"));
            }
            if (profileData.containsKey("city")) {
                petSitter.setCity((String) profileData.get("city"));
            }
            if (profileData.containsKey("experience")) {
                petSitter.setExperience((String) profileData.get("experience"));
            }
            if (profileData.containsKey("availability")) {
                petSitter.setAvailability((String) profileData.get("availability"));
            }
            
            // Save updated pet sitter to database
            petSitterDAO.updatePetSitter(petSitter);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Pet sitter profile updated successfully"
            ));
        } catch (Exception e) {
            System.err.println("Error updating sitter profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Error updating sitter profile: " + e.getMessage()
            ));
        }
    }
} 