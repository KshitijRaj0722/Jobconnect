package com.jobconnect.service;

import com.jobconnect.model.Job;
import com.jobconnect.model.User;
import com.jobconnect.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;

    @Autowired
    public JobServiceImpl(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public Job saveJob(Job job, User employer) {
        job.setEmployer(employer);
        return jobRepository.save(job);
    }

    @Override
    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with id: " + id));
    }

    @Override
    public List<Job> getJobsByEmployer(User employer) {
        return jobRepository.findByEmployerOrderByCreatedAtDesc(employer);
    }

    @Override
    public List<Job> searchJobs(String keyword, String location) {
        return jobRepository.searchJobs(keyword, location);
    }

    @Override
    public void deleteJob(Long id, User employer) {
        Job job = getJobById(id);
        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new IllegalArgumentException("You are not authorized to delete this job posting.");
        }
        jobRepository.delete(job);
    }
}
