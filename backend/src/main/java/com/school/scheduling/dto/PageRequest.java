package com.school.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {
    private int page = 0;
    private int size = 20;
    private String sort = "id";
    private String direction = "ASC";

    public PageRequest(int page, int size) {
        this.page = Math.max(0, page);
        this.size = Math.min(100, Math.max(1, size)); // Max 100 items per page
    }

    public PageRequest(int page, int size, String sort) {
        this(page, size);
        this.sort = sort != null ? sort : "id";
    }

    public PageRequest(int page, int size, String sort, String direction) {
        this(page, size, sort);
        this.direction = direction != null ? direction.toUpperCase() : "ASC";
    }

    public org.springframework.data.domain.PageRequest toSpringPageRequest() {
        return org.springframework.data.domain.PageRequest.of(
                page,
                size,
                org.springframework.data.domain.Sort.Direction.valueOf(direction),
                sort
        );
    }
}