package nc.itf.hbbb.dxrelation;


import com.ufsoft.iufo.util.parser.IFuncType;
import com.ufsoft.script.function.UfoFuncInfo;
import com.ufsoft.script.function.UfoFuncList;

public interface IDxFunctionConst {

	 /** ����ģ�庯������ */
    public static final byte DXFUNC = 1;

    /** ����ģ�庯���������� */
    public static final String[] CATNAMES ={ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0126")/*@res "����ģ�庯��"*/ };//"����ģ�庯��"


    /** ����ģ������  ֱ�������ڲ����׶��˹���  */
    public static final String DirectDealMeet = "8";
	 /**
     * ����ģ�庯������
     * ��������ΪӢ�Ĵ�д
     * */
    public final static String CESUM = "CESUM";			//������¼�ϼ�ֵȡ������
    public final static String DPSUM = "DPSUM";         //���յ��ۼƾ�������㺯��
    public final static String INTR = "INTR";					//�ڲ�����ȡ������
    public final static String INTRBYKEY = "INTRBYKEY";           //�ڲ�����ȡ������
//    public final static String INVSUM = "INVSUM";					//�ۼ�Ͷ�ʺ���
    public final static String IPROPORTION = "IPROPORTION";	//ֱ��Ͷ�ʱ�������
    public final static String OPCE = "OPCE";			//������¼�Է���Ŀ�������
    public final static String PTPSUM = "PTPSUM";       //����Ӧ���о�������㺯��
//    public final static String SINTR = "SINTR";				//V6.0�������ڲ���������ȡ������
    public final static String SREP = "SREP";						//����ȡ������
    public final static String TPSUM = "TPSUM";         //Ӧ�����ۼƾ�������㺯��
    public final static String UCHECK = "UCHECK";		//V6.0������UAP�ڲ����׶��˹�����
    public final static String UCHECKBYKEY = "UCHECKBYKEY";		//V6.0������UAP�ڲ����׶��˹�����
    public final static String UCHECKBYORG = "UCHECKBYORG";		//V6.0������UAP�ڲ����׶��˹�����

    public final static String ESELECT = "ESELECT";		//V6.0����������ȡ������
    public final static String ZMONTH = "ZMONTH";

    public final static String KEYFUNC = "K";//63֧��k����

    /**
	 * ����ģ�庯������
	 * ��Ҫ�������������Һ�������ΪӢ�Ĵ�д
	 * �������б�Ĳ������Ͷ�����Ϊint��
	 */
    public static final UfoFuncInfo[] FUNCLIST = {
    	
    	  new UfoFuncInfo(
    	             CESUM,
    	             DXFUNC,
    	             new String[]{UfoFuncList.PARANAMES[UfoFuncList.HBACCOUNT],UfoFuncList.PARANAMES[UfoFuncList.CREDIT],UfoFuncList.PARANAMES[UfoFuncList.DATASETPARAMCOND]
    	                		/*UfoFuncList.PARANAMES[UfoFuncList.DATEPROP],"uiufofunc226"*/},
    	                new String[]{UfoFuncList.DETAILPARAMSTRING[UfoFuncList.HBACCOUNT],UfoFuncList.DETAILPARAMSTRING[UfoFuncList.CREDIT],UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASETPARAMCOND]
    	                        		/*UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATEPROP],"uiufofunc225"*/},
    	                new byte[] {
    	                    UfoFuncList.STRING,///�ϲ���Ŀ��������ʱ����Ϊstring����Ҫ�޸ģ�
    	                    UfoFuncList.INT,//�������������Ϊint
    	                    /*UfoFuncList.DATEPROP |(byte) 0x80,*/
    	                    UfoFuncList.INT |(byte) 0x80 },
    	                     (byte) IFuncType.VALUE,
    	                     nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0128")/*@res "������¼�ϼ�ֵȡ������"*//*"������¼�ϼ�ֵȡ������"*/,
    	                  new String[]{IDxFuncParamRefConst.UNION_REPORT_PROJECT_REF,IDxFuncParamRefConst.AMOUNT_DIRECTION,/*IDxFuncParamRefConst.TIME_ATTR_REF,*/IDxFuncParamRefConst.NONE_REF}
//    	                new int[]{IFuncType.PARAM_REF_TYPE_ACCOUNT,IFuncType.PARAM_COMBOX_TYPE_CREDIT,IFuncType.PARAM_REF_TYPE_TIME,IFuncType.PARAM_REF_TYPE_NOREF}
    	             ),
    	 new UfoFuncInfo(
    	 	         	     "CHECKBYKEY",
    		         	     DXFUNC,
    		         	     new String[]{/*UfoFuncList.PARANAMES[UfoFuncList.HBACCOUNT]*/nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("common","UC000-0003069")/*@res "��Ŀ����"*/
    		         	    		,UfoFuncList.PARANAMES[UfoFuncList.DATASOURCE]
    		         	    		,"ȡ����ʽ"
    		         	    		,"ȡ������"},
    		    	         new String[]{/*UfoFuncList.DETAILPARAMSTRING[UfoFuncList.HBACCOUNT]*/nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0491")/*@res "��ʾ��Ҫ������ݵĿ�Ŀ����"*/
    		         	    		,UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASOURCE]
    		         	    		,"ȡ����ʽ������������ȡ���ǰ�����ȡ"
    		         		        ,"��������ȡ������"},
    		    	         new byte[] {
    		    	                UfoFuncList.STRING,
    		    	                UfoFuncList.INT,
    		    	                UfoFuncList.INT,
    		    	                UfoFuncList.VALUE},
    		    	                 (byte) IFuncType.VALUE,
    		    	                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0135")/*@res "UAP�ڲ����׶��˹�����"*/ /* "UAP�ڲ����׶��˹�����"*/,
    		    	                new String[]{IDxFuncParamRefConst.UAP_ACCOUNT,IDxFuncParamRefConst.UAPDATASOURCE,IDxFuncParamRefConst.UAPDATATYPE,IDxFuncParamRefConst.ACCCONDITION}
    		    	           /*new int[]{IFuncType.PARAM_REF_TYPE_UAPACCOUNT,IFuncType.PARAM_REF_TYPE_TIME,IFuncType.PARAM_REF_TYPE_NOREF}*/),
    	   new UfoFuncInfo(
    	         	 DPSUM,
    	         	 DXFUNC,
    	         	new String[]{UfoFuncList.PARANAMES[UfoFuncList.HBACCOUNT],UfoFuncList.PARANAMES[UfoFuncList.DATASOURCE],UfoFuncList.PARANAMES[UfoFuncList.DATASETPARAMCOND]
    	                		/*"uiufofunc226"*/},
    	            new String[]{UfoFuncList.DETAILPARAMSTRING[UfoFuncList.HBACCOUNT],UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASOURCE],UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASETPARAMCOND]
    	                        		/*"uiufofunc225"*/},
    	            new byte[] {
    	                    UfoFuncList.HBACCOUNT,
    	                    UfoFuncList.INT,
    	                    UfoFuncList.INT |(byte) 0x80},
    	                     (byte) IFuncType.VALUE,
    	                     nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0129")/*@res "���յ��ۼƾ�������㺯��"*/ /*"���յ��ۼƾ�������㺯��"*/,
    	                new String[]{IDxFuncParamRefConst.UNION_REPORT_PROJECT_REF,IDxFuncParamRefConst.REPORT_DATASOURCE,IDxFuncParamRefConst.NONE_REF}
