const API_BASE = "https://full-stack-chatbot-3.onrender.com";

document.getElementById("signupForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    const firstName = document.getElementById("firstName").value.trim();
    const lastName = document.getElementById("lastName").value.trim();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;

    const name = `${firstName} ${lastName}`;

    try {
        const response = await fetch(`${API_BASE}/api/auth/signup`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ name, email, password })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || "Registration failed");
        }

        // Success → redirect to login
        alert("Signup successful! Please login.");
        window.location.href = "login.html";

    } catch (error) {
        alert(error.message);
    }
});
