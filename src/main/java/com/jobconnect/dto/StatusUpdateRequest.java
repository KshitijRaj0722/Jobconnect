package com.jobconnect.dto;

import com.jobconnect.model.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusUpdateRequest {
    @NotNull
    private ApplicationStatus status;
}
