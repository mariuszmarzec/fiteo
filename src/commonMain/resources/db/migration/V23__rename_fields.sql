-- Zmiana nazw pól value, name, date na zgodne z konwencją
ALTER TABLE weights CHANGE COLUMN value weight_value FLOAT NOT NULL;
ALTER TABLE weights CHANGE COLUMN date weight_date DATETIME(6) NOT NULL;

ALTER TABLE feature_toggles CHANGE COLUMN name feature_toggle_name VARCHAR(100) NOT NULL UNIQUE;
ALTER TABLE feature_toggles CHANGE COLUMN value feature_toggle_value VARCHAR(100) NOT NULL;

ALTER TABLE categories CHANGE COLUMN name category_name VARCHAR(100) NOT NULL;
ALTER TABLE equipment CHANGE COLUMN name equipment_name VARCHAR(100) NOT NULL;
