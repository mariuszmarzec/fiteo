CREATE TABLE task_share (
    task_id INT NOT NULL,
    user_id INT NOT NULL,
    owner_id INT NOT NULL,
    permission VARCHAR(255) NOT NULL,
    PRIMARY KEY (task_id, user_id),
    FOREIGN KEY (task_id) REFERENCES todo_tasks(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (owner_id) REFERENCES users(id)
)