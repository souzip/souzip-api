CREATE TABLE admin (
                       id UUID PRIMARY KEY,
                       username VARCHAR(20) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL,
                       login_fail_count INT NOT NULL DEFAULT 0,
                       locked_at TIMESTAMP,
                       last_login_at TIMESTAMP,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_admin_username ON admin(username);
