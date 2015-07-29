CREATE SEQUENCE event_id;
CREATE TYPE element_type AS ENUM ('unknown', 'noninteractive', 'clickable', 'writable', 'terminal');
DROP TABLE handles;
CREATE TABLE handles
(
  url VARCHAR,
  xpath VARCHAR,
  eltype element_type
);