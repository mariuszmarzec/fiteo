CREATE TABLE IF NOT EXISTS papers (ID INT AUTO_INCREMENT PRIMARY KEY, code VARCHAR(100) NOT NULL, name VARCHAR(200) NOT NULL, type VARCHAR(100) NOT NULL);
CREATE TABLE IF NOT EXISTS transactions (ID INT AUTO_INCREMENT PRIMARY KEY, TITLE VARCHAR(200) NOT NULL, DATE_TIME DATETIME(6) NOT NULL, TARGET_PAPER_ID INT NOT NULL, SOURCE_PAPER_ID INT NOT NULL, TARGET_VALUE VARCHAR(100) NOT NULL, TOTAL_PRICE_IN_SOURCE VARCHAR(100) NOT NULL, PRICE_PER_UNIT VARCHAR(100) NOT NULL, SETTLEMENT_RATE VARCHAR(100) NOT NULL, FEE VARCHAR(100) NOT NULL, FEE_PAPER_ID INT NOT NULL, type VARCHAR(100) NOT NULL, USER_ID INT NOT NULL, CONSTRAINT FK_TRANSACTIONS_TARGET_PAPER_ID FOREIGN KEY(TARGET_PAPER_ID) REFERENCES papers(id) ON DELETE RESTRICT ON UPDATE RESTRICT, CONSTRAINT FK_TRANSACTIONS_SOURCE_PAPER_ID FOREIGN KEY(SOURCE_PAPER_ID) REFERENCES papers(id) ON DELETE RESTRICT ON UPDATE RESTRICT, CONSTRAINT FK_TRANSACTIONS_FEE_PAPER_ID_ID FOREIGN KEY(FEE_PAPER_ID) REFERENCES papers(id) ON DELETE RESTRICT ON UPDATE RESTRICT, CONSTRAINT FK_TRANSACTIONS_USER_ID_ID FOREIGN KEY(USER_ID) REFERENCES users(id) ON DELETE CASCADE ON UPDATE RESTRICT );