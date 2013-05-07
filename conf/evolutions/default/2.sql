# Metrics schema
 
# --- !Ups

ALTER TABLE metric ADD last_update BIGINT;

# --- !Downs

ALTER TABLE metric DELETE last_update;
