ALTER TABLE accounts DROP CONSTRAINT accounts_role_check;
ALTER TABLE accounts ADD CONSTRAINT accounts_role_check CHECK(role IN ('PASSENGER','ADMIN','DRIVER','COUNTER_STAFF'));
ALTER TABLE bookings ALTER COLUMN passenger_id DROP NOT NULL;
ALTER TABLE bookings ALTER COLUMN hold_id DROP NOT NULL;
ALTER TABLE bookings ADD COLUMN sales_channel varchar(16) NOT NULL DEFAULT 'ONLINE';
ALTER TABLE bookings ADD COLUMN sold_by_id uuid REFERENCES accounts(id);
ALTER TABLE bookings ADD COLUMN payment_updated_by_id uuid REFERENCES accounts(id);
ALTER TABLE bookings ADD COLUMN payment_updated_at timestamptz;
ALTER TABLE bookings ADD CONSTRAINT bookings_channel_check CHECK (
 (sales_channel='ONLINE' AND passenger_id IS NOT NULL AND hold_id IS NOT NULL)
 OR (sales_channel='COUNTER' AND passenger_id IS NULL AND hold_id IS NULL AND sold_by_id IS NOT NULL));
ALTER TABLE bookings ADD CONSTRAINT bookings_payment_check CHECK(payment_status IN ('UNPAID','PAID','REFUND_DUE','REFUNDED'));
ALTER TABLE bookings ADD CONSTRAINT bookings_method_check CHECK(payment_method IN ('PAY_ON_BOARD','CASH_COUNTER','CASH_ON_BOARD'));
CREATE INDEX bookings_sold_by ON bookings(sold_by_id);
