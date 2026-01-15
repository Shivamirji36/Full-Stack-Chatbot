document.addEventListener("DOMContentLoaded", () => {

    const form = document.getElementById("loginForm");
    const errorLabel = document.getElementById("loginError");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        // Clear previous error
        errorLabel.textContent = "";

        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;

        try {
            const response = await fetch("http://localhost:8080/api/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ email, password })
            });

            let data = {};
            const text = await response.text();
            if (text) {
                data = JSON.parse(text);
            }

            if (!response.ok) {
                // ❌ Invalid credentials
                errorLabel.textContent = "Invalid email/password";
                return;
            }

            // ✅ Store JWT and user info
            localStorage.setItem("token", data.token);
            localStorage.setItem("userId", data.userId);
            localStorage.setItem("name", data.name);

            // ✅ Redirect to dashboard
            window.location.href = "dashboard.html";

        } catch (error) {
            console.error(error);
            errorLabel.textContent = "Something went wrong. Please try again.";
        }
    });

    // Optional: clear error while typing
    document.getElementById("email").addEventListener("input", () => {
        errorLabel.textContent = "";
    });
    document.getElementById("password").addEventListener("input", () => {
        errorLabel.textContent = "";
    });
});
