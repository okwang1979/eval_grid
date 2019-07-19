
/* tablename: 合并汇总方案脚本 */
create table iufo_hb_scheme (
pk_hbscheme char(20) not null 
/*主键*/,

pk_rms varchar2(20) default '~' null 
/*报表合并组织体系*/,
pk_rmsversion varchar2(20) null 
/*合并组织体系版本*/,

app_org varchar2(20) null
/*设置主体主键*/,
totalType integer null 
/*汇总类型*/,

otherInfo varchar(4000) null
/*自定义设置主体下级PK用逗号隔开*/,

pk_org varchar2(20) default '~' null 
/*所属组织*/,

pk_group varchar2(20) default '~' null 
/*所属集团*/,

creator varchar2(20) default '~' null 
/*创建人*/,
creationtime char(19) null 
/*创建时间*/,
modifier varchar2(20) default '~' null 
/*最后修改人*/,
modifiedtime char(19) null 
/*最后修改时间*/,


def1 varchar(200) null,
 constraint pk_hb_total_scheme primary key (pk_hbscheme),
 ts char(19) default to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'),
dr number(10) default 0
)


  