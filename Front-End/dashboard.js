const API_BASE = "https://full-stack-chatbot-3.onrender.com";

document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("token");

    console.log("TOKEN ON DASHBOARD LOAD:", token);

    if (!token) {
        window.location.href = "login.html";
        return;
    }

    loadProjects();
});

/* ===========================
   LOAD PROJECTS
=========================== */
async function loadProjects() {
    const container = document.getElementById("projectsContainer");

    if (!container) {
        console.error("projectsContainer not found");
        return;
    }

    container.innerHTML = "<p>Loading projects...</p>";

    try {
        const res = await fetch(`${API_BASE}/projects`, {
            headers: {
                "Authorization": `Bearer ${localStorage.getItem("token")}`,
                "Content-Type": "application/json"
            }
        });

        // 🚫 DO NOT AUTO-LOGOUT
        if (res.status === 401 || res.status === 403) {
            console.warn("Unauthorized when loading projects (token kept)");
            container.innerHTML =
                "<p>Unable to load projects (unauthorized).</p>";
            return;
        }

        if (!res.ok) {
            console.error("Projects API failed:", res.status);
            container.innerHTML =
                "<p>Failed to load projects.</p>";
            return;
        }

        const projects = await res.json();

        container.innerHTML = "";

        if (!projects || projects.length === 0) {
            container.innerHTML = "<p>No projects yet</p>";
            return;
        }

        projects.forEach(project => {
            const card = document.createElement("div");
            card.className = "project-card";
            card.innerHTML = `
                <h4>${project.name}</h4>
                <p>${project.model || ""}</p>
            `;

            card.addEventListener("click", () => openProject(project));
            container.appendChild(card);
        });

    } catch (err) {
        console.error("Failed to load projects", err);
        container.innerHTML = "<p>Error loading projects</p>";
    }
}

/* ===========================
   OPEN PROJECT
=========================== */
function openProject(project) {
    console.log("Selected project:", project);
    localStorage.setItem("currentProject", JSON.stringify(project));
    window.location.href = "chat.html";
}

/* ===========================
   CREATE PROJECT
=========================== */
async function createProject() {
    const nameInput = document.getElementById("projectName");
    const promptInput = document.getElementById("systemPrompt");

    const name = nameInput?.value.trim();
    const systemPrompt = promptInput?.value.trim();

    if (!name) {
        alert("Project name required");
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/projects`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${localStorage.getItem("token")}`
            },
            body: JSON.stringify({ name, systemPrompt })
        });

        if (res.status === 401 || res.status === 403) {
            alert("Not authorized to create project");
            return;
        }

        if (!res.ok) {
            alert("Failed to create project");
            return;
        }

        nameInput.value = "";
        promptInput.value = "";

        loadProjects();

    } catch (err) {
        console.error("Create project failed", err);
        alert("Error creating project");
    }
}

/* ===========================
   LOGOUT (USER ACTION ONLY)
=========================== */
function logout() {
    localStorage.clear();
    window.location.href = "login.html";
}
