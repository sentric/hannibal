# Metrics schema
 
# --- !Ups

ALTER TABLE metric ADD last_update BIGINT;

# --- !Downs

ALTER TABLE metric DROP COLUMN last_update;
