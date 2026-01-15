package com.chatbot.service;

import com.chatbot.dto.ProjectDTOs.ProjectRequest;
import com.chatbot.model.Project;
import com.chatbot.repository.MessageRepository;
import com.chatbot.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    public ProjectRepository getProjectRepository() {
        return projectRepository;
    }

    public void setProjectRepository(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public MessageRepository getMessageRepository() {
        return messageRepository;
    }

    public void setMessageRepository(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Autowired
    private MessageRepository messageRepository;

    public Project createProject(ProjectRequest request, String userId) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setModel(request.getModel() != null ? request.getModel() : "gpt-4o");
        project.setUserId(userId);
        project.setSystemPrompt(request.getSystemPrompt());
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        return projectRepository.save(project);
    }

    public List<Project> getUserProjects(String userId) {
        return projectRepository.findByUserId(userId);
    }

    public Project getProject(String projectId, String userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to project");
        }

        return project;
    }

    public Project updateProject(String projectId, ProjectRequest request, String userId) {
        Project project = getProject(projectId, userId);

        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getModel() != null) {
            project.setModel(request.getModel());
        }
        if (request.getSystemPrompt() != null) {
            project.setSystemPrompt(request.getSystemPrompt());
        }

        project.setUpdatedAt(LocalDateTime.now());

        return projectRepository.save(project);
    }

    public void deleteProject(String projectId, String userId) {
        Project project = getProject(projectId, userId);
        messageRepository.deleteByProjectId(projectId);
        projectRepository.delete(project);
    }
}