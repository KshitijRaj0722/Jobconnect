package com.jobconnect.controller;

import com.jobconnect.dto.JobRequest;
import com.jobconnect.model.Job;
import com.jobconnect.model.User;
import com.jobconnect.service.JobService;
import com.jobconnect.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class ApiJobController {

    private final JobService jobService;
    private final UserService userService;

    public ApiJobController(JobService jobService, UserService userService) {
        this.jobService = jobService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Job>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location) {
        return ResponseEntity.ok(jobService.searchJobs(keyword, location));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER')")
    public ResponseEntity<List<Job>> myJobs(@AuthenticationPrincipal UserDetails userDetails) {
        User employer = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(jobService.getJobsByEmployer(employer));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER')")
    public ResponseEntity<Job> createJob(@Valid @RequestBody JobRequest req,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        User employer = userService.findByEmail(userDetails.getUsername());
        Job job = new Job();
        job.setTitle(req.getTitle());
        job.setDescription(req.getDescription());
        job.setLocation(req.getLocation());
        job.setSalary(req.getSalary());
        job.setDeadline(req.getDeadline());
        return ResponseEntity.ok(jobService.saveJob(job, employer));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER')")
    public ResponseEntity<?> updateJob(@PathVariable Long id,
                                       @Valid @RequestBody JobRequest req,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        User employer = userService.findByEmail(userDetails.getUsername());
        Job existing = jobService.getJobById(id);
        if (!existing.getEmployer().getId().equals(employer.getId())) {
            return ResponseEntity.status(403).body("Access denied");
        }
        existing.setTitle(req.getTitle());
        existing.setDescription(req.getDescription());
        existing.setLocation(req.getLocation());
        existing.setSalary(req.getSalary());
        existing.setDeadline(req.getDeadline());
        return ResponseEntity.ok(jobService.saveJob(existing, employer));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER')")
    public ResponseEntity<?> deleteJob(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        User employer = userService.findByEmail(userDetails.getUsername());
        try {
            jobService.deleteJob(id, employer);
            return ResponseEntity.ok("Job deleted");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
