select * 
from recordfull

INSERT INTO userdefinedqueryableproperties VALUES (7);

INSERT INTO datasets VALUES (7,null,null,'','',null,FALSE,'','', '', null);

insert into recordfull (fk_datasets, format, data)
values (7, 1, 'this is a test...');



--to delete the inserted dataset for recordBrief
delete from recordbrief
where fk_datasets >= 7;

delete from recordsummary
where fk_datasets >= 7;

delete from recordfull
where fk_datasets >= 7;

delete from isoqp_title
where fk_datasets >= 7;

delete from isoqp_type
where fk_datasets >= 7;

delete from isoqp_format
where fk_datasets >= 7;

delete from isoqp_abstract
where fk_datasets >= 7;

delete from isoqp_boundingbox
where fk_datasets >= 7;

delete from datasets 
where id >= 7;

delete from userdefinedqueryableproperties 
where fk_datasets >= 7;