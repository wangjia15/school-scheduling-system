package com.school.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterRequest {
    private Map<String, String> filters;
    private String search;
    private Boolean active;
    private String department;
    private String semester;
    private String dateFrom;
    private String dateTo;
}