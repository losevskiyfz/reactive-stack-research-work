package com.losevskiyfz.reactivestackresearchwork.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("books")
@Builder
public record BookRecord(
        @Id
        @JsonIgnore
        String id,
        String type,
        Integer quantity,
        String[] authors,
        String name,
        Integer pages,
        String publisher,
        Integer year,
        String city,
        String department_id,
        String summary,
        String room
) {}
