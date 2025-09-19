package com.school.scheduling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> content;
    private Pagination pagination;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pagination {
        private int page;
        private int size;
        private int totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
        private boolean empty;
        private int numberOfElements;
    }

    public static <T> PaginatedResponse<T> of(Page<T> page) {
        return new PaginatedResponse<>(
                page.getContent(),
                new Pagination(
                        page.getNumber(),
                        page.getSize(),
                        (int) page.getTotalElements(),
                        page.getTotalPages(),
                        page.isFirst(),
                        page.isLast(),
                        page.isEmpty(),
                        page.getNumberOfElements()
                )
        );
    }

    public static <T> PaginatedResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new PaginatedResponse<>(
                content,
                new Pagination(
                        page,
                        size,
                        (int) totalElements,
                        totalPages,
                        page == 0,
                        page >= totalPages - 1,
                        content.isEmpty(),
                        content.size()
                )
        );
    }

    public static <T> PaginatedResponse<T> empty() {
        return new PaginatedResponse<>(
                List.of(),
                new Pagination(0, 0, 0, 0, true, true, true, 0)
        );
    }
}