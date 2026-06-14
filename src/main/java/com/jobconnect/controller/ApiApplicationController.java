package com.jobconnect.controller;

import com.jobconnect.dto.ApplicationRequest;
import com.jobconnect.dto.StatusUpdateRequest;
import com.jobconnect.model.Application;
import com.jobconnect.model.User;
import com.jobconnect.service.ApplicationService;
import com.jobconnect.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApiApplicationController {

    private final ApplicationService applicationService;
    private final UserService userService;

    public ApiApplicationController(ApplicationService applicationService, UserService userService) {
        this.applicationService = applicationService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_JOB_SEEKER')")
    public ResponseEntity<?> apply(@Valid @RequestBody ApplicationRequest req,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        User seeker = userService.findByEmail(userDetails.getUsername());
        try {
            Application app = applicationService.applyForJob(req.getJobId(), seeker, req.getCoverLetter(), req.getResumeUrl());
            return ResponseEntity.ok(app);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAuthority('ROLE_JOB_SEEKER')")
    public ResponseEntity<List<Application>> myApplications(@AuthenticationPrincipal UserDetails userDetails) {
        User seeker = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(applicationService.getApplicationsForSeeker(seeker));
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER')")
    public ResponseEntity<?> jobApplications(@PathVariable Long jobId,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        User employer = userService.findByEmail(userDetails.getUsername());
        try {
            List<Application> apps = applicationService.getApplicationsForJob(jobId, employer);
            return ResponseEntity.ok(apps);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER')")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @Valid @RequestBody StatusUpdateRequest req,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        User employer = userService.findByEmail(userDetails.getUsername());
        try {
            Application app = applicationService.updateApplicationStatus(id, req.getStatus(), employer);
            return ResponseEntity.ok(app);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
