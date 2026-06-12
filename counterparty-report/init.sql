-- Таблица пользователей
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица запросов на проверку
CREATE TABLE verification_requests (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    inn_ogrn VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица данных о контрагенте
CREATE TABLE counterparty_data (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL UNIQUE REFERENCES verification_requests(id) ON DELETE CASCADE,
    inn VARCHAR(20),
    ogrn VARCHAR(20),
    name VARCHAR(500),
    address VARCHAR(500),
    status VARCHAR(50),
    registration_date VARCHAR(50),
    full_data TEXT
);