ALTER TABLE weights CHANGE COLUMN value weight_value FLOAT NOT NULL;
ALTER TABLE weights CHANGE COLUMN date weight_date DATETIME(6) NOT NULL;

ALTER TABLE feature_toggles CHANGE COLUMN name feature_toggle_name VARCHAR(100) NOT NULL UNIQUE;
ALTER TABLE feature_toggles CHANGE COLUMN value feature_toggle_value VARCHAR(100) NOT NULL;

ALTER TABLE categories CHANGE COLUMN name category_name VARCHAR(100) NOT NULL;
ALTER TABLE equipment CHANGE COLUMN name equipment_name VARCHAR(100) NOT NULL;

ALTER TABLE series CHANGE COLUMN `date` create_date_in_millis DATETIME(6) NOT NULL;

ALTER TABLE training_parts CHANGE COLUMN `name` training_part_name VARCHAR(300) NOT NULL;
ALTER TABLE training_templates CHANGE COLUMN `name` training_template_name VARCHAR(300) NOT NULL;
ALTER TABLE exercises CHANGE COLUMN `name` exercise_name VARCHAR(300) NOT NULL;
ALTER TABLE exercise_with_progress CHANGE COLUMN `name` exercise_name VARCHAR(300) NOT NULL;
