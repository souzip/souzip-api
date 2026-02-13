CREATE TABLE admin_refresh_token (
     id UUID PRIMARY KEY,
     admin_id UUID NOT NULL,
     token VARCHAR(500) UNIQUE NOT NULL,
     expires_at TIMESTAMP NOT NULL,
     created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_admin_refresh_token_admin_id ON admin_refresh_token(admin_id);
CREATE INDEX idx_admin_refresh_token_token ON admin_refresh_token(token);
