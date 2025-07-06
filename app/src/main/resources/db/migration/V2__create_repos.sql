CREATE TABLE repo_visibilities (
    id SMALLINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

INSERT INTO repo_visibilities (name) VALUES ('public');
INSERT INTO repo_visibilities (name) VALUES ('private');
INSERT INTO repo_visibilities (name) VALUES ('unlisted');

CREATE TABLE repos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    UNIQUE (user_id, name),
    visibility_id SMALLINT NOT NULL,
    FOREIGN KEY (visibility_id) REFERENCES repo_visibilities(id)
);