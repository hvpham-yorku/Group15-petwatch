document.addEventListener("DOMContentLoaded", function () {
    console.log("Login.js loaded!");

    const loginButtons = document.querySelectorAll(".action-button");

    if (loginButtons) {
        loginButtons.forEach(button => {
            button.addEventListener("click", function (event) {
                event.preventDefault(); // Prevent default form submission

                const email = document.getElementById("email").value;
                const password = document.getElementById("password").value;

                console.log("🔍 Entered Email:", email, " Password:", password);

                if (!email || !password) {
                    alert("Please enter both email and password.");
                    return;
                }

                // Determine user type
                let userType;
                if (window.location.pathname.includes("login-owner")) {
                    userType = "owner";
                } else if (window.location.pathname.includes("login-sitter")) {
                    userType = "sitter";
                } else {
                    alert("Invalid login role.");
                    return;
                }

                console.log("🔍 Detected user type:", userType);

                // Store user info
                localStorage.setItem("loggedInUser", email);
                localStorage.setItem("userType", userType);

                // Redirect based on role
                if (userType === "owner") {
                    window.location.href = "dashboard-owner.html";
                } else if (userType === "sitter") {
                    window.location.href = "dashboard-sitter.html";
                }
            });
        });
    } else {
        console.error("No login buttons found!");
    }
});
