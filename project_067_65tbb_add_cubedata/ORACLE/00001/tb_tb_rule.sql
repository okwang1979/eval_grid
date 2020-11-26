/* tablename: tb_imp_data */
create table tb_imp_data (pk_obj char(20) not null 
/*pk_obj*/,
cube_code varchar2(20) null 
/*cube_code*/,
user_code varchar2(50) null 
/*user_code*/,
remark varchar2(500) null 
/*remark*/,
def1 varchar2(500) null 
/*def1*/,
def2 varchar2(500) null 
/*def2*/,
def3 varchar2(500) null 
/*def3*/,
def4 varchar2(500) null 
/*def4*/,
def5 varchar2(500) null 
/*def5*/,
def6 varchar2(500) null 
/*def6*/,
def7 varchar2(500) null 
/*def7*/,
def8 varchar2(500) null 
/*def8*/,
 constraint pk_tb_imp_data primary key (pk_obj),
 ts char(19) default to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'),
dr number(10) default 0
)
/

/* tablename: tb_imp_data_v */
create table tb_imp_data_v (pk_obj char(20) not null 
/*pk_obj*/,
pk_parent char(20) null 
/*pk_parent*/,
unikey varchar2(100) null 
/*UNIKEY*/,
code_version varchar2(50) default 'v0' not null 
/*CODE_VERSION*/,
code_curr varchar2(50) default 'CNY' not null 
/*CODE_CURR*/,
code_aimcurr varchar2(50) default 'CNY' not null 
/*CODE_AIMCURR*/,
code_mvtype varchar2(50) default 'BUDGET' not null 
/*CODE_MVTYPE*/,
code_entity varchar2(50) not null 
/*CODE_ENTITY*/,
code_year varchar2(50) not null 
/*CODE_YEAR*/,
code_month varchar2(50) null 
/*CODE_MONTH*/,
code_measure varchar2(50) not null 
/*CODE_MEASURE*/,
code_dept varchar2(50) null 
/*CODE_DEPT*/,
code_ysl01 varchar2(50) null 
/*CODE_YSL01*/,
code_ysh01 varchar2(50) null 
/*CODE_YSH01*/,
code_employee varchar2(50) null 
/*CODE_EMPLOYEE*/,
value number(33, 8) null 
/*VALUE*/,
txtvalue varchar2(200) null 
/*TXTVALUE*/,
status smallint null 
/*STATUS*/,
def1 varchar2(500) null 
/*DEF1*/,
def2 varchar2(500) null 
/*DEF2*/,
def3 varchar2(500) null 
/*DEF3*/,
def4 varchar2(500) null 
/*DEF4*/,
def5 varchar2(500) null 
/*DEF5*/,
def6 varchar2(500) null 
/*DEF6*/,
def7 varchar2(500) null 
/*DEF7*/,
def8 varchar2(500) null 
/*DEF8*/,
def9 varchar2(500) null 
/*DEF9*/,
 constraint pk_tb_imp_data_v primary key (pk_obj),
 ts char(19) default to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'),
dr number(10) default 0
)
/