//    	                     new int[]{IFuncType.PARAM_REF_TYPE_ACCOUNT,IFuncType.PARAM_COMBOX_TYPE_DATASOURCE,IFuncType.PARAM_REF_TYPE_NOREF}
    	         	),
    	    new UfoFuncInfo(
         	    	  ESELECT,
         	    	  DXFUNC,
         	    	        new String[]{UfoFuncList.PARANAMES[UfoFuncList.HBACCOUNT],/*UfoFuncList.PARANAMES[UfoFuncList.DATASOURCE],*/UfoFuncList.PARANAMES[UfoFuncList.DATASETPARAMCOND]

         	                		/*UfoFuncList.PARANAMES[UfoFuncList.DATEPROP],"uiufofunc226"*/,
         	                		nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("common","UCMD1-000551")/*@res "�汾"*/},
         	                new String[]{UfoFuncList.DETAILPARAMSTRING[UfoFuncList.HBACCOUNT],/*UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASOURCE],*/
         	                        		/*UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATEPROP],*/UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASETPARAMCOND],
         	                        		UfoFuncList.DETAILPARAMSTRING[UfoFuncList.VERSION]},
         	                new byte[] {
         	                    UfoFuncList.HBACCOUNT,
         	                /*    UfoFuncList.INT,*/
         	                   /* UfoFuncList.DATEPROP | (byte) 0x80,*/
         	                    UfoFuncList.INT | (byte) 0x80,UfoFuncList.INT
         	                  /*  UfoFuncList.VERSION | (byte) 0x80*/ },
         	                     (byte) IFuncType.VALUE,
         	                     nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0490")/*@res "����ȡ������"*/  /*hbbbdx100019*//*"����ȡ������"*/,


         	                     //�����Ǳ߹�ʽ���µı��,�������� modify By liyongru For V60 at 20110415
         	                     new String[]{IDxFuncParamRefConst.UNION_REPORT_PROJECT_REF,/*IDxFuncParamRefConst.REPORT_DATASOURCE,*//*IDxFuncParamRefConst.TIME_ATTR_REF,*/IDxFuncParamRefConst.NONE_REF,IDxFuncParamRefConst.ESELECT_VERSION}

