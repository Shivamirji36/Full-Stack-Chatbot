const API_BASE = "https://full-stack-chatbot-3.onrender.com";

document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("token");
    const projectRaw = localStorage.getItem("currentProject");

    console.log("JWT Token:", token);
    console.log("Project Raw:", projectRaw);

    if (!token) {
        alert("Please login again.");
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

    document.getElementById("projectName").textContent = project.name;

    // ✅ Wire Send button
    const sendBtn = document.getElementById("sendBtn");
    if (sendBtn) {
        sendBtn.addEventListener("click", sendMessage);
    } else {
        console.error("Send button not found");
    }

    // ✅ Enter key sends message (inside input only)
    const input = document.getElementById("messageInput");
    input.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            sendMessage();
        }
    });

    loadChatHistory(project.id);
});

/* ===========================
   LOAD CHAT HISTORY
=========================== */
async function loadChatHistory(projectId) {
    const chatBody = document.getElementById("chatBody");
    chatBody.innerHTML = "";

    try {
        const res = await fetch(
            `${API_BASE}/api/chat/${projectId}/history`,
            {
                headers: {
                    "Authorization": "Bearer " + localStorage.getItem("token"),
                    "Content-Type": "application/json"
                }
            }
        );

        if (!res.ok) {
            console.error("Chat history load failed:", res.status);
            renderMessage("assistant", "⚠️ Unable to load chat history.");
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

/* ===========================
   SEND MESSAGE
=========================== */
async function sendMessage() {
    const input = document.getElementById("messageInput");
    const text = input.value.trim();
    if (!text) return;

    const project = JSON.parse(localStorage.getItem("currentProject"));

    renderMessage("user", text);
    input.value = "";

    try {
        const res = await fetch(
            `${API_BASE}/api/chat/${project.id}`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + localStorage.getItem("token")
                },
                body: JSON.stringify({ message: text })
            }
        );

        if (!res.ok) {
            console.error("Chat send failed:", res.status);
            renderMessage("assistant", "⚠️ Message failed.");
            return;
        }

        const data = await res.json();
        renderMessage("assistant", data.response);

    } catch (err) {
        console.error("Chat error:", err);
        renderMessage("assistant", "⚠️ AI service unavailable.");
    }
}

/* ===========================
   UI HELPERS
=========================== */
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

function goBack() {
    window.location.href = "dashboard.html";
}
