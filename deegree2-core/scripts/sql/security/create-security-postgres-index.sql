create index sec_services_index on sec_services (id);
create index sec_services_objects_index on sec_services_objects (serviceid);
create index idx_FK_GROUPS_MEMBER on SEC_JT_GROUPS_GROUPS (FK_GROUPS_MEMBER);
create index idx_FK_GROUPS1 on SEC_JT_GROUPS_GROUPS (FK_GROUPS);

create index idx_FK_GROUPS2 on SEC_JT_GROUPS_ROLES (FK_GROUPS);
create index idx_FK_ROLES1 on SEC_JT_GROUPS_ROLES (FK_ROLES);

create index idx_FK_ROLES2 on SEC_JT_ROLES_PRIVILEGES (FK_ROLES);
create index idx_FK_PRIVILEGES1 on SEC_JT_ROLES_PRIVILEGES (FK_PRIVILEGES);

create index idx_FK_ROLES3 on SEC_JT_ROLES_SECOBJECTS (FK_ROLES);
create index idx_FK_SECURABLE_OBJECTS1 on SEC_JT_ROLES_SECOBJECTS (FK_SECURABLE_OBJECTS);
create index idx_FK_RIGHTS1 on SEC_JT_ROLES_SECOBJECTS (FK_RIGHTS);

create index idx_FK_USERS1 on SEC_JT_USERS_GROUPS (FK_USERS);
create index idx_FK_GROUPS3 on SEC_JT_USERS_GROUPS (FK_GROUPS);

create index idx_FK_USERS2 on SEC_JT_USERS_ROLES(FK_USERS);
create index idx_FK_ROLES4 on SEC_JT_USERS_ROLES(FK_ROLES);

create index idx_name1 on SEC_PRIVILEGES(name);

create index idx_name2 on SEC_RIGHTS(name);

create index idx_name3 on SEC_SECURABLE_OBJECTS(name);
create index idx_title1 on SEC_SECURABLE_OBJECTS(title);

create index idx_name4 on SEC_SECURED_OBJECT_TYPES(name);

create index idx_FK_SECURED_OBJECT_TYPES on SEC_SECURED_OBJECTS(FK_SECURED_OBJECT_TYPES);