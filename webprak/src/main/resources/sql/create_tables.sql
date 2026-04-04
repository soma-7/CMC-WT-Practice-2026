CREATE TYPE operation_type AS ENUM (
    'deposit',
    'withdrawal',
    'service_payment',
    'service_purchase',
    'service_cancellation'
    );

CREATE TABLE clients (
                         client_id SERIAL PRIMARY KEY,
                         info JSONB NOT NULL,
                         registration_date TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE services (
                          service_id SERIAL PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          description TEXT,
                          includes JSONB,
                          other JSONB
);

CREATE TABLE profiles (
                          profile_id SERIAL PRIMARY KEY,
                          client_id INTEGER NOT NULL REFERENCES clients(client_id) ON DELETE CASCADE,
                          name VARCHAR(200) NOT NULL,
                          phone VARCHAR(20) UNIQUE NOT NULL,
                          balance FLOAT NOT NULL DEFAULT 0.0 CHECK (balance >= 0),
                          other JSONB,
                          created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE operations (
                            operation_id SERIAL PRIMARY KEY,
                            profile_id INTEGER NOT NULL REFERENCES profiles(profile_id) ON DELETE CASCADE,
                            type operation_type NOT NULL,
                            timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
                            service_id INTEGER REFERENCES services(service_id) ON DELETE SET NULL,
                            balance_change FLOAT NOT NULL,
                            description TEXT
);