DROP TABLE IF EXISTS dbinfo;
DROP TABLE IF EXISTS files;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS dbinfo (
  version VARCHAR(45)
);

CREATE TABLE IF NOT EXISTS users (
  id        INT                                    NOT NULL PRIMARY KEY AUTO_INCREMENT,
  client_id VARCHAR(45)                            NOT NULL,
  provider  ENUM ('google', 'twitter', 'facebook') NOT NULL,
  username VARCHAR(45) NOT NULL             DEFAULT '',
  CONSTRAINT client_id UNIQUE (client_id, provider)
);

CREATE TABLE IF NOT EXISTS projects (
  id                INT                                    NOT NULL PRIMARY KEY AUTO_INCREMENT,
  public_id         VARCHAR(45)                            NOT NULL UNIQUE,
  owner_id          INT                                    NOT NULL,
  name              VARCHAR(100)                           NOT NULL             DEFAULT '',
  args              VARCHAR(45)                            NOT NULL             DEFAULT '',
  run_configuration ENUM ('java', 'js', 'canvas', 'junit') NOT NULL             DEFAULT 'java',
  origin            VARCHAR(100),
  read_only_files   TEXT                                   NOT NULL,
  CONSTRAINT project_name UNIQUE (owner_id, name),
  FOREIGN KEY (owner_id) REFERENCES users (id)
    ON DELETE CASCADE
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