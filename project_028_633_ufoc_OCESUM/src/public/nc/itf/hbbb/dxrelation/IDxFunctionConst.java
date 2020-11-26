package nc.itf.hbbb.dxrelation;


import com.ufsoft.iufo.util.parser.IFuncType;
import com.ufsoft.script.function.UfoFuncInfo;
import com.ufsoft.script.function.UfoFuncList;

public interface IDxFunctionConst {

	 /** 抵销模板函数类型 */
    public static final byte DXFUNC = 1;

    /** 抵销模板函数类型名称 */
    public static final String[] CATNAMES ={ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0126")/*@res "抵销模板函数"*/ };//"抵销模板函数"


    /** 抵销模板类型  直接总账内部交易对账规则  */
    public static final String DirectDealMeet = "8";
	 /**
     * 抵销模板函数名称
     * 函数名称为英文大写
     * */
    public final static String CESUM = "CESUM";			//抵销分录合计值取数函数
    public final static String DPSUM = "DPSUM";         //已收到累计净利润计算函数
    public final static String INTR = "INTR";					//内部交易取数函数
    public final static String INTRBYKEY = "INTRBYKEY";           //内部交易取数函数
//    public final static String INVSUM = "INVSUM";					//累计投资函数
    public final static String IPROPORTION = "IPROPORTION";	//直接投资比例函数
    public final static String OPCE = "OPCE";			//抵销分录对方项目发生额函数
    public final static String PTPSUM = "PTPSUM";       //当年应享有净利润计算函数
//    public final static String SINTR = "SINTR";				//V6.0新增，内部购销交易取数函数
    public final static String SREP = "SREP";						//报表取数函数
    public final static String TPSUM = "TPSUM";         //应享有累计净利润计算函数
    public final static String UCHECK = "UCHECK";		//V6.0新增，UAP内部交易对账规则函数
    public final static String UCHECKBYKEY = "UCHECKBYKEY";		//V6.0新增，UAP内部交易对账规则函数
    public final static String UCHECKBYORG = "UCHECKBYORG";		//V6.0新增，UAP内部交易对账规则函数

    public final static String ESELECT = "ESELECT";		//V6.0新增，其他取数函数
    public final static String ZMONTH = "ZMONTH";

    public final static String KEYFUNC = "K";//63支持k函数

    /**
	 * 抵销模板函数定义
	 * 需要按照升序排序且函数名称为英文大写
	 * 将下列列表的参数类型都定义为int型
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
    	                    UfoFuncList.STRING,///合并科目的类型暂时定义为string（需要修改）
    	                    UfoFuncList.INT,//借贷方参数类型为int
    	                    /*UfoFuncList.DATEPROP |(byte) 0x80,*/
    	                    UfoFuncList.INT |(byte) 0x80 },
    	                     (byte) IFuncType.VALUE,
    	                     nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0128")/*@res "抵销分录合计值取数函数"*//*"抵销分录合计值取数函数"*/,
    	                  new String[]{IDxFuncParamRefConst.UNION_REPORT_PROJECT_REF,IDxFuncParamRefConst.AMOUNT_DIRECTION,/*IDxFuncParamRefConst.TIME_ATTR_REF,*/IDxFuncParamRefConst.NONE_REF}
//    	                new int[]{IFuncType.PARAM_REF_TYPE_ACCOUNT,IFuncType.PARAM_COMBOX_TYPE_CREDIT,IFuncType.PARAM_REF_TYPE_TIME,IFuncType.PARAM_REF_TYPE_NOREF}
    	             ),
    	 new UfoFuncInfo(
    	 	         	     "CHECKBYKEY",
    		         	     DXFUNC,
    		         	     new String[]{/*UfoFuncList.PARANAMES[UfoFuncList.HBACCOUNT]*/nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("common","UC000-0003069")/*@res "科目名称"*/
    		         	    		,UfoFuncList.PARANAMES[UfoFuncList.DATASOURCE]
    		         	    		,"取数方式"
    		         	    		,"取数条件"},
    		    	         new String[]{/*UfoFuncList.DETAILPARAMSTRING[UfoFuncList.HBACCOUNT]*/nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0491")/*@res "表示需要获得数据的科目名称"*/
    		         	    		,UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASOURCE]
    		         	    		,"取数方式：按辅助核算取还是按账龄取"
    		         		        ,"辅助核算取数条件"},
    		    	         new byte[] {
    		    	                UfoFuncList.STRING,
    		    	                UfoFuncList.INT,
    		    	                UfoFuncList.INT,
    		    	                UfoFuncList.VALUE},
    		    	                 (byte) IFuncType.VALUE,
    		    	                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0135")/*@res "UAP内部交易对账规则函数"*/ /* "UAP内部交易对账规则函数"*/,
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
    	                     nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0129")/*@res "已收到累计净利润计算函数"*/ /*"已收到累计净利润计算函数"*/,
    	                new String[]{IDxFuncParamRefConst.UNION_REPORT_PROJECT_REF,IDxFuncParamRefConst.REPORT_DATASOURCE,IDxFuncParamRefConst.NONE_REF}
