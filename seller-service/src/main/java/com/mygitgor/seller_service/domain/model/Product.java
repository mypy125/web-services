package com.mygitgor.seller_service.domain.model;

import com.mygitgor.seller_service.domain.model.status.ProductStatus;
import com.mygitgor.seller_service.shared.exception.DomainException;
import com.mygitgor.seller_service.shared.valueobject.id.ProductId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Builder(toBuilder = true)
public class Product {
    private final ProductId id;
    private final SellerId sellerId;

    private String title;
    private String description;
    private String shortDescription;
    private String slug;
    private String sku;
    private String barcode;

    private Double mrpPrice;
    private Double sellingPrice;
    private Double costPerItem;
    private Double discountPercent;
    private Double discountAmount;
    private Double taxRate;
    private String currency;

    private Integer quantity;
    private Integer reservedQuantity;
    private Integer soldQuantity;
    private Integer minimumStockLevel;
    private Integer maximumStockLevel;
    private boolean backorderAllowed;
    private boolean preorderAllowed;
    private LocalDateTime preorderAvailableFrom;
    private String inventoryStatus;

    private Category category;
    @Builder.Default
    private List<Category> categories = new ArrayList<>();
    private String categoryPath;

    private String color;
    @Builder.Default
    private List<String> colors = new ArrayList<>();
    private String size;
    @Builder.Default
    private List<String> sizes = new ArrayList<>();
    private String material;
    private String pattern;
    private String weightUnit;
    private Double weight;
    private String dimensions;

    @Builder.Default
    private List<String> images = new ArrayList<>();
    private String mainImage;
    private String thumbnailImage;
    @Builder.Default
    private List<String> videos = new ArrayList<>();
    @Builder.Default
    private List<String> documents = new ArrayList<>();

    private ProductStatus status;
    private boolean isActive;
    private boolean isFeatured;
    private boolean isNew;
    private boolean isBestSeller;
    private boolean isOnSale;
    private boolean isDigital;
    private boolean isPhysical;
    private boolean isBundle;
    private boolean isCustomizable;
    private boolean isReturnable;
    private Integer returnPeriodDays;

    private String shippingInfo;
    private Double shippingWeight;
    private String shippingDimensions;
    private Double shippingCost;
    private boolean freeShipping;
    private String deliveryTime;
    private String deliveryArea;

    @Builder.Default private Integer numRatings = 0;
    @Builder.Default private Double averageRating = 0.0;
    @Builder.Default private Integer totalReviews = 0;
    @Builder.Default private Integer positiveReviews = 0;
    @Builder.Default private Integer neutralReviews = 0;
    @Builder.Default private Integer negativeReviews = 0;

    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    private String seoUrl;

    @Builder.Default private List<String> tags = new ArrayList<>();
    @Builder.Default private Map<String, String> attributes = new HashMap<>();
    @Builder.Default private Map<String, String> specifications = new HashMap<>();

    private String warrantyInfo;
    private String returnPolicy;
    private String returnInstructions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private LocalDateTime discontinuedAt;

    public static Product create(
            SellerId sellerId,
            String title,
            String description,
            Double sellingPrice,
            Integer quantity,
            Category category
    ) {
        LocalDateTime now = LocalDateTime.now();

        return Product.builder()
                .id(new ProductId())
                .sellerId(sellerId)
                .title(title)
                .description(description)
                .slug(generateSlug(title))
                .sellingPrice(sellingPrice)
                .mrpPrice(sellingPrice * 1.2)
                .quantity(quantity)
                .reservedQuantity(0)
                .soldQuantity(0)
                .minimumStockLevel(5)
                .maximumStockLevel(1000)
                .backorderAllowed(false)
                .preorderAllowed(false)
                .currency("USD")
                .category(category)
                .status(ProductStatus.DRAFT)
                .isActive(false)
                .isFeatured(false)
                .isNew(true)
                .isBestSeller(false)
                .isOnSale(false)
                .isDigital(false)
                .isPhysical(true)
                .isBundle(false)
                .isCustomizable(false)
                .isReturnable(true)
                .returnPeriodDays(30)
                .taxRate(0.0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void updatePrice(Double mrpPrice, Double sellingPrice, Double discountPercent) {
        if (sellingPrice == null || sellingPrice <= 0) {
            throw new DomainException("Selling price must be greater than 0");
        }
        if (mrpPrice != null && mrpPrice < sellingPrice) {
            throw new DomainException("MRP price cannot be less than selling price");
        }

        this.mrpPrice = mrpPrice;
        this.sellingPrice = sellingPrice;
        this.discountPercent = discountPercent;
        this.discountAmount = mrpPrice != null ? mrpPrice - sellingPrice : 0.0;
        this.updatedAt = LocalDateTime.now();
        this.isOnSale = discountPercent != null && discountPercent > 0;
    }

    public void updateDiscount(Double discountPercent) {
        if (discountPercent == null || discountPercent < 0 || discountPercent > 100) {
            throw new DomainException("Discount must be between 0 and 100%");
        }
        this.discountPercent = discountPercent;
        if (this.mrpPrice != null) {
            this.discountAmount = (this.mrpPrice * discountPercent) / 100;
            this.sellingPrice = this.mrpPrice - this.discountAmount;
        } else if (this.sellingPrice != null) {
            this.discountAmount = (this.sellingPrice * discountPercent) / 100;
            this.mrpPrice = this.sellingPrice;
            this.sellingPrice = this.mrpPrice - this.discountAmount;
        }
        this.updatedAt = LocalDateTime.now();
        this.isOnSale = discountPercent > 0;
    }

    public void updateQuantity(Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new DomainException("Quantity cannot be negative");
        }
        this.quantity = quantity;
        this.updatedAt = LocalDateTime.now();
        updateInventoryStatus();
    }

    public void reserveQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new DomainException("Quantity to reserve must be positive");
        }
        int currentReserved = this.reservedQuantity != null ? this.reservedQuantity : 0;
        int currentTotal = this.quantity != null ? this.quantity : 0;

