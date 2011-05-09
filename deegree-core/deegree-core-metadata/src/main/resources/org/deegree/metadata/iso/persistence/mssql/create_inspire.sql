 -- add constraint for INSPIRE
 DROP INDEX RespPartyRole_idx ON IDXTB_MAIN;
 ALTER TABLE idxtb_main ALTER COLUMN RespPartyRole varchar(20) NOT NULL;
CREATE INDEX RespPartyRole_idx ON IDXTB_MAIN (RespPartyRole);
