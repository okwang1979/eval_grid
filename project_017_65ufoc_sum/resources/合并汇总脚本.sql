
/* tablename: �ϲ����ܷ����ű� */
create table iufo_hb_scheme (
pk_hbscheme char(20) not null 
/*����*/,

pk_rms varchar2(20) default '~' null 
/*����ϲ���֯��ϵ*/,
pk_rmsversion varchar2(20) null 
/*�ϲ���֯��ϵ�汾*/,

app_org varchar2(20) null
/*������������*/,
totalType integer null 
/*��������*/,

otherInfo varchar(4000) null
/*�Զ������������¼�PK�ö��Ÿ���*/,

pk_org varchar2(20) default '~' null 
/*������֯*/,

pk_group varchar2(20) default '~' null 
/*��������*/,

creator varchar2(20) default '~' null 
/*������*/,
creationtime char(19) null 
/*����ʱ��*/,
modifier varchar2(20) default '~' null 
/*����޸���*/,
modifiedtime char(19) null 
/*����޸�ʱ��*/,


def1 varchar(200) null,
 constraint pk_hb_total_scheme primary key (pk_hbscheme),
 ts char(19) default to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'),
dr number(10) default 0
)


  