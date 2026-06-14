package com.jobconnect.service;

import com.jobconnect.model.Job;
import com.jobconnect.model.User;
import java.util.List;

public interface JobService {
    Job saveJob(Job job, User employer);
    Job getJobById(Long id);
    List<Job> getJobsByEmployer(User employer);
    List<Job> searchJobs(String keyword, String location);
    void deleteJob(Long id, User employer);
}
