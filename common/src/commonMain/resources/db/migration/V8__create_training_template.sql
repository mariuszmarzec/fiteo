CREATE TABLE IF NOT EXISTS training_templates (id INT AUTO_INCREMENT PRIMARY KEY, `name` VARCHAR(300) NOT NULL, user_id INT NOT NULL, CONSTRAINT fk_training_templates_user_id_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE RESTRICT);
CREATE TABLE IF NOT EXISTS training_parts_to_excluded_equipment (id INT AUTO_INCREMENT PRIMARY KEY, training_part_id INT NOT NULL, excluded_equipment VARCHAR(36) NOT NULL, CONSTRAINT fk_training_parts_to_excluded_equipment_training_part_id_id FOREIGN KEY (training_part_id) REFERENCES training_templates(id) ON DELETE CASCADE ON UPDATE RESTRICT, CONSTRAINT fk_training_parts_to_excluded_equipment_excluded_equipment_id FOREIGN KEY (excluded_equipment) REFERENCES equipment(id) ON DELETE CASCADE ON UPDATE RESTRICT);
CREATE TABLE IF NOT EXISTS training_parts_to_excluded_exercises (id INT AUTO_INCREMENT PRIMARY KEY, training_part_id INT NOT NULL, excluded_exercise_id INT NOT NULL, CONSTRAINT fk_training_parts_to_excluded_exercises_training_part_id_id FOREIGN KEY (training_part_id) REFERENCES training_templates(id) ON DELETE CASCADE ON UPDATE RESTRICT, CONSTRAINT fk_training_parts_to_excluded_exercises_excluded_exercise_id_id FOREIGN KEY (excluded_exercise_id) REFERENCES exercises(id) ON DELETE CASCADE ON UPDATE RESTRICT);
CREATE TABLE IF NOT EXISTS training_parts_to_categories (id INT AUTO_INCREMENT PRIMARY KEY, training_part_id INT NOT NULL, category_id VARCHAR(36) NOT NULL, CONSTRAINT fk_training_parts_to_categories_training_part_id_id FOREIGN KEY (training_part_id) REFERENCES training_templates(id) ON DELETE CASCADE ON UPDATE RESTRICT, CONSTRAINT fk_training_parts_to_categories_category_id_id FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE ON UPDATE RESTRICT);
CREATE TABLE IF NOT EXISTS training_parts (id INT AUTO_INCREMENT PRIMARY KEY, `name` VARCHAR(300) NOT NULL);
CREATE TABLE IF NOT EXISTS training_templates_to_training_part (id INT AUTO_INCREMENT PRIMARY KEY, training_template_id INT NOT NULL, training_part_id INT NOT NULL, CONSTRAINT fk_training_templates_to_training_part_training_template_id_id FOREIGN KEY (training_template_id) REFERENCES training_templates(id) ON DELETE CASCADE ON UPDATE RESTRICT, CONSTRAINT fk_training_templates_to_training_part_training_part_id_id FOREIGN KEY (training_part_id) REFERENCES training_parts(id) ON DELETE CASCADE ON UPDATE RESTRICT);
CREATE TABLE IF NOT EXISTS training_templates_to_available_equipment (id INT AUTO_INCREMENT PRIMARY KEY, training_template_id INT NOT NULL, available_equipment_id VARCHAR(36) NOT NULL, CONSTRAINT fk_training_templates_to_available_equipment_training_template_i FOREIGN KEY (training_template_id) REFERENCES training_templates(id) ON DELETE CASCADE ON UPDATE RESTRICT, CONSTRAINT fk_training_templates_to_available_equipment_available_equipment FOREIGN KEY (available_equipment_id) REFERENCES equipment(id) ON DELETE CASCADE ON UPDATE RESTRICT);