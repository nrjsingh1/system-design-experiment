-- Create sample data
DO $$
DECLARE
    i INTEGER;
    j INTEGER;
    customer_id BIGINT;
    product_id BIGINT;
    order_id BIGINT;
    order_items_count INTEGER;
    order_total DECIMAL(10,2);
    product_categories VARCHAR[] := ARRAY['Electronics', 'Books', 'Clothing', 'Home & Kitchen', 'Sports', 'Toys', 'Beauty', 'Automotive', 'Garden', 'Food'];
    product_names VARCHAR[] := ARRAY['Premium', 'Basic', 'Pro', 'Ultra', 'Essential', 'Deluxe', 'Classic', 'Elite', 'Standard', 'Advanced'];
    order_statuses VARCHAR[] := ARRAY['PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'];
    first_names VARCHAR[] := ARRAY['James', 'Mary', 'John', 'Patricia', 'Robert', 'Jennifer', 'Michael', 'Linda', 'William', 'Elizabeth', 'David', 'Susan', 'Richard', 'Jessica', 'Joseph', 'Sarah', 'Thomas', 'Karen', 'Charles', 'Nancy'];
    last_names VARCHAR[] := ARRAY['Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis', 'Rodriguez', 'Martinez', 'Hernandez', 'Lopez', 'Gonzalez', 'Wilson', 'Anderson', 'Thomas', 'Taylor', 'Moore', 'Jackson', 'Martin'];
BEGIN
    -- Insert 500 products
    FOR i IN 1..500 LOOP
        INSERT INTO products (name, category, price, stock)
        VALUES (
            product_names[1 + mod(i, array_length(product_names, 1))] || ' ' || 
            product_categories[1 + mod(i, array_length(product_categories, 1))] || ' ' || 
            (i::text),
            product_categories[1 + mod(i, array_length(product_categories, 1))],
            (random() * 990 + 10)::numeric(10,2),  -- Prices between 10 and 1000
            (random() * 200 + 50)::integer         -- Stock between 50 and 250
        );
    END LOOP;

    -- Insert 10000 customers
    FOR i IN 1..10000 LOOP
        INSERT INTO customers (first_name, last_name, email, phone, address)
        VALUES (
            first_names[1 + mod(i, array_length(first_names, 1))],
            last_names[1 + mod(i, array_length(last_names, 1))],
            lower(first_names[1 + mod(i, array_length(first_names, 1))] || '.' || 
            last_names[1 + mod(i, array_length(last_names, 1))] || i || '@example.com'),
            '+1' || (floor(random() * 900 + 100)::text) || 
            (floor(random() * 900 + 100)::text) || 
            (floor(random() * 9000 + 1000)::text),
            floor(random() * 9900 + 100)::text || ' ' || 
            CASE mod(i, 4) 
                WHEN 0 THEN 'Main Street'
                WHEN 1 THEN 'Oak Avenue'
                WHEN 2 THEN 'Maple Road'
                WHEN 3 THEN 'Cedar Lane'
            END || ', ' ||
            CASE mod(i, 5)
                WHEN 0 THEN 'New York'
                WHEN 1 THEN 'Los Angeles'
                WHEN 2 THEN 'Chicago'
                WHEN 3 THEN 'Houston'
                WHEN 4 THEN 'Phoenix'
            END || ', ' ||
            CASE mod(i, 5)
                WHEN 0 THEN 'NY'
                WHEN 1 THEN 'CA'
                WHEN 2 THEN 'IL'
                WHEN 3 THEN 'TX'
                WHEN 4 THEN 'AZ'
            END || ' ' ||
            (floor(random() * 90000 + 10000)::text)
        );
    END LOOP;

    -- Insert ~2000 orders (approximately 2 orders per customer)
    FOR customer_id IN SELECT id FROM customers LOOP
        -- Each customer gets 1-3 orders
        FOR i IN 1..floor(random() * 3 + 1)::integer LOOP
            order_total := 0;
            
            -- Create order
            INSERT INTO orders (customer_id, order_date, status, total_amount)
            VALUES (
                customer_id,
                NOW() - (random() * 365 * interval '1 day'),  -- Orders from past year
                order_statuses[1 + floor(random() * array_length(order_statuses, 1))::integer],
                0  -- Will update this after adding items
            )
            RETURNING id INTO order_id;

            -- Add 1-4 items to each order
            order_items_count := floor(random() * 4 + 1)::integer;
            
            FOR j IN 1..order_items_count LOOP
                -- Get random product
                SELECT id, price INTO product_id, order_total
                FROM products
                OFFSET floor(random() * 500) LIMIT 1;

                -- Add order item
                INSERT INTO order_items (order_id, product_id, quantity, price)
                VALUES (
                    order_id,
                    product_id,
                    floor(random() * 5 + 1)::integer,  -- Quantity between 1 and 5
                    order_total
                );
            END LOOP;

            -- Update order total
            UPDATE orders 
            SET total_amount = (
                SELECT sum(quantity * price) 
                FROM order_items 
                WHERE order_items.order_id = orders.id
            )
            WHERE id = order_id;
        END LOOP;
    END LOOP;
END $$;