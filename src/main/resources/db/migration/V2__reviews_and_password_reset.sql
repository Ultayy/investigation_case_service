
ALTER TABLE users ADD COLUMN iin         VARCHAR(12);
ALTER TABLE users ADD COLUMN first_name  VARCHAR(255);
ALTER TABLE users ADD COLUMN last_name   VARCHAR(255);
ALTER TABLE users ADD COLUMN father_name VARCHAR(255);
CREATE UNIQUE INDEX uq_users_iin ON users (iin);


CREATE TABLE reviews (
    id            BIGSERIAL PRIMARY KEY,
    review_type   VARCHAR(32) NOT NULL,
    review_status VARCHAR(32) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    reviewed_by   BIGINT REFERENCES users (id),
    reviewed_at   TIMESTAMPTZ
);
CREATE INDEX idx_reviews_type_status ON reviews (review_type, review_status);


CREATE TABLE registration_reviews_details (
    id            BIGSERIAL PRIMARY KEY,
    login         VARCHAR(64)  NOT NULL,
    iin           VARCHAR(12)  NOT NULL,
    region_id     BIGINT       NOT NULL REFERENCES regions (id),
    first_name    VARCHAR(255) NOT NULL,
    last_name     VARCHAR(255) NOT NULL,
    father_name   VARCHAR(255),
    password_hash VARCHAR(255),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    review_status VARCHAR(32)  NOT NULL,
    reviewed_by   BIGINT REFERENCES users (id),
    reviewed_at   TIMESTAMPTZ,
    review_id     BIGINT       NOT NULL UNIQUE REFERENCES reviews (id)
);
CREATE UNIQUE INDEX uq_registration_pending_login ON registration_reviews_details (login) WHERE review_status = 'PENDING';
CREATE UNIQUE INDEX uq_registration_pending_iin   ON registration_reviews_details (iin)   WHERE review_status = 'PENDING';
CREATE INDEX idx_registration_region_status ON registration_reviews_details (region_id, review_status);

CREATE TABLE region_change_reviews (
    id                      BIGSERIAL PRIMARY KEY,
    created_by              BIGINT      NOT NULL REFERENCES users (id),
    old_region_id           BIGINT      NOT NULL REFERENCES regions (id),
    new_region_id           BIGINT      NOT NULL REFERENCES regions (id),
    departure_review_status VARCHAR(32) NOT NULL,
    reviewed_by             BIGINT REFERENCES users (id),
    reviewed_at             TIMESTAMPTZ,
    arrival_status          VARCHAR(32) NOT NULL,
    arrival_reviewed_by     BIGINT REFERENCES users (id),
    arrival_reviewed_at     TIMESTAMPTZ,
    review_id               BIGINT      NOT NULL UNIQUE REFERENCES reviews (id)
);
CREATE INDEX idx_region_change_old_region ON region_change_reviews (old_region_id);
CREATE INDEX idx_region_change_new_region ON region_change_reviews (new_region_id);
CREATE INDEX idx_region_change_created_by ON region_change_reviews (created_by);

CREATE TABLE password_reset_tokens (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users (id),
    code_hash  VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    verified   BOOLEAN      NOT NULL DEFAULT FALSE,
    attempts   INTEGER      NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_password_reset_user ON password_reset_tokens (user_id);
