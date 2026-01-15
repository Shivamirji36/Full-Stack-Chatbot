
const token = localStorage.getItem("token");

if (!token) {
    window.location.href = "login.html";
}

const projectsList = document.getElementById("projectsList");
const createBtn = document.getElementById("createProjectBtn");
const projectNameInput = document.getElementById("projectName");
const systemPromptInput = document.getElementById("systemPrompt");
const logoutBtn = document.querySelector(".logout-btn");

document.addEventListener("DOMContentLoaded", loadProjects);

async function loadProjects() {
    try {
        const response = await fetch("http://localhost:8080/api/projects", {
            method: "GET",
            headers: {
                "Authorization": "Bearer " + token
            }
        });

        if (response.status === 401) {
            logout();
            return;
        }

        const projects = await response.json();
        renderProjects(projects);

    } catch (error) {
        alert("Failed to load projects");
        console.error(error);
    }
}

function renderProjects(projects) {
    projectsList.innerHTML = "";

    if (!projects || projects.length === 0) {
        projectsList.innerHTML = "<p>No projects yet.</p>";
        return;
    }

    projects.forEach(project => {
        const div = document.createElement("div");
        div.className = "project-item";

        div.innerHTML = `
            <span>${project.name}</span>
            <span>→</span>
        `;

        div.addEventListener("click", () => {
            localStorage.setItem("currentProjectId", project.id);
            window.location.href = "chat.html";
        });

        projectsList.appendChild(div);
    });
}

createBtn.addEventListener("click", async () => {
    const name = projectNameInput.value.trim();
    const systemPrompt = systemPromptInput.value.trim();

    if (!name) {
        alert("Project name is required");
        return;
    }

    try {
        const response = await fetch("http://localhost:8080/api/projects", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            },
            body: JSON.stringify({
                name,
                systemPrompt,
                model: "gpt-4o"
            })
        });

        if (response.status === 401) {
            logout();
            return;
        }

        const project = await response.json();

        // Clear inputs
        projectNameInput.value = "";
        systemPromptInput.value = "";

        // Add new project to list immediately
        appendProject(project);

    } catch (error) {
        alert("Failed to create project");
        console.error(error);
    }
});

function appendProject(project) {
    const div = document.createElement("div");
    div.className = "project-item";

    div.innerHTML = `
        <span>${project.name}</span>
        <span>→</span>
    `;

    div.addEventListener("click", () => {
        localStorage.setItem("currentProjectId", project.id);
        window.location.href = "chat.html";
    });

    projectsList.appendChild(div);
}

logoutBtn.addEventListener("click", logout);

function logout() {
    localStorage.clear();
    window.location.href = "login.html";
}
