const API_BASE = "https://full-stack-chatbot-3.onrender.com";

document.addEventListener("DOMContentLoaded", () => {

    console.log("login.js loaded");

    const form = document.getElementById("loginForm");
    const errorLabel = document.getElementById("loginError");
    const emailInput = document.getElementById("email");
    const passwordInput = document.getElementById("password");

    // 🔥 HARD GUARDS (very important)
    if (!form) {
        console.error("❌ loginForm not found");
        return;
    }
    if (!emailInput) {
        console.error("❌ email input not found");
        return;
    }
    if (!passwordInput) {
        console.error("❌ password input not found");
        return;
    }

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        errorLabel.textContent = "";

        const email = emailInput.value.trim();
        const password = passwordInput.value;

        // 🔍 LOG WHAT IS ACTUALLY BEING SENT
        console.log("LOGIN PAYLOAD →", { email, password });

        // ❗ Prevent invalid requests (avoids 400)
        if (!email || !password) {
            errorLabel.textContent = "Email and password are required";
            return;
        }

        try {
            const response = await fetch(`${API_BASE}/api/auth/login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    email: email,
                    password: password
                })
            });

            const text = await response.text();
            let data = {};

            if (text) {
                data = JSON.parse(text);
            }

            console.log("LOGIN RESPONSE STATUS:", response.status);
            console.log("LOGIN RESPONSE BODY:", data);

            if (!response.ok) {
                // 🔥 Show backend validation / auth error
                if (data.error) {
                    errorLabel.textContent = data.error;
                } else {
                    errorLabel.textContent = "Login failed. Check credentials.";
                }
                return;
            }

            // ✅ Store JWT and user info
            localStorage.setItem("token", data.token);
            localStorage.setItem("userId", data.userId);
            localStorage.setItem("name", data.name);

            console.log("✅ Login successful, redirecting...");

            // ✅ Redirect to dashboard
            window.location.href = "dashboard.html";

        } catch (error) {
            console.error("LOGIN ERROR:", error);
            errorLabel.textContent = "Something went wrong. Please try again.";
        }
    });

    // Optional UX cleanup
    emailInput.addEventListener("input", () => {
        errorLabel.textContent = "";
    });
    passwordInput.addEventListener("input", () => {
        errorLabel.textContent = "";
    });
});
