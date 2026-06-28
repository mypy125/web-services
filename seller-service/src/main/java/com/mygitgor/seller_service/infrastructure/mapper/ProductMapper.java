package com.mygitgor.seller_service.infrastructure.mapper;

import com.mygitgor.seller_service.application.dto.external.ProductSummaryDto;
import com.mygitgor.seller_service.application.dto.request.CreateProductRequest;
import com.mygitgor.seller_service.application.dto.request.UpdateProductRequest;
import com.mygitgor.seller_service.application.dto.response.CategoryResponse;
import com.mygitgor.seller_service.application.dto.response.ProductResponse;
import com.mygitgor.seller_service.shared.valueobject.Category;
import com.mygitgor.seller_service.shared.valueobject.Product;
import com.mygitgor.seller_service.shared.valueobject.ProductStatus;
import com.mygitgor.seller_service.shared.valueobject.id.CategoryId;
import com.mygitgor.seller_service.shared.valueobject.id.ProductId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import org.mapstruct.*;
import org.springframework.data.domain.Page;
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    @ObjectFactory
    default Product createProduct(CreateProductRequest request, SellerId sellerId) {
        Category category = toCategory(request.categoryId());

        Product product = Product.create(
                sellerId,
                request.title(),
                request.description(),
                request.sellingPrice(),
                request.quantity(),
                category
        );

        product.updatePrice(request.mrpPrice(), request.sellingPrice(), request.discountPercent());
        return product;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sellerId", ignore = true)
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "toCategory")
    @Mapping(target = "categories", source = "categoryIds", qualifiedByName = "toCategoryList")
    Product toDomain(CreateProductRequest request, SellerId sellerId);

    @Mapping(target = "category", source = "categoryId", qualifiedByName = "toCategory")
    @Mapping(target = "categories", source = "categoryIds", qualifiedByName = "toCategoryList")
    void updateDomain(@MappingTarget Product product, UpdateProductRequest request);

    @AfterMapping
    default void handleDomainUpdate(@MappingTarget Product product, UpdateProductRequest request) {
        if (request.sellingPrice() != null || request.mrpPrice() != null || request.discountPercent() != null) {
            Double selling = request.sellingPrice() != null ? request.sellingPrice() : product.getSellingPrice();
            Double mrp = request.mrpPrice() != null ? request.mrpPrice() : product.getMrpPrice();
            Double discount = request.discountPercent() != null ? request.discountPercent() : product.getDiscountPercent();
            product.updatePrice(mrp, selling, discount);
        }

        if (request.quantity() != null) {
            product.updateQuantity(request.quantity());
        }

        if (request.status() != null) {
            ProductStatus status = stringToProductStatus(request.status());
            if (status == ProductStatus.PUBLISHED) product.publish();
            else if (status == ProductStatus.DRAFT) product.unpublish();
            else if (status == ProductStatus.ARCHIVED) product.archive();
            else if (status == ProductStatus.DELETED) product.delete();
        }
    }

    @Mapping(target = "id", source = "id", qualifiedByName = "productIdToString")
    @Mapping(target = "sellerId", source = "sellerId", qualifiedByName = "sellerIdToString")
    @Mapping(target = "shortDescription", expression = "java(product.getDynamicShortDescription())")
    @Mapping(target = "inStock", expression = "java(product.isInStock())")
    @Mapping(target = "lowStock", expression = "java(product.isLowStock())")
    @Mapping(target = "outOfStock", expression = "java(product.isOutOfStock())")
    @Mapping(target = "profitMargin", expression = "java(product.getProfitMargin())")
    @Mapping(target = "totalValue", expression = "java(product.getTotalValue())")
    @Mapping(target = "category", source = "category", qualifiedByName = "categoryToResponse")
    @Mapping(target = "categories", source = "categories", qualifiedByName = "categoryListToResponse")
    ProductResponse toResponse(Product product);

    @Mapping(target = "title", source = "name")
    @Mapping(target = "sellingPrice", source = "price")
    @Mapping(target = "mrpPrice", source = "compareAtPrice")
    @Mapping(target = "quantity", source = "availableQuantity")
    @Mapping(target = "soldQuantity", source = "totalQuantitySold")
    @Mapping(target = "mainImage", source = "mainImageUrl")
    @Mapping(target = "images", source = "imageUrls")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "categories", ignore = true)
    ProductResponse summaryToResponse(ProductSummaryDto dto);

    default Page<ProductResponse> toResponsePage(Page<Product> productPage) {
        return productPage == null ? Page.empty() : productPage.map(this::toResponse);
    }

    default List<ProductResponse> toResponseList(List<Product> products) {
        return products == null ? new ArrayList<>() : products.stream().map(this::toResponse).toList();
    }


    @Named("productIdToString")
    default String productIdToString(ProductId productId) {
        return productId == null ? null : productId.toString();
    }

    @Named("sellerIdToString")
    default String sellerIdToString(SellerId sellerId) {
        return sellerId == null ? null : sellerId.toString();
    }

    @Named("stringToProductStatus")
    default ProductStatus stringToProductStatus(String status) {
        return ProductStatus.fromString(status);
    }

    @Named("toCategory")
    default Category toCategory(String categoryId) {
        if (categoryId == null) return null;
        return Category.builder()
                .id(new CategoryId(categoryId))
                .name("Category")
                .build();
    }

    @Named("toCategoryList")
    default List<Category> toCategoryList(List<String> categoryIds) {
        return categoryIds == null ? new ArrayList<>() : categoryIds.stream().map(this::toCategory).toList();
    }

    @Named("categoryToResponse")
    default CategoryResponse categoryToResponse(Category category) {
        if (category == null) return null;
        return CategoryResponse.builder()
                .id(category.getId() != null ? category.getId().toString() : null)
                .name(category.getName())
                .level(category.getLevel())
                .build();
    }

    @Named("categoryListToResponse")
    default List<CategoryResponse> categoryListToResponse(List<Category> categories) {
        return categories == null ? new ArrayList<>() : categories.stream().map(this::categoryToResponse).toList();
    }
}