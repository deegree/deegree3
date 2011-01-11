create table requests (
ID             serial primary key,
wfsidintern    character varying(100),
wfsidextern    character varying(100),
username       character varying(20),
starttime      timestamp with time zone,
endtime        timestamp with time zone,
requestformat  int,
rawrequest     bytea,
numfeatures    int
);
