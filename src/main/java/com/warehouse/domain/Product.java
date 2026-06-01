package com.warehouse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {
    @Id
    private String sku;
    private String name;
    private String description;

    // Getters, Setters, Constructors
    public Product() {}
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}