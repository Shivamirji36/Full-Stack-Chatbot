const API_BASE = "https://full-stack-chatbot-3.onrender.com";

document.getElementById("signupForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    const firstName = document.getElementById("firstName").value.trim();
    const lastName = document.getElementById("lastName").value.trim();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;

    const name = `${firstName} ${lastName}`;

    try {
        const response = await fetch(`${API_BASE}/api/auth/register`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ name, email, password })
        });

        // ✅ SAFELY READ RESPONSE
        const text = await response.text();
        let data = {};
        if (text) {
            data = JSON.parse(text);
        }

        if (!response.ok) {
            throw new Error(data.error || `Signup failed (${response.status})`);
        }

        alert("Signup successful! Please login.");
        window.location.href = "login.html";

    } catch (error) {
        console.error("Signup error:", error);
        alert(error.message || "Something went wrong");
    }
});
