/* tablename: �ı���������ת�� */
create table tb_txt_data (
pk_obj char(20) not null 
/*����*/,
pk_taskdef char(20) not null
/*������pk��Ӧ��tb_md_taskdef������*/,
pk_task char(20) not null
/*����pk��Ӧtb_md_task�������*/,
pk_sheet char(20) not  null
/*��ӦsheetPK��Ӧtb_md_task�������*/,
varid varchar2(100) not  null
/*������ID��Ӧtb_md_sheettable��varid�ֶ�*/,
row_num smallint  null /*�к�*/,
col01 varchar2(2000)   null
/*��1��*/,
col02 varchar2(2000)   null
/*��2��*/,
col03 varchar2(2000)   null
/*��3��*/,
col04 varchar2(2000)   null
/*��4��*/,
col05 varchar2(2000)   null
/*��5��*/,
col06 varchar2(2000)   null
/*��6��*/,
col07 varchar2(2000)   null
/*��7��*/,
col08 varchar2(2000)   null
/*��8��*/,
col09 varchar2(2000)   null
/*��9��*/,
col10 varchar2(2000)   null
/*��10��*/,
col11 varchar2(2000)   null
/*��11��*/,
col12 varchar2(2000)   null
/*��12��*/,

col13 varchar2(2000)   null
/*��13��*/,
col14 varchar2(2000)   null
/*��14��*/,
col15 varchar2(2000)   null
/*��15��*/,
col16 varchar2(2000)   null
/*��16��*/,
col17 varchar2(2000)   null
/*��17��*/,
col18 varchar2(2000)   null
/*��18��*/,
col19 varchar2(2000)   null
/*��19��*/,
col20 varchar2(2000)   null
/*��20��*/,
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
/*��������*/,
pk_org varchar2(20)  null 
/*��������*/,

 constraint pk_tb_txt_data primary key (pk_obj),
 ts char(19) default to_char(sysdate,'yyyy-mm-dd hh24:mi:ss'),
dr number(10) default 0
)