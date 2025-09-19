package com.school.scheduling.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictResolutionRequest {
    @NotBlank(message = "Resolution notes are required")
    @Size(max = 1000, message = "Resolution notes must be less than 1000 characters")
    private String resolutionNotes;
}