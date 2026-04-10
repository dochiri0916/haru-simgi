CREATE TABLE habit_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    public_id VARCHAR(36) UNIQUE NOT NULL,
    owner_type VARCHAR(20) NOT NULL,
    owner_reference_id VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    color_index INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    KEY idx_owner (owner_type, owner_reference_id)
);

CREATE TABLE habits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    public_id VARCHAR(36) UNIQUE NOT NULL,
    owner_type VARCHAR(20) NOT NULL,
    owner_reference_id VARCHAR(255) NOT NULL,
    category_id BIGINT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(10) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    KEY idx_owner (owner_type, owner_reference_id),
    KEY idx_category (category_id),
    FOREIGN KEY (category_id) REFERENCES habit_categories(id) ON DELETE SET NULL
);

CREATE TABLE habit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    public_id VARCHAR(36) UNIQUE NOT NULL,
    habit_id BIGINT NOT NULL,
    logged_date DATE NOT NULL,
    value INT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_habit_log_date (habit_id, logged_date),
    KEY idx_logged_date (logged_date),
    FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE
);