//         	                new int[]{IFuncType.PARAM_REF_TYPE_ACCOUNT,IFuncType.PARAM_COMBOX_TYPE_DATASOURCE,IFuncType.PARAM_REF_TYPE_TIME,IFuncType.PARAM_REF_TYPE_NOREF/*,*/
//         	                	/*IFuncType.PARAM_REF_TYPE_VER*/}
         	    	        ),      	
    	   new UfoFuncInfo(
         		     INTR,
         		     DXFUNC,
         		    new String[]{UfoFuncList.PARANAMES[UfoFuncList.HBACCOUNT],UfoFuncList.PARANAMES[UfoFuncList.IDATASOURCE],UfoFuncList.PARANAMES[UfoFuncList.DATASETPARAMCOND]
    	                		/*UfoFuncList.PARANAMES[UfoFuncList.DATEPROP],"uiufofunc226"*/},
    	            new String[]{UfoFuncList.DETAILPARAMSTRING[UfoFuncList.HBACCOUNT],UfoFuncList.DETAILPARAMSTRING[UfoFuncList.IDATASOURCE],UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASETPARAMCOND]
    	                        		/*UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATEPROP],"uiufofunc225"*/},
    	            new byte[] {
    	                    UfoFuncList.HBACCOUNT,
    	                    UfoFuncList.INT,
    	                  /*  UfoFuncList.DATEPROP | (byte) 0x80,*/
    	                    UfoFuncList.INT | (byte) 0x80},
    	                     (byte) IFuncType.VALUE,
    	                     nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0130")/*@res "�ڲ�����ȡ������"*/ /*"�ڲ�����ȡ������"*/,
    	                    new String[]{IDxFuncParamRefConst.UNION_REPORT_PROJECT_REF,IDxFuncParamRefConst.REPORT_INTRDATASOURCE,/*IDxFuncParamRefConst.TIME_ATTR_REF,*/IDxFuncParamRefConst.NONE_REF}
//    	                     new int[]{IFuncType.PARAM_REF_TYPE_ACCOUNT,IFuncType.PARAM_COMBOX_TYPE_IDATASOURCE,IFuncType.PARAM_REF_TYPE_TIME,IFuncType.PARAM_REF_TYPE_NOREF}
         		    ),
         		    new UfoFuncInfo(
         		    		INTRBYKEY,
         		    		DXFUNC,
         		    		new String[] {
         		    				UfoFuncList.PARANAMES[UfoFuncList.HBACCOUNT],
         		    				UfoFuncList.PARANAMES[UfoFuncList.IDATASOURCE],
         		    				UfoFuncList.PARANAMES[UfoFuncList.DATASETPARAMCOND],
         		    				"�ؼ�������"
         		    				/*
         		    				 * UfoFuncList.PARANAMES[UfoFuncList.DATEPROP],"uiufofunc226"
         		    				 */},
         		    				 new String[] {
         		    				UfoFuncList.DETAILPARAMSTRING[UfoFuncList.HBACCOUNT],
         		    				UfoFuncList.DETAILPARAMSTRING[UfoFuncList.IDATASOURCE],
         		    				UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASETPARAMCOND],
         		    				"ȡ�ؼ�������"
         		    				/*
         		    				 * UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATEPROP],
         		    				 * "uiufofunc225"
         		    				 */},
         		    				 new byte[] { UfoFuncList.HBACCOUNT, UfoFuncList.INT,
         		    				/* UfoFuncList.DATEPROP | (byte) 0x80, */
         		    				UfoFuncList.INT | (byte) 0x80,
         		    				UfoFuncList.VALUE | (byte) 0x80 },
         		    				(byte) IFuncType.VALUE,
         		    				"�ڲ�����ȡ������-����",
         		    				new String[] {
         		    			IDxFuncParamRefConst.UNION_REPORT_PROJECT_REF,
         		    			IDxFuncParamRefConst.REPORT_INTRDATASOURCE,/*
         		    			 * IDxFuncParamRefConst
         		    			 * .
         		    			 * TIME_ATTR_REF
         		    			 * ,
         		    			 */
         		    			IDxFuncParamRefConst.NONE_REF,
         		    			IDxFuncParamRefConst.HBBB_KEY_WORD_REF /* ����������Ĳ��� */}
         		    		// new
         		    		// int[]{IFuncType.PARAM_REF_TYPE_ACCOUNT,IFuncType.PARAM_COMBOX_TYPE_IDATASOURCE,IFuncType.PARAM_REF_TYPE_TIME,IFuncType.PARAM_REF_TYPE_NOREF}
         		    		),
    	   new UfoFuncInfo(
    			   IPROPORTION,
    	         	DXFUNC,
    	         	new String[]{/*UfoFuncList.PARANAMES[UfoFuncList.CHECKTYPE],*//*UfoFuncList.PARANAMES[UfoFuncList.DATEPROP],*//*"uiufofunc226"*/UfoFuncList.PARANAMES[UfoFuncList.DATASETPARAMCOND]},
	                new String[]{/*UfoFuncList.DETAILPARAMSTRING[UfoFuncList.CHECKTYPE]*/
	                        		/*UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATEPROP],*//*"uiufofunc225"*/UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASETPARAMCOND]},
	                new byte[] {
//	                    UfoFuncList.CHECKTYPE,
	                 /*   UfoFuncList.DATEPROP | (byte) 0x80,*/
	                    UfoFuncList.INT | (byte) 0x80},
	                     (byte) IFuncType.VALUE,
	                     nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0131")/*@res "ֱ��Ͷ�ʱ�������"*/,//"ֱ��Ͷ�ʱ�������",
	              new String[]{/*IDxFuncParamRefConst.TIME_ATTR_REF,*/IDxFuncParamRefConst.NONE_REF}
//	                   new int[]{/*IFuncType.PARAM_COMBOX_TYPE_CHECKTYPE,*/IFuncType.PARAM_REF_TYPE_TIME,IFuncType.PARAM_REF_TYPE_NOREF}
    	         	),
