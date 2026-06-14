package com.jobconnect.service;

import com.jobconnect.model.Application;
import com.jobconnect.model.ApplicationStatus;
import com.jobconnect.model.User;
import java.util.List;

public interface ApplicationService {
    Application applyForJob(Long jobId, User seeker, String coverLetter, String resumeUrl);
    boolean hasAlreadyApplied(Long jobId, User seeker);
    List<Application> getApplicationsForSeeker(User seeker);
    List<Application> getApplicationsForJob(Long jobId, User employer);
    Application updateApplicationStatus(Long applicationId, ApplicationStatus status, User employer);
    Application getApplicationById(Long id);
}
