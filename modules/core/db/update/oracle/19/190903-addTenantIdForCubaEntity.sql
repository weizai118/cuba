create table SEC_TENANT (
    ID varchar2(32) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50 char),
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50 char),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50 char),
    TENANT_ID varchar2(255 char) not null,
    --
    NAME varchar2(255 char) not null,
    ACCESS_GROUP_ID varchar2(32) not null,
    ADMIN_ID varchar2(32) not null,
    --
    primary key (ID)
)^

alter table SEC_TENANT add constraint FK_SDBMT_TENANT_ACCESS_GROUP foreign key (ACCESS_GROUP_ID) references SEC_GROUP(ID)^
alter table SEC_TENANT add constraint FK_SDBMT_TENANT_ADMIN foreign key (ADMIN_ID) references SEC_USER(ID)^
create unique index IDX_SEC_TENANT_UK_ADMIN_ID on SEC_TENANT (ADMIN_ID) ^
create unique index IDX_SEC_TENANT_UK_ACCGR_ID on SEC_TENANT (ACCESS_GROUP_ID) ^
create unique index IDX_SEC_TENANT_UK_NAME on SEC_TENANT (NAME) ^

alter table SYS_CATEGORY_ATTR add TENANT_ID varchar2(255)^
alter table SYS_SERVER add TENANT_ID varchar2(255)^
alter table SYS_CONFIG add TENANT_ID varchar2(255)^
alter table SYS_FILE add TENANT_ID varchar2(255)^
alter table SYS_LOCK_CONFIG add TENANT_ID varchar2(255)^
alter table SYS_ENTITY_STATISTICS add TENANT_ID varchar2(255)^
alter table SYS_SCHEDULED_TASK add TENANT_ID varchar2(255)^
alter table SYS_SCHEDULED_EXECUTION add TENANT_ID varchar2(255)^
alter table SEC_ROLE add TENANT_ID varchar2(255)^
alter table SEC_GROUP add TENANT_ID varchar2(255)^
alter table SEC_GROUP_HIERARCHY add TENANT_ID varchar2(255)^
alter table SEC_USER add TENANT_ID varchar2(255)^
alter table SEC_USER_ROLE add TENANT_ID varchar2(255)^
alter table SEC_PERMISSION add TENANT_ID varchar2(255)^
alter table SEC_CONSTRAINT add TENANT_ID varchar2(255)^
alter table SEC_LOCALIZED_CONSTRAINT_MSG add TENANT_ID varchar2(255)^
alter table SEC_SESSION_ATTR add TENANT_ID varchar2(255)^
alter table SEC_USER_SETTING add TENANT_ID varchar2(255)^
alter table SEC_USER_SUBSTITUTION add TENANT_ID varchar2(255)^
alter table SEC_LOGGED_ENTITY add TENANT_ID varchar2(255)^
alter table SEC_LOGGED_ATTR add TENANT_ID varchar2(255)^
alter table SEC_ENTITY_LOG add TENANT_ID varchar2(255)^
alter table SEC_FILTER add TENANT_ID varchar2(255)^
alter table SYS_FOLDER add TENANT_ID varchar2(255)^
alter table SYS_APP_FOLDER add TENANT_ID varchar2(255)^
alter table SEC_PRESENTATION add TENANT_ID varchar2(255)^
alter table SEC_SEARCH_FOLDER add TENANT_ID varchar2(255)^
alter table SYS_FTS_QUEUE add TENANT_ID varchar2(255)^
alter table SEC_SCREEN_HISTORY add TENANT_ID varchar2(255)^
alter table SYS_SENDING_MESSAGE add TENANT_ID varchar2(255)^
alter table SYS_SENDING_ATTACHMENT add TENANT_ID varchar2(255)^
alter table SYS_ENTITY_SNAPSHOT add TENANT_ID varchar2(255)^
alter table SYS_CATEGORY add TENANT_ID varchar2(255)^
alter table SYS_CATEGORY_ATTR add TENANT_ID varchar2(255)^
alter table SYS_ATTR_VALUE add TENANT_ID varchar2(255)^
alter table SYS_QUERY_RESULT add TENANT_ID varchar2(255)^
alter table SYS_JMX_INSTANCE add TENANT_ID varchar2(255)^
alter table SEC_REMEMBER_ME add TENANT_ID varchar2(255)^
alter table SEC_SESSION_LOG add TENANT_ID varchar2(255)^


