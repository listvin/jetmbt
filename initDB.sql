CREATE SEQUENCE event_id;
CREATE TYPE element_type AS ENUM ('unknown', 'noninteractive', 'clickable', 'writable', 'terminal');
DROP TABLE elements;
CREATE TABLE elements
(
  url VARCHAR,
  xpath VARCHAR,
  eltype element_type
);