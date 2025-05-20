-- 3NF
CREATE TABLE IF NOT EXISTS users (
    PRIMARY KEY(id),
    id               UUID,
    username         VARCHAR(64)   NOT NULL,
                     CONSTRAINT users_username_key
                         UNIQUE (username),
                     CONSTRAINT users_username_not_empty_check
                          CHECK (length(trim(username)) > 0),
    password_hash    VARCHAR(128)  NOT NULL,
    email            VARCHAR(376),
    role VARCHAR(15) NOT NULL
);

CREATE INDEX IF NOT EXISTS users_email_idx ON users(email);

-- 3NF
CREATE TABLE IF NOT EXISTS items (
    PRIMARY KEY(id),
    id               UUID,
    name             VARCHAR(255) NOT NULL,
    type             VARCHAR(20) NOT NULL CHECK (type IN ('COIN', 'ANTIQUE')),
    description      TEXT,
    production_year  VARCHAR(50) NOT NULL,
    country          VARCHAR(255),
    condition        VARCHAR(20) NOT NULL CHECK (condition IN ('EXCELLENT', 'GOOD', 'FAIR', 'POOR')),
    image_path       VARCHAR(2048)
);

-- 3NF
CREATE TABLE IF NOT EXISTS collections (
    PRIMARY KEY(id),
    id               UUID,
    user_id          UUID NOT NULL,
                      CONSTRAINT collections_user_id_users_id_fkey
                     FOREIGN KEY (user_id)
                      REFERENCES users(id)
                       ON DELETE CASCADE,
    name             VARCHAR(128) NOT NULL,
                     CONSTRAINT collections_name_not_empty_check
                          CHECK (length(trim(name)) > 0),
    created_at       TIMESTAMP
);

-- 2NF
CREATE TABLE IF NOT EXISTS item_collection (
    PRIMARY KEY(collection_id, item_id),
    collection_id   UUID NOT NULL,
                     CONSTRAINT item_collection_collection_id_collections_id_fkey
                    FOREIGN KEY (collection_id)
                     REFERENCES collections(id)
                      ON DELETE CASCADE,

    item_id    UUID NOT NULL,
                     CONSTRAINT item_collection_item_id_items_id_fkey
                    FOREIGN KEY (item_id)
                     REFERENCES items(id)
                      ON DELETE CASCADE
);

-- 3NF
CREATE TABLE IF NOT EXISTS transactions (
    PRIMARY KEY(id),
    id               UUID,
    user_id          UUID NOT NULL,
                      CONSTRAINT transactions_user_id_fkey
                     FOREIGN KEY (user_id)
                      REFERENCES users(id)
                       ON DELETE CASCADE,
    item_id          UUID NOT NULL,
                      CONSTRAINT transactions_item_id_fkey
                     FOREIGN KEY (item_id)
                      REFERENCES items(id)
                       ON DELETE CASCADE,
    type             VARCHAR(20) NOT NULL CHECK (type IN ('PURCHASE', 'SALE')),
    timestamp        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);