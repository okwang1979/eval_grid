package nc.impl.iufo.total.hb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.impl.iufo.total.HBTotalPubdataDMO;
import nc.impl.iufo.total.HBTotalRepDataDMO;
import nc.itf.hbbb.hbrepstru.IHBRepstruQrySrv;
import nc.itf.iufo.commit.ICommitManageService;
import nc.itf.iufo.data.IRepDataSrv;
import nc.itf.iufo.total.hb.IHBTotalManageService;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.jdbc.framework.util.DBConsts;
import nc.pub.iufo.cache.KeyGroupCache;
import nc.pub.iufo.cache.ReportCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pub.iufo.exception.UFOSrvException;
import nc.pubitf.eaa.InnerCodeUtil;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.util.iufo.storecell.StoreCellUtil;
import nc.vo.corg.ReportCombineStruMemberVO;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.hbbb.total.HbTotalSchemeVO;
import nc.vo.iufo.data.MeasureDataUtil;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.storecell.IStoreCell;
import nc.vo.vorg.ReportCombineStruMemberVersionVO;
import nc.vo.vorg.ReportCombineStruVersionVO;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.iuforeport.repdatainput.RepDataOperResultVO;


/**
 * 合并汇总管理服务实现类
 * @author xulink
 *
 */
public class HBTotalManageImpl implements IHBTotalManageService{

	private IRepDataSrv repDataSrv;

	private IRepDataSrv getRepDataSrv(){
		if(repDataSrv == null){
			repDataSrv = NCLocator.getInstance().lookup(IRepDataSrv.class);
		}
		return repDataSrv;
	}
	
	@Override
	public RepDataOperResultVO createTotalResults(MeasurePubDataVO mainPubData,String busiTime,
			HbTotalSchemeVO totalScheme, String[] reportIds,
			boolean[] extendParams,HBSchemeVO hbschemeVO,String oper_user,String mainOrgPK) throws UFOSrvException {

    	//执行汇总的报表
//    	String[] validReportIds = reportIds.toArray(new String[0]);
    	//当前规则涉及的参与汇总的组织sql
		try {
			String strSQL = getTotalOrgCondSQL(totalScheme,mainPubData,busiTime,hbschemeVO.getPk_hbscheme());
			HBTotalPubdataDMO tempDMO = new HBTotalPubdataDMO();
			boolean joinTotal = true;
			if (!(totalScheme.getTotalType()== HbTotalSchemeVO.TOTAL_TYPE_DIY)) {
				joinTotal = checkTotalOrgs(strSQL);
			}
			
			if(joinTotal) {
				//生成临时表（存放参与汇总的表，同iufo_measure_pubdata表结构完全相同的临时表）
				String strCondTable = tempDMO.createTempPubDataTableFromCond(strSQL,mainPubData,reportIds,/*bContainNotCommit*/ true,/*bPermission*/ true,totalScheme,hbschemeVO);
				//汇总前，需要将参加汇总的表对应该aloneid的数据清掉
		        for (String validReportId : reportIds) {
		        	getRepDataSrv().removeOneRepData(validReportId,null,mainPubData,true);
		        }
				//生成汇总结果
		      
				innerCreateTotalResult(mainPubData,reportIds,strCondTable,oper_user,totalScheme, hbschemeVO);
		   
				//更新报表的最后修改时间，最后修改人
		        ICommitManageService commitSrv=NCLocator.getInstance().lookup(ICommitManageService.class);
	//	        for (String validReportId : validReportIds) {
	//	        	 commitSrv.addRepInputSate(pk_task, mainPubData.getAloneID(), validReportId, oper_user, true, null);
	//	        }
			}
    
		} catch (Exception e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * 生成汇总表数据的方法，被createTotalResult和createTotalSubResults方法调用，
	 * @param mainPubData，汇总结果对应MeasurePubDataVO
	 * @param refIds,参加汇总的报表
	 * @param strSQL，筛选条件的SQL语句
	 * @param strTotalID，汇总条件ID，如果为空，表示将汇总结果存进个别报表数据中
	 * @throws Exception
	 */
//	private void innerCreateTotalResult(MeasurePubDataVO mainPubData,String[] refIds,String strCondTable,String strTotalTimeID,DataSourceVO dataSource) throws Exception{
	private void innerCreateTotalResult(MeasurePubDataVO mainPubData,String[] reportIds,String strCondTable,String userPk,HbTotalSchemeVO totalScheme,HBSchemeVO hbschemeVO) throws Exception{

		// 主表关键字的PubData先存入DB,防止自有汇总时出现查询主表关键字为Null的情况，汇总会出现异常
		MeasurePubDataBO_Client.getAloneID(mainPubData);
		//生成参加汇总的报表ID的数组
	    Vector<String> vRepID=new Vector<String>(Arrays.asList(reportIds));

	    Object[] objs=getPubDataAndMeasTableHashtable(mainPubData,reportIds,strCondTable,userPk,vRepID);

	    Hashtable<String,Set<MeasurePubDataVO>> hashPubData=(Hashtable<String,Set<MeasurePubDataVO>>)objs[0];
	    Hashtable<String,List<Set<IStoreCell>>> tableMeasHashtable =(Hashtable<String,List<Set<IStoreCell>>>)objs[1];

	    Enumeration<String> enumPubData=hashPubData.keys();
	    while (enumPubData.hasMoreElements()){
	        String strDBTable=enumPubData.nextElement();
	        Set<MeasurePubDataVO> vPubData=hashPubData.get(strDBTable);

//	        Vector<MeasurePubDataVO> vOneAllPubData=new Vector<MeasurePubDataVO>(Arrays.asList(vAllPubData.toArray(new MeasurePubDataVO[0])));
	        //将vPubData中的MeasurePubDataVO置上已生成的aloneid值
			List<String> alone_ids = new ArrayList<>();
	        for (MeasurePubDataVO onePubData:vPubData){
	        	alone_ids.add(MeasureDataUtil.getAloneID(onePubData));
	        }
	        //执行汇总
	        excuteTotalResult(new ArrayList<MeasurePubDataVO>(vPubData), mainPubData,tableMeasHashtable,strDBTable, strCondTable,vRepID);
	    }

	    //执行汇总结果的计算和保存
//	    calcTotalFormulas(strUserID,mainPubData, refIds,strOrgPK,dataSource);
	  //  calcTotalFormulas(userPk,mainPubData, reportIds);

	    //TODO:WUYONG改方法：往iufo_checkresult表中插入记录
//	    CheckResultDMO dmo = new CheckResultDMO();
//	    dmo.creatInputResult(mainPubData.getAloneID(), reportIds);
	}

	private Object[] getPubDataAndMeasTableHashtable(MeasurePubDataVO targetPubData,String[] reportIds,String strCondTable,String userPk,Vector<String> vRepID) throws Exception{
		ReportCache repCache = UFOCacheManager.getSingleton().getReportCache();
		KeyGroupCache keyCache = UFOCacheManager.getSingleton().getKeyGroupCache();

		//得到参加汇总报表中所有的关键字组合
		Hashtable<String,KeyGroupVO> hashKeyGroupPK = new Hashtable<String,KeyGroupVO>();
		for (String reportId : reportIds) {
			String[] strKeyGroupPKs = repCache.getKeyCombs(reportId);
			if(strKeyGroupPKs == null || strKeyGroupPKs.length <= 0)
				continue;
			for (String strKeyGroupPK : strKeyGroupPKs) {
				KeyGroupVO keyGroup = keyCache.getByPK(strKeyGroupPK);
				if(keyGroup == null)
					keyGroup = new KeyGroupVO();
					hashKeyGroupPK.put(strKeyGroupPK, keyGroup);
			}
		}

		//得到报表中所有的指标，包括自身的和引用的
		IStoreCell[] storeCells= StoreCellUtil.getMeasureStoreCellsByRepIds(reportIds);
		Hashtable<String,List<Set<IStoreCell>>> tableMeasHashtable = new Hashtable<String,List<Set<IStoreCell>>>();
		//指标数据表：主表pubVO
		Hashtable<String,Set<MeasurePubDataVO>> hashPubData=new Hashtable<String,Set<MeasurePubDataVO>>();
		Set<MeasurePubDataVO> setAllPubData=new HashSet<MeasurePubDataVO>();
		//按指标数据表分组指标和MeasurePubDataVO
		for(int i = 0; storeCells!=null && i < storeCells.length; i++){
			List<Set<IStoreCell>> vStoreCell =tableMeasHashtable.get(storeCells[i].getDbtable());
			if(vStoreCell == null){
				vStoreCell = new Vector<Set<IStoreCell>>();
				vStoreCell.add(new HashSet<IStoreCell>());
				vStoreCell.add(new HashSet<IStoreCell>());
				Set<MeasurePubDataVO> vPubData=new HashSet<MeasurePubDataVO>();
				//此处因为新版中已无私有关键字
				if (targetPubData.getKType().equals(storeCells[i].getKeyCombPK()))
				{
					vPubData.add((MeasurePubDataVO)targetPubData.clone());
				}
				tableMeasHashtable.put(storeCells[i].getDbtable(), vStoreCell);
				hashPubData.put(storeCells[i].getDbtable(),vPubData);
			}

			Set<IStoreCell> oneSetMeasure=null;
			if (storeCells[i].getType()==IStoreCell.TYPE_NUMBER || storeCells[i].getType()==IStoreCell.TYPE_BIGDECIMAL){
				oneSetMeasure=vStoreCell.get(0);
			}
			else{
				oneSetMeasure=vStoreCell.get(1);
			}
			oneSetMeasure.add(storeCells[i]);
		}

		HBTotalPubdataDMO pubdataDMO = new HBTotalPubdataDMO();
		//按关键字组合，得到生成的汇总结果对应的MeasurePubDataVO
		Enumeration<KeyGroupVO> enumKeyGroup = hashKeyGroupPK.elements();
		while(enumKeyGroup.hasMoreElements()){
			KeyGroupVO keyGroup =enumKeyGroup.nextElement();
			MeasurePubDataVO[] pubDatas = null;

			//如果关键字组合与主表关键字相同，或关键字组合中含有私有关键字，则直接取主表的MeasurePubDataVO即可
			if(targetPubData.getKType().equals(keyGroup.getKeyGroupPK()))
			{
				continue;
			}
			//查找出生成的汇总结果对应的MeasurePubDataVO
			pubDatas = pubdataDMO.loadAllTotalPubDatas(strCondTable,targetPubData,keyCache.getByPK(targetPubData.getKType()),keyGroup,null);

			if (pubDatas==null || pubDatas.length<=0)
				continue;

			//对指标数据表做循环
			Enumeration<String> enumTable=tableMeasHashtable.keys();
			while(enumTable.hasMoreElements()){
				String strDBTable=enumTable.nextElement();

				//得到指标数据表中的指标
				List<Set<IStoreCell>> vStoreCell=tableMeasHashtable.get(strDBTable);
				if (vStoreCell==null || vStoreCell.size()<=0)
					continue;

				//如果指标数据表中的指标的关键字组合与当前的关键字不相同，则忽略
				IStoreCell measure=null;
				if (vStoreCell.get(0).size()>0)
					measure=vStoreCell.get(0).toArray(new IStoreCell[1])[0];
				else
					measure=vStoreCell.get(1).toArray(new IStoreCell[1])[0];
				if (measure.getKeyCombPK().equals(keyGroup.getKeyGroupPK())==false)
					continue;

				//查找当前指标数据表需要插入的汇总结果的MeasurePubDataVO
				pubDatas=pubdataDMO.loadAllTotalPubDatas(strCondTable, targetPubData,keyCache.getByPK(targetPubData.getKType()), keyGroup,strDBTable);
				if (pubDatas==null || pubDatas.length<=0)
					continue;

				//将查找到的MeasurePubDataVO转化成存放汇总结果对应的MeasurePubDataVO
				Set<MeasurePubDataVO> vPubData=hashPubData.get(strDBTable);
				for (MeasurePubDataVO pubData : pubDatas) {
					pubData.setVer(targetPubData.getVer());
					vPubData.add(pubData);
					setAllPubData.add(pubData);
				}
			}
		}
		MeasurePubDataVO[] allPubDatas=setAllPubData.toArray(new MeasurePubDataVO[0]);

		// @edit by wuyongc at 2014-1-9,上午9:41:49
		for (MeasurePubDataVO allPubData : allPubDatas) {
			//此方法远程调用次数过多
			String strAloneID=MeasurePubDataBO_Client.getAloneID(allPubData);
			allPubData.setAloneID(strAloneID);
		}
		return new Object[]{hashPubData,tableMeasHashtable};
	}
	
	private boolean checkTotalOrgs(String sql) {
		BaseDAO dao = new BaseDAO();
		List<Object[]> objs = null;
		try {
			objs = (List<Object[]>)dao.executeQuery(sql, new ArrayListProcessor());
		} catch (DAOException e) {
			AppDebug.debug(e);
		}
		if(objs == null || objs.size() <= 0) {
			return false;
		}
		else{
			return true;
		}
	}

	private String getTotalOrgCondSQL(HbTotalSchemeVO totalScheme,MeasurePubDataVO mainPubData, String busiTime, String pk_hbscheme) throws UFOSrvException {
		KeyGroupVO keyGroup = mainPubData.getKeyGroup();
		KeyVO timeKey = keyGroup.getTTimeKey();
		IHBRepstruQrySrv service = NCLocator.getInstance().lookup(IHBRepstruQrySrv.class);
		ReportCombineStruVersionVO rcs = null;
				
//		if (timeKey != null) {
//			String strDate = mainPubData.getKeywordByPK(timeKey.getPk_keyword());
//			if (timeKey.isAccPeriodKey()) {
//				rcs  = service.getHBRepStruVOByAccPeriod(mainPubData.getAccSchemePK(), timeKey.getPk_keyword(), strDate, totalScheme.getPk_rms());
//			} else {
//				rcs = service.getHBRepStruVOByDate(strDate, totalScheme.getPk_rms());
//			}
//		}else{
//			rcs = service.getHBRepStruVOByDate(busiTime, totalScheme.getPk_rms());
//		}
		return getTotalOrgCondSQL( totalScheme,  totalScheme.getPk_rmsversion(),  pk_hbscheme);
		
	}

	public String getTotalOrgCondSQL(HbTotalSchemeVO totalScheme, String rcsVerPk, String pk_hbscheme)throws UFOSrvException{
			try {
				BaseDAO dao=new BaseDAO();
				StringBuffer buffer = new StringBuffer();
				if (totalScheme.getTotalType()== HbTotalSchemeVO.TOTAL_TYPE_DIY)
				{
					//自定义的类型
				}else if(totalScheme.getTotalType()== HbTotalSchemeVO.TOTAL_TYPE_NOT){
					//不汇总
				}else {
					Collection set=dao.retrieveByClause(ReportCombineStruMemberVersionVO.class,"pk_svid='"+rcsVerPk +"' and pk_org='"+totalScheme.getPk_org()+"'");
					if (set==null || set.size()<=0)
					{
						throw new UFOSrvException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1139")/*@res "没有找到组织成员"*/);
					}
					String strInnerCode=((ReportCombineStruMemberVO)set.toArray()[0]).getInnercode();
					buffer.append(" select t1.pk_org from org_rcsmember_v t1  where t1.pk_svid='"+rcsVerPk+"' and t1.innercode like '"+strInnerCode+"%'") ;
					    if (totalScheme.getTotalType()== HbTotalSchemeVO.TOTAL_TYPE_DIRECT)
						{
					    	//直接下级
					    	if (dao.getDBType()==DBConsts.ORACLE || dao.getDBType()==DBConsts.DB2)
					    	{
					    		buffer.append(" and t1.innercode <> '"+strInnerCode+"'  and length(t1.innercode)=" +(strInnerCode.trim().length()+InnerCodeUtil.INNERCODELENGTH));
					    	} else if(dao.getDBType()==DBConsts.POSTGRESQL){
					    		buffer.append(" and t1.innercode <> '"+strInnerCode+"'  and char_length(t1.innercode)=" +(strInnerCode.trim().length()+InnerCodeUtil.INNERCODELENGTH));
					    	} else
					    	{
					    		buffer.append(" and t1.innercode <> '"+strInnerCode+"'  and len(t1.innercode)=" +(strInnerCode.trim().length()+InnerCodeUtil.INNERCODELENGTH));
					    	}
						}else if (totalScheme.getTotalType()== HbTotalSchemeVO.TOTAL_TYPE_ALL)						{
							//所有末级
							buffer.append(" and t1.innercode <> '"+strInnerCode+"' and not EXISTS  (select 1 from org_rcsmember_v where t1.pk_org=pk_fatherorg and pk_svid = '"+rcsVerPk+"' )");
						}
				    
				}

		        return buffer.toString();
			} catch (Exception ex) {
				AppDebug.debug(ex);
				throw new UFOSrvException(ex.getMessage());
			}
		
	 }
	
	/**
	 * 对一个指标数据表执行生成汇总结果的操作
	 * @param vPubData,需要生成的汇总结果的对应MeasurePubDataVO数组
	 * @param mainPubData，主表的MeasurePubDataVO
	 * @param hashMeasureTable，以指标数据表作主键的将指标分组的Hashtable
	 * @param strDBTable,指标数据表
	 * @param strCond，筛选条件SQL语句
	 * @param strTotalID，汇总条件ID，如果该值为空，表示为录入界面汇总下级
	 * @param vRepID，参加汇总的报表ID数组
	 * @throws Exception
	 */
	private void excuteTotalResult(List<MeasurePubDataVO> vPubData, MeasurePubDataVO mainPubData,Map<String,List<Set<IStoreCell>>> hashMeasureTable,String strDBTable,String strCond,List<String> vRepID) throws Exception{

	    if (vPubData==null || vPubData.size()<=0)
	    	return;

	    Vector<String[]> vKeyVal=new Vector<String[]>();
	    List<Set<IStoreCell>> vStoreCell =hashMeasureTable.get(strDBTable);

	    //得到子表关键字与主表关键字之间的对应关系
	    Hashtable<Integer,Integer> hashKeyPos=genKeywordPos(vPubData.get(0),mainPubData);

	    //抽取一个子表对应的MeasurePubDataVO
	    MeasurePubDataVO pubData=null;
	    for (int i=0;i<vPubData.size();i++){
	        pubData=vPubData.get(i);

	        //为了拼SQL语句的方便，需要将子表MeasurePubDataVO中对应的主表关键字的值清空，并返回被清空以前的值
	        String[] strKeyVals=reSetPubDataKeyVals(pubData, mainPubData,hashKeyPos);

	        //如果当前汇总的是主表，需要判断否有数据需要汇总，子表不需要判断，因为前面已经判断过了
	        if (i==0 && pubData.getKType().equals(mainPubData.getKType()))
	        {
	        	//xulm 暂时不判断是否有数据需要汇总
//	        	if (dataDMO.loadTotalDataCount(mainPubData, pubData, strCond, strDBTable)<=0){
//	        		//没有需要汇总的数据，将MeasurePubDataVO对应的关键字值还原
//	        		pubData.setKeywords(strKeyVals,false);
//	                return;
//	             }
	        }
	        vKeyVal.add(strKeyVals);
	    }

	    IStoreCell measure =null;
	    if (vStoreCell.get(0).size()>0)
	    	measure=vStoreCell.get(0).toArray(new IStoreCell[1])[0];
	    else
	    	measure=vStoreCell.get(1).toArray(new IStoreCell[1])[0];

	    String strKeyGroupPK = measure.getKeyCombPK();

	    //存储数值指标
	    Set<IStoreCell> setNumStoreCell=vStoreCell.get(0);
	    //存储非数值指标
	    Set<IStoreCell> setNoNumStoreCell=vStoreCell.get(1);
	    HBTotalRepDataDMO repdataDMO = new HBTotalRepDataDMO();
	    if (setNumStoreCell.size()>0){

		    //执行插入或更新数据的操作
		    //如果动态区有私有关键字，对于汇总下级与真正汇总，其采用的方法是一样的
		    if(pubData.getKType().equals(strKeyGroupPK) == false){
		    	//xulm 私有关键字已经不存在了，所以此处暂不处理
//		    	setNumMeasure.addAll(setNoNumMeasure);
//		    	setNoNumMeasure.clear();
//		    	dataDMO.createTotalDynDatas(strDBTable,strCond,new ArrayList<MeasureVO>(setNumMeasure),pubData,strKeyGroupPK);
		    }
		    //如果是真正汇总或指标属于参加汇总的报表，则用insert语句实现
//			else if (strTotalTimeID!=null || vRepID.contains(measure.getReportPK())==true)
//		    else if (strTotalTimeID!=null)
//			{
//		    	repdataDMO.createTotalNoDynDatasByInsert(strDBTable,strCond,new ArrayList<IStoreCell>(setNumStoreCell),mainPubData,vPubData);
//			}
			//如果对应汇总下级，并且指标属于引用指标，需要判断是用insert还是用update语句实现
			else
			{
				repdataDMO.createTotalNoDynDatasWithHZSub(strDBTable,strCond,new ArrayList<IStoreCell>(setNumStoreCell),mainPubData,vPubData);
			}
	    }

	    //TODO:字符是如何处理呢？？解决主表扩展区字符类型汇总后显示空值的问题。 modified by jiaah at 20110618
//	    if (setNoNumStoreCell.size()>0 && (pubData.getKType().equals(mainPubData.getKType())==false || pubData.getKType().equals(strKeyGroupPK) == false)){
	    if (setNoNumStoreCell.size()>0 ){
	    	if (setNumStoreCell.size()>0)
	    	{
	    		repdataDMO.createTotalNoDynDatasWithHZSub(strDBTable,strCond,new ArrayList<IStoreCell>(setNoNumStoreCell),mainPubData,vPubData);
	    	}
	    	else{
			    if(pubData.getKType().equals(strKeyGroupPK) == false)
			    {
			    	//xulm 私有关键字的处理暂时去掉
//			    	dataDMO.createTotalDynDatas(strDBTable,strCond,new ArrayList<MeasureVO>(setNoNumMeasure),pubData,strKeyGroupPK);
			    }
//			    else if (strTotalTimeID!=null || vRepID.contains(measure.getReportPK())==true)
//			    else if (strTotalTimeID!=null)
//			    {
//			    	repdataDMO.createTotalNoDynDatasByInsert(strDBTable,strCond,new ArrayList<IStoreCell>(setNoNumStoreCell),mainPubData,vPubData);
//			    }
				else{
					repdataDMO.createTotalNoDynDatasWithHZSub(strDBTable,strCond,new ArrayList<IStoreCell>(setNoNumStoreCell),mainPubData,vPubData);
				}
	    	}
	    }

	    //将MeasurePubDataVO对应的关键字值还原
	    for (int i=0;i<vPubData.size();i++){
	    	String[] strKeyVal=vKeyVal.get(i);
	    	pubData=vPubData.get(i);
	    	pubData.setKeywords(strKeyVal,false);
	    }
	}
	
	/**
	 * 对某一个MeasurePubDataVO，用主表中关键字值替换其对应的关键字值
	 * @param pubData，要替换的MeasurePubDataVO
	 * @param mainPubData,主表MeasurePubDataVO
	 * @param hashPos,主表关键字与子表关键字的对照关系
	 * @return
	 */
	private String[] reSetPubDataKeyVals(MeasurePubDataVO pubData, MeasurePubDataVO mainPubData,Hashtable<Integer,Integer> hashPos){
		String[] strRetKeyVals=new String[pubData.getKeywords().length];
		for (int i=0;i<pubData.getKeywords().length;i++){
			String strKeyVal=pubData.getKeywordByIndex(i+1);
			if (strKeyVal!=null && strKeyVal.trim().length()>0)
				strRetKeyVals[i]=new String(strKeyVal);
		}

		for (int i=0;i<mainPubData.getKeywords().length;i++){
			String strKeyVal=mainPubData.getKeywordByIndex(i+1);
			if (strKeyVal!=null && strKeyVal.trim().length()>0){
				if (hashPos.get(Integer.valueOf(i))!=null){
					int iSubPos=hashPos.get(Integer.valueOf(i)).intValue();
					pubData.setKeywordByIndex(iSubPos+1,null);
				}
			}
		}
		return strRetKeyVals;
	}

	/**
	 * 得到动态区关键字与主表关键字的对应关系，即对于主表中某一关键字，其在动态区关键字中位于第几的位置
	 * @param subPubData
	 * @param mainPubData
	 * @return
	 */
	private Hashtable<Integer,Integer> genKeywordPos(MeasurePubDataVO subPubData,MeasurePubDataVO mainPubData){
	 	KeyVO[] mainKeys=mainPubData.getKeyGroup().getKeys();
	 	KeyVO[] subKeys=subPubData.getKeyGroup().getKeys();
		Hashtable<Integer,Integer> hashPos=new Hashtable<Integer,Integer>();
		for (int i=0;i<mainKeys.length;i++)
		{
			for (int j=0;j<subKeys.length;j++)
			{
				if (subKeys[j].equals(mainKeys[i]))
				{
					hashPos.put(Integer.valueOf(i),Integer.valueOf(j));
					break;
				}
			}
		}
		return hashPos;
	}

}