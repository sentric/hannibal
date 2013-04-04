# Metrics schema
 
# --- !Ups

ALTER TABLE metric ADD last_update BIGINT;

# --- !Downs

ALTER TABLE metric DROP last_update;
