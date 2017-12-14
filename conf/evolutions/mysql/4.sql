# Metrics schema
 
# --- !Ups

ALTER TABLE metric MODIFY target_desc VARCHAR(1000) DEFAULT '-unkown-';

# --- !Downs

ALTER TABLE metric MODIFY target_desc VARCHAR(255) DEFAULT '-unkown-';
