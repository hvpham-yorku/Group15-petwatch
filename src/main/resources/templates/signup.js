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

        // Collect form data
        const formData = {
            firstName: document.getElementById("firstName").value,
            email: document.getElementById("email").value,
            password: document.getElementById("password").value,
            phoneNumber: document.getElementById("phoneNumber").value,
            country: document.getElementById("country").value,
            state: document.getElementById("stateProvince").value,
            city: document.getElementById("city").value,
            address: document.getElementById("address").value,
            postalCode: document.getElementById("postalCode").value,
        };

        console.log("📤 Sending data:", formData);

        // Send data to the backend
        fetch("http://localhost:5000/api/signup", {
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
            window.location.href = "login-choice.html"; // Redirect to login page
        })
        .catch(error => {
            console.error("Error signing up:", error);
            alert("Error signing up. Please try again.");
        });
    });
});