//    	                     new int[]{IFuncType.PARAM_REF_TYPE_ACCOUNT,IFuncType.PARAM_COMBOX_TYPE_DATASOURCE,IFuncType.PARAM_REF_TYPE_NOREF}
    	         	),
    	    new UfoFuncInfo(
         	    	  ESELECT,
         	    	  DXFUNC,
         	    	        new String[]{UfoFuncList.PARANAMES[UfoFuncList.HBACCOUNT],/*UfoFuncList.PARANAMES[UfoFuncList.DATASOURCE],*/UfoFuncList.PARANAMES[UfoFuncList.DATASETPARAMCOND]

         	                		/*UfoFuncList.PARANAMES[UfoFuncList.DATEPROP],"uiufofunc226"*/,
         	                		nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("common","UCMD1-000551")/*@res "版本"*/},
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
         	                     nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0490")/*@res "其他取数函数"*/  /*hbbbdx100019*//*"报表取数函数"*/,


         	                     //报表那边公式有新的变更,适配其用 modify By liyongru For V60 at 20110415
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
    	                     nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0130")/*@res "内部交易取数函数"*/ /*"内部交易取数函数"*/,
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
         		    				"关键字条件"
         		    				/*
         		    				 * UfoFuncList.PARANAMES[UfoFuncList.DATEPROP],"uiufofunc226"
         		    				 */},
         		    				 new String[] {
         		    				UfoFuncList.DETAILPARAMSTRING[UfoFuncList.HBACCOUNT],
         		    				UfoFuncList.DETAILPARAMSTRING[UfoFuncList.IDATASOURCE],
         		    				UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASETPARAMCOND],
         		    				"取关键字条件"
         		    				/*
         		    				 * UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATEPROP],
         		    				 * "uiufofunc225"
         		    				 */},
         		    				 new byte[] { UfoFuncList.HBACCOUNT, UfoFuncList.INT,
         		    				/* UfoFuncList.DATEPROP | (byte) 0x80, */
         		    				UfoFuncList.INT | (byte) 0x80,
         		    				UfoFuncList.VALUE | (byte) 0x80 },
         		    				(byte) IFuncType.VALUE,
         		    				"内部交易取数函数-条件",
         		    				new String[] {
         		    			IDxFuncParamRefConst.UNION_REPORT_PROJECT_REF,
         		    			IDxFuncParamRefConst.REPORT_INTRDATASOURCE,/*
         		    			 * IDxFuncParamRefConst
         		    			 * .
         		    			 * TIME_ATTR_REF
         		    			 * ,
         		    			 */
         		    			IDxFuncParamRefConst.NONE_REF,
         		    			IDxFuncParamRefConst.HBBB_KEY_WORD_REF /* 用作存货类别的参照 */}
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
	                     nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0131")/*@res "直接投资比例函数"*/,//"直接投资比例函数",
	              new String[]{/*IDxFuncParamRefConst.TIME_ATTR_REF,*/IDxFuncParamRefConst.NONE_REF}
//	                   new int[]{/*IFuncType.PARAM_COMBOX_TYPE_CHECKTYPE,*/IFuncType.PARAM_REF_TYPE_TIME,IFuncType.PARAM_REF_TYPE_NOREF}
    	         	),
//    	   new UfoFuncInfo(IPROPORTION, (byte)0,null, (byte) IFuncType.FLOAT, hbbbdx100015),//"直接投资比例函数"
    	  new UfoFuncInfo(
                    "OESUM",
        	             DXFUNC,
        	             new String[]{"合并方案","抵消分录类型",UfoFuncList.PARANAMES[UfoFuncList.HBACCOUNT],UfoFuncList.PARANAMES[UfoFuncList.CREDIT],UfoFuncList.PARANAMES[UfoFuncList.DATASETPARAMCOND]
        	                		/*UfoFuncList.PARANAMES[UfoFuncList.DATEPROP],"uiufofunc226"*/},
        	                new String[]{"合并方案","抵消分录类型",UfoFuncList.DETAILPARAMSTRING[UfoFuncList.HBACCOUNT],UfoFuncList.DETAILPARAMSTRING[UfoFuncList.CREDIT],UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASETPARAMCOND]
        	                        		/*UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATEPROP],"uiufofunc225"*/},
        	                new byte[] {
        	            		 UfoFuncList.STRING,
        	            		 UfoFuncList.INT |(byte) 0x80 ,
        	                    UfoFuncList.STRING,///合并科目的类型暂时定义为string（需要修改）
        	                    UfoFuncList.INT,//借贷方参数类型为int
        	                    /*UfoFuncList.DATEPROP |(byte) 0x80,*/
        	                    UfoFuncList.INT |(byte) 0x80 },
        	                     (byte) IFuncType.VALUE,
        	                     nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0128")/*@res "抵销分录合计值取数函数"*//*"抵销分录合计值取数函数"*/,
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
   	                  nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0132")/*@res "抵销分录对方项目发生额函数"*//*"抵销分录对方项目发生额函数"*/,
   	                 new String[]{IDxFuncParamRefConst.UNION_REPORT_PROJECT_REF,IDxFuncParamRefConst.UNION_REPORT_PROJECT_REF,IDxFuncParamRefConst.AMOUNT_DIRECTION,/*IDxFuncParamRefConst.TIME_ATTR_REF,*/IDxFuncParamRefConst.NONE_REF}
//   	                  new int[]{IFuncType.PARAM_REF_TYPE_ACCOUNT,IFuncType.PARAM_REF_TYPE_ACCOUNT,IFuncType.PARAM_COMBOX_TYPE_CREDIT,IFuncType.PARAM_REF_TYPE_TIME,IFuncType.PARAM_REF_TYPE_NOREF}
    	         	 ) ,
    	   //当年应享有净利润计算函数（待修改）
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
	                     nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0133")/*@res "当年应享有净利润计算函数"*/ /*"当年应享有净利润计算函数"*/,
	                new String[]{IDxFuncParamRefConst.UNION_REPORT_PROJECT_REF,IDxFuncParamRefConst.REPORT_DATASOURCE}
	                   /*  new int[]{IFuncType.PARAM_REF_TYPE_ACCOUNT,IFuncType.PARAM_COMBOX_TYPE_DATASOURCE}*/),
    	  //V6.0新增，内部购销交易取数函数(多一个核算类型项)
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
//	                     hbbbdx100018 /* "内部购销交易取数函数"*/,
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
	       	                     nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0127")/*@res "报表取数函数"*//*"报表取数函数"*/,


	       	                     //报表那边公式有新的变更,适配其用 modify By liyongru For V60 at 20110415
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
                    nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0134")/*@res "应享有累计净利润计算函数"*//*"应享有累计净利润计算函数"*/,
                    new String[]{IDxFuncParamRefConst.UNION_REPORT_PROJECT_REF,IDxFuncParamRefConst.REPORT_DATASOURCE,IDxFuncParamRefConst.NONE_REF}
//                    new int[]{IFuncType.PARAM_REF_TYPE_ACCOUNT,IFuncType.PARAM_COMBOX_TYPE_DATASOURCE,IFuncType.PARAM_REF_TYPE_NOREF}
        		    ),
         
     		//V6.0新增，UAP内部交易对账规则函数
     	    new UfoFuncInfo(
     	    		UCHECK,
     	    		DXFUNC,
     	    		new String[]{/*UfoFuncList.PARANAMES[UfoFuncList.HBACCOUNT]*/nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("common","UC000-0003069")/*@res "科目名称"*/,UfoFuncList.PARANAMES[UfoFuncList.DATASOURCE]},
	                new String[]{/*UfoFuncList.DETAILPARAMSTRING[UfoFuncList.HBACCOUNT]*/nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0491")/*@res "表示需要获得数据的科目名称"*/,UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASOURCE]
	                        		},
	                new byte[] {
	                    UfoFuncList.HBACCOUNT,
	                    UfoFuncList.INT},
	                     (byte) IFuncType.VALUE,
	                     nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0135")/*@res "UAP内部交易对账规则函数"*/ /* "UAP内部交易对账规则函数"*/,
	                     new String[]{IDxFuncParamRefConst.UAP_ACCOUNT,IDxFuncParamRefConst.UAPDATASOURCE}
	                /*new int[]{IFuncType.PARAM_REF_TYPE_UAPACCOUNT,IFuncType.PARAM_REF_TYPE_TIME,IFuncType.PARAM_REF_TYPE_NOREF}*/),
	         new UfoFuncInfo(
	         	     UCHECKBYKEY,
	         	     DXFUNC,
	         	     new String[]{/*UfoFuncList.PARANAMES[UfoFuncList.HBACCOUNT]*/nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("common","UC000-0003069")/*@res "科目名称"*/
	         	    		,UfoFuncList.PARANAMES[UfoFuncList.DATASOURCE]
	         	    		,"取数方式"
	         	    		,"取数条件"},
	    	         new String[]{/*UfoFuncList.DETAILPARAMSTRING[UfoFuncList.HBACCOUNT]*/nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0491")/*@res "表示需要获得数据的科目名称"*/
	         	    		,UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASOURCE]
	         	    		,"取数方式：按辅助核算取还是按账龄取"
	         		        ,"辅助核算取数条件"},
	    	         new byte[] {
	    	                UfoFuncList.HBACCOUNT,
	    	                UfoFuncList.INT,
	    	                UfoFuncList.INT,
	    	                UfoFuncList.VALUE},
	    	                 (byte) IFuncType.VALUE,
	    	                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0135")/*@res "UAP内部交易对账规则函数"*/ /* "UAP内部交易对账规则函数"*/,
	    	                new String[]{IDxFuncParamRefConst.UAP_ACCOUNT,IDxFuncParamRefConst.UAPDATASOURCE,IDxFuncParamRefConst.UAPDATATYPE,IDxFuncParamRefConst.ACCCONDITION}
	    	           /*new int[]{IFuncType.PARAM_REF_TYPE_UAPACCOUNT,IFuncType.PARAM_REF_TYPE_TIME,IFuncType.PARAM_REF_TYPE_NOREF}*/),
	    	           
			new UfoFuncInfo(
					UCHECKBYORG,
					DXFUNC,
					new String[] {/* UfoFuncList.PARANAMES[UfoFuncList.HBACCOUNT] */
							nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("common", "UC000-0003069")/*
																		 * @res
																		 * "科目名称"
																		 */
							, UfoFuncList.PARANAMES[UfoFuncList.DATASOURCE]
							, "取数方式"
							, "取数条件"
							, "对方单位" },
					new String[] {/*
								 * UfoFuncList.DETAILPARAMSTRING[UfoFuncList.
								 * HBACCOUNT]
								 */
							nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0491")/*
																		 * @res
																		 * "表示需要获得数据的科目名称"
																		 */
							,UfoFuncList.DETAILPARAMSTRING[UfoFuncList.DATASOURCE]
							,"取数方式：按辅助核算取还是按账龄取"
							, "辅助核算取数条件"
							, "对方单位" },
					new byte[] { 
							UfoFuncList.HBACCOUNT, 
							UfoFuncList.INT,
							UfoFuncList.INT, 
							UfoFuncList.VALUE,
							UfoFuncList.VALUE },
					(byte) IFuncType.VALUE,
					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0135")/* @res "UAP内部交易对账规则函数" *//* "UAP内部交易对账规则函数" */,
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
	           
    	 new UfoFuncInfo(ZMONTH, DXFUNC, null, (byte) IFuncType.INTEGER, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0136")/*@res "zmonth()上一年年末"*//*"当前计算的日期的月"*/)
    };


}
