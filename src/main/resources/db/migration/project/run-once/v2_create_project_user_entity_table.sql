CREATE TABLE IF NOT EXISTS project_user_entity
(
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    project_id uuid NOT NULL,
    FOREIGN KEY (project_id)
        REFERENCES project_entity (id)
);
