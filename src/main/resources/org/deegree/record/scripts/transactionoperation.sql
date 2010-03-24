

delete from datasets 
where id >= 7;


delete from userdefinedqueryableproperties 
where fk_datasets >= 7;



--for update: to rebuild the start
update datasets set identifier = '2345-aa453-ade456' where id = 10


--for delete action



