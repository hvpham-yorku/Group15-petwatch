document.addEventListener("DOMContentLoaded", function () {
    console.log("Dashboard.js loaded!");

    // Check if user is logged in
    const loggedInUser = localStorage.getItem("loggedInUser");
    const userType = localStorage.getItem("userType");

    if (!loggedInUser) {
        window.location.href = "login-choice.html"; // Redirect to login if not authenticated
    }

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
    
    let selectedPetType = "";
    let editingPet = null;

    // Show correct dashboard based on user type
    if (userType === "owner") {
        console.log("Showing Owner Dashboard");
        ownerDashboard.classList.remove("hidden");
        ownerBtn.classList.remove("hidden");
    } else if (userType === "sitter") {
        console.log("Showing Sitter Dashboard");
        sitterDashboard.classList.remove("hidden");
        sitterBtn.classList.remove("hidden");
    } else {
        console.log("Invalid userType! Redirecting to login.");
        window.location.href = "login-choice.html"; // Redirect if userType is missing
    }

    // Logout functionality
    function attachLogoutListener() {
        const logoutBtn = document.getElementById("logoutBtn");
        if (logoutBtn) {
            logoutBtn.addEventListener("click", function () {
                console.log("Logging out...");
                localStorage.removeItem("loggedInUser");
                localStorage.removeItem("userType");
                window.location.href = "login-choice.html";
            });
        } else {
            console.error("Logout button not found! Retrying...");
            setTimeout(attachLogoutListener, 1000);
        }
    }
    attachLogoutListener();

    // Owner: Open modal for adding a new pet
    if (addNewPetBtn) {
        addNewPetBtn.addEventListener("click", function () {
            console.log("Add New Pet Clicked!");
            modalTitle.innerText = "Add New Pet";
            selectedPetType = "";
            petModal.classList.remove("hidden");
        });
    }

    // Owner: Select pet type
    function attachPetTypeButtons() {
        document.querySelectorAll(".pet-type-button").forEach((btn) => {
            btn.addEventListener("click", function () {
                selectedPetType = this.getAttribute("data-type");
                console.log("Selected Pet Type: " + selectedPetType);
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
            
            console.log("Adding New Pet...");
            const newPetCard = document.createElement("div");
            newPetCard.classList.add("bg-white", "p-4", "shadow", "rounded-lg");
            newPetCard.innerHTML = `
                <h3 class="text-lg font-bold text-center pet-name">${selectedPetType}</h3>
                <p class="text-sm text-gray-500 text-center pet-breed">Type: ${selectedPetType}</p>
                <button class="edit-pet w-full mt-4 px-4 py-2 bg-blue-500 text-white rounded">Edit Pet</button>
            `;
            petContainer.appendChild(newPetCard);
            attachEditButtons();
            closeModal();
        });
    }

    // Close modal function
    window.closeModal = function () {
        petModal.classList.add("hidden");
    };
});
