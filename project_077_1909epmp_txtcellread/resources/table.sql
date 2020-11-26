/* tablename: 文本浮动数据转存 */
create table tb_txt_data (
pk_obj char(20) not null 
/*主键*/,
pk_taskdef char(20) not null
/*任务定义pk对应表tb_md_taskdef的主键*/,
pk_task char(20) not null
/*任务pk对应tb_md_task表的主键*/,
pk_sheet char(20) not  null
/*对应sheetPK对应tb_md_task表的主键*/,
varid varchar2(100) not  null
/*浮动区ID对应tb_md_sheettable的varid字段*/,
row_num smallint  null /*行号*/,
col01 varchar2(2000)   null
/*第1列*/,
col02 varchar2(2000)   null
/*第2列*/,
col03 varchar2(2000)   null
/*第3列*/,
col04 varchar2(2000)   null
/*第4列*/,
col05 varchar2(2000)   null
/*第5列*/,
col06 varchar2(2000)   null
/*第6列*/,
col07 varchar2(2000)   null
/*第7列*/,
col08 varchar2(2000)   null
/*第8列*/,
col09 varchar2(2000)   null
/*第9列*/,
col10 varchar2(2000)   null
/*第10列*/,
col11 varchar2(2000)   null
/*第11列*/,
col12 varchar2(2000)   null
/*第12列*/,

col13 varchar2(2000)   null
/*第13列*/,
col14 varchar2(2000)   null
/*第14列*/,
col15 varchar2(2000)   null
/*第15列*/,
col16 varchar2(2000)   null
/*第16列*/,
col17 varchar2(2000)   null
/*第17列*/,
col18 varchar2(2000)   null
/*第18列*/,
col19 varchar2(2000)   null
/*第19列*/,
col20 varchar2(2000)   null
/*第20列*/,
col21 varchar2(2000)   null,
col22 varchar2(2000)   null,
col23 varchar2(2000)   null,
col24 varchar2(2000)   null,
col25 varchar2(2000)   null,
col26 varchar2(2000)   null,
col27 varchar2(2000)   null,
col28 varchar2(2000)   null,
col29 varchar2(2000)   null,
col30 varchar2(2000)   null,

def1 varchar2(2000)   null
,
def2 varchar2(2000)   null
,
def3 varchar2(2000)   null
,
def4 varchar2(2000)   null
,
def5 varchar2(2000)   null
,
pk_group varchar2(20) null 
/*创建集团*/,
pk_org varchar2(20)  null 
/*创建主体*/,

 constraint pk_tb_txt_data primary key (pk_obj),
 ts char(19) default to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'),
dr number(10) default 0
)