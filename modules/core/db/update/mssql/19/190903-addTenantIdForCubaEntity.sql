create table SEC_TENANT (
    ID uniqueidentifier not null,
    VERSION integer not null default 1,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    TENANT_ID varchar(255) not null,
    --
    NAME varchar(255) not null,
    ACCESS_GROUP_ID uniqueidentifier not null,
    ADMIN_ID uniqueidentifier not null,
    --
    primary key (ID),
    constraint IDX_SEC_TENANT_UNIQ_TENANT_ID unique (TENANT_ID, DELETE_TS)
)^

alter table SEC_TENANT add constraint FK_SEC_TENANT_ON_ACCESS_GROUP foreign key (ACCESS_GROUP_ID) references SEC_GROUP(ID)^
alter table SEC_TENANT add constraint FK_SEC_TENANT_ON_ADMIN foreign key (ADMIN_ID) references SEC_USER(ID)^
create unique index IDX_SEC_TENANT_UNIQ_ACCESS_GROUP_ID on SEC_TENANT (ACCESS_GROUP_ID) ^
create unique index IDX_SEC_TENANT_UNIQ_ADMIN_ID on SEC_TENANT (ADMIN_ID) ^
create unique index IDX_SEC_TENANT_UNIQ_NAME on SEC_TENANT (NAME) ^

alter table SYS_CATEGORY_ATTR add TENANT_ID varchar(255)^
alter table SYS_SERVER add TENANT_ID varchar(255)^
alter table SYS_CONFIG add TENANT_ID varchar(255)^
alter table SYS_FILE add TENANT_ID varchar(255)^
alter table SYS_LOCK_CONFIG add TENANT_ID varchar(255)^
alter table SYS_ENTITY_STATISTICS add TENANT_ID varchar(255)^
alter table SYS_SCHEDULED_TASK add TENANT_ID varchar(255)^
alter table SYS_SCHEDULED_EXECUTION add TENANT_ID varchar(255)^
alter table SEC_ROLE add TENANT_ID varchar(255)^
alter table SEC_GROUP add TENANT_ID varchar(255)^
alter table SEC_GROUP_HIERARCHY add TENANT_ID varchar(255)^
alter table SEC_USER add TENANT_ID varchar(255)^
alter table SEC_USER_ROLE add TENANT_ID varchar(255)^
alter table SEC_PERMISSION add TENANT_ID varchar(255)^
alter table SEC_CONSTRAINT add TENANT_ID varchar(255)^
alter table SEC_LOCALIZED_CONSTRAINT_MSG add TENANT_ID varchar(255)^
alter table SEC_SESSION_ATTR add TENANT_ID varchar(255)^
alter table SEC_USER_SETTING add TENANT_ID varchar(255)^
alter table SEC_USER_SUBSTITUTION add TENANT_ID varchar(255)^
alter table SEC_LOGGED_ENTITY add TENANT_ID varchar(255)^
alter table SEC_LOGGED_ATTR add TENANT_ID varchar(255)^
alter table SEC_ENTITY_LOG add TENANT_ID varchar(255)^
alter table SEC_FILTER add TENANT_ID varchar(255)^
alter table SYS_FOLDER add TENANT_ID varchar(255)^
alter table SYS_APP_FOLDER add TENANT_ID varchar(255)^
alter table SEC_PRESENTATION add TENANT_ID varchar(255)^
alter table SEC_SEARCH_FOLDER add TENANT_ID varchar(255)^
alter table SYS_FTS_QUEUE add TENANT_ID varchar(255)^
alter table SEC_SCREEN_HISTORY add TENANT_ID varchar(255)^
alter table SYS_SENDING_MESSAGE add TENANT_ID varchar(255)^
alter table SYS_SENDING_ATTACHMENT add TENANT_ID varchar(255)^
alter table SYS_ENTITY_SNAPSHOT add TENANT_ID varchar(255)^
alter table SYS_CATEGORY add TENANT_ID varchar(255)^
alter table SYS_CATEGORY_ATTR add TENANT_ID varchar(255)^
alter table SYS_ATTR_VALUE add TENANT_ID varchar(255)^
alter table SYS_QUERY_RESULT add TENANT_ID varchar(255)^
alter table SYS_JMX_INSTANCE add TENANT_ID varchar(255)^
alter table SEC_REMEMBER_ME add TENANT_ID varchar(255)^
alter table SEC_SESSION_LOG add TENANT_ID varchar(255)^


