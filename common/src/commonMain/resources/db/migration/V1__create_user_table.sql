CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, email VARCHAR(100) NOT NULL, password VARCHAR(32) NOT NULL);
ALTER TABLE users ADD CONSTRAINT users_email_unique UNIQUE (email);

insert into users (id, email, password) values (0, 'mariusz.marzec00@gmail.com', 'password');