CREATE SEQUENCE event_id;
CREATE TYPE element_type AS ENUM ('unknown', 'noninteractive', 'clickable', 'writable', 'terminal');
DROP TABLE handles;
DROP TABLE urls;
CREATE TABLE IF NOT EXISTS handles
(
  url VARCHAR,
  xpath VARCHAR,
  eltype element_type
);

CREATE TABLE IF NOT EXISTS urls
(
  url VARCHAR,
  hash VARCHAR,
  last_update timestamp DEFAULT current_timestamp
);