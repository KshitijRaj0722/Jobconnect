package com.jobconnect;

import com.jobconnect.model.Application;
import com.jobconnect.model.ApplicationStatus;
import com.jobconnect.model.Job;
import com.jobconnect.model.Role;
import com.jobconnect.model.User;
import com.jobconnect.repository.ApplicationRepository;
import com.jobconnect.repository.JobRepository;
import com.jobconnect.service.ApplicationServiceImpl;
import com.jobconnect.service.JobServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class JobConnectServiceTests {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private com.jobconnect.service.SmsService smsService;

    @InjectMocks
    private JobServiceImpl jobService;

    private ApplicationServiceImpl applicationService;

    private User employer;
    private User seeker;
    private Job job;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        applicationService = new ApplicationServiceImpl(applicationRepository, jobService, smsService);

        employer = User.builder()
                .id(1L)
                .email("employer@jobconnect.com")
                .fullName("Employer Name")
                .role(Role.ROLE_EMPLOYER)
                .build();

        seeker = User.builder()
                .id(2L)
                .email("seeker@jobconnect.com")
                .fullName("Seeker Name")
                .phoneNumber("+1234567890")
                .role(Role.ROLE_JOB_SEEKER)
                .build();

        job = Job.builder()
                .id(10L)
                .employer(employer)
                .title("Software Engineer")
                .description("Job description here")
                .location("Remote")
                .salary(100000.0)
                .deadline(LocalDate.now().plusDays(10))
                .build();
    }

    @Test
    void testSaveJob() {
        when(jobRepository.save(any(Job.class))).thenReturn(job);

        Job savedJob = jobService.saveJob(job, employer);

        assertNotNull(savedJob);
        assertEquals(employer, savedJob.getEmployer());
        verify(jobRepository, times(1)).save(job);
    }

    @Test
    void testDeleteJobSuccess() {
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        doNothing().when(jobRepository).delete(job);

        assertDoesNotThrow(() -> jobService.deleteJob(10L, employer));
        verify(jobRepository, times(1)).delete(job);
    }

    @Test
    void testDeleteJobUnauthorizedThrowsException() {
        User otherEmployer = User.builder().id(99L).build();
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            jobService.deleteJob(10L, otherEmployer);
        });

        assertEquals("You are not authorized to delete this job posting.", exception.getMessage());
        verify(jobRepository, never()).delete(any(Job.class));
    }

    @Test
    void testApplyForJobSuccess() {
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByJobAndSeeker(job, seeker)).thenReturn(false);
        
        Application expectedApp = Application.builder()
                .job(job)
                .seeker(seeker)
                .coverLetter("Hi")
                .status(ApplicationStatus.APPLIED)
                .build();
        when(applicationRepository.save(any(Application.class))).thenReturn(expectedApp);

        Application createdApp = applicationService.applyForJob(10L, seeker, "Hi", "http://resume");

        assertNotNull(createdApp);
        assertEquals(ApplicationStatus.APPLIED, createdApp.getStatus());
        verify(applicationRepository, times(1)).save(any(Application.class));
        verify(smsService, times(1)).sendSms(eq("+1234567890"), anyString());
    }

    @Test
    void testApplyForJobDuplicateThrowsException() {
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByJobAndSeeker(job, seeker)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            applicationService.applyForJob(10L, seeker, "Hi", "http://resume");
        });

        assertEquals("You have already applied for this job.", exception.getMessage());
        verify(applicationRepository, never()).save(any(Application.class));
        verify(smsService, never()).sendSms(anyString(), anyString());
    }

    @Test
    void testUpdateApplicationStatusSuccess() {
        Application application = Application.builder()
                .id(50L)
                .job(job)
                .seeker(seeker)
                .status(ApplicationStatus.APPLIED)
                .build();

        when(applicationRepository.findById(50L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        Application updatedApp = applicationService.updateApplicationStatus(50L, ApplicationStatus.SHORTLISTED, employer);

        assertNotNull(updatedApp);
        assertEquals(ApplicationStatus.SHORTLISTED, updatedApp.getStatus());
        verify(applicationRepository, times(1)).save(application);
        verify(smsService, times(1)).sendSms(eq("+1234567890"), anyString());
    }
}
