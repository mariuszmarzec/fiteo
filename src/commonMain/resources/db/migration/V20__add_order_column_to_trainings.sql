ALTER TABLE training_parts ADD ordinal_number INT NOT NULL DEFAULT (0);
Update training_parts SET ordinal_number = id Where ordinal_number = 0;

ALTER TABLE exercise_with_progress ADD ordinal_number INT NOT NULL DEFAULT (0);
Update exercise_with_progress SET ordinal_number = id Where ordinal_number = 0;
