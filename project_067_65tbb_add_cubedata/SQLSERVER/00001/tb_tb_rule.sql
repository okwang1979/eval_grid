/* tablename: tb_imp_data */
create table tb_imp_data (
pk_obj nchar(20) not null 
/*pk_obj*/,
cube_code nvarchar(20) null 
/*cube_code*/,
user_code nvarchar(50) null 
/*user_code*/,
remark nvarchar(500) null 
/*remark*/,
def1 nvarchar(500) null 
/*def1*/,
def2 nvarchar(500) null 
/*def2*/,
def3 nvarchar(500) null 
/*def3*/,
def4 nvarchar(500) null 
/*def4*/,
def5 nvarchar(500) null 
/*def5*/,
def6 nvarchar(500) null 
/*def6*/,
def7 nvarchar(500) null 
/*def7*/,
def8 nvarchar(500) null 
/*def8*/,
 constraint pk_tb_imp_data primary key (pk_obj),
 ts char(19) null default convert(char(19),getdate(),20),
dr smallint null default 0
)
go

/* tablename: tb_imp_data_v */
create table tb_imp_data_v (
pk_obj nchar(20) not null 
/*pk_obj*/,
pk_parent nchar(20) null 
/*pk_parent*/,
unikey nvarchar(100) null 
/*UNIKEY*/,
code_version nvarchar(50) not null default 'v0' 
/*CODE_VERSION*/,
code_curr nvarchar(50) not null default 'CNY' 
/*CODE_CURR*/,
code_aimcurr nvarchar(50) not null default 'CNY' 
/*CODE_AIMCURR*/,
code_mvtype nvarchar(50) not null default 'BUDGET' 
/*CODE_MVTYPE*/,
code_entity nvarchar(50) not null 
/*CODE_ENTITY*/,
code_year nvarchar(50) not null 
/*CODE_YEAR*/,
code_month nvarchar(50) null 
/*CODE_MONTH*/,
code_measure nvarchar(50) not null 
/*CODE_MEASURE*/,
code_dept nvarchar(50) null 
/*CODE_DEPT*/,
code_ysl01 nvarchar(50) null 
/*CODE_YSL01*/,
code_ysh01 nvarchar(50) null 
/*CODE_YSH01*/,
code_employee nvarchar(50) null 
/*CODE_EMPLOYEE*/,
value decimal(33, 8) null 
/*VALUE*/,
txtvalue nvarchar(200) null 
/*TXTVALUE*/,
status smallint null 
/*STATUS*/,
def1 nvarchar(500) null 
/*DEF1*/,
def2 nvarchar(500) null 
/*DEF2*/,
def3 nvarchar(500) null 
/*DEF3*/,
def4 nvarchar(500) null 
/*DEF4*/,
def5 nvarchar(500) null 
/*DEF5*/,
def6 nvarchar(500) null 
/*DEF6*/,
def7 nvarchar(500) null 
/*DEF7*/,
def8 nvarchar(500) null 
/*DEF8*/,
def9 nvarchar(500) null 
/*DEF9*/,
 constraint pk_tb_imp_data_v primary key (pk_obj),
 ts char(19) null default convert(char(19),getdate(),20),
dr smallint null default 0
)
go

