CREATE TABLE IF NOT EXISTS sellers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    seller_name VARCHAR(255) NOT NULL,
    store_name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    mobile VARCHAR(50),
    phone_number VARCHAR(50),
    profile_image TEXT,
    cover_image TEXT,

    business_details JSONB,
    bank_details JSONB,
    pickup_address JSONB,
    return_address JSONB,
    warehouse_addresses JSONB,

    gst_number VARCHAR(100),
    pan_number VARCHAR(100),
    tin_number VARCHAR(100),
    business_registration_number VARCHAR(100),

    tax_info_verified BOOLEAN NOT NULL DEFAULT FALSE,
    tax_info_verified_at TIMESTAMP,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    account_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    verification_document JSONB,
    rejection_reason TEXT,
    rejected_at TIMESTAMP,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_SELLER',

    commission_rate DECIMAL(5,2),
    minimum_commission_rate DECIMAL(5,2),
    maximum_commission_rate DECIMAL(5,2),
    cashback_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    auto_accept_orders BOOLEAN NOT NULL DEFAULT FALSE,
    auto_confirm_delivery BOOLEAN NOT NULL DEFAULT FALSE,
    processing_time_days INTEGER NOT NULL DEFAULT 1,
    shipping_time_days INTEGER NOT NULL DEFAULT 3,
    free_shipping_threshold DECIMAL(15,2),
    domestic_shipping_cost DECIMAL(15,2),
    international_shipping_cost DECIMAL(15,2),

    store_logo TEXT,
    store_banner TEXT,
    store_description TEXT,
    store_tagline VARCHAR(255),
    store_website VARCHAR(255),
    store_email VARCHAR(255),
    store_phone VARCHAR(50),
    social_media_links TEXT,
    store_category VARCHAR(100),
    store_categories JSONB,

    average_rating DECIMAL(3,2) NOT NULL DEFAULT 0.00,
    total_reviews INTEGER NOT NULL DEFAULT 0,
    total_positive_reviews INTEGER NOT NULL DEFAULT 0,
    total_neutral_reviews INTEGER NOT NULL DEFAULT 0,
    total_negative_reviews INTEGER NOT NULL DEFAULT 0,
    response_rate DECIMAL(5,2),
    response_time_hours DECIMAL(5,2),
    followers_count INTEGER NOT NULL DEFAULT 0,
    total_products INTEGER NOT NULL DEFAULT 0,
    total_active_products INTEGER NOT NULL DEFAULT 0,
    total_out_of_stock_products INTEGER NOT NULL DEFAULT 0,

    total_orders INTEGER NOT NULL DEFAULT 0,
    total_sales DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_earnings DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_refunds DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_tax DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    net_earnings DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    average_order_value DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    conversion_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    return_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    cancellation_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    canceled_orders INTEGER NOT NULL DEFAULT 0,
    total_transactions INTEGER NOT NULL DEFAULT 0,
    total_commission_paid DECIMAL(15,2) NOT NULL DEFAULT 0.00,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    email_verified_at TIMESTAMP,
    business_verified_at TIMESTAMP,
    last_active_at TIMESTAMP,
    commission_updated_at TIMESTAMP,
    suspended_at TIMESTAMP,
    banned_at TIMESTAMP,
    reactivated_at TIMESTAMP,
    business_hours JSONB,

    CONSTRAINT sellers_role_check CHECK (role IN ('ROLE_SELLER', 'ROLE_ADMIN')),
    CONSTRAINT sellers_verification_check CHECK (verification_status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT sellers_status_check CHECK (account_status IN ('ACTIVE', 'SUSPENDED', 'BANNED', 'PENDING_VERIFICATION')),
    CONSTRAINT sellers_rating_check CHECK (average_rating >= 0 AND average_rating <= 5)
);

CREATE TABLE IF NOT EXISTS seller_addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id UUID NOT NULL REFERENCES sellers(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    phone_number VARCHAR(50),
    landmark VARCHAR(255),
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    address_type VARCHAR(50) NOT NULL DEFAULT 'WAREHOUSE',

    CONSTRAINT seller_addresses_type_check CHECK (address_type IN ('PICKUP', 'RETURN', 'WAREHOUSE', 'OFFICE'))
);

CREATE TABLE IF NOT EXISTS seller_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id UUID NOT NULL REFERENCES sellers(id) ON DELETE CASCADE,
    period VARCHAR(50) NOT NULL,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,

    total_earnings DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_sales DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_refunds DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_tax DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    net_earnings DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_commission DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_shipping_cost DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_discount_given DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_cashback_given DECIMAL(15,2) NOT NULL DEFAULT 0.00,

    total_orders INTEGER NOT NULL DEFAULT 0,
    completed_orders INTEGER NOT NULL DEFAULT 0,
    canceled_orders INTEGER NOT NULL DEFAULT 0,
    returned_orders INTEGER NOT NULL DEFAULT 0,
    refunded_orders INTEGER NOT NULL DEFAULT 0,
    pending_orders INTEGER NOT NULL DEFAULT 0,
    processing_orders INTEGER NOT NULL DEFAULT 0,
    shipped_orders INTEGER NOT NULL DEFAULT 0,
    delivered_orders INTEGER NOT NULL DEFAULT 0,
    total_transactions INTEGER NOT NULL DEFAULT 0,

    total_products_sold INTEGER NOT NULL DEFAULT 0,
    total_unique_products_sold INTEGER NOT NULL DEFAULT 0,
    best_selling_product_id VARCHAR(255),
    best_selling_product_name VARCHAR(255),
    best_selling_product_quantity INTEGER NOT NULL DEFAULT 0,
    best_selling_product_revenue DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    top_category VARCHAR(100),
    top_category_sales INTEGER NOT NULL DEFAULT 0,

    total_customers INTEGER NOT NULL DEFAULT 0,
    new_customers INTEGER NOT NULL DEFAULT 0,
    returning_customers INTEGER NOT NULL DEFAULT 0,
    customer_retention_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    average_order_value DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    average_customer_lifetime_value DECIMAL(15,2) NOT NULL DEFAULT 0.00,

    average_rating DECIMAL(3,2) NOT NULL DEFAULT 0.00,
    total_reviews INTEGER NOT NULL DEFAULT 0,
    positive_reviews INTEGER NOT NULL DEFAULT 0,
    neutral_reviews INTEGER NOT NULL DEFAULT 0,
    negative_reviews INTEGER NOT NULL DEFAULT 0,

    response_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    average_response_time_hours DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    conversion_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    return_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    cancellation_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    refund_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    fulfillment_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    on_time_delivery_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    profit_margin DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    growth_percentage DECIMAL(5,2) NOT NULL DEFAULT 0.00,

    comparison_metrics JSONB,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    report_generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT seller_reports_period_check CHECK (period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY', 'CUSTOM')),
    CONSTRAINT seller_reports_rating_check CHECK (average_rating >= 0 AND average_rating <= 5)
);

CREATE INDEX IF NOT EXISTS idx_sellers_email ON sellers(email);
CREATE INDEX IF NOT EXISTS idx_sellers_account_status ON sellers(account_status);
CREATE INDEX IF NOT EXISTS idx_sellers_verification_status ON sellers(verification_status);
CREATE INDEX IF NOT EXISTS idx_sellers_created_at ON sellers(created_at);
CREATE INDEX IF NOT EXISTS idx_sellers_role_status ON sellers(role, account_status);
CREATE INDEX IF NOT EXISTS idx_sellers_store_name ON sellers(store_name);

CREATE INDEX IF NOT EXISTS idx_sellers_banned ON sellers(banned_at) WHERE banned_at IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_seller_addresses_seller_id ON seller_addresses(seller_id);
CREATE INDEX IF NOT EXISTS idx_seller_addresses_type ON seller_addresses(address_type);

CREATE INDEX IF NOT EXISTS idx_seller_reports_seller_id ON seller_reports(seller_id);
CREATE INDEX IF NOT EXISTS idx_seller_reports_period ON seller_reports(period);
CREATE INDEX IF NOT EXISTS idx_seller_reports_metrics ON seller_reports(seller_id, period, period_start, period_end);

COMMENT ON TABLE sellers IS 'Stores basic information about sellers, their settings, and cached financial indicators';
COMMENT ON COLUMN sellers.id IS 'Unique Seller Identifier';
COMMENT ON COLUMN sellers.email IS 'Seller email';
COMMENT ON COLUMN sellers.business_details IS 'Company legal data in JSONB format';
COMMENT ON COLUMN sellers.bank_details IS 'Bank details in JSONB format';
COMMENT ON COLUMN sellers.account_status IS 'Account status (ACTIVE, SUSPENDED, BANNED, PENDING_VERIFICATION)';
COMMENT ON COLUMN sellers.commission_rate IS 'Basic commission rate for the seller';

COMMENT ON TABLE seller_addresses IS 'Physical addresses of warehouses, pickup points, and seller returns';
COMMENT ON TABLE seller_reports IS 'Generated analytical reports on sales, customers and performance for periods';

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_sellers_updated_at
    BEFORE UPDATE ON sellers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_seller_reports_updated_at
    BEFORE UPDATE ON seller_reports
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE VIEW admin_seller_dashboard AS
SELECT
    COUNT(*) as total_sellers,
    COUNT(CASE WHEN account_status = 'ACTIVE' THEN 1 END) as active_sellers,
    COUNT(CASE WHEN account_status = 'PENDING_VERIFICATION' THEN 1 END) as pending_verification_sellers,
    COUNT(CASE WHEN account_status = 'SUSPENDED' THEN 1 END) as suspended_sellers,
    COUNT(CASE WHEN account_status = 'BANNED' THEN 1 END) as banned_sellers,
    COUNT(CASE WHEN verification_status = 'APPROVED' THEN 1 END) as fully_verified,
    SUM(total_sales) as platform_total_sales,
    SUM(total_commission_paid) as platform_earned_commission,
    AVG(average_rating) as global_average_seller_rating,
    NOW() as calculated_at
FROM sellers;