# Metrics schema
 
# --- !Ups

ALTER TABLE metric ADD target_desc VARCHAR(255) DEFAULT '-unkown-';

# --- !Downs

ALTER TABLE metric DROP COLUMN target_desc;
