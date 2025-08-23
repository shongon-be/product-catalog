package com.shongon.catalog.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {
    @Id
    ObjectId id;
    String name;
    String description;
    double price;
    String category;
}

