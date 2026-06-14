package com.jobconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationRequest {
    @NotNull
    private Long jobId;
    @NotBlank
    private String coverLetter;
    private String resumeUrl;
}
