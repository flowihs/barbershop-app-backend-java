package com.github.barbershop.category.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException() {
        super(
                "Категории не были найдены"
        );
    }
}
