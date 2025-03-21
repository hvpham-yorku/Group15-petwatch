document.addEventListener("DOMContentLoaded", function () {
    console.log("Signup.js loaded!");

    const signupForm = document.getElementById("signupForm");

    if (!signupForm) {
        console.error("Signup form not found! Make sure it has id='signupForm'");
        return;
    }

    signupForm.addEventListener("submit", function (event) {
        event.preventDefault(); // Prevent default form submission

        console.log("🔄 Form submitted! Sending data...");

        // Determine if this is a pet sitter signup based on the URL path
        const isPetSitterSignup = window.location.pathname.includes("signup-sitter");
        
        // Collect form data
        const formData = {
            firstName: document.getElementById("firstName")?.value || "",
            email: document.getElementById("email")?.value || "",
            password: document.getElementById("password")?.value || "",
            phoneNumber: document.getElementById("phoneNumber")?.value || "",
            // Include role based on the current page
            role: isPetSitterSignup ? "employee" : "owner",
        };
        
        // Add pet owner specific fields if present
        if (document.getElementById("country")) {
            formData.country = document.getElementById("country").value;
        }
        
        if (document.getElementById("stateProvince")) {
            formData.state = document.getElementById("stateProvince").value;
        }
        
        if (document.getElementById("city")) {
            formData.city = document.getElementById("city").value;
        }
        
        if (document.getElementById("address")) {
            formData.address = document.getElementById("address").value;
        }
        
        if (document.getElementById("postalCode")) {
            formData.postalCode = document.getElementById("postalCode").value;
        }
        
        // Add pet sitter specific fields if present
        if (document.getElementById("experience")) {
            formData.experience = document.getElementById("experience").value;
        }
        
        if (document.getElementById("bio")) {
            formData.bio = document.getElementById("bio").value;
        }

        console.log("📤 Sending data:", formData);

        // Send data to the backend
        fetch("/api/signup", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(formData),
        })
        .then(response => {
            if (!response.ok) {
                throw new Error("Signup failed");
            }
            return response.json();
        })
        .then(data => {
            console.log("Signup successful:", data);
            alert("Signup successful! Redirecting to login...");
            window.location.href = "/login-choice"; // Redirect to login page
        })
        .catch(error => {
            console.error("Error signing up:", error);
            alert("Error signing up. Please try again.");
        });
    });
});
