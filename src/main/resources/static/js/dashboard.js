document.addEventListener("DOMContentLoaded", function () {
    console.log("Dashboard.js loaded!");

    // Check if user is logged in
    fetch('/api/check-auth')
        .then(response => {
            if (!response.ok) {
                window.location.href = "/login-choice";
            }
        })
        .catch(() => window.location.href = "/login-choice");

    const ownerDashboard = document.getElementById("ownerDashboard");
    const sitterDashboard = document.getElementById("sitterDashboard");
    const ownerBtn = document.getElementById("ownerBtn");
    const sitterBtn = document.getElementById("sitterBtn");
    const logoutBtn = document.getElementById("logoutBtn");

    const addNewPetBtn = document.querySelector(".add-new-pet");
    const petContainer = document.querySelector(".pet-container");
    const requestList = document.getElementById("request-list");

    const petModal = document.getElementById("petModal");
    const modalTitle = document.getElementById("modalTitle");
    const savePetBtn = document.getElementById("savePet");
    const petTypeContainer = document.getElementById("petTypeContainer");
    const selectedPetTypeText = document.getElementById("selectedPetTypeText");

    let selectedPetType = "";
    let editingPet = null;
    let currentPetId = null;
    let loggedInUser = "";

    // Determine which dashboard to show based on the URL path
    const currentPath = window.location.pathname;

    fetch('/api/current-user')
        .then(response => response.json())
        .then(data => {
            loggedInUser = data.email;
            console.log("Current user:", loggedInUser);
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
        })
        .catch(console.error);

    function loadPets() {
        if (!loggedInUser) return;
        
        // Add null check for petContainer
        if (!petContainer) {
            console.warn("Pet container element not found");
            return;
        }

        const existingPets = petContainer.querySelectorAll("div:not(:first-child)");
        existingPets.forEach(pet => pet.remove());

        fetch(`/api/pets?email=${encodeURIComponent(loggedInUser)}`)
            .then(response => {
                if (!response.ok) throw new Error('Fetch error');
                return response.json();
            })
            .then(pets => {
                pets.forEach(pet => {
                    const petId = pet.id ? parseInt(pet.id, 10) : '';
                    const newPetCard = document.createElement("div");
                    newPetCard.classList.add("bg-white", "p-4", "shadow", "rounded-lg");
                    newPetCard.dataset.petId = petId;
                    newPetCard.dataset.petType = pet.type;

                    newPetCard.innerHTML = `
                        <h3 class="text-lg font-bold text-center pet-name">${pet.type}</h3>
                        <button class="edit-pet w-full mt-4 px-4 py-2 bg-blue-500 text-white rounded">Edit Pet</button>
                    `;
                    petContainer.appendChild(newPetCard);
                });
                attachEditButtons();
            })
            .catch(console.error);
    }

    if (currentPath.includes('dashboard-owner')) {
        console.log("Showing Owner Dashboard");
        if (ownerDashboard) ownerDashboard.classList.remove("hidden");
        if (ownerBtn) ownerBtn.classList.remove("hidden");
        // Don't load jobs here - we'll do it after user is fetched
    } else if (currentPath.includes('dashboard-sitter')) {
        console.log("Showing Sitter Dashboard");
        if (sitterDashboard) sitterDashboard.classList.remove("hidden");
        if (sitterBtn) sitterBtn.classList.remove("hidden");
    } else {
        window.location.href = "/login-choice";
    }

    function attachLogoutListener() {
        const logoutBtn = document.getElementById("logoutBtn");
        if (logoutBtn) {
            logoutBtn.addEventListener("click", () => {
                const logoutForm = document.getElementById("logoutForm");
                logoutForm ? logoutForm.submit() : window.location.href = "/perform-logout";
            });
        } else {
            setTimeout(attachLogoutListener, 1000);
        }
    }
    attachLogoutListener();

    function resetPetForm(isEditing = false, petType = null, petId = null) {
        modalTitle.innerText = isEditing ? "Edit Pet" : "Add New Pet";
        currentPetId = petId;
        selectedPetType = petType || "";
        selectedPetTypeText.innerText = selectedPetType ? `Selected Pet: ${selectedPetType}` : "Selected Pet: None";

        document.querySelectorAll(".pet-type-button").forEach((btn) => {
            btn.classList.remove("ring-4", "ring-blue-300");
            if (btn.getAttribute("data-type") === selectedPetType) {
                btn.classList.add("ring-4", "ring-blue-300");
            }
        });
    }

    if (addNewPetBtn) {
        addNewPetBtn.addEventListener("click", () => {
            resetPetForm(false);
            petModal.classList.remove("hidden");
        });
    }

    function attachPetTypeButtons() {
        document.querySelectorAll(".pet-type-button").forEach((btn) => {
            btn.addEventListener("click", function () {
                selectedPetType = this.getAttribute("data-type");
                selectedPetTypeText.innerText = "Selected Pet: " + selectedPetType;

                document.querySelectorAll(".pet-type-button").forEach((b) => {
                    b.classList.remove("ring-4", "ring-blue-300");
                });
                this.classList.add("ring-4", "ring-blue-300");
            });
        });
    }
    attachPetTypeButtons();

    if (savePetBtn) {
        savePetBtn.addEventListener("click", function () {
            if (!selectedPetType) return alert("Select a pet type");

            const petData = {
                type: selectedPetType,
                id: currentPetId ? String(currentPetId) : null
            };

            fetch(`/api/pets?email=${encodeURIComponent(loggedInUser)}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...(document.querySelector('meta[name="_csrf"]') ? {
                        'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
                    } : {})
                },
                body: JSON.stringify(petData)
            })
                .then(response => response.ok ? response.json() : Promise.reject(response.statusText))
                .then(() => {
                    loadPets();
                    closeModal();
                })
                .catch(err => {
                    console.error(err);
                    alert("Error saving pet");
                });
        });
    }

    window.closeModal = function () {
        petModal.classList.add("hidden");
    };

    function attachEditButtons() {
        document.querySelectorAll(".edit-pet").forEach((btn) => {
            btn.addEventListener("click", function() {
                const petCard = this.closest("div");
                const petType = petCard.dataset.petType;
                const petId = parseInt(petCard.dataset.petId, 10);
                if (isNaN(petId)) return alert("Invalid pet ID");

                editingPet = petCard;
                resetPetForm(true, petType, petId);
                petModal.classList.remove("hidden");
            });
        });
    }

    // 🧠 Sitter Edit Modal Logic
    const editProfileBtn = document.querySelector(".edit-profile-btn");
    const sitterModal = document.getElementById("sitterModal");

    if (editProfileBtn && sitterModal) {
        editProfileBtn.addEventListener("click", () => {
            // Modal is pre-filled by loadSitterProfile
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
            address: document.getElementById("ownerBio").value, // Using bio field for address in owner
            phone: document.getElementById("ownerPhone").value
        };
        
        // Send updated profile to server
        fetch(`/api/profile/owner?email=${encodeURIComponent(loggedInUser)}`, {
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

    // Handler for job post form
    const jobPostForm = document.getElementById('jobPostForm');
    if (jobPostForm) {
        jobPostForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            // Check if user is logged in
            if (!loggedInUser) {
                console.warn("No user email available, can't post job");
                alert("Please log in to post a job.");
                return;
            }
            
            // Collect form data
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
            
            // Validate form data
            if (!jobData.petType || !jobData.priority || !jobData.startDate || !jobData.endDate || 
                !jobData.description || !jobData.payRate || !jobData.paymentMethod) {
                alert("Please fill in all required fields.");
                return;
            }
            
            // Make sure end date is after start date
            if (new Date(jobData.endDate) <= new Date(jobData.startDate)) {
                alert("End date must be after start date.");
                return;
            }
            
            // Save job to server
            fetch(`/api/jobs?email=${encodeURIComponent(loggedInUser)}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    // Add CSRF token if needed
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

    // Function to load jobs posted by the owner
    function loadPostedJobs() {
        if (!loggedInUser) {
            console.warn("No user email available, can't load jobs");
            return;
        }
        
        // Get container element
        const postedJobsList = document.getElementById('postedJobsList');
        if (!postedJobsList) {
            console.warn("Posted jobs list container not found");
            return;
        }
        
        console.log("Loading posted jobs for user:", loggedInUser);
        
        // Clear existing jobs
        postedJobsList.innerHTML = '';
        
        // Fetch jobs from server
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
                    
                    // Truncate description if it's too long
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
                    
                    // Truncate description if it's too long
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
                    
                    // Truncate description if it's too long
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
});
