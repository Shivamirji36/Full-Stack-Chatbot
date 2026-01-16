const API_BASE = "https://full-stack-chatbot-3.onrender.com";

document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("token");

    if (!token) {
        window.location.href = "login.html";
        return;
    }

    loadProjects();
});


async function loadProjects() {
    const container = document.getElementById("projectsContainer");
    container.innerHTML = "<p>Loading projects...</p>";

    try {
        const res = await fetch(`${API_BASE}/api/projects`, {
            headers: {
                "Authorization": "Bearer " + localStorage.getItem("token")
            }
        });

        if (!res.ok) {
            console.error("Load projects failed:", res.status);
            container.innerHTML = "<p>Failed to load projects</p>";
            return;
        }

        const projects = await res.json();
        container.innerHTML = "";

        if (projects.length === 0) {
            container.innerHTML = "<p>No projects yet</p>";
            return;
        }

        projects.forEach(project => {
            const card = document.createElement("div");
            card.className = "project-card";
            card.innerHTML = `
                <h4 class = "project-name">${project.name}</h4>
                <p class = "project-name">${project.model || ""}</p>
            `;

            card.addEventListener("click", () => openProject(project));
            container.appendChild(card);
        });

    } catch (err) {
        console.error("Failed to load projects", err);
        container.innerHTML = "<p>Error loading projects</p>";
    }
}

async function createProject() {
    const nameInput = document.getElementById("projectName");
    const promptInput = document.getElementById("systemPrompt");

    const name = nameInput.value.trim();
    const systemPrompt = promptInput.value.trim();

    if (!name) {
        alert("Project name required");
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/api/projects`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + localStorage.getItem("token")
            },
            body: JSON.stringify({ name, systemPrompt })
        });

        if (!res.ok) {
            alert("Failed to create project");
            return;
        }

        nameInput.value = "";
        promptInput.value = "";
        loadProjects();

    } catch (err) {
        console.error("Create project failed", err);
    }
}

function openProject(project) {
    localStorage.setItem("currentProject", JSON.stringify(project));
    window.location.href = "chat.html";
}

function logout() {
    localStorage.clear();
    window.location.href = "login.html";
}
