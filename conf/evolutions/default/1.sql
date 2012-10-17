# Metrics schema
 
# --- !Ups

CREATE SEQUENCE metric_id_seq;
CREATE TABLE metric (
    id INT NOT NULL DEFAULT nextval('metric_id_seq'),
    name VARCHAR(255),
    target VARCHAR(255),
    last_value DOUBLE
);
CREATE UNIQUE INDEX metric_name_target_idx ON metric(name, target);

CREATE SEQUENCE record_id_seq;
CREATE TABLE record (
    id INT NOT NULL DEFAULT nextval('record_id_seq'),
    metric_id INTEGER,
    timestamp BIGINT,
    prev_value DOUBLE,
    value DOUBLE
);
CREATE INDEX record_metric_id ON record(metric_id);
CREATE INDEX record_timestamp ON record(timestamp);

# --- !Downs

DROP INDEX record_metric_id;
DROP INDEX record_timestamp;
DROP TABLE record;
DROP SEQUENCE record_id_seq;

DROP INDEX metric_name_target_index;
DROP TABLE metric;
DROP SEQUENCE metric_id_seq;