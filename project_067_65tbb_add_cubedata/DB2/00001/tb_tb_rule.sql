/* tablename: tb_imp_data */
create table tb_imp_data (pk_obj char(20) not null 
/*pk_obj*/,
cube_code varchar(20) null 
/*cube_code*/,
user_code varchar(50) null 
/*user_code*/,
remark varchar(500) null 
/*remark*/,
def1 varchar(500) null 
/*def1*/,
def2 varchar(500) null 
/*def2*/,
def3 varchar(500) null 
/*def3*/,
def4 varchar(500) null 
/*def4*/,
def5 varchar(500) null 
/*def5*/,
def6 varchar(500) null 
/*def6*/,
def7 varchar(500) null 
/*def7*/,
def8 varchar(500) null 
/*def8*/,
 constraint pk_tb_imp_data primary key (pk_obj),
 ts char(19) null,
dr smallint null default 0
)
;

/* tablename: tb_imp_data_v */
create table tb_imp_data_v (pk_obj char(20) not null 
/*pk_obj*/,
pk_parent char(20) null 
/*pk_parent*/,
unikey varchar(100) null 
/*UNIKEY*/,
code_version varchar(50) not null default 'v0' 
/*CODE_VERSION*/,
code_curr varchar(50) not null default 'CNY' 
/*CODE_CURR*/,
code_aimcurr varchar(50) not null default 'CNY' 
/*CODE_AIMCURR*/,
code_mvtype varchar(50) not null default 'BUDGET' 
/*CODE_MVTYPE*/,
code_entity varchar(50) not null 
/*CODE_ENTITY*/,
code_year varchar(50) not null 
/*CODE_YEAR*/,
code_month varchar(50) null 
/*CODE_MONTH*/,
code_measure varchar(50) not null 
/*CODE_MEASURE*/,
code_dept varchar(50) null 
/*CODE_DEPT*/,
code_ysl01 varchar(50) null 
/*CODE_YSL01*/,
code_ysh01 varchar(50) null 
/*CODE_YSH01*/,
code_employee varchar(50) null 
/*CODE_EMPLOYEE*/,
value decimal(33, 8) null 
/*VALUE*/,
txtvalue varchar(200) null 
/*TXTVALUE*/,
status smallint null 
/*STATUS*/,
def1 varchar(500) null 
/*DEF1*/,
def2 varchar(500) null 
/*DEF2*/,
def3 varchar(500) null 
/*DEF3*/,
def4 varchar(500) null 
/*DEF4*/,
def5 varchar(500) null 
/*DEF5*/,
def6 varchar(500) null 
/*DEF6*/,
def7 varchar(500) null 
/*DEF7*/,
def8 varchar(500) null 
/*DEF8*/,
def9 varchar(500) null 
/*DEF9*/,
 constraint pk_tb_imp_data_v primary key (pk_obj),
 ts char(19) null,
dr smallint null default 0
)
;

