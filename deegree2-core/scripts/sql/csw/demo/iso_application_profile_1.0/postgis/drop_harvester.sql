ALTER TABLE jt_source_responsehandler DROP CONSTRAINT FK_jt_source_responsehandler_harvestsource;
ALTER TABLE jt_source_responsehandler DROP CONSTRAINT FK_jt_source_responsehandler_responsehandler;
ALTER TABLE metadatacache DROP CONSTRAINT FK_metadatacache_harvestsource;

DROP TABLE harvestsource;
DROP TABLE jt_source_responsehandler;
DROP TABLE metadatacache;
DROP TABLE responsehandler;

DROP SEQUENCE harvestsource_ID_seq;
DROP SEQUENCE metadatacache_ID_seq;
DROP SEQUENCE responsehandler_ID_seq;
