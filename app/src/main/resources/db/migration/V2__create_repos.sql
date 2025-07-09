CREATE TABLE repos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    UNIQUE (user_id, name),
    visibility ENUM('PUBLIC', 'PRIVATE', 'UNLISTED') NOT NULL
);