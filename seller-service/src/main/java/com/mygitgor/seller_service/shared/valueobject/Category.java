package com.mygitgor.seller_service.shared.valueobject;

import com.mygitgor.seller_service.shared.exception.DomainException;
import com.mygitgor.seller_service.shared.valueobject.id.CategoryId;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder(toBuilder = true)
public class Category {
    private final CategoryId id;
    private String name;
    private String slug;
    private String description;
    private String categoryId;
    private Category parentCategory;

    @Builder.Default
    private List<Category> subCategories = new ArrayList<>();

    private Integer level;
    private Integer sortOrder;
    private boolean isActive;
    private String icon;
    private String imageUrl;
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Category create(String name, String categoryId, Category parentCategory) {
        LocalDateTime now = LocalDateTime.now();

        int parentLevel = (parentCategory != null && parentCategory.getLevel() != null)
                ? parentCategory.getLevel()
                : 0;
        Integer calculatedLevel = parentCategory != null ? parentLevel + 1 : 0;

        return Category.builder()
                .id(new CategoryId())
                .name(name)
                .slug(generateSlug(name))
                .categoryId(categoryId)
                .parentCategory(parentCategory)
                .level(calculatedLevel)
                .sortOrder(0)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void addSubCategory(Category subCategory) {
        ensureSubCategoriesInitialized();
        if (subCategory != null) {
            this.subCategories.add(subCategory);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void removeSubCategory(Category subCategory) {
        if (this.subCategories != null) {
            this.subCategories.remove(subCategory);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void updateName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("Category name cannot be empty");
        }
        this.name = name;
        this.slug = generateSlug(name);
        this.updatedAt = LocalDateTime.now();
    }

    public void updateSortOrder(Integer sortOrder) {
        if (sortOrder == null || sortOrder < 0) {
            throw new DomainException("Sort order must be non-negative");
        }
        this.sortOrder = sortOrder;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }


    public String getPath() {
        if (this.parentCategory != null) {
            return this.parentCategory.getPath() + " > " + this.name;
        }
        return this.name;
    }

    public List<Category> getFullPath() {
        List<Category> path = new ArrayList<>();
        Category current = this;
        while (current != null) {
            path.add(0, current);
            current = current.getParentCategory();
        }
        return path;
    }

    public boolean isRootCategory() {
        return this.level != null && this.level == 0;
    }

    public boolean hasSubCategories() {
        return this.subCategories != null && !this.subCategories.isEmpty();
    }

    private void ensureSubCategoriesInitialized() {
        if (this.subCategories == null) {
            this.subCategories = new ArrayList<>();
        }
    }

    private static String generateSlug(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .trim();
    }

    @Override
    public String toString() {
        return String.format("Category{id=%s, name='%s', level=%d, parent='%s'}",
                id, name, level, parentCategory != null ? parentCategory.getName() : "null");
    }
}