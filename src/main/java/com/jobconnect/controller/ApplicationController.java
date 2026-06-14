package com.jobconnect.controller;

import com.jobconnect.model.Application;
import com.jobconnect.model.ApplicationStatus;
import com.jobconnect.model.User;
import com.jobconnect.service.ApplicationService;
import com.jobconnect.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final UserService userService;

    @Autowired
    public ApplicationController(ApplicationService applicationService, UserService userService) {
        this.applicationService = applicationService;
        this.userService = userService;
    }

    @PostMapping("/apply")
    @PreAuthorize("hasAuthority('ROLE_JOB_SEEKER')")
    public String applyForJob(@RequestParam("jobId") Long jobId,
                              @RequestParam("coverLetter") String coverLetter,
                              @RequestParam("resumeUrl") String resumeUrl,
                              @AuthenticationPrincipal UserDetails userDetails) {
        User seeker = userService.findByEmail(userDetails.getUsername());
        try {
            applicationService.applyForJob(jobId, seeker, coverLetter, resumeUrl);
        } catch (IllegalArgumentException e) {
            return "redirect:/jobs/" + jobId + "?error=" + e.getMessage();
        }
        return "redirect:/dashboard?applySuccess";
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER')")
    public String viewJobApplications(@PathVariable("jobId") Long jobId,
                                      Model model,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        User employer = userService.findByEmail(userDetails.getUsername());
        try {
            List<Application> applications = applicationService.getApplicationsForJob(jobId, employer);
            model.addAttribute("applications", applications);
            model.addAttribute("jobId", jobId);
        } catch (IllegalArgumentException e) {
            return "redirect:/dashboard?error=" + e.getMessage();
        }
        return "applicants-list";
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER')")
    public String updateApplicationStatus(@PathVariable("id") Long id,
                                          @RequestParam("status") ApplicationStatus status,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        User employer = userService.findByEmail(userDetails.getUsername());
        Application application;
        try {
            application = applicationService.updateApplicationStatus(id, status, employer);
        } catch (IllegalArgumentException e) {
            return "redirect:/dashboard?error=" + e.getMessage();
        }
        return "redirect:/applications/job/" + application.getJob().getId() + "?statusUpdatedSuccess";
    }
}
