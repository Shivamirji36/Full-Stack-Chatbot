document.addEventListener("DOMContentLoaded", () => {

    const form = document.getElementById("loginForm");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

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
                throw new Error(data.error || "Login failed");
            }

            // ✅ Store JWT
            localStorage.setItem("token", data.token);
            localStorage.setItem("userId", data.userId);
            localStorage.setItem("name", data.name);

            window.location.href = "dashboard.html";

        } catch (error) {
            alert(error.message);
        }
    });
});
