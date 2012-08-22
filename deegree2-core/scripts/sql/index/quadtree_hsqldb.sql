--This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license.

--comment in the following four lines, if you want to rerun the hsql db setup
--ALTER TABLE TAB_DEEGREE_IDX DROP CONSTRAINT FK_TAB_DEEGREE_IDX_TAB_Quadtree RESTRICT;
--ALTER TABLE TAB_Quadtree DROP CONSTRAINT FK_TAB_Quadtree_TAB_QTNODE RESTRICT;
--DROP TABLE TAB_DEEGREE_IDX;
--DROP TABLE TAB_Quadtree;

CREATE TABLE TAB_DEEGREE_IDX ( 
	ID integer NOT NULL,
	column_name varchar(50) NOT NULL,
	table_name varchar(50) NOT NULL,
	owner varchar(50) NOT NULL,
	index_name varchar(50) NOT NULL,
	FK_INDEXTREE int NOT NULL
);

CREATE TABLE TAB_Quadtree ( 
	ID integer NOT NULL,
	FK_ROOT varchar(150),
	DEPTH int,
    VERSION varchar(15)
);

ALTER TABLE TAB_DEEGREE_IDX
    ADD CONSTRAINT UQ_TAB_DEEGREE_IDX_id UNIQUE (ID);

ALTER TABLE TAB_DEEGREE_IDX
    ADD CONSTRAINT UQ_TAB_DEEGREE_IDX_index_name UNIQUE (index_name);

ALTER TABLE TAB_Quadtree
    ADD CONSTRAINT UQ_TAB_Quadtree_ID UNIQUE (ID);

ALTER TABLE TAB_DEEGREE_IDX ADD CONSTRAINT FK_TAB_DEEGREE_IDX_TAB_Quadtree 
	FOREIGN KEY (FK_INDEXTREE) REFERENCES TAB_Quadtree (ID);

ALTER TABLE TAB_DEEGREE_IDX ADD CONSTRAINT PK_TAB_DEEGREE_IDX 
	PRIMARY KEY (ID);

ALTER TABLE TAB_Quadtree ADD CONSTRAINT PK_TAB_Quadtree 
	PRIMARY KEY (ID);