//    	   new UfoFuncInfo(IPROPORTION, (byte)0,null, (byte) IFuncType.FLOAT, hbbbdx100015),//"ֱ��Ͷ�ʱ�������"
    	  new UfoFuncInfo(
                    "OESUM",
        	             DXFUNC,
        	             new String[]{"�ϲ�����","������¼����",UfoFuncList.PARANAMES[UfoFuncList.HBACCOUNT],UfoFuncList.PARANAMES[UfoFuncList.CREDIT],UfoFuncList.PARANAMES[UfoFuncList.DATASETPARAMCOND]
        	                		/*UfoFuncList.PARANAMES[UfoFuncList.DATEPROP],"uiufofunc226"*/},
        	                new String[]{"�ϲ�����","������¼����",UfoFuncList.DETAILPARAMSTRING[UfoFuncList.HBACCOUNT],UfoFuncList.DETAILPARAMSTRING[UfoFuncList.CREDIT],UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASETPARAMCOND]
        	                        		/*UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATEPROP],"uiufofunc225"*/},
        	                new byte[] {
        	            		 UfoFuncList.STRING,
        	            		 UfoFuncList.INT |(byte) 0x80 ,
        	                    UfoFuncList.STRING,///�ϲ���Ŀ��������ʱ����Ϊstring����Ҫ�޸ģ�
        	                    UfoFuncList.INT,//�������������Ϊint
        	                    /*UfoFuncList.DATEPROP |(byte) 0x80,*/
        	                    UfoFuncList.INT |(byte) 0x80 },
        	                     (byte) IFuncType.VALUE,
        	                     nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0128")/*@res "������¼�ϼ�ֵȡ������"*//*"������¼�ϼ�ֵȡ������"*/,
        	                  new String[]{"nc.ui.hbbb.hbfunction.refprocessor.HBSchemeRefProcessor","nc.ui.hbbb.dxrelation.formula.refprocessor.VoucherTypeProcessor",IDxFuncParamRefConst.UNION_REPORT_PROJECT_REF,IDxFuncParamRefConst.AMOUNT_DIRECTION,/*IDxFuncParamRefConst.TIME_ATTR_REF,*/IDxFuncParamRefConst.NONE_REF}
//        	    	                new int[]{IFuncType.PARAM_REF_TYPE_ACCOUNT,IFuncType.PARAM_COMBOX_TYPE_CREDIT,IFuncType.PARAM_REF_TYPE_TIME,IFuncType.PARAM_REF_TYPE_NOREF}
        	             ),
    	   new UfoFuncInfo(
    	         	OPCE,
    	         	DXFUNC,
    	         	new String[]{UfoFuncList.PARANAMES[UfoFuncList.HBACCOUNT],UfoFuncList.PARANAMES[UfoFuncList.HBACCOUNT],UfoFuncList.PARANAMES[UfoFuncList.CREDIT],UfoFuncList.PARANAMES[UfoFuncList.DATASETPARAMCOND]
   	                		/*UfoFuncList.PARANAMES[UfoFuncList.DATEPROP],"uiufofunc226"*/},
   	                new String[]{UfoFuncList.DETAILPARAMSTRING[UfoFuncList.HBACCOUNT],UfoFuncList.DETAILPARAMSTRING[UfoFuncList.HBACCOUNT],UfoFuncList.DETAILPARAMSTRING[UfoFuncList.CREDIT],UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASETPARAMCOND]
   	                        		/*UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATEPROP],"uiufofunc225"*/},
   	                new byte[] {
   	                    UfoFuncList.HBACCOUNT,
   	                    UfoFuncList.HBACCOUNT,
   	                    UfoFuncList.INT,
   	                    /*UfoFuncList.DATEPROP | (byte) 0x80,*/
   	                    UfoFuncList.INT | (byte) 0x80},
   	                     (byte) IFuncType.VALUE,
   	                  nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0132")/*@res "������¼�Է���Ŀ�������"*//*"������¼�Է���Ŀ�������"*/,
   	                 new String[]{IDxFuncParamRefConst.UNION_REPORT_PROJECT_REF,IDxFuncParamRefConst.UNION_REPORT_PROJECT_REF,IDxFuncParamRefConst.AMOUNT_DIRECTION,/*IDxFuncParamRefConst.TIME_ATTR_REF,*/IDxFuncParamRefConst.NONE_REF}
//   	                  new int[]{IFuncType.PARAM_REF_TYPE_ACCOUNT,IFuncType.PARAM_REF_TYPE_ACCOUNT,IFuncType.PARAM_COMBOX_TYPE_CREDIT,IFuncType.PARAM_REF_TYPE_TIME,IFuncType.PARAM_REF_TYPE_NOREF}
    	         	 ) ,
    	   //����Ӧ���о�������㺯�������޸ģ�
    	   new UfoFuncInfo(
    	         	PTPSUM,
    	         	DXFUNC,
    	         	new String[]{UfoFuncList.PARANAMES[UfoFuncList.HBACCOUNT],UfoFuncList.PARANAMES[UfoFuncList.DATASOURCE]},
	                new String[]{UfoFuncList.DETAILPARAMSTRING[UfoFuncList.HBACCOUNT],
	                        		UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASOURCE]},
	                new byte[] {
	                    UfoFuncList.HBACCOUNT,
	                    UfoFuncList.INT},
	                     (byte) IFuncType.VALUE,
	                     nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0133")/*@res "����Ӧ���о�������㺯��"*/ /*"����Ӧ���о�������㺯��"*/,
	                new String[]{IDxFuncParamRefConst.UNION_REPORT_PROJECT_REF,IDxFuncParamRefConst.REPORT_DATASOURCE}
	                   /*  new int[]{IFuncType.PARAM_REF_TYPE_ACCOUNT,IFuncType.PARAM_COMBOX_TYPE_DATASOURCE}*/),
    	  //V6.0�������ڲ���������ȡ������(��һ������������)
//    	   new UfoFuncInfo(
//    		      SINTR,
//    		      DXFUNC,
//    		      new String[]{UfoFuncList.PARANAMES[UfoFuncList.HBACCOUNT],UfoFuncList.PARANAMES[UfoFuncList.IDATASOURCE],UfoFuncList.PARANAMES[UfoFuncList.CHECKTYPE],
//	                		UfoFuncList.PARANAMES[UfoFuncList.DATEPROP],"uiufofunc226"},
//	              new String[]{UfoFuncList.DETAILPARAMSTRING[UfoFuncList.HBACCOUNT],UfoFuncList.DETAILPARAMSTRING[UfoFuncList.IDATASOURCE],UfoFuncList.DETAILPARAMSTRING[UfoFuncList.CHECKTYPE],
//	                        		UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATEPROP],"uiufofunc225"},
//	              new byte[] {
//	                    UfoFuncList.HBACCOUNT,
//	                    UfoFuncList.INT,
//	                    UfoFuncList.CHECKTYPE,
//	                    UfoFuncList.DATEPROP | (byte) 0x80,
//	                    UfoFuncList.INT | (byte) 0x80},
//	                     (byte) IFuncType.VALUE,
//	                     hbbbdx100018 /* "�ڲ���������ȡ������"*/,
//	              new int[]{IFuncType.PARAM_REF_TYPE_ACCOUNT,IFuncType.PARAM_COMBOX_TYPE_IDATASOURCE,IFuncType.PARAM_COMBOX_TYPE_CHECKTYPE,IFuncType.PARAM_REF_TYPE_TIME,IFuncType.PARAM_REF_TYPE_NOREF}
//   	              ),
	                   
	         new UfoFuncInfo(
	       	    	SREP,
	       	    	DXFUNC,
	       	    	        new String[]{UfoFuncList.PARANAMES[UfoFuncList.HBACCOUNT],UfoFuncList.PARANAMES[UfoFuncList.DATASOURCE],UfoFuncList.PARANAMES[UfoFuncList.DATASETPARAMCOND]
	       	                		/*UfoFuncList.PARANAMES[UfoFuncList.DATEPROP],*//*"uiufofunc226"*//*,*/
	       	                		/*UfoFuncList.PARANAMES[UfoFuncList.VERSION]*/},
	       	                new String[]{UfoFuncList.DETAILPARAMSTRING[UfoFuncList.HBACCOUNT],UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASOURCE],UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASETPARAMCOND]
	       	                        		/*UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATEPROP],"uiufofunc225"*//*,*/
	       	                        		/*UfoFuncList.DETAILPARAMSTRING[UfoFuncList.VERSION]*/},
	       	                new byte[] {
	       	                    UfoFuncList.HBACCOUNT,
	       	                    UfoFuncList.INT,
	       	                    /*UfoFuncList.DATEPROP | (byte) 0x80,*/
	       	                    UfoFuncList.INT | (byte) 0x80/*,*/
	       	                  /*  UfoFuncList.VERSION | (byte) 0x80*/ },
	       	                     (byte) IFuncType.VALUE,
	       	                     nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0127")/*@res "����ȡ������"*//*"����ȡ������"*/,


	       	                     //�����Ǳ߹�ʽ���µı��,�������� modify By liyongru For V60 at 20110415
	       	                     new String[]{IDxFuncParamRefConst.UNION_REPORT_PROJECT_REF,IDxFuncParamRefConst.REPORT_DATASOURCE/*,IDxFuncParamRefConst.TIME_ATTR_REF*/,IDxFuncParamRefConst.NONE_REF}

//	       	                new int[]{IFuncType.PARAM_REF_TYPE_ACCOUNT,IFuncType.PARAM_COMBOX_TYPE_DATASOURCE,IFuncType.PARAM_REF_TYPE_TIME,IFuncType.PARAM_REF_TYPE_NOREF/*,*/
//	       	                	/*IFuncType.PARAM_REF_TYPE_VER*/}
	       	    	        ),
            new UfoFuncInfo(
        		    TPSUM,
        		    DXFUNC,
        		    new String[]{UfoFuncList.PARANAMES[UfoFuncList.HBACCOUNT],UfoFuncList.PARANAMES[UfoFuncList.DATASOURCE],UfoFuncList.PARANAMES[UfoFuncList.DATASETPARAMCOND]},
	                new String[]{UfoFuncList.DETAILPARAMSTRING[UfoFuncList.HBACCOUNT],
                    		UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASOURCE],UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASETPARAMCOND]},
                    new byte[] {
                            UfoFuncList.HBACCOUNT,
                            UfoFuncList.INT,
                            UfoFuncList.INT | (byte) 0x80},
                    (byte) IFuncType.VALUE,
                    nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0134")/*@res "Ӧ�����ۼƾ�������㺯��"*//*"Ӧ�����ۼƾ�������㺯��"*/,
                    new String[]{IDxFuncParamRefConst.UNION_REPORT_PROJECT_REF,IDxFuncParamRefConst.REPORT_DATASOURCE,IDxFuncParamRefConst.NONE_REF}
//                    new int[]{IFuncType.PARAM_REF_TYPE_ACCOUNT,IFuncType.PARAM_COMBOX_TYPE_DATASOURCE,IFuncType.PARAM_REF_TYPE_NOREF}
        		    ),
         
     		//V6.0������UAP�ڲ����׶��˹�����
     	    new UfoFuncInfo(
     	    		UCHECK,
     	    		DXFUNC,
     	    		new String[]{/*UfoFuncList.PARANAMES[UfoFuncList.HBACCOUNT]*/nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("common","UC000-0003069")/*@res "��Ŀ����"*/,UfoFuncList.PARANAMES[UfoFuncList.DATASOURCE]},
	                new String[]{/*UfoFuncList.DETAILPARAMSTRING[UfoFuncList.HBACCOUNT]*/nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0491")/*@res "��ʾ��Ҫ������ݵĿ�Ŀ����"*/,UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASOURCE]
	                        		},
	                new byte[] {
	                    UfoFuncList.HBACCOUNT,
	                    UfoFuncList.INT},
	                     (byte) IFuncType.VALUE,
	                     nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0135")/*@res "UAP�ڲ����׶��˹�����"*/ /* "UAP�ڲ����׶��˹�����"*/,
	                     new String[]{IDxFuncParamRefConst.UAP_ACCOUNT,IDxFuncParamRefConst.UAPDATASOURCE}
	                /*new int[]{IFuncType.PARAM_REF_TYPE_UAPACCOUNT,IFuncType.PARAM_REF_TYPE_TIME,IFuncType.PARAM_REF_TYPE_NOREF}*/),
	         new UfoFuncInfo(
	         	     UCHECKBYKEY,
	         	     DXFUNC,
	         	     new String[]{/*UfoFuncList.PARANAMES[UfoFuncList.HBACCOUNT]*/nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("common","UC000-0003069")/*@res "��Ŀ����"*/
	         	    		,UfoFuncList.PARANAMES[UfoFuncList.DATASOURCE]
	         	    		,"ȡ����ʽ"
	         	    		,"ȡ������"},
	    	         new String[]{/*UfoFuncList.DETAILPARAMSTRING[UfoFuncList.HBACCOUNT]*/nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0491")/*@res "��ʾ��Ҫ������ݵĿ�Ŀ����"*/
	         	    		,UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASOURCE]
	         	    		,"ȡ����ʽ������������ȡ���ǰ�����ȡ"
	         		        ,"��������ȡ������"},
	    	         new byte[] {
	    	                UfoFuncList.HBACCOUNT,
	    	                UfoFuncList.INT,
	    	                UfoFuncList.INT,
	    	                UfoFuncList.VALUE},
	    	                 (byte) IFuncType.VALUE,
	    	                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0135")/*@res "UAP�ڲ����׶��˹�����"*/ /* "UAP�ڲ����׶��˹�����"*/,
	    	                new String[]{IDxFuncParamRefConst.UAP_ACCOUNT,IDxFuncParamRefConst.UAPDATASOURCE,IDxFuncParamRefConst.UAPDATATYPE,IDxFuncParamRefConst.ACCCONDITION}
	    	           /*new int[]{IFuncType.PARAM_REF_TYPE_UAPACCOUNT,IFuncType.PARAM_REF_TYPE_TIME,IFuncType.PARAM_REF_TYPE_NOREF}*/),
	    	           
			new UfoFuncInfo(
					UCHECKBYORG,
					DXFUNC,
					new String[] {/* UfoFuncList.PARANAMES[UfoFuncList.HBACCOUNT] */
							nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("common", "UC000-0003069")/*
																		 * @res
																		 * "��Ŀ����"
																		 */
							, UfoFuncList.PARANAMES[UfoFuncList.DATASOURCE]
							, "ȡ����ʽ"
							, "ȡ������"
							, "�Է���λ" },
					new String[] {/*
								 * UfoFuncList.DETAILPARAMSTRING[UfoFuncList.
								 * HBACCOUNT]
								 */
							nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0491")/*
																		 * @res
																		 * "��ʾ��Ҫ������ݵĿ�Ŀ����"
																		 */
							,UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASOURCE]
							,"ȡ����ʽ������������ȡ���ǰ�����ȡ"
							, "��������ȡ������"
							, "�Է���λ" },
					new byte[] { 
							UfoFuncList.HBACCOUNT, 
							UfoFuncList.INT,
							UfoFuncList.INT, 
							UfoFuncList.VALUE,
							UfoFuncList.VALUE },
					(byte) IFuncType.VALUE,
					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0135")/* @res "UAP�ڲ����׶��˹�����" *//* "UAP�ڲ����׶��˹�����" */,
					new String[] { 
							IDxFuncParamRefConst.UAP_ACCOUNT,
							IDxFuncParamRefConst.UAPDATASOURCE,
							IDxFuncParamRefConst.UAPDATATYPE,
							IDxFuncParamRefConst.ACCCONDITION,
							IDxFuncParamRefConst.OPPORGREF }
			/*
			 * new int[]{IFuncType.PARAM_REF_TYPE_UAPACCOUNT,IFuncType.
			 * PARAM_REF_TYPE_TIME,IFuncType.PARAM_REF_TYPE_NOREF}
			 */),
	           
    	 new UfoFuncInfo(ZMONTH, DXFUNC, null, (byte) IFuncType.INTEGER, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0136")/*@res "zmonth()��һ����ĩ"*//*"��ǰ��������ڵ���"*/)
    };


}
