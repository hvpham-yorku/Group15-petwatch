document.addEventListener("DOMContentLoaded", function () {
    console.log("Login.js loaded!");

    // For Spring Security form login, we don't need custom JS logic
    // The form will submit to the backend and Spring Security will handle authentication
    
    // Instead, we can just add some client-side validation and debugging
    const loginForms = document.querySelectorAll("form");
    
    if (loginForms) {
        loginForms.forEach(form => {
            form.addEventListener("submit", function (event) {
                // Get the current form fields using the updated IDs
                const email = document.getElementById("email");
                const password = document.getElementById("password");
                
                // Simple validation
                if (!email || !email.value || !password || !password.value) {
                    event.preventDefault(); // Stop form submission
                    alert("Please enter both email and password.");
                    return false;
                }
                
                console.log("Form submitted with email: " + email.value);
                console.log("Password length: " + password.value.length);
                console.log("Form action: " + form.action);
                console.log("Form method: " + form.method);
                
                // Log all form fields for debugging
                const formData = new FormData(form);
                for (let [key, value] of formData.entries()) {
                    if (key !== "password") {
                        console.log(`${key}: ${value}`);
                    } else {
                        console.log(`${key}: [REDACTED]`);
                    }
                }
                
                // Let the form submit naturally to Spring Security endpoint
                return true;
            });
        });
    } else {
        console.error("No login form found!");
    }
});
