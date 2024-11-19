ALTER TABLE eg_pgr_service ADD COLUMN active BOOLEAN DEFAULT TRUE;
CREATE INDEX IF NOT EXISTS index_eg_pgr_address_locality ON eg_pgr_address (locality);
ALTER TABLE eg_pgr_service ALTER COLUMN description DROP NOT NULL;