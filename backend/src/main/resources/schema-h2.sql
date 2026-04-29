DROP TABLE IF EXISTS platform_user;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS idle_product;
DROP TABLE IF EXISTS product_favorite;
DROP TABLE IF EXISTS product_message;
DROP TABLE IF EXISTS trade_order;
DROP TABLE IF EXISTS trade_review;
DROP TABLE IF EXISTS product_report;
DROP TABLE IF EXISTS donation_record;

CREATE TABLE platform_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    avatar_url VARCHAR(255),
    role VARCHAR(20) NOT NULL,
    audit_status VARCHAR(20) NOT NULL,
    disabled INT DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    sort_no INT DEFAULT 0,
    enabled INT DEFAULT 1,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE idle_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description CLOB,
    price DECIMAL(10,2) NOT NULL,
    category_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    audit_status VARCHAR(20) NOT NULL,
    donation_enabled INT DEFAULT 0,
    images_json CLOB,
    contact_qr_url VARCHAR(255),
    view_count INT DEFAULT 0,
    favorite_count INT DEFAULT 0,
    recycle_score INT DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE product_favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at TIMESTAMP
);

CREATE TABLE product_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    read_flag INT DEFAULT 0,
    created_at TIMESTAMP
);

CREATE TABLE trade_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL,
    product_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    dispute_reason VARCHAR(500),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    finished_at TIMESTAMP
);

CREATE TABLE trade_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    reviewee_id BIGINT NOT NULL,
    score INT NOT NULL,
    content VARCHAR(500),
    created_at TIMESTAMP
);

CREATE TABLE product_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    handler_note VARCHAR(500),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE donation_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    donor_id BIGINT NOT NULL,
    organization VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    received_note VARCHAR(500),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
