document.addEventListener("DOMContentLoaded", function () {
    console.log("Dashboard.js loaded!");

    // Check if user is logged in - we'll add a simple API call to check auth status
    fetch('/api/check-auth')
        .then(response => {
            if (!response.ok) {
                // If not authenticated, redirect to login
                window.location.href = "/login-choice";
            }
        })
        .catch(error => {
            console.error("Auth check failed:", error);
            // On error, assume not authenticated
            window.location.href = "/login-choice";
        });

    // Get elements
    const ownerDashboard = document.getElementById("ownerDashboard");
    const sitterDashboard = document.getElementById("sitterDashboard");
    const ownerBtn = document.getElementById("ownerBtn");
    const sitterBtn = document.getElementById("sitterBtn");
    const logoutBtn = document.getElementById("logoutBtn");

    const addNewPetBtn = document.querySelector(".add-new-pet"); // "Add New Pet" button
    const petContainer = document.querySelector(".pet-container"); // Pet container grid
    const requestList = document.getElementById("request-list"); // Sitter's request list
    const bioContainer = document.getElementById("bio-container");
    const editBioBtn = document.querySelector(".edit-bio"); // "Edit Bio" button

    // Modal Elements (For Owner)
    const petModal = document.getElementById("petModal");
    const modalTitle = document.getElementById("modalTitle");
    const savePetBtn = document.getElementById("savePet");
    const petTypeContainer = document.getElementById("petTypeContainer");
    const selectedPetTypeText = document.getElementById("selectedPetTypeText");

   // Modal Elements (For Sitter)
   const bioModal = document.getElementById("bioModal");
   const bioModalTitle = document.getElementById("bioModalTitle");
   const saveBioBtn = document.getElementById("saveBio");
   const bioTypeContainer = document.getElementById("bioTypeContainer");

    let selectedPetType = "";
    let editingPet = null;
    let currentPetId = null;
    let loggedInUser = ""; // Will be set from the server side
    let loggedInUserId = 0;

    // Get current user email from a simple API endpoint
    fetch('/api/current-user')
        .then(response => response.json())
        .then(data => {
            console.log('User data :', data);
            loggedInUser = data.email;
            loggedInUserId = data.id;
            console.log("Current user:", loggedInUser);
            console.log("Current user Id :", loggedInUserId);
            loadPets(); // Load pets once we have the user email
        })
        .catch(error => {
            console.error("Failed to get current user:", error);
        });
    
    // Load pets from the server
    function loadPets() {
        if (!loggedInUser) {
            console.warn("No user email available, can't load pets");
            return;
        }
        
        // Clear existing pets (except the "Add New Pet" button)
        const existingPets = petContainer.querySelectorAll("div");
        existingPets.forEach(pet => pet.remove());
        
        // Call API to get pets for the current user
        fetch(`/api/pets?email=${encodeURIComponent(loggedInUser)}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error fetching pets: ' + response.statusText);
                }
                return response.json();
            })
            .then(pets => {
                console.log("Loaded pets:", pets);
                
                // Add saved pets to the container
                pets.forEach(pet => {
                    // Ensure petId is treated as a number
                    const petId = pet.id ? parseInt(pet.id, 10) : '';
                    console.log(`Creating pet card for ${pet.type} with ID: ${petId}`);
                    
                    const newPetCard = document.createElement("div");
                    newPetCard.classList.add("bg-white", "p-4", "shadow", "rounded-lg");
                    newPetCard.dataset.petId = petId; // Store pet ID for editing/deleting
                    newPetCard.dataset.petType = pet.type; // Store pet type in dataset for easy access
                    
                    newPetCard.innerHTML = `
                        <h3 class="text-lg font-bold text-center pet-name">${pet.type}</h3>
                        <button class="edit-pet w-full mt-4 px-4 py-2 bg-blue-500 text-white rounded">Edit Pet</button>
                    `;
                    petContainer.appendChild(newPetCard);
                });
                
                // Reattach event listeners
                attachEditButtons();
            })
            .catch(error => {
                console.error("Error loading pets:", error);
            });
    }

    
        // Determine which dashboard to show based on the URL path
        const currentPath = window.location.pathname;
        if (currentPath.includes('dashboard-owner')) {
            console.log("Showing Owner Dashboard");
            if (ownerDashboard) ownerDashboard.classList.remove("hidden");
            if (ownerBtn) ownerBtn.classList.remove("hidden");
            // Load pets when dashboard is shown (will be called after getting user email)
        } else if (currentPath.includes('dashboard-sitter')) {
            console.log("Showing Sitter Dashboard");
            if (sitterDashboard) sitterDashboard.classList.remove("hidden");
            if (sitterBtn) sitterBtn.classList.remove("hidden");
        } else {
            console.log("Invalid dashboard path! Redirecting to login.");
            window.location.href = "/login-choice"; // Redirect if path is unexpected
        }

    // Logout functionality - now using Spring Security logout form
    function attachLogoutListener() {
        const logoutBtn = document.getElementById("logoutBtn");
        if (logoutBtn) {
            logoutBtn.addEventListener("click", function () {
                console.log("Logging out...");
                // Submit the logout form (which has CSRF token)
                const logoutForm = document.getElementById("logoutForm");
                if (logoutForm) {
                    logoutForm.submit();
                } else {
                    console.error("Logout form not found!");
                    // Fallback to direct link if form not found
                    window.location.href = "/perform-logout";
                }
            });
        } else {
            console.error("Logout button not found! Retrying...");
            setTimeout(attachLogoutListener, 1000);
        }
    }
    attachLogoutListener();

    // Function to reset the pet editing form
    function resetPetForm(isEditing = false, petType = null, petId = null) {
        // Update form title
        modalTitle.innerText = isEditing ? "Edit Pet" : "Add New Pet";
        
        // Reset pet ID
        currentPetId = petId;
        
        // Reset selection
        selectedPetType = petType || "";
        selectedPetTypeText.innerText = selectedPetType ? `Selected Pet: ${selectedPetType}` : "Selected Pet: None";
        
        // Reset selection styling on buttons
        document.querySelectorAll(".pet-type-button").forEach((btn) => {
            btn.classList.remove("ring-4", "ring-blue-300");
            if (selectedPetType && btn.getAttribute("data-type") === selectedPetType) {
                btn.classList.add("ring-4", "ring-blue-300");
            }
        });
        
        console.log(`Pet form reset for ${isEditing ? 'editing' : 'creation'}, type: ${selectedPetType}, ID: ${currentPetId}`);
    }

    // Owner: Open modal for adding a new pet
    if (addNewPetBtn) {
        addNewPetBtn.addEventListener("click", function () {
            console.log("Add New Pet Clicked!");
            resetPetForm(false);
            petModal.classList.remove("hidden");
        });
    }

    // Owner: Select pet type
    function attachPetTypeButtons() {
        document.querySelectorAll(".pet-type-button").forEach((btn) => {
            btn.addEventListener("click", function () {
                const newType = this.getAttribute("data-type");
                console.log(`Changing pet type to: ${newType}, previous type was: ${selectedPetType}, current pet ID: ${currentPetId}`);
                
                // Remove selection styling from all buttons
                document.querySelectorAll(".pet-type-button").forEach((b) => {
                    b.classList.remove("ring-4", "ring-blue-300");
                });
                
                // Add selection styling to clicked button
                this.classList.add("ring-4", "ring-blue-300");
                
                // Update selected pet type
                selectedPetType = newType;
                selectedPetTypeText.innerText = "Selected Pet: " + selectedPetType;
            });
        });
    }
    attachPetTypeButtons();

    // Owner: Save pet (either add new or edit existing)
    if (savePetBtn) {
        savePetBtn.addEventListener("click", function () {
            if (!selectedPetType) {
                alert("Please select a pet type before saving.");
                return;
            }
            
            // Prepare pet data
            const petData = {
                type: selectedPetType,
                id: currentPetId ? String(currentPetId) : null // Convert to string for API
            };
            
            console.log("Saving pet with data:", petData);
            
            // Save to server
            fetch(`/api/pets?email=${encodeURIComponent(loggedInUser)}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    // Add CSRF token if needed
                    ...(document.querySelector('meta[name="_csrf"]') ? {
                        'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
                    } : {})
                },
                body: JSON.stringify(petData)
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok: ' + response.statusText);
                }
                return response.json();
            })
            .then(data => {
                console.log("Pet saved successfully:", data);
                // Reload pets to reflect changes
                loadPets();
                closeModal();
            })
            .catch(error => {
                console.error("Error saving pet:", error);
                alert("Error saving pet. Please try again.");
            });
        });
    }

    // Close modal function
    window.closeModal = function () {
        petModal.classList.add("hidden");
    };

    // Close modal function
    window.closeBioModal = function () {
        bioModal.classList.add("hidden");
    };
    
    // Function to attach event listeners to Edit Pet buttons
    function attachEditButtons() {
        document.querySelectorAll(".edit-pet").forEach((btn) => {
            btn.addEventListener("click", function() {
                const petCard = this.closest("div");
                const petType = petCard.dataset.petType; // Get pet type from the dataset attribute
                const petIdStr = petCard.dataset.petId;
                const petId = petIdStr ? parseInt(petIdStr, 10) : null;
                
                console.log(`Edit button clicked for pet: ${petType} with ID: ${petId}`);
                
                if (isNaN(petId)) {
                    console.error("Invalid pet ID:", petIdStr);
                    alert("Error: This pet has an invalid ID and cannot be edited.");
                    return;
                }
                
                // Set editing mode and reset form
                editingPet = petCard;
                resetPetForm(true, petType, petId);
                
                // Show modal
                petModal.classList.remove("hidden");
            });
        });
    }
});
