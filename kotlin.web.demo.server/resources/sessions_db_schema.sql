CREATE TABLE IF NOT EXISTS tomcat_sessions (
  session_id     VARCHAR(100) NOT NULL PRIMARY KEY ,
  valid_session  CHAR(1) NOT NULL ,
  max_inactive   INT NOT NULL ,
  last_access    BIGINT NOT NULL ,
  app_name       VARCHAR(255),
  session_data   MEDIUMBLOB,
  KEY kapp_name(app_name)
);