package com.jobconnect.service;

import com.jobconnect.model.Application;
import com.jobconnect.model.ApplicationStatus;
import com.jobconnect.model.Job;
import com.jobconnect.model.User;
import com.jobconnect.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobService jobService;
    private final SmsService smsService;

    @Autowired
    public ApplicationServiceImpl(ApplicationRepository applicationRepository, JobService jobService, SmsService smsService) {
        this.applicationRepository = applicationRepository;
        this.jobService = jobService;
        this.smsService = smsService;
    }

    @Override
    public Application applyForJob(Long jobId, User seeker, String coverLetter, String resumeUrl) {
        Job job = jobService.getJobById(jobId);

        if (hasAlreadyApplied(jobId, seeker)) {
            throw new IllegalArgumentException("You have already applied for this job.");
        }

        Application application = Application.builder()
                .job(job)
                .seeker(seeker)
                .coverLetter(coverLetter)
                .resumeUrl(resumeUrl)
                .status(ApplicationStatus.APPLIED)
                .build();

        Application savedApplication = applicationRepository.save(application);

        // Trigger SMS Confirmation to Seeker
        if (seeker.getPhoneNumber() != null && !seeker.getPhoneNumber().trim().isEmpty()) {
            String message = String.format("Hello %s, your application for '%s' at '%s' has been received successfully! You can track its status on your JobConnect dashboard.",
                    seeker.getFullName(), job.getTitle(), job.getEmployer().getFullName());
            try {
                smsService.sendSms(seeker.getPhoneNumber(), message);
            } catch (Exception e) {
                System.err.println("Failed to send application confirmation SMS: " + e.getMessage());
            }
        }

        return savedApplication;
    }

    @Override
    public boolean hasAlreadyApplied(Long jobId, User seeker) {
        Job job = jobService.getJobById(jobId);
        return applicationRepository.existsByJobAndSeeker(job, seeker);
    }

    @Override
    public List<Application> getApplicationsForSeeker(User seeker) {
        return applicationRepository.findBySeekerOrderByCreatedAtDesc(seeker);
    }

    @Override
    public List<Application> getApplicationsForJob(Long jobId, User employer) {
        Job job = jobService.getJobById(jobId);
        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new IllegalArgumentException("You are not authorized to view applications for this job.");
        }
        return applicationRepository.findByJobOrderByCreatedAtDesc(job);
    }

    @Override
    public Application getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with id: " + id));
    }

    @Override
    public Application updateApplicationStatus(Long applicationId, ApplicationStatus status, User employer) {
        Application application = getApplicationById(applicationId);
        Job job = application.getJob();

        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new IllegalArgumentException("You are not authorized to update status for this application.");
        }

        application.setStatus(status);
        Application savedApplication = applicationRepository.save(application);

        // Trigger SMS Alert to Seeker for status update
        User seeker = application.getSeeker();
        if (seeker.getPhoneNumber() != null && !seeker.getPhoneNumber().trim().isEmpty()) {
            String message = String.format("Hello %s, your application status for '%s' has been updated to: %s. Log in to JobConnect to view details.",
                    seeker.getFullName(), job.getTitle(), status.name());
            try {
                smsService.sendSms(seeker.getPhoneNumber(), message);
            } catch (Exception e) {
                System.err.println("Failed to send status update SMS: " + e.getMessage());
            }
        }

        return savedApplication;
    }
}
