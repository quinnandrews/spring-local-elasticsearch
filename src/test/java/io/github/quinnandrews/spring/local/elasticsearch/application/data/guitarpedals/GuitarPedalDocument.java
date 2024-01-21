package io.github.quinnandrews.spring.local.elasticsearch.application.data.guitarpedals;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "guitar_pedals")
public class GuitarPedalDocument {

    @Id
    @Field(type = FieldType.Long)
    private Long id;

    @Field(type = FieldType.Keyword)
    private String name;

    public GuitarPedalDocument() {
        // no-op
    }

    public GuitarPedalDocument(final GuitarPedal guitarPedal) {
        this.id = guitarPedal.getId();
        this.name = guitarPedal.getName();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
