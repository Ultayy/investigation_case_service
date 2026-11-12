CREATE TABLE regions (
    id   BIGSERIAL PRIMARY KEY,
    code VARCHAR(32)  NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    login           VARCHAR(64)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    role            VARCHAR(32)  NOT NULL,
    region_id       BIGINT       NOT NULL REFERENCES regions (id),
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    failed_attempts INTEGER      NOT NULL DEFAULT 0,
    locked_until    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE refresh_tokens (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users (id),
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE cases (
    id          BIGSERIAL PRIMARY KEY,
    number      VARCHAR(64)  NOT NULL UNIQUE,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    status      VARCHAR(32)  NOT NULL,
    owner_id    BIGINT       NOT NULL REFERENCES users (id),
    region_id   BIGINT       NOT NULL REFERENCES regions (id),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE case_members (
    case_id     BIGINT      NOT NULL REFERENCES cases (id),
    user_id     BIGINT      NOT NULL REFERENCES users (id),
    member_type VARCHAR(32) NOT NULL,
    can_view    BOOLEAN     NOT NULL,
    can_edit    BOOLEAN     NOT NULL,
    granted_by  BIGINT      NOT NULL REFERENCES users (id),
    granted_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (case_id, user_id)
);

CREATE TABLE case_files (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    case_id BIGINT       NOT NULL REFERENCES cases (id)
);

CREATE TABLE file_access (
    id         BIGSERIAL PRIMARY KEY,
    file_id    BIGINT      NOT NULL REFERENCES case_files (id),
    user_id    BIGINT      NOT NULL REFERENCES users (id),
    granted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (file_id, user_id)
);

CREATE TABLE audit_events (
    id          BIGSERIAL PRIMARY KEY,
    case_id     BIGINT       NOT NULL,
    actor_id    BIGINT       NOT NULL REFERENCES users (id),
    event_type  VARCHAR(32)  NOT NULL,
    object_type VARCHAR(32)  NOT NULL,
    object_id   VARCHAR(255) NOT NULL,
    old_value   TEXT,
    new_value   TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_events_case_created ON audit_events (case_id, created_at);