        if (currentReserved + quantity > currentTotal) {
            throw new DomainException("Insufficient stock to reserve");
        }
        this.reservedQuantity = currentReserved + quantity;
        this.updatedAt = LocalDateTime.now();
        updateInventoryStatus();
    }

    public void releaseReservedQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new DomainException("Quantity to release must be positive");
        }
        int currentReserved = this.reservedQuantity != null ? this.reservedQuantity : 0;
        if (currentReserved < quantity) {
            throw new DomainException("Cannot release more than reserved");
        }
        this.reservedQuantity = currentReserved - quantity;
        this.updatedAt = LocalDateTime.now();
        updateInventoryStatus();
    }

    public void sellQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new DomainException("Quantity to sell must be positive");
        }
        int currentTotal = this.quantity != null ? this.quantity : 0;
        int currentSold = this.soldQuantity != null ? this.soldQuantity : 0;

        if (currentTotal < quantity) {
            throw new DomainException("Insufficient stock to sell");
        }
        this.quantity = currentTotal - quantity;
        this.soldQuantity = currentSold + quantity;
        this.updatedAt = LocalDateTime.now();
        updateInventoryStatus();
    }

    private void updateInventoryStatus() {
        int currentTotal = this.quantity != null ? this.quantity : 0;
        int minStock = this.minimumStockLevel != null ? this.minimumStockLevel : 5;

        if (currentTotal <= 0) {
            this.inventoryStatus = "OUT_OF_STOCK";
            this.isActive = false;
        } else if (currentTotal <= minStock) {
            this.inventoryStatus = "LOW_STOCK";
        } else {
            this.inventoryStatus = "IN_STOCK";
        }
    }

    public void publish() {
        if (this.status == ProductStatus.PUBLISHED) {
            throw new DomainException("Product already published");
        }
        if (this.title == null || this.title.isBlank()) {
            throw new DomainException("Cannot publish product without title");
        }
        if (this.sellingPrice == null || this.sellingPrice <= 0) {
            throw new DomainException("Cannot publish product without price");
        }
        if (this.quantity == null || this.quantity <= 0) {
            throw new DomainException("Cannot publish product without stock");
        }

        this.status = ProductStatus.PUBLISHED;
        this.isActive = true;
        this.publishedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void unpublish() {
        if (this.status == ProductStatus.DRAFT) {
            throw new DomainException("Product is already in draft");
        }
        this.status = ProductStatus.DRAFT;
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void archive() {
        this.status = ProductStatus.ARCHIVED;
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        this.status = ProductStatus.DELETED;
        this.isActive = false;
        this.discontinuedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void feature() {
        this.isFeatured = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void unfeature() {
        this.isFeatured = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateCategory(Category category) {
        this.category = category;
        this.updatedAt = LocalDateTime.now();
        if (category != null) {
            this.categoryPath = category.getPath();
        }
    }

    public void addCategory(Category category) {
        ensureCategoriesInitialized();
        this.categories.add(category);
        this.updatedAt = LocalDateTime.now();
    }

    public void removeCategory(Category category) {
        if (this.categories != null) {
            this.categories.remove(category);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void addColor(String color) {
        ensureColorsInitialized();
        if (!this.colors.contains(color)) {
            this.colors.add(color);
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void addSize(String size) {
        ensureSizesInitialized();
        if (!this.sizes.contains(size)) {
            this.sizes.add(size);
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void addImage(String imageUrl) {
        ensureImagesInitialized();
        this.images.add(imageUrl);
        if (this.mainImage == null) {
            this.mainImage = imageUrl;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void removeImage(String imageUrl) {
        if (this.images != null) {
            this.images.remove(imageUrl);
            if (this.mainImage != null && this.mainImage.equals(imageUrl) && !this.images.isEmpty()) {
                this.mainImage = this.images.get(0);
            }
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void setMainImage(String imageUrl) {
        if (this.images != null && this.images.contains(imageUrl)) {
            this.mainImage = imageUrl;
            this.updatedAt = LocalDateTime.now();
        } else {
            throw new DomainException("Image not found in product images");
        }
    }

    public void addRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new DomainException("Rating must be between 1 and 5");
        }

        int currentNumRatings = this.numRatings != null ? this.numRatings : 0;
        double currentAverage = this.averageRating != null ? this.averageRating : 0.0;

        double totalRatingScore = currentAverage * currentNumRatings;
        this.numRatings = currentNumRatings + 1;
        this.averageRating = (totalRatingScore + rating) / this.numRatings;

        if (rating >= 4) {
            this.positiveReviews = (this.positiveReviews != null ? this.positiveReviews : 0) + 1;
        } else if (rating == 3) {
            this.neutralReviews = (this.neutralReviews != null ? this.neutralReviews : 0) + 1;
        } else {
            this.negativeReviews = (this.negativeReviews != null ? this.negativeReviews : 0) + 1;
        }
        this.totalReviews = this.numRatings;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isInStock() {
        return quantity != null && quantity > 0;
    }

    public boolean isLowStock() {
        return quantity != null && quantity <= (minimumStockLevel != null ? minimumStockLevel : 5);
    }

    public boolean isOutOfStock() {
        return quantity == null || quantity <= 0;
    }

    public Double getProfitMargin() {
        if (sellingPrice == null || sellingPrice == 0 || costPerItem == null) {
            return 0.0;
        }
        return ((sellingPrice - costPerItem) / sellingPrice) * 100;
    }

    public Double getTotalValue() {
        if (quantity == null || sellingPrice == null) {
            return 0.0;
        }
        return quantity * sellingPrice;
    }

    public String getDynamicShortDescription() {
        if (description == null) return "";
        return description.length() > 200 ? description.substring(0, 200) + "..." : description;
    }

    private void ensureCategoriesInitialized() { if (this.categories == null) this.categories = new ArrayList<>(); }
    private void ensureColorsInitialized() { if (this.colors == null) this.colors = new ArrayList<>(); }
    private void ensureSizesInitialized() { if (this.sizes == null) this.sizes = new ArrayList<>(); }
    private void ensureImagesInitialized() { if (this.images == null) this.images = new ArrayList<>(); }

    private static String generateSlug(String title) {
        if (title == null) return "";
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .trim();
    }

    @Override
    public String toString() {
        return String.format("Product{id=%s, title='%s', sellerId=%s, status=%s, price=%s}",
                id, title, sellerId, status, sellingPrice);
    }
}