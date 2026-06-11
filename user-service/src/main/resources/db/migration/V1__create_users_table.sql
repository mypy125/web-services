CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    profile_image TEXT,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_CUSTOMER',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    account_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    default_address_id UUID,
    default_payment_method_id UUID,
    total_orders_count INTEGER DEFAULT 0,
    total_spent_amount DECIMAL(10,2) DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    email_verified_at TIMESTAMP,

    CONSTRAINT users_role_check CHECK (role IN ('ROLE_CUSTOMER', 'ROLE_SELLER', 'ROLE_ADMIN', 'ROLE_MODERATOR', 'ROLE_ANALYST')),
    CONSTRAINT users_status_check CHECK (account_status IN ('ACTIVE', 'SUSPENDED', 'BANNED', 'PENDING_VERIFICATION'))
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_account_status ON users(account_status);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);
CREATE INDEX IF NOT EXISTS idx_users_last_login_at ON users(last_login_at);
CREATE INDEX IF NOT EXISTS idx_users_full_name ON users(full_name);

COMMENT ON TABLE users IS 'Хранит основную информацию о пользователях';
COMMENT ON COLUMN users.id IS 'Уникальный идентификатор пользователя';
COMMENT ON COLUMN users.email IS 'Email пользователя (уникальный)';
COMMENT ON COLUMN users.full_name IS 'Полное имя пользователя';
COMMENT ON COLUMN users.phone_number IS 'Номер телефона';
COMMENT ON COLUMN users.profile_image IS 'URL аватара пользователя';
COMMENT ON COLUMN users.role IS 'Роль пользователя (CUSTOMER, SELLER, ADMIN, MODERATOR, ANALYST)';
COMMENT ON COLUMN users.email_verified IS 'Флаг верификации email';
COMMENT ON COLUMN users.account_status IS 'Статус аккаунта (ACTIVE, SUSPENDED, BANNED, PENDING_VERIFICATION)';
COMMENT ON COLUMN users.default_address_id IS 'ID адреса по умолчанию (ссылка на address-service)';
COMMENT ON COLUMN users.default_payment_method_id IS 'ID метода оплаты по умолчанию (ссылка на payment-service)';
COMMENT ON COLUMN users.total_orders_count IS 'Общее количество заказов (кэш)';
COMMENT ON COLUMN users.total_spent_amount IS 'Общая сумма потраченных средств (кэш)';

CREATE TABLE IF NOT EXISTS user_statistics (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,

    total_orders INTEGER NOT NULL DEFAULT 0,
    total_spent DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    average_order_value DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    last_order_date TIMESTAMP,

    total_products_purchased INTEGER NOT NULL DEFAULT 0,
    most_purchased_category VARCHAR(100),
    favorite_product_id UUID,
    favorite_product_name VARCHAR(255),

    total_reviews INTEGER NOT NULL DEFAULT 0,
    average_rating DECIMAL(3,2) NOT NULL DEFAULT 0.00,

    last_active_at TIMESTAMP,
    days_active INTEGER NOT NULL DEFAULT 0,
    consecutive_login_days INTEGER NOT NULL DEFAULT 0,

    wishlist_items_count INTEGER NOT NULL DEFAULT 0,
    cart_items_count INTEGER NOT NULL DEFAULT 0,

    coupons_used INTEGER NOT NULL DEFAULT 0,
    total_discount_received DECIMAL(10,2) NOT NULL DEFAULT 0.00,

    loyalty_points INTEGER NOT NULL DEFAULT 0,
    loyalty_tier VARCHAR(20) NOT NULL DEFAULT 'BRONZE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT user_stats_tier_check CHECK (loyalty_tier IN ('BRONZE', 'SILVER', 'GOLD', 'PLATINUM')),
    CONSTRAINT user_stats_rating_check CHECK (average_rating >= 0 AND average_rating <= 5)
);

CREATE INDEX IF NOT EXISTS idx_user_stats_tier ON user_statistics(loyalty_tier);
CREATE INDEX IF NOT EXISTS idx_user_stats_points ON user_statistics(loyalty_points);
CREATE INDEX IF NOT EXISTS idx_user_stats_last_active ON user_statistics(last_active_at);
CREATE INDEX IF NOT EXISTS idx_user_stats_total_spent ON user_statistics(total_spent);
CREATE INDEX IF NOT EXISTS idx_user_stats_total_orders ON user_statistics(total_orders);

COMMENT ON TABLE user_statistics IS 'Хранит статистику и метрики пользователей';
COMMENT ON COLUMN user_statistics.user_id IS 'ID пользователя (ссылка на users)';
COMMENT ON COLUMN user_statistics.total_orders IS 'Общее количество заказов';
COMMENT ON COLUMN user_statistics.total_spent IS 'Общая сумма потраченных средств';
COMMENT ON COLUMN user_statistics.average_order_value IS 'Средняя стоимость заказа';
COMMENT ON COLUMN user_statistics.loyalty_points IS 'Количество бонусных баллов';
COMMENT ON COLUMN user_statistics.loyalty_tier IS 'Уровень лояльности (BRONZE, SILVER, GOLD, PLATINUM)';

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_user_statistics_updated_at
    BEFORE UPDATE ON user_statistics
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE FUNCTION create_user_statistics()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO user_statistics (user_id)
    VALUES (NEW.id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_create_user_statistics
    AFTER INSERT ON users
    FOR EACH ROW
    EXECUTE FUNCTION create_user_statistics();

CREATE OR REPLACE FUNCTION update_loyalty_tier()
RETURNS TRIGGER AS $$
BEGIN
    NEW.loyalty_tier = CASE
        WHEN NEW.loyalty_points >= 10000 THEN 'PLATINUM'
        WHEN NEW.loyalty_points >= 5000 THEN 'GOLD'
        WHEN NEW.loyalty_points >= 1000 THEN 'SILVER'
        ELSE 'BRONZE'
    END;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_loyalty_tier
    BEFORE UPDATE OF loyalty_points ON user_statistics
    FOR EACH ROW
    EXECUTE FUNCTION update_loyalty_tier();

CREATE INDEX IF NOT EXISTS idx_users_role_status ON users(role, account_status);
CREATE INDEX IF NOT EXISTS idx_users_name_email ON users(full_name, email);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(account_status) WHERE account_status = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_user_stats_loyalty_range ON user_statistics(loyalty_points);
CREATE INDEX IF NOT EXISTS idx_users_registration_date ON users(date(created_at));

CREATE OR REPLACE VIEW admin_user_dashboard AS
SELECT
    COUNT(*) as total_users,
    COUNT(CASE WHEN account_status = 'ACTIVE' THEN 1 END) as active_users,
    COUNT(CASE WHEN account_status = 'SUSPENDED' THEN 1 END) as suspended_users,
    COUNT(CASE WHEN account_status = 'BANNED' THEN 1 END) as banned_users,
    COUNT(CASE WHEN role = 'ROLE_CUSTOMER' THEN 1 END) as customers,
    COUNT(CASE WHEN role = 'ROLE_SELLER' THEN 1 END) as sellers,
    COUNT(CASE WHEN role = 'ROLE_ADMIN' THEN 1 END) as admins,
    COUNT(CASE WHEN email_verified = true THEN 1 END) as verified_emails,
    AVG(total_spent_amount) as avg_spent,
    NOW() as calculated_at
FROM users;

CREATE OR REPLACE VIEW loyalty_statistics AS
SELECT
    loyalty_tier,
    COUNT(*) as users_count,
    AVG(loyalty_points) as avg_points,
    SUM(total_spent) as total_spent,
    AVG(average_order_value) as avg_order_value,
    NOW() as calculated_at
FROM user_statistics
GROUP BY loyalty_tier
ORDER BY
    CASE loyalty_tier
        WHEN 'PLATINUM' THEN 1
        WHEN 'GOLD' THEN 2
        WHEN 'SILVER' THEN 3
        ELSE 4
    END;