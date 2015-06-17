CREATE TABLE IF NOT EXISTS dbinfo (
  version VARCHAR(45)
);

CREATE TABLE IF NOT EXISTS users (
  id        INT                                    NOT NULL PRIMARY KEY AUTO_INCREMENT,
  client_id VARCHAR(45)                            NOT NULL,
  provider  ENUM ('google', 'twitter', 'facebook', 'github', 'jba') NOT NULL,
  username VARCHAR(45) NOT NULL             DEFAULT '',
  CONSTRAINT client_id UNIQUE (client_id, provider)
);

CREATE TABLE IF NOT EXISTS projects (
  id                INT                                    NOT NULL PRIMARY KEY AUTO_INCREMENT,
  public_id         VARCHAR(45)                            NOT NULL UNIQUE,
  owner_id          INT                                    NOT NULL,
  FOREIGN KEY (owner_id) REFERENCES users (id)
    ON DELETE CASCADE,
  name              VARCHAR(100)                           NOT NULL             DEFAULT '',
  args              VARCHAR(45)                            NOT NULL             DEFAULT '',
  run_configuration ENUM ('java', 'js', 'canvas', 'junit') NOT NULL             DEFAULT 'java',
  origin            VARCHAR(100),
  read_only_files   TEXT                                   NOT NULL,
  type    ENUM('USER_PROJECT', 'KOANS_TASK', 'INCOMPLETE_KOANS_TASK') NOT NULL DEFAULT 'USER_PROJECT',
  task_id INT                                                                  DEFAULT NULL, #NULL for user projects
  FOREIGN KEY (task_id) REFERENCES koans_tasks (id)
    ON DELETE RESTRICT,
  CONSTRAINT UNIQUE (owner_id, name, type),
  CONSTRAINT UNIQUE (owner_id, task_id)
);

CREATE TABLE IF NOT EXISTS koans_tasks (
  id        INT          NOT NULL PRIMARY KEY AUTO_INCREMENT,
  public_id VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS files (
  id         INT          NOT NULL PRIMARY KEY AUTO_INCREMENT,
  public_id  VARCHAR(45)  NOT NULL UNIQUE,
  project_id INT          NOT NULL,
  name       VARCHAR(100) NOT NULL,
  content    LONGTEXT,
  CONSTRAINT file_name UNIQUE (project_id, name),
  FOREIGN KEY (project_id) REFERENCES projects (id)
    ON DELETE CASCADE
);