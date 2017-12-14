# Metrics schema
 
# --- !Ups

CREATE TABLE region (
    hash VARCHAR(255),
    name TEXT
);
CREATE UNIQUE INDEX region_hash ON region(hash);

INSERT INTO region (hash, name) SELECT DISTINCT target, target_desc FROM metric;

ALTER TABLE metric DROP COLUMN target_desc;

# --- !Downs

ALTER TABLE metric ADD target_desc VARCHAR(1000) DEFAULT '-unkown-';

DROP TABLE region;
