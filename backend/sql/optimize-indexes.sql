ALTER TABLE platform_user
    ADD INDEX idx_user_audit_status (audit_status),
    ADD INDEX idx_user_role (role),
    ADD INDEX idx_user_created_at (created_at);

ALTER TABLE category
    ADD INDEX idx_category_enabled_sort (enabled, sort_no);

ALTER TABLE idle_product
    ADD INDEX idx_product_audit_status (audit_status),
    ADD INDEX idx_product_status (status),
    ADD INDEX idx_product_created_at (created_at),
    ADD INDEX idx_product_search_main (audit_status, status, category_id, created_at),
    ADD INDEX idx_product_hot_sort (audit_status, status, favorite_count, view_count);

ALTER TABLE product_favorite
    ADD INDEX idx_favorite_user_created (user_id, created_at),
    ADD INDEX idx_favorite_product (product_id);

ALTER TABLE product_message
    ADD INDEX idx_message_product_created (product_id, created_at),
    ADD INDEX idx_message_to_user_read (to_user_id, read_flag, created_at);

ALTER TABLE trade_order
    ADD INDEX idx_order_buyer_created (buyer_id, created_at),
    ADD INDEX idx_order_seller_created (seller_id, created_at),
    ADD INDEX idx_order_status_created (status, created_at),
    ADD INDEX idx_order_product (product_id);

ALTER TABLE trade_review
    ADD INDEX idx_review_product_created (product_id, created_at),
    ADD INDEX idx_review_order_reviewer (order_id, reviewer_id);

ALTER TABLE product_report
    ADD INDEX idx_report_status_created (status, created_at),
    ADD INDEX idx_report_product (product_id),
    ADD INDEX idx_report_reporter (reporter_id);

ALTER TABLE donation_record
    ADD INDEX idx_donation_donor_created (donor_id, created_at),
    ADD INDEX idx_donation_status_created (status, created_at),
    ADD INDEX idx_donation_product (product_id);
