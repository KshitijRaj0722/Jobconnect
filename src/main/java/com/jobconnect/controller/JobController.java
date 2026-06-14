package com.jobconnect.controller;

import com.jobconnect.model.Job;
import com.jobconnect.model.User;
import com.jobconnect.service.JobService;
import com.jobconnect.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/jobs")
public class JobController {

    private final JobService jobService;
    private final UserService userService;

    @Autowired
    public JobController(JobService jobService, UserService userService) {
        this.jobService = jobService;
        this.userService = userService;
    }

    @GetMapping("/post")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER')")
    public String showPostJobForm(Model model) {
        model.addAttribute("job", new Job());
        return "job-form";
    }

    @PostMapping("/post")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER')")
    public String postJob(@Valid @ModelAttribute("job") Job job,
                          BindingResult result,
                          @AuthenticationPrincipal UserDetails userDetails) {
        if (result.hasErrors()) {
            return "job-form";
        }
        User employer = userService.findByEmail(userDetails.getUsername());
        jobService.saveJob(job, employer);
        return "redirect:/dashboard?jobPostedSuccess";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER')")
    public String showEditJobForm(@PathVariable("id") Long id,
                                  Model model,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        User employer = userService.findByEmail(userDetails.getUsername());
        Job job = jobService.getJobById(id);
        if (!job.getEmployer().getId().equals(employer.getId())) {
            return "redirect:/dashboard?unauthorized";
        }
        model.addAttribute("job", job);
        model.addAttribute("isEdit", true);
        return "job-form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER')")
    public String editJob(@PathVariable("id") Long id,
                          @Valid @ModelAttribute("job") Job job,
                          BindingResult result,
                          @AuthenticationPrincipal UserDetails userDetails,
                          Model model) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "job-form";
        }
        User employer = userService.findByEmail(userDetails.getUsername());
        Job existingJob = jobService.getJobById(id);
        if (!existingJob.getEmployer().getId().equals(employer.getId())) {
            return "redirect:/dashboard?unauthorized";
        }
        
        existingJob.setTitle(job.getTitle());
        existingJob.setDescription(job.getDescription());
        existingJob.setLocation(job.getLocation());
        existingJob.setSalary(job.getSalary());
        existingJob.setDeadline(job.getDeadline());

        jobService.saveJob(existingJob, employer);
        return "redirect:/dashboard?jobUpdatedSuccess";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER')")
    public String deleteJob(@PathVariable("id") Long id,
                            @AuthenticationPrincipal UserDetails userDetails) {
        User employer = userService.findByEmail(userDetails.getUsername());
        try {
            jobService.deleteJob(id, employer);
        } catch (IllegalArgumentException e) {
            return "redirect:/dashboard?error=" + e.getMessage();
        }
        return "redirect:/dashboard?jobDeletedSuccess";
    }

    @GetMapping("/search")
    public String searchJobs(@RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "location", required = false) String location,
                             Model model) {
        List<Job> jobs = jobService.searchJobs(keyword, location);
        model.addAttribute("jobs", jobs);
        model.addAttribute("keyword", keyword);
        model.addAttribute("location", location);
        return "job-search";
    }

    @GetMapping("/{id}")
    public String viewJobDetails(@PathVariable("id") Long id,
                                 Model model,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        Job job = jobService.getJobById(id);
        model.addAttribute("job", job);
        
        // Pass current user role to toggle Apply button logic in view
        if (userDetails != null) {
            User user = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("currentUser", user);
        }
        return "job-details";
    }
}
