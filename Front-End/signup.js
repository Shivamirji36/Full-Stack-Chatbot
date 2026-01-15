const API_BASE = "https://full-stack-chatbot-3.onrender.com";

document.addEventListener("DOMContentLoaded", () => {

    const form = document.getElementById("signupForm");

    if (!form) {
        console.error("❌ signupForm not found in DOM");
        return;
    }

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        console.log("✅ Signup form submitted");

        const firstName = document.getElementById("firstName")?.value.trim();
        const lastName = document.getElementById("lastName")?.value.trim();
        const email = document.getElementById("email")?.value.trim();
        const password = document.getElementById("password")?.value;

        const name = `${firstName} ${lastName}`;

        try {
            const response = await fetch(`${API_BASE}/api/auth/register`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ name, email, password })
            });

            const text = await response.text();
            let data = {};
            if (text) {
                data = JSON.parse(text);
            }

            console.log("📡 Response:", response.status, data);

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
});
