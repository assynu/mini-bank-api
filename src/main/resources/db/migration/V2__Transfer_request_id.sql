ALTER TABLE transfers ADD COLUMN request_id VARCHAR(255);

UPDATE transfers SET request_id = id::text;

ALTER TABLE transfers ALTER COLUMN request_id SET NOT NULL;

ALTER TABLE transfers ADD CONSTRAINT transfers_request_id_unique UNIQUE (request_id);