package com.chatbot.controller;

import com.chatbot.dto.ProjectDTOs.ProjectRequest;
import com.chatbot.model.Project;
import com.chatbot.security.JwtUtil;
import com.chatbot.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    public JwtUtil getJwtUtil() {
        return jwtUtil;
    }

    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public ProjectService getProjectService() {
        return projectService;
    }

    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> createProject(
            @Valid @RequestBody ProjectRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = getUserIdFromToken(token);
            Project project = projectService.createProject(request, userId);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorMap(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserProjects(@RequestHeader("Authorization") String token) {
        try {
            String userId = getUserIdFromToken(token);
            List<Project> projects = projectService.getUserProjects(userId);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorMap(e.getMessage()));
        }
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<?> getProject(
            @PathVariable String projectId,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = getUserIdFromToken(token);
            Project project = projectService.getProject(projectId, userId);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorMap(e.getMessage()));
        }
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<?> updateProject(
            @PathVariable String projectId,
            @Valid @RequestBody ProjectRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = getUserIdFromToken(token);
            Project project = projectService.updateProject(projectId, request, userId);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorMap(e.getMessage()));
        }
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(
            @PathVariable String projectId,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = getUserIdFromToken(token);
            projectService.deleteProject(projectId, userId);
            return ResponseEntity.ok(createSuccessMap("Project deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorMap(e.getMessage()));
        }
    }

    private String getUserIdFromToken(String token) {
        String jwt = token.substring(7);
        return jwtUtil.getUserIdFromToken(jwt);
    }

    private Map<String, String> createErrorMap(String message) {
        Map<String, String> map = new HashMap<>();
        map.put("error", message);
        return map;
    }

    private Map<String, String> createSuccessMap(String message) {
        Map<String, String> map = new HashMap<>();
        map.put("message", message);
        return map;
    }
}