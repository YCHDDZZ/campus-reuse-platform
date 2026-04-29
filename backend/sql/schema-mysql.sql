CREATE DATABASE IF NOT EXISTS campus_reuse DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE campus_reuse;

DROP TABLE IF EXISTS donation_record;
DROP TABLE IF EXISTS product_report;
DROP TABLE IF EXISTS trade_review;
DROP TABLE IF EXISTS trade_order;
DROP TABLE IF EXISTS product_message;
DROP TABLE IF EXISTS product_favorite;
DROP TABLE IF EXISTS idle_product;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS platform_user;

CREATE TABLE platform_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    avatar_url VARCHAR(255),
    role VARCHAR(20) NOT NULL,
    audit_status VARCHAR(20) NOT NULL,
    disabled TINYINT DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME,
    KEY idx_user_audit_status(audit_status),
    KEY idx_user_role(role),
    KEY idx_user_created_at(created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    sort_no INT DEFAULT 0,
    enabled TINYINT DEFAULT 1,
    created_at DATETIME,
    updated_at DATETIME,
    KEY idx_category_enabled_sort(enabled, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE idle_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    audit_status VARCHAR(20) NOT NULL,
    donation_enabled TINYINT DEFAULT 0,
    images_json TEXT,
    contact_qr_url VARCHAR(255),
    view_count INT DEFAULT 0,
    favorite_count INT DEFAULT 0,
    recycle_score INT DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME,
    KEY idx_product_category_id(category_id),
    KEY idx_product_seller_id(seller_id),
    KEY idx_product_audit_status(audit_status),
    KEY idx_product_status(status),
    KEY idx_product_created_at(created_at),
    KEY idx_product_search_main(audit_status, status, category_id, created_at),
    KEY idx_product_hot_sort(audit_status, status, favorite_count, view_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at DATETIME,
    UNIQUE KEY uk_user_product(user_id, product_id),
    KEY idx_favorite_user_created(user_id, created_at),
    KEY idx_favorite_product(product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    read_flag TINYINT DEFAULT 0,
    created_at DATETIME,
    KEY idx_message_product_created(product_id, created_at),
    KEY idx_message_to_user_read(to_user_id, read_flag, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE trade_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    dispute_reason VARCHAR(500),
    created_at DATETIME,
    updated_at DATETIME,
    finished_at DATETIME,
    KEY idx_order_buyer_created(buyer_id, created_at),
    KEY idx_order_seller_created(seller_id, created_at),
    KEY idx_order_status_created(status, created_at),
    KEY idx_order_product(product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE trade_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    reviewee_id BIGINT NOT NULL,
    score INT NOT NULL,
    content VARCHAR(500),
    created_at DATETIME,
    KEY idx_review_product_created(product_id, created_at),
    KEY idx_review_order_reviewer(order_id, reviewer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    handler_note VARCHAR(500),
    created_at DATETIME,
    updated_at DATETIME,
    KEY idx_report_status_created(status, created_at),
    KEY idx_report_product(product_id),
    KEY idx_report_reporter(reporter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE donation_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    donor_id BIGINT NOT NULL,
    organization VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    received_note VARCHAR(500),
    created_at DATETIME,
    updated_at DATETIME,
    KEY idx_donation_donor_created(donor_id, created_at),
    KEY idx_donation_status_created(status, created_at),
    KEY idx_donation_product(product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
