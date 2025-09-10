package com.shongon.catalog.dto.cache;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CacheablePage<T> {
    List<T> content;
    long totalElements;
    int totalPages;
    int number;
    int size;
    boolean first;
    boolean last;
    boolean empty;

    // Convert from Spring Page to CacheablePage for Redis storage
    public static <T> CacheablePage<T> from(Page<T> page) {
        return CacheablePage.<T>builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .number(page.getNumber())
                .size(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }


     // Convert back to Spring Page for API response
    public Page<T> toPage(Pageable pageable) {
        return new PageImpl<>(content, pageable, totalElements);
    }
}
