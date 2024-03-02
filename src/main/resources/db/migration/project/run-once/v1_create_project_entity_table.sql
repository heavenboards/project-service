CREATE TABLE IF NOT EXISTS project_entity
(
    id uuid PRIMARY KEY,
    name varchar(64) NOT NULL,
    position_weight integer NOT NULL
);
