alter table SYS_CATEGORY_ATTR add MIN_INT integer^
alter table SYS_CATEGORY_ATTR add MIN_DOUBLE numeric(36,6)^
alter table SYS_CATEGORY_ATTR add MAX_INT integer^
alter table SYS_CATEGORY_ATTR add MAX_DOUBLE numeric(36,6)^

alter table SYS_CATEGORY_ATTR add DEFAULT_DECIMAL numeric(36,10)^
alter table SYS_CATEGORY_ATTR add MIN_DECIMAL numeric(36,10)^
alter table SYS_CATEGORY_ATTR add MAX_DECIMAL numeric(36,10)^

alter table SYS_ATTR_VALUE add DECIMAL_VALUE numeric(36,10)^

alter table SYS_CATEGORY_ATTR add VALIDATOR_GROOVY_SCRIPT varchar(max)^
alter table SYS_CATEGORY_ATTR add VALIDATOR_ERROR_MESSAGE varchar(255)^