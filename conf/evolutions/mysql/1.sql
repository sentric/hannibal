# Metrics schema

# --- !Ups

CREATE TABLE metric (
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255),
    target VARCHAR(255),
    last_value DOUBLE
);
CREATE UNIQUE INDEX metric_name_target_idx ON metric(name, target);

CREATE TABLE record (
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    metric_id INTEGER,
    timestamp BIGINT,
    prev_value DOUBLE,
    value DOUBLE
);
CREATE INDEX record_metric_id ON record(metric_id);
CREATE INDEX record_timestamp ON record(timestamp);

# --- !Downs

ALTER TABLE record DROP INDEX record_metric_id;
ALTER TABLE record DROP INDEX record_timestamp;
DROP TABLE record;

ALTER TABLE metric DROP INDEX metric_name_target_idx;
DROP TABLE metric;
