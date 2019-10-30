alter table SYS_FILE add CUBA_TENANT_ID varchar(255)^
alter table SYS_SCHEDULED_TASK add CUBA_TENANT_ID varchar(255)^
alter table SYS_SCHEDULED_EXECUTION add CUBA_TENANT_ID varchar(255)^
alter table SEC_ROLE add CUBA_TENANT_ID varchar(255)^
alter table SEC_GROUP add CUBA_TENANT_ID varchar(255)^
alter table SEC_GROUP_HIERARCHY add CUBA_TENANT_ID varchar(255)^
alter table SEC_USER add CUBA_TENANT_ID varchar(255)^
alter table SEC_USER_ROLE add CUBA_TENANT_ID varchar(255)^
alter table SEC_PERMISSION add CUBA_TENANT_ID varchar(255)^
alter table SEC_CONSTRAINT add CUBA_TENANT_ID varchar(255)^
alter table SEC_LOCALIZED_CONSTRAINT_MSG add CUBA_TENANT_ID varchar(255)^
alter table SEC_SESSION_ATTR add CUBA_TENANT_ID varchar(255)^
alter table SEC_USER_SUBSTITUTION add CUBA_TENANT_ID varchar(255)^
alter table SEC_ENTITY_LOG add CUBA_TENANT_ID varchar(255)^
alter table SEC_FILTER add CUBA_TENANT_ID varchar(255)^
alter table SYS_FOLDER add CUBA_TENANT_ID varchar(255)^
alter table SEC_PRESENTATION add CUBA_TENANT_ID varchar(255)^
alter table SEC_SCREEN_HISTORY add CUBA_TENANT_ID varchar(255)^
alter table SYS_SENDING_MESSAGE add CUBA_TENANT_ID varchar(255)^
alter table SYS_SENDING_ATTACHMENT add CUBA_TENANT_ID varchar(255)^
alter table SYS_ENTITY_SNAPSHOT add CUBA_TENANT_ID varchar(255)^
alter table SEC_SESSION_LOG add CUBA_TENANT_ID varchar(255)^

alter table SEC_USER drop constraint IDX_SEC_USER_UNIQ_LOGIN^

alter table SEC_USER add constraint IDX_SEC_USER_UNIQ_LOGIN unique (LOGIN_LC, CUBA_TENANT_ID, DELETE_TS)^

