package com.petwatch.petwatch.Controller;

import com.petwatch.petwatch.DAO.PetDAO;
import com.petwatch.petwatch.DAO.PetOwnerDAO;
import com.petwatch.petwatch.DAO.PetSitterDAO;
import com.petwatch.petwatch.DAO.UserDAO;
import com.petwatch.petwatch.Model.Pet;
import com.petwatch.petwatch.Model.PetOwner;
import com.petwatch.petwatch.Model.PetSitter;
import com.petwatch.petwatch.Model.User;
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
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Constructor to initialize DAOs
    public ApiController() {
        this.userDAO = new UserDAO();
        this.petDAO = new PetDAO();
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
            User.Role role = (roleStr != null) ? 
                ("owner".equalsIgnoreCase(roleStr) ? User.Role.USER : User.Role.EMPLOYEE) : 
                User.Role.USER;
            
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
        
        System.out.println("Received request to save pet: " + petData);
        
        // Get user by email
        User user = userDAO.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error", 
                "message", "User not found"
            ));
        }
        
        // Check if user has a pet_owner record, create one if not
        int petOwnerId = userDAO.getPetOwnerIdForUser(user.getId());
        if (petOwnerId == -1) {
            // Create a pet_owner record for this user
            petOwnerId = userDAO.createPetOwnerForUser(user.getId(), user.getEmail(), "555-555-5555", "123 Main St");
            if (petOwnerId == -1) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to create pet owner profile"
                ));
            }
            System.out.println("Created pet owner profile with ID: " + petOwnerId + " for user ID: " + user.getId());
        }
        
        // Get the pet type from request
        String petTypeStr = (String) petData.get("type");
        Pet.PetType petType;
        try {
            petType = Pet.PetType.valueOf(petTypeStr.toUpperCase());
        } catch (Exception e) {
            petType = Pet.PetType.OTHER;
        }
        
        // Extract name and age if provided, otherwise use defaults
        String petName = petData.containsKey("name") ? (String) petData.get("name") : "Pet";
        int petAge = 1;
        if (petData.containsKey("age")) {
            Object ageObj = petData.get("age");
            if (ageObj instanceof Number) {
                petAge = ((Number) ageObj).intValue();
            } else if (ageObj instanceof String) {
                try {
                    petAge = Integer.parseInt((String) ageObj);
                } catch (NumberFormatException e) {
                    // Use default age
                }
            }
        }
        
        // Parse pet ID if provided
        Object petIdObj = petData.get("id");
        Integer petId = null;
        
        if (petIdObj != null) {
            try {
                if (petIdObj instanceof String) {
                    String petIdStr = (String) petIdObj;
                    if (!petIdStr.isEmpty()) {
                        petId = Integer.parseInt(petIdStr);
                    }
                } else if (petIdObj instanceof Number) {
                    petId = ((Number) petIdObj).intValue();
                }
            } catch (NumberFormatException e) {
                System.err.println("Error parsing pet ID: " + e.getMessage());
            }
        }
        
        // Create or update pet
        Pet updatedPet = null;
        boolean success = false;
        
        if (petId != null && petId > 0) {
            // Try to update existing pet
            updatedPet = petDAO.getPetById(petId);
            
            if (updatedPet != null) {
                // Update pet details
                updatedPet.setType(petType);
                updatedPet.setPetName(petName);
                updatedPet.setPetAge(petAge);
                updatedPet.setPetOwnerId(petOwnerId); // Use pet_owner_id instead of user_id
                
                // Perform the database update
                success = petDAO.updatePet(updatedPet);
                if (success) {
                    System.out.println("Updated pet with ID: " + petId);
                } else {
                    return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Failed to update pet in database"
                    ));
                }
            } else {
                // Pet ID was provided but not found, create new
                System.out.println("Pet with ID " + petId + " not found. Creating new pet.");
                updatedPet = new Pet(petName, petAge, petType, petOwnerId); // Use pet_owner_id instead of user_id
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
        } else {
            // Create new pet
            System.out.println("Creating new pet for pet owner ID: " + petOwnerId);
            updatedPet = new Pet(petName, petAge, petType, petOwnerId); // Use pet_owner_id instead of user_id
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
            response.put("pet", Map.of(
                "id", String.valueOf(updatedPet.getPetId()),
                "type", updatedPet.getType().toString(),
                "name", updatedPet.getPetName(),
                "age", updatedPet.getPetAge()
            ));
        }
        
        return ResponseEntity.ok(response);
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
} 