document.addEventListener("DOMContentLoaded", function () {
    console.log("Dashboard.js loaded!");

<<<<<<< Updated upstream
    // Check if user is logged in - we'll add a simple API call to check auth status
=======
    // check if loggedin
>>>>>>> Stashed changes
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

    // Modal Elements (For Owner)
    const petModal = document.getElementById("petModal");
    const modalTitle = document.getElementById("modalTitle");
    const savePetBtn = document.getElementById("savePet");
    const petTypeContainer = document.getElementById("petTypeContainer");
    const selectedPetTypeText = document.getElementById("selectedPetTypeText");
    
    let selectedPetType = "";
    let editingPet = null;
    let currentPetId = null;
<<<<<<< Updated upstream
    let loggedInUser = ""; // Will be set from the server side
    
    // Get current user email from a simple API endpoint
=======
    let loggedInUser = "";

    // Determine which dashboard to show based on the URL path
    const currentPath = window.location.pathname;

>>>>>>> Stashed changes
    fetch('/api/current-user')
        .then(response => response.json())
        .then(data => {
            loggedInUser = data.email;
            console.log("Current user:", loggedInUser);
<<<<<<< Updated upstream
            loadPets(); // Load pets once we have the user email
=======
            // Only load pets on owner dashboard
            if (currentPath.includes('dashboard-owner')) {
                loadPets();
                loadPostedJobs();
                loadOwnerProfile();
            } else if (currentPath.includes('dashboard-sitter')) {
                loadOpenJobs();
                loadAcceptedJobs();
                loadSitterProfile();
            }
>>>>>>> Stashed changes
        })
        .catch(error => {
            console.error("Failed to get current user:", error);
        });
    
    // Load pets from the server
    function loadPets() {
<<<<<<< Updated upstream
        if (!loggedInUser) {
            console.warn("No user email available, can't load pets");
            return;
        }
        
        // Clear existing pets (except the "Add New Pet" button)
=======
        if (!loggedInUser) return;
        
        if (!petContainer) {
            console.warn("Pet container element not found");
            return;
        }

>>>>>>> Stashed changes
        const existingPets = petContainer.querySelectorAll("div:not(:first-child)");
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
                        <h3 class="text-lg font-bold text-center pet-type">${pet.type}</h3>
                        ${pet.name ? `<p class="text-md text-center text-gray-600 pet-name">${pet.name}</p>` : ''}
                        ${pet.age ? `<p class="text-sm text-center text-gray-500">${pet.age} years old</p>` : ''}
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

<<<<<<< Updated upstream
    // Determine which dashboard to show based on the URL path
    const currentPath = window.location.pathname;
=======
>>>>>>> Stashed changes
    if (currentPath.includes('dashboard-owner')) {
        console.log("Showing Owner Dashboard");
        if (ownerDashboard) ownerDashboard.classList.remove("hidden");
        if (ownerBtn) ownerBtn.classList.remove("hidden");
<<<<<<< Updated upstream
        // Load pets when dashboard is shown (will be called after getting user email)
=======
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
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
=======
    function attachEditButtons() {
        const petEditButtons = document.querySelectorAll(".edit-pet");
        petEditButtons.forEach(button => {
            button.addEventListener("click", function() {
                const petCard = this.closest("div");
                currentPetId = petCard.dataset.petId ? parseInt(petCard.dataset.petId, 10) : null;
                selectedPetType = petCard.dataset.petType;
                
                if (petModal) {
                    modalTitle.textContent = "Edit Pet";
                    
                    const petName = petCard.querySelector('.pet-name')?.textContent || '';
                    const petAge = petCard.querySelector('.text-gray-500')?.textContent?.split(' ')[0] || '';
                    
                    document.getElementById('petNameInput').value = petName;
                    document.getElementById('petAgeInput').value = petAge;
                    
                    selectedPetTypeText.textContent = `Selected Pet: ${selectedPetType}`;
                    
                    petTypeContainer.querySelectorAll('.pet-type-button').forEach(btn => {
                        if (btn.dataset.type === selectedPetType) {
                            btn.classList.add('ring-2', 'ring-offset-2', 'ring-blue-500');
                        } else {
                            btn.classList.remove('ring-2', 'ring-offset-2', 'ring-blue-500');
                        }
                    });
                    
                    petModal.classList.remove("hidden");
                }
            });
>>>>>>> Stashed changes
        });
        
        console.log(`Pet form reset for ${isEditing ? 'editing' : 'creation'}, type: ${selectedPetType}, ID: ${currentPetId}`);
    }

<<<<<<< Updated upstream
    // Owner: Open modal for adding a new pet
    if (addNewPetBtn) {
        addNewPetBtn.addEventListener("click", function () {
            console.log("Add New Pet Clicked!");
            resetPetForm(false);
=======
    if (petTypeContainer) {
        const petTypeButtons = petTypeContainer.querySelectorAll(".pet-type-button");
        petTypeButtons.forEach(button => {
            button.addEventListener("click", function () {
                selectedPetType = this.dataset.type;
                selectedPetTypeText.textContent = `Selected Pet: ${selectedPetType}`;
                
                petTypeButtons.forEach(btn => {
                    if (btn.dataset.type === selectedPetType) {
                        btn.classList.add('ring-2', 'ring-offset-2', 'ring-blue-500');
                    } else {
                        btn.classList.remove('ring-2', 'ring-offset-2', 'ring-blue-500');
                    }
                });
            });
        });
    }

    function closeModal() {
        if (petModal) {
            petModal.classList.add("hidden");
            selectedPetType = "";
            currentPetId = null;
            document.getElementById('petNameInput').value = '';
            document.getElementById('petAgeInput').value = '';
            selectedPetTypeText.textContent = "Selected Pet: None";
            petTypeContainer.querySelectorAll('.pet-type-button').forEach(btn => {
                btn.classList.remove('ring-2', 'ring-offset-2', 'ring-blue-500');
            });
        }
    }

    window.closeModal = closeModal;

    if (addNewPetBtn) {
        addNewPetBtn.addEventListener("click", function () {
            modalTitle.textContent = "Add New Pet";
>>>>>>> Stashed changes
            petModal.classList.remove("hidden");
        });
    }

<<<<<<< Updated upstream
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
=======
    if (savePetBtn) {
        savePetBtn.addEventListener("click", function () {
            if (!selectedPetType) {
                alert("Please select a pet type.");
                return;
            }
            
            if (!loggedInUser) {
                alert("Please log in to save a pet.");
                return;
            }
            
            const petName = document.getElementById('petNameInput').value;
            const petAge = document.getElementById('petAgeInput').value;
            
            const petData = {
                type: selectedPetType,
                name: petName,
                age: petAge ? parseInt(petAge) : null
            };
            
            if (currentPetId) {
                petData.id = currentPetId;
            }
            
            fetch(`/api/pets?email=${encodeURIComponent(loggedInUser)}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(petData)
            })
            .then(response => response.json())
            .then(data => {
                if (data.status === 'success') {
                    closeModal();
                    loadPets();
                } else {
                    alert(`Error: ${data.message}`);
                }
>>>>>>> Stashed changes
            })
            .catch(error => {
                console.error("Error saving pet:", error);
                alert("Error saving pet. Please try again.");
            });
        });
    }

<<<<<<< Updated upstream
    // Close modal function
    window.closeModal = function () {
        petModal.classList.add("hidden");
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
=======
   
    const editProfileBtn = document.querySelector(".edit-profile-btn");
    const sitterModal = document.getElementById("sitterModal");

    if (editProfileBtn && sitterModal) {
        editProfileBtn.addEventListener("click", () => {
      
            sitterModal.classList.remove("hidden");
        });
    }

    window.closeSitterModal = function () {
        sitterModal.classList.add("hidden");
    };

    document.getElementById("editSitterForm")?.addEventListener("submit", function (e) {
        e.preventDefault();
        
        if (!loggedInUser) {
            alert("Please log in to update your profile.");
            return;
        }
        
        const profileData = {
            name: document.getElementById("sitterName").value,
            bio: document.getElementById("sitterBio").value,
            phone: document.getElementById("sitterPhone").value
        };
        
        // Send updated profile to server
        fetch(`/api/profile/sitter?email=${encodeURIComponent(loggedInUser)}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                // Add CSRF token if needed
                ...(document.querySelector('meta[name="_csrf"]') ? {
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
                } : {})
            },
            body: JSON.stringify(profileData)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Error updating profile');
            }
            return response.json();
        })
        .then(data => {
            if (data.status === 'success') {
                alert("Profile updated successfully!");
                closeSitterModal();
                loadSitterProfile(); // Reload profile to show updated data
            } else {
                alert("Error updating profile: " + data.message);
            }
        })
        .catch(error => {
            console.error("Error updating profile:", error);
            alert("Error updating profile. Please try again.");
        });
    });

    // 🧠 Owner Edit Modal Logic
    const ownerEditProfileBtn = document.querySelector(".edit-owner-profile-btn");
    const ownerModal = document.getElementById("ownerModal");

    if (ownerEditProfileBtn && ownerModal) {
        ownerEditProfileBtn.addEventListener("click", () => {
            // Modal is pre-filled by loadOwnerProfile
            ownerModal.classList.remove("hidden");
        });
    }

    window.closeOwnerModal = function () {
        ownerModal.classList.add("hidden");
    };

    document.getElementById("editOwnerForm")?.addEventListener("submit", function (e) {
        e.preventDefault();
        
        if (!loggedInUser) {
            alert("Please log in to update your profile.");
            return;
        }
        
        const profileData = {
            name: document.getElementById("ownerName").value,
            address: document.getElementById("ownerBio").value, 
            phone: document.getElementById("ownerPhone").value
        };
        
        // send updated profile to server
        fetch(`/api/profile/owner?email=${encodeURIComponent(loggedInUser)}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                // add CSRF token for cors ussues
                ...(document.querySelector('meta[name="_csrf"]') ? {
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
                } : {})
            },
            body: JSON.stringify(profileData)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Error updating profile');
            }
            return response.json();
        })
        .then(data => {
            if (data.status === 'success') {
                alert("Profile updated successfully!");
                closeOwnerModal();
                loadOwnerProfile(); // Reload profile to show updated data
            } else {
                alert("Error updating profile: " + data.message);
            }
        })
        .catch(error => {
            console.error("Error updating profile:", error);
            alert("Error updating profile. Please try again.");
        });
    });

    // handler for job post form
    const jobPostForm = document.getElementById('jobPostForm');
    if (jobPostForm) {
        jobPostForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            // check if user is logged in
            if (!loggedInUser) {
                console.warn("No user email available, can't post job");
                alert("Please log in to post a job.");
                return;
            }
            
            // collect form data
            const jobData = {
                petType: document.getElementById('jobPetType').value,
                priority: document.getElementById('jobPriority').value,
                startDate: document.getElementById('startDate').value,
                endDate: document.getElementById('endDate').value,
                description: document.getElementById('jobDescription').value,
                notes: document.getElementById('jobNotes').value,
                payRate: document.getElementById('jobPayRate').value,
                paymentMethod: document.getElementById('jobPaymentMethod').value
            };
            
            console.log("Posting job with data:", jobData);
            
            // validate form data
            if (!jobData.petType || !jobData.priority || !jobData.startDate || !jobData.endDate || 
                !jobData.description || !jobData.payRate || !jobData.paymentMethod) {
                alert("Please fill in all required fields.");
                return;
            }
            
            // make sure end date is after start date
            if (new Date(jobData.endDate) <= new Date(jobData.startDate)) {
                alert("End date must be after start date.");
                return;
            }
            
            // mave job to server
            fetch(`/api/jobs?email=${encodeURIComponent(loggedInUser)}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    
                    ...(document.querySelector('meta[name="_csrf"]') ? {
                        'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
                    } : {})
                },
                body: JSON.stringify(jobData)
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok: ' + response.statusText);
                }
                return response.json();
            })
            .then(data => {
                console.log("Job posted successfully:", data);
                // Show success message
                alert("Job posted successfully!");
                // Clear form fields
                jobPostForm.reset();
                // Refresh jobs list
                loadPostedJobs();
            })
            .catch(error => {
                console.error("Error posting job:", error);
                alert("Error posting job. Please try again.");
            });
        });
    }

    // load jobs posted
    function loadPostedJobs() {
        if (!loggedInUser) {
            console.warn("No user email available, can't load jobs");
            return;
        }
        
        // get container element
        const postedJobsList = document.getElementById('postedJobsList');
        if (!postedJobsList) {
            console.warn("Posted jobs list container not found");
            return;
        }
        
        console.log("Loading posted jobs for user:", loggedInUser);
        
        // clear existing jobs
        postedJobsList.innerHTML = '';
        
        // fetch jobs from server
        fetch(`/api/jobs/owner?email=${encodeURIComponent(loggedInUser)}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error fetching jobs: ' + response.statusText);
                }
                return response.json();
            })
            .then(jobs => {
                console.log("Loaded jobs:", jobs);
                
                if (jobs.length === 0) {
                    postedJobsList.innerHTML = '<p class="text-gray-500">You have not posted any jobs yet.</p>';
                    return;
                }
                
                // Add jobs to the list
                jobs.forEach(job => {
                    const jobItem = document.createElement('li');
                    
                    // Set border color based on status
                    let borderColor, statusColor;
                    switch(job.status) {
                        case 'Open':
                            borderColor = 'border-yellow-500';
                            statusColor = 'text-yellow-500';
                            break;
                        case 'Accepted':
                            borderColor = 'border-green-500';
                            statusColor = 'text-green-500';
                            break;
                        case 'Declined':
                            borderColor = 'border-red-500';
                            statusColor = 'text-red-500';
                            break;
                        case 'Cancelled':
                            borderColor = 'border-gray-500';
                            statusColor = 'text-gray-500';
                            break;
                        default:
                            borderColor = 'border-blue-500';
                            statusColor = 'text-blue-500';
                    }
                    
                    jobItem.classList.add('bg-white', 'p-3', 'shadow', 'rounded-lg', 'border-l-4', borderColor, 'flex', 'flex-col', 'h-full');
                    jobItem.dataset.jobId = job.id; // Store job ID for potential future use
                    
                    const shortDescription = job.description.length > 60 ? 
                        job.description.substring(0, 60) + '...' : 
                        job.description;
                    
                    // Format the job info in a more compact way
                    jobItem.innerHTML = `
                        <div class="flex-grow">
                            <div class="flex justify-between items-start">
                                <p class="text-md font-semibold text-gray-800">${job.petType} • ${job.priority}</p>
                                <p class="text-xs text-gray-500">$${job.payRate}</p>
                            </div>
                            <p class="text-xs text-gray-600 mb-1">📅 ${formatDate(job.startDate)} → ${formatDate(job.endDate)}</p>
                            <p class="text-sm mb-2 line-clamp-2" title="${job.description}">${shortDescription}</p>
                            ${job.notes ? `<p class="text-xs italic text-gray-500 line-clamp-1" title="${job.notes}">${job.notes}</p>` : ''}
                            <div class="flex justify-between items-center mt-auto">
                                <p class="text-xs font-medium ${statusColor}">Status: ${job.status}</p>
                                ${job.status === 'Accepted' && job.sitterName ? 
                                `<p class="text-xs font-medium text-green-600">Sitter: ${job.sitterName}</p>` : 
                                ''}
                            </div>
                        </div>
                    `;
                    
                    postedJobsList.appendChild(jobItem);
                });
            })
            .catch(error => {
                console.error("Error loading jobs:", error);
                postedJobsList.innerHTML = '<p class="text-red-500">Error loading jobs. Please refresh the page.</p>';
            });
    }

    // Helper function to format dates nicely
    function formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', { 
            year: 'numeric', 
            month: '2-digit', 
            day: '2-digit' 
        });
    }

    // Function to load open jobs (job requests)
    function loadOpenJobs() {
        if (!loggedInUser) {
            console.warn("No user email available, can't load jobs");
            return;
        }
        
        // Get container element for job requests
        const jobRequestsList = document.getElementById('jobRequestsList');
        if (!jobRequestsList) {
            console.error("Job requests list container not found! Element with ID 'jobRequestsList' is missing in the HTML.");
            return;
        }
        
        console.log("Loading open jobs for sitter to view");
        
        // Clear existing job requests
        jobRequestsList.innerHTML = '';
        
        // Add loading indicator
        jobRequestsList.innerHTML = '<p class="text-gray-500">Loading job requests...</p>';
        
        // Fetch open jobs from server
        fetch(`/api/jobs/open`)
            .then(response => {
                console.log("Open jobs response status:", response.status);
                if (!response.ok) {
                    throw new Error('Error fetching open jobs: ' + response.statusText);
                }
                return response.json();
            })
            .then(jobs => {
                console.log("Loaded open jobs:", jobs);
                
                // Clear loading indicator
                jobRequestsList.innerHTML = '';
                
                if (jobs.length === 0) {
                    jobRequestsList.innerHTML = '<p class="text-gray-500">There are no open job requests available.</p>';
                    return;
                }
                
                // Add jobs to the list
                jobs.forEach(job => {
                    const jobItem = document.createElement('li');
                    jobItem.classList.add('bg-white', 'p-3', 'shadow', 'rounded-lg', 'border-l-4', 'border-yellow-500', 'flex', 'flex-col', 'h-full');
                    jobItem.dataset.jobId = job.id;
                    
                    const shortDescription = job.description.length > 60 ? 
                        job.description.substring(0, 60) + '...' : 
                        job.description;
                    
                    // Format the job info in a more compact way
                    jobItem.innerHTML = `
                        <div class="flex-grow">
                            <div class="flex justify-between items-start">
                                <p class="text-md font-semibold text-gray-800">${job.petType} • ${job.priority}</p>
                                <p class="text-xs text-gray-500">$${job.payRate}</p>
                            </div>
                            <p class="text-xs text-gray-600 mb-1">📅 ${formatDate(job.startDate)} → ${formatDate(job.endDate)}</p>
                            <p class="text-sm mb-2 line-clamp-2" title="${job.description}">${shortDescription}</p>
                        </div>
                        <div class="flex justify-between mt-auto pt-2 border-t border-gray-100">
                            <button class="accept-job-btn px-3 py-1 text-sm bg-green-500 text-white rounded hover:bg-green-600"
                                    data-job-id="${job.id}">Accept</button>
                            <button class="decline-job-btn px-3 py-1 text-sm bg-gray-300 text-gray-700 rounded hover:bg-gray-400"
                                    data-job-id="${job.id}">Decline</button>
                        </div>
                    `;
                    
                    jobRequestsList.appendChild(jobItem);
                });
                
                // Attach event listeners to accept/decline buttons
                attachJobActionButtons();
            })
            .catch(error => {
                console.error("Error loading open jobs:", error);
                jobRequestsList.innerHTML = '<p class="text-red-500">Error loading job requests. Please refresh the page.</p>';
            });
    }
    
    // Function to load jobs accepted by the current sitter
    function loadAcceptedJobs() {
        if (!loggedInUser) {
            console.warn("No user email available, can't load accepted jobs");
            return;
        }
        
        // Get container element for accepted jobs
        const myJobsList = document.getElementById('myJobsList');
        if (!myJobsList) {
            console.warn("My jobs list container not found");
            return;
        }
        
        console.log("Loading accepted jobs for sitter:", loggedInUser);
        
        // Clear existing accepted jobs
        myJobsList.innerHTML = '';
        
        // Fetch accepted jobs from server
        fetch(`/api/jobs/sitter?email=${encodeURIComponent(loggedInUser)}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error fetching accepted jobs: ' + response.statusText);
                }
                return response.json();
            })
            .then(jobs => {
                console.log("Loaded accepted jobs:", jobs);
                
                if (jobs.length === 0) {
                    myJobsList.innerHTML = '<p class="text-gray-500">You have not accepted any jobs yet.</p>';
                    return;
                }
                
                // Add jobs to the list
                jobs.forEach(job => {
                    const jobItem = document.createElement('li');
                    
                    // Set appropriate color based on job status
                    let borderColor, statusColor;
                    switch(job.status) {
                        case 'Accepted':
                            borderColor = 'border-green-500';
                            statusColor = 'text-green-500';
                            break;
                        case 'Cancelled':
                            borderColor = 'border-gray-500';
                            statusColor = 'text-gray-500';
                            break;
                        case 'Open':
                            borderColor = 'border-yellow-500';
                            statusColor = 'text-yellow-500';
                            break;
                        default:
                            borderColor = 'border-blue-500';
                            statusColor = 'text-blue-500';
                    }
                    
                    jobItem.classList.add('bg-white', 'p-3', 'shadow', 'rounded-lg', 'border-l-4', borderColor, 'flex', 'flex-col', 'h-full');
                    jobItem.dataset.jobId = job.id;
                    
                    const shortDescription = job.description.length > 60 ? 
                        job.description.substring(0, 60) + '...' : 
                        job.description;
                    
                    jobItem.innerHTML = `
                        <div class="flex-grow">
                            <div class="flex justify-between items-start">
                                <p class="text-md font-semibold text-gray-800">${job.petType} • ${job.priority}</p>
                                <p class="text-xs text-gray-500">$${job.payRate}</p>
                            </div>
                            <p class="text-xs text-gray-600 mb-1">📅 ${formatDate(job.startDate)} → ${formatDate(job.endDate)}</p>
                            <p class="text-sm mb-2 line-clamp-2" title="${job.description}">${shortDescription}</p>
                            <p class="text-xs font-medium ${statusColor}">Status: ${job.status}</p>
                        </div>
                        <div class="flex justify-end mt-auto pt-2 border-t border-gray-100">
                            <button class="cancel-job-btn px-3 py-1 text-sm bg-red-500 text-white rounded hover:bg-red-600"
                                    data-job-id="${job.id}">Cancel</button>
                        </div>
                    `;
                    
                    myJobsList.appendChild(jobItem);
                });
                
                // Attach event listeners to cancel buttons
                attachCancelJobButtons();
            })
            .catch(error => {
                console.error("Error loading accepted jobs:", error);
                myJobsList.innerHTML = '<p class="text-red-500">Error loading your jobs. Please refresh the page.</p>';
            });
    }
    
    // Function to attach event listeners to accept/decline buttons
    function attachJobActionButtons() {
        // Accept job buttons
        document.querySelectorAll('.accept-job-btn').forEach(button => {
            button.addEventListener('click', function() {
                const jobId = this.dataset.jobId;
                acceptJob(jobId);
            });
        });
        
        // Decline job buttons
        document.querySelectorAll('.decline-job-btn').forEach(button => {
            button.addEventListener('click', function() {
                const jobId = this.dataset.jobId;
                
                // Confirm decline
                if (confirm("Are you sure you want to decline this job request?")) {
                    console.log("Declining job:", jobId);
                    
                    // Call decline endpoint - simply mark as "declined" in the database
                    fetch(`/api/jobs/${jobId}/reject?email=${encodeURIComponent(loggedInUser)}`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            // Add CSRF token if needed
                            ...(document.querySelector('meta[name="_csrf"]') ? {
                                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
                            } : {})
                        }
                    })
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('Network response was not ok');
                        }
                        return response.json();
                    })
                    .then(data => {
                        console.log("Job declined successfully:", data);
                        // Remove the job from the list
                        this.closest('li').remove();
                        // Show success message
                        alert("Job declined successfully!");
                    })
                    .catch(error => {
                        console.error("Error declining job:", error);
                        alert("Error declining job. Please try again.");
                    });
                }
            });
        });
    }
    
    // Function to attach event listeners to cancel buttons
    function attachCancelJobButtons() {
        document.querySelectorAll('.cancel-job-btn').forEach(button => {
            button.addEventListener('click', function() {
                const jobId = this.dataset.jobId;
                cancelJobAcceptance(jobId);
            });
        });
    }
    
    // Function to accept a job
    function acceptJob(jobId) {
        if (!loggedInUser) {
            console.warn("No user email available, can't accept job");
            alert("Please log in to accept jobs.");
            return;
        }
        
        // Show loading state
        const jobItem = document.querySelector(`li[data-job-id="${jobId}"]`);
        if (jobItem) {
            const acceptBtn = jobItem.querySelector('.accept-job-btn');
            if (acceptBtn) {
                acceptBtn.textContent = "Accepting...";
                acceptBtn.disabled = true;
            }
        }
        
        console.log("Accepting job:", jobId);
        
        // Send request to accept job
        fetch(`/api/jobs/${jobId}/accept?email=${encodeURIComponent(loggedInUser)}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                // Add CSRF token if needed
                ...(document.querySelector('meta[name="_csrf"]') ? {
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
                } : {})
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            console.log("Job accepted successfully:", data);
            // Show success message
            alert("Job accepted successfully!");
            // Refresh job lists
            loadOpenJobs();
            loadAcceptedJobs();
        })
        .catch(error => {
            console.error("Error accepting job:", error);
            alert("Error accepting job. Please try again.");
            // Reset button state
            if (jobItem && acceptBtn) {
                acceptBtn.textContent = "Accept";
                acceptBtn.disabled = false;
            }
        });
    }
    
    // Function to cancel job acceptance
    function cancelJobAcceptance(jobId) {
        if (!loggedInUser) {
            console.warn("No user email available, can't cancel job");
            alert("Please log in to cancel jobs.");
            return;
        }
        
        console.log("Canceling job acceptance:", jobId);
        
        // Confirm cancellation
        if (!confirm("Are you sure you want to cancel your acceptance of this job?")) {
            return;
        }
        
        // Show loading state
        const jobItem = document.querySelector(`li[data-job-id="${jobId}"]`);
        if (jobItem) {
            const cancelBtn = jobItem.querySelector('.cancel-job-btn');
            if (cancelBtn) {
                cancelBtn.textContent = "Cancelling...";
                cancelBtn.disabled = true;
            }
        }
        
        // Send request to decline job
        fetch(`/api/jobs/${jobId}/decline?email=${encodeURIComponent(loggedInUser)}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                // Add CSRF token if needed
                ...(document.querySelector('meta[name="_csrf"]') ? {
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
                } : {})
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            console.log("Job canceled successfully:", data);
            // Show success message
            alert("Job canceled successfully!");
            // Refresh job lists
            loadOpenJobs();
            loadAcceptedJobs();
        })
        .catch(error => {
            console.error("Error canceling job:", error);
            alert("Error canceling job. Please try again.");
            // Reset button state
            if (jobItem && cancelBtn) {
                cancelBtn.textContent = "Cancel";
                cancelBtn.disabled = false;
            }
        });
    }

    // Function to load owner profile data
    function loadOwnerProfile() {
        if (!loggedInUser) return;
        
        fetch(`/api/profile/owner?email=${encodeURIComponent(loggedInUser)}`)
            .then(response => {
                if (!response.ok) throw new Error('Error fetching owner profile');
                return response.json();
            })
            .then(data => {
                if (data.status === 'success') {
                    const profile = data.profile;
                    
                    // Update profile display with real data
                    const nameElement = document.querySelector('.bg-white.p-6 h3');
                    if (nameElement) {
                        nameElement.innerHTML = `👤 ${profile.name || 'No Name Set'}`;
                    }
                    
                    const bioElement = document.getElementById('ownerBioDisplay');
                    if (bioElement) {
                        // City is used for the bio on owner profile
                        bioElement.textContent = profile.address ? 
                            `Pet owner based in ${profile.city || 'Unknown Location'}` : 
                            'No address set';
                    }
                    
                    const emailElement = document.getElementById('ownerEmailDisplay');
                    if (emailElement) {
                        emailElement.textContent = profile.email || loggedInUser;
                    }
                    
                    const phoneElement = document.getElementById('ownerPhoneDisplay');
                    if (phoneElement) {
                        phoneElement.textContent = profile.phone || 'No phone number set';
                    }
                    
                    // Pre-fill form fields for editing
                    document.getElementById('ownerName').value = profile.name || '';
                    document.getElementById('ownerBio').value = profile.address || '';
                    document.getElementById('ownerPhone').value = profile.phone || '';
                    document.getElementById('ownerEmail').value = profile.email || loggedInUser;
                }
            })
            .catch(error => {
                console.error('Error loading owner profile:', error);
            });
    }
    
    // Function to load sitter profile data
    function loadSitterProfile() {
        if (!loggedInUser) return;
        
        fetch(`/api/profile/sitter?email=${encodeURIComponent(loggedInUser)}`)
            .then(response => {
                if (!response.ok) throw new Error('Error fetching sitter profile');
                return response.json();
            })
            .then(data => {
                if (data.status === 'success') {
                    const profile = data.profile;
                    
                    // Update profile display with real data
                    const nameElement = document.querySelector('.bg-white.p-6 h3');
                    if (nameElement) {
                        nameElement.textContent = profile.name || 'No Name Set';
                    }
                    
                    const bioElement = document.querySelector('.bg-white.p-6 p.text-gray-500');
                    if (bioElement) {
                        bioElement.textContent = profile.bio || 'No bio set';
                    }
                    
                    const contactElement = document.querySelector('.bg-white.p-6 p.mt-2.text-sm');
                    if (contactElement) {
                        contactElement.innerHTML = `<i class="fas fa-phone"></i> Contact: ${profile.phone || 'No phone'}, ${profile.email || loggedInUser}`;
                    }
                    
                    // Pre-fill form fields for editing
                    document.getElementById('sitterName').value = profile.name || '';
                    document.getElementById('sitterBio').value = profile.bio || '';
                    document.getElementById('sitterPhone').value = profile.phone || '';
                }
            })
            .catch(error => {
                console.error('Error loading sitter profile:', error);
            });
    }
>>>>>>> Stashed changes
});
