document.addEventListener("DOMContentLoaded", function () {
    console.log("Dashboard-sitter.js loaded!");

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
   const bioText = document.getElementById("bioText");
   const selectedBioText = document.getElementById("selectedBioText");

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
            if(data.role === "EMPLOYEE") {
                console.log('load bio');
                loadBio();
            } else {
                console.log('load Pets');
                loadPets(); // Load pets once we have the user email
            }
        })
        .catch(error => {
            console.error("Failed to get current user:", error);
        });

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

    if (saveBioBtn) {
        saveBioBtn.addEventListener("click", function () {
            /*if (!selectedPetType) {
                alert("Please select a pet type before saving.");
                return;
            }*/
            console.log("Calling saving.");

            // Prepare pet data
            /*
               this.userId = userId;
                    this.name = name;
                    this.experience = experience;
                    this.availability = availability;
                    this.rating = 0;
                    this.city = city;
                    this.bio = bio;
                    this.phone = phone;

            */
            const petSitter = {
                userId: loggedInUserId,
                phone: document.getElementById("phoneNumber")?.value || "444-444-4444",
                experience: document.getElementById("experience")?.value || "5 years",
                bio: document.getElementById("bio")?.value || "testing",
                name: 'name',
                availability: 'availability',
                rating: 2.0,
                city: 'city'
            };

            console.log("Saving Profile with data:", petSitter);

            // Save to server
            fetch("/api/sitter", {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    // Add CSRF token if needed
                    ...(document.querySelector('meta[name="_csrf"]') ? {
                        'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
                    } : {})
                },
                body: JSON.stringify(petSitter)
            })
            .then(response => {
                console.log("Sitter Profile saved successfully:", response);
                if (!response.ok) {
                    throw new Error('Network response was not ok: ' + response.statusText);
                }
                // Reload pets to reflect changes
                loadBio();
                closeBioModal();
                return response.json();
            })
            .then(data => {
                console.log("Sitter Profile saved data:", data);
            })
            .catch(error => {
                console.error("Error saving pet:", error);
                alert("Error saving pet. Please try again.");
            });
        });
    }

    function attachEditBioButton() {
        const editBioBtn = document.querySelector(".edit-bio"); // "Edit Bio" button
        // Sitter: Open modal for adding sitter bio
        if (editBioBtn) {
            editBioBtn.addEventListener("click", function () {
                console.log("Edit Bio Clicked!");
                //resetPetForm(false);
                bioModal.classList.remove("hidden");
            });
        }
    }

    function loadBio(){
        if (!loggedInUser) {
            console.warn("No user email available, can't load pets");
            return;
        }

        // Clear existing pets (except the "Add New Pet" button)
        const existingSitter = bioContainer.querySelectorAll("div:not(:first-child)");

        fetch('/api/sitter?userId='+ loggedInUserId)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error fetching bio: ' + response.statusText);
                }
                return response.json();
            })
            .then(bio => {
                console.log("Loaded bio:", bio);
                const newSitterCard = document.createElement("div");
                //newSitterCard.classList.add("bg-white", "p-4", "shadow", "rounded-lg");
                newSitterCard.dataset.name = bio.name; // Store pet type in dataset for easy access
                newSitterCard.dataset.bio = bio.bio;
                newSitterCard.dataset.phone = bio.phone;

                newSitterCard.innerHTML = `
                    <div class="bg-white p-6 shadow-lg rounded-lg mt-6">
                        <h3 class="text-xl font-bold">👤 ${bio.name}</h3>
                        <p class="text-gray-500">${bio.bio}</p>
                        <p class="mt-2 text-sm"><i class="fas fa-phone"></i> Contact: +1 ${bio.phone}</p>
                        <button class="edit-bio mt-4 px-4 py-2 bg-green-500 text-white rounded">Edit Profile</button>
                    </div>
                `;
                bioContainer.appendChild(newSitterCard);

                document.getElementById("phoneNumber").value = bio.phone;
                document.getElementById("experience").value = bio.experience;
                document.getElementById("bio").value = bio.bio;

                // Reattach event listeners
                attachEditBioButton();
            })
            .catch(error => {
                console.error("Error loading bio:", error);
            });
        }

    // Close modal function
    window.closeBioModal = function () {
        bioModal.classList.add("hidden");
    };

});
