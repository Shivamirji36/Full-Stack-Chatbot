const API_BASE = "https://full-stack-chatbot-3.onrender.com";


document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("token");
    const projectRaw = localStorage.getItem("currentProject");

    // 🔍 DEBUG (keep for now)
    console.log("JWT Token:", token);
    console.log("Project Raw:", projectRaw);

    if (!token) {
        alert("Session expired. Please login again.");
        redirectToLogin();
        return;
    }

    if (!projectRaw) {
        alert("No project selected.");
        redirectToDashboard();
        return;
    }

    let project;
    try {
        project = JSON.parse(projectRaw);
    } catch (e) {
        console.error("Invalid project data", e);
        redirectToDashboard();
        return;
    }

    if (!project.id || !project.name) {
        alert("Invalid project data.");
        redirectToDashboard();
        return;
    }

    // ✅ Set project name dynamically
    document.getElementById("projectName").textContent = project.name;

    // ✅ Load messages
    loadChatHistory(project.id);
});


async function loadChatHistory(projectId) {
    const chatBody = document.getElementById("chatBody");
    chatBody.innerHTML = "";

    try {
        const res = await fetch(`${API_BASE}/chat/${projectId}/history`, {
            headers: {
                "Authorization": "Bearer " + localStorage.getItem("token")
            }
        });

        if (res.status === 401 || res.status === 403) {
            alert("Unauthorized. Please login again.");
            redirectToLogin();
            return;
        }

        const messages = await res.json();

        messages.forEach(msg => {
            renderMessage(msg.role, msg.content);
        });

        chatBody.scrollTop = chatBody.scrollHeight;

    } catch (err) {
        console.error("Failed to load chat history", err);
        renderMessage("assistant", "⚠️ Failed to load previous messages.");
    }
}


async function sendMessage() {
    const input = document.getElementById("messageInput");
    const text = input.value.trim();
    if (!text) return;

    const project = JSON.parse(localStorage.getItem("currentProject"));

    renderMessage("user", text);
    input.value = "";

    try {
        const res = await fetch(`${API_BASE}/chat/${project.id}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + localStorage.getItem("token")
            },
            body: JSON.stringify({ message: text })
        });

        if (res.status === 401 || res.status === 403) {
            alert("Session expired.");
            redirectToLogin();
            return;
        }

        const data = await res.json();
        renderMessage("assistant", data.response);

    } catch (err) {
        console.error("Chat error:", err);
        renderMessage("assistant", "⚠️ AI service unavailable.");
    }
}


function renderMessage(role, text) {
    const chatBody = document.getElementById("chatBody");

    const div = document.createElement("div");
    div.className = role === "user" ? "message user" : "message bot";
    div.textContent = text;

    chatBody.appendChild(div);
    chatBody.scrollTop = chatBody.scrollHeight;
}

function logout() {
    localStorage.clear();
    redirectToLogin();
}

function redirectToLogin() {
    window.location.href = "login.html";
}

function redirectToDashboard() {
    window.location.href = "dashboard.html";
}


document.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
        sendMessage();
    }
});
