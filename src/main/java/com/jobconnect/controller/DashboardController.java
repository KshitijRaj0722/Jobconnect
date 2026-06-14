package com.jobconnect.controller;

import com.jobconnect.model.Application;
import com.jobconnect.model.Job;
import com.jobconnect.model.Role;
import com.jobconnect.model.User;
import com.jobconnect.service.ApplicationService;
import com.jobconnect.service.JobService;
import com.jobconnect.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class DashboardController {

    private final UserService userService;
    private final JobService jobService;
    private final ApplicationService applicationService;

    @Autowired
    public DashboardController(UserService userService, JobService jobService, ApplicationService applicationService) {
        this.userService = userService;
        this.jobService = jobService;
        this.applicationService = applicationService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userService.findByEmail(userDetails.getUsername());
        model.addAttribute("currentUser", user);

        if (user.getRole() == Role.ROLE_EMPLOYER) {
            List<Job> myJobs = jobService.getJobsByEmployer(user);
            model.addAttribute("myJobs", myJobs);
        } else if (user.getRole() == Role.ROLE_JOB_SEEKER) {
            List<Application> myApplications = applicationService.getApplicationsForSeeker(user);
            model.addAttribute("myApplications", myApplications);
        }

        return "dashboard";
    }
}
