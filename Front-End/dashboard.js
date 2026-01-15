const API_BASE = "http://localhost:8080/api";

document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("token");

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
    container.innerHTML = "<p>Loading projects...</p>";

    try {
        const res = await fetch(`${API_BASE}/projects`, {
            headers: {
                "Authorization": "Bearer " + localStorage.getItem("token")
            }
        });

        if (res.status === 401 || res.status === 403) {
            logout();
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
                <h4>${project.name}</h4>
                <p>${project.model}</p>
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

    const name = nameInput.value.trim();
    const systemPrompt = promptInput.value.trim();

    if (!name) {
        alert("Project name required");
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/projects`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + localStorage.getItem("token")
            },
            body: JSON.stringify({
                name,
                systemPrompt
            })
        });

        if (res.status === 401 || res.status === 403) {
            logout();
            return;
        }

        nameInput.value = "";
        promptInput.value = "";

        loadProjects(); // 🔥 refresh list

    } catch (err) {
        console.error("Create project failed", err);
    }
}

function logout() {
    localStorage.clear();
    window.location.href = "login.html";
}
