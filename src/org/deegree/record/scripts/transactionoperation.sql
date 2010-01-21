
--to delete the inserted datasets
delete from recordbrief
where fk_datasets >= 7;

delete from recordsummary
where fk_datasets >= 7;

delete from recordfull
where fk_datasets >= 7;

delete from isoqp_title
where fk_datasets >= 7;

delete from isoqp_keyword
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



--for update: to rebuild the start
update datasets set identifier = '2345-aa453-ade456' where id = 10


--for delete action



