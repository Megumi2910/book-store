-- Fix price column precision to handle large VND amounts
-- Run this script on your database to update the column definitions

-- Update orders.total_amount column
ALTER TABLE orders MODIFY COLUMN total_amount DECIMAL(15,0) NOT NULL;

-- Update order_item.price_at_purchase column  
ALTER TABLE order_item MODIFY COLUMN price_at_purchase DECIMAL(15,0) NOT NULL;

-- Update book_detail.price column
ALTER TABLE book_detail MODIFY COLUMN price DECIMAL(15,0) NOT NULL;

