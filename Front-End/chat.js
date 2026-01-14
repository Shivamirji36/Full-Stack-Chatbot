function goBack() {
    window.location.href = "./dashboard.html";
}

function sendMessage() {
    const input = document.getElementById("messageInput");
    const chatBody = document.getElementById("chatBody");

    const text = input.value.trim();
    if (!text) return;

    // User message
    const userMsg = document.createElement("div");
    userMsg.className = "message user";
    userMsg.textContent = text;
    chatBody.appendChild(userMsg);

    input.value = "";
    chatBody.scrollTop = chatBody.scrollHeight;

    // Fake bot reply (placeholder)
    setTimeout(() => {
        const botMsg = document.createElement("div");
        botMsg.className = "message bot";
        botMsg.textContent = "Thanks for your message. I’ll help you with that.";
        chatBody.appendChild(botMsg);
        chatBody.scrollTop = chatBody.scrollHeight;
    }, 500);
}

/* 🔑 Keyboard handling (works on Windows + macOS) */
document.addEventListener("DOMContentLoaded", () => {
    const input = document.getElementById("messageInput");

    input.addEventListener("keydown", (event) => {
        if (event.key === "Enter") {
            event.preventDefault(); // stops form submit / page reload
            sendMessage();
        }
    });
});
