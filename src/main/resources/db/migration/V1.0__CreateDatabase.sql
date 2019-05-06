CREATE TABLE customers (
  id INTEGER NOT NULL CONSTRAINT customer_pk PRIMARY KEY,
  name VARCHAR(100),
  uuid CHAR(36)
);

CREATE TABLE accounts (
  id INTEGER NOT NULL CONSTRAINT account_pk PRIMARY KEY,
  iban_number VARCHAR(34),
  uuid CHAR(36),
  total_amount DECIMAL,
  currency CHAR(3),
  customer_id INTEGER
);

CREATE TABLE account_transactions (
   id INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1),
   uuid CHAR(36),
   type VARCHAR(10),
   account_id INTEGER,
   amount DECIMAL,
   currency CHAR(3),
   date_time TIMESTAMP,
   CONSTRAINT transaction_pk PRIMARY KEY(id)
);