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
 * �ϲ����ܹ������ʵ����
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

    	//ִ�л��ܵı���
//    	String[] validReportIds = reportIds.toArray(new String[0]);
    	//��ǰ�����漰�Ĳ�����ܵ���֯sql
		try {
			String strSQL = getTotalOrgCondSQL(totalScheme,mainPubData,busiTime,hbschemeVO.getPk_hbscheme());
			HBTotalPubdataDMO tempDMO = new HBTotalPubdataDMO();
			boolean joinTotal = true;
			if (!(totalScheme.getTotalType()== HbTotalSchemeVO.TOTAL_TYPE_DIY)) {
				joinTotal = checkTotalOrgs(strSQL);
			}
			
			if(joinTotal) {
				//������ʱ����Ų�����ܵı�ͬiufo_measure_pubdata��ṹ��ȫ��ͬ����ʱ��
				String strCondTable = tempDMO.createTempPubDataTableFromCond(strSQL,mainPubData,reportIds,/*bContainNotCommit*/ true,/*bPermission*/ true,totalScheme,hbschemeVO);
				//����ǰ����Ҫ���μӻ��ܵı��Ӧ��aloneid���������
		        for (String validReportId : reportIds) {
		        	getRepDataSrv().removeOneRepData(validReportId,null,mainPubData,true);
		        }
				//���ɻ��ܽ��
		      
				innerCreateTotalResult(mainPubData,reportIds,strCondTable,oper_user,totalScheme, hbschemeVO);
		   
				//���±��������޸�ʱ�䣬����޸���
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
	 * ���ɻ��ܱ����ݵķ�������createTotalResult��createTotalSubResults�������ã�
	 * @param mainPubData�����ܽ����ӦMeasurePubDataVO
	 * @param refIds,�μӻ��ܵı���
	 * @param strSQL��ɸѡ������SQL���
	 * @param strTotalID����������ID�����Ϊ�գ���ʾ�����ܽ��������𱨱�������
	 * @throws Exception
	 */
//	private void innerCreateTotalResult(MeasurePubDataVO mainPubData,String[] refIds,String strCondTable,String strTotalTimeID,DataSourceVO dataSource) throws Exception{
	private void innerCreateTotalResult(MeasurePubDataVO mainPubData,String[] reportIds,String strCondTable,String userPk,HbTotalSchemeVO totalScheme,HBSchemeVO hbschemeVO) throws Exception{

		// ����ؼ��ֵ�PubData�ȴ���DB,��ֹ���л���ʱ���ֲ�ѯ����ؼ���ΪNull����������ܻ�����쳣
		MeasurePubDataBO_Client.getAloneID(mainPubData);
		//���ɲμӻ��ܵı���ID������
	    Vector<String> vRepID=new Vector<String>(Arrays.asList(reportIds));

	    Object[] objs=getPubDataAndMeasTableHashtable(mainPubData,reportIds,strCondTable,userPk,vRepID);

	    Hashtable<String,Set<MeasurePubDataVO>> hashPubData=(Hashtable<String,Set<MeasurePubDataVO>>)objs[0];
	    Hashtable<String,List<Set<IStoreCell>>> tableMeasHashtable =(Hashtable<String,List<Set<IStoreCell>>>)objs[1];

	    Enumeration<String> enumPubData=hashPubData.keys();
	    while (enumPubData.hasMoreElements()){
	        String strDBTable=enumPubData.nextElement();
	        Set<MeasurePubDataVO> vPubData=hashPubData.get(strDBTable);

//	        Vector<MeasurePubDataVO> vOneAllPubData=new Vector<MeasurePubDataVO>(Arrays.asList(vAllPubData.toArray(new MeasurePubDataVO[0])));
	        //��vPubData�е�MeasurePubDataVO���������ɵ�aloneidֵ
			List<String> alone_ids = new ArrayList<>();
	        for (MeasurePubDataVO onePubData:vPubData){
	        	alone_ids.add(MeasureDataUtil.getAloneID(onePubData));
	        }
	        //ִ�л���
	        excuteTotalResult(new ArrayList<MeasurePubDataVO>(vPubData), mainPubData,tableMeasHashtable,strDBTable, strCondTable,vRepID);
	    }

	    //ִ�л��ܽ���ļ���ͱ���
//	    calcTotalFormulas(strUserID,mainPubData, refIds,strOrgPK,dataSource);
	  //  calcTotalFormulas(userPk,mainPubData, reportIds);

	    //TODO:WUYONG�ķ�������iufo_checkresult���в����¼
//	    CheckResultDMO dmo = new CheckResultDMO();
//	    dmo.creatInputResult(mainPubData.getAloneID(), reportIds);
	}

	private Object[] getPubDataAndMeasTableHashtable(MeasurePubDataVO targetPubData,String[] reportIds,String strCondTable,String userPk,Vector<String> vRepID) throws Exception{
		ReportCache repCache = UFOCacheManager.getSingleton().getReportCache();
		KeyGroupCache keyCache = UFOCacheManager.getSingleton().getKeyGroupCache();

		//�õ��μӻ��ܱ��������еĹؼ������
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

		//�õ����������е�ָ�꣬��������ĺ����õ�
		IStoreCell[] storeCells= StoreCellUtil.getMeasureStoreCellsByRepIds(reportIds);
		Hashtable<String,List<Set<IStoreCell>>> tableMeasHashtable = new Hashtable<String,List<Set<IStoreCell>>>();
		//ָ�����ݱ�����pubVO
		Hashtable<String,Set<MeasurePubDataVO>> hashPubData=new Hashtable<String,Set<MeasurePubDataVO>>();
		Set<MeasurePubDataVO> setAllPubData=new HashSet<MeasurePubDataVO>();
		//��ָ�����ݱ����ָ���MeasurePubDataVO
		for(int i = 0; storeCells!=null && i < storeCells.length; i++){
			List<Set<IStoreCell>> vStoreCell =tableMeasHashtable.get(storeCells[i].getDbtable());
			if(vStoreCell == null){
				vStoreCell = new Vector<Set<IStoreCell>>();
				vStoreCell.add(new HashSet<IStoreCell>());
				vStoreCell.add(new HashSet<IStoreCell>());
				Set<MeasurePubDataVO> vPubData=new HashSet<MeasurePubDataVO>();
				//�˴���Ϊ�°�������˽�йؼ���
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
		//���ؼ�����ϣ��õ����ɵĻ��ܽ����Ӧ��MeasurePubDataVO
		Enumeration<KeyGroupVO> enumKeyGroup = hashKeyGroupPK.elements();
		while(enumKeyGroup.hasMoreElements()){
			KeyGroupVO keyGroup =enumKeyGroup.nextElement();
			MeasurePubDataVO[] pubDatas = null;

			//����ؼ������������ؼ�����ͬ����ؼ�������к���˽�йؼ��֣���ֱ��ȡ�����MeasurePubDataVO����
			if(targetPubData.getKType().equals(keyGroup.getKeyGroupPK()))
			{
				continue;
			}
			//���ҳ����ɵĻ��ܽ����Ӧ��MeasurePubDataVO
			pubDatas = pubdataDMO.loadAllTotalPubDatas(strCondTable,targetPubData,keyCache.getByPK(targetPubData.getKType()),keyGroup,null);

			if (pubDatas==null || pubDatas.length<=0)
				continue;

			//��ָ�����ݱ���ѭ��
			Enumeration<String> enumTable=tableMeasHashtable.keys();
			while(enumTable.hasMoreElements()){
				String strDBTable=enumTable.nextElement();

				//�õ�ָ�����ݱ��е�ָ��
				List<Set<IStoreCell>> vStoreCell=tableMeasHashtable.get(strDBTable);
				if (vStoreCell==null || vStoreCell.size()<=0)
					continue;

				//���ָ�����ݱ��е�ָ��Ĺؼ�������뵱ǰ�Ĺؼ��ֲ���ͬ�������
				IStoreCell measure=null;
				if (vStoreCell.get(0).size()>0)
					measure=vStoreCell.get(0).toArray(new IStoreCell[1])[0];
				else
					measure=vStoreCell.get(1).toArray(new IStoreCell[1])[0];
				if (measure.getKeyCombPK().equals(keyGroup.getKeyGroupPK())==false)
					continue;

				//���ҵ�ǰָ�����ݱ���Ҫ����Ļ��ܽ����MeasurePubDataVO
				pubDatas=pubdataDMO.loadAllTotalPubDatas(strCondTable, targetPubData,keyCache.getByPK(targetPubData.getKType()), keyGroup,strDBTable);
				if (pubDatas==null || pubDatas.length<=0)
					continue;

				//�����ҵ���MeasurePubDataVOת���ɴ�Ż��ܽ����Ӧ��MeasurePubDataVO
				Set<MeasurePubDataVO> vPubData=hashPubData.get(strDBTable);
				for (MeasurePubDataVO pubData : pubDatas) {
					pubData.setVer(targetPubData.getVer());
					vPubData.add(pubData);
					setAllPubData.add(pubData);
				}
			}
		}
		MeasurePubDataVO[] allPubDatas=setAllPubData.toArray(new MeasurePubDataVO[0]);

		// @edit by wuyongc at 2014-1-9,����9:41:49
		for (MeasurePubDataVO allPubData : allPubDatas) {
			//�˷���Զ�̵��ô�������
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
					//�Զ��������
				}else if(totalScheme.getTotalType()== HbTotalSchemeVO.TOTAL_TYPE_NOT){
					//������
				}else {
					Collection set=dao.retrieveByClause(ReportCombineStruMemberVersionVO.class,"pk_svid='"+rcsVerPk +"' and pk_org='"+totalScheme.getPk_org()+"'");
					if (set==null || set.size()<=0)
					{
						throw new UFOSrvException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1139")/*@res "û���ҵ���֯��Ա"*/);
					}
					String strInnerCode=((ReportCombineStruMemberVO)set.toArray()[0]).getInnercode();
					buffer.append(" select t1.pk_org from org_rcsmember_v t1  where t1.pk_svid='"+rcsVerPk+"' and t1.innercode like '"+strInnerCode+"%'") ;
					    if (totalScheme.getTotalType()== HbTotalSchemeVO.TOTAL_TYPE_DIRECT)
						{
					    	//ֱ���¼�
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
							//����ĩ��
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
	 * ��һ��ָ�����ݱ�ִ�����ɻ��ܽ���Ĳ���
	 * @param vPubData,��Ҫ���ɵĻ��ܽ���Ķ�ӦMeasurePubDataVO����
	 * @param mainPubData�������MeasurePubDataVO
	 * @param hashMeasureTable����ָ�����ݱ��������Ľ�ָ������Hashtable
	 * @param strDBTable,ָ�����ݱ�
	 * @param strCond��ɸѡ����SQL���
	 * @param strTotalID����������ID�������ֵΪ�գ���ʾΪ¼���������¼�
	 * @param vRepID���μӻ��ܵı���ID����
	 * @throws Exception
	 */
	private void excuteTotalResult(List<MeasurePubDataVO> vPubData, MeasurePubDataVO mainPubData,Map<String,List<Set<IStoreCell>>> hashMeasureTable,String strDBTable,String strCond,List<String> vRepID) throws Exception{

	    if (vPubData==null || vPubData.size()<=0)
	    	return;

	    Vector<String[]> vKeyVal=new Vector<String[]>();
	    List<Set<IStoreCell>> vStoreCell =hashMeasureTable.get(strDBTable);

	    //�õ��ӱ�ؼ���������ؼ���֮��Ķ�Ӧ��ϵ
	    Hashtable<Integer,Integer> hashKeyPos=genKeywordPos(vPubData.get(0),mainPubData);

	    //��ȡһ���ӱ��Ӧ��MeasurePubDataVO
	    MeasurePubDataVO pubData=null;
	    for (int i=0;i<vPubData.size();i++){
	        pubData=vPubData.get(i);

	        //Ϊ��ƴSQL���ķ��㣬��Ҫ���ӱ�MeasurePubDataVO�ж�Ӧ������ؼ��ֵ�ֵ��գ������ر������ǰ��ֵ
	        String[] strKeyVals=reSetPubDataKeyVals(pubData, mainPubData,hashKeyPos);

	        //�����ǰ���ܵ���������Ҫ�жϷ���������Ҫ���ܣ��ӱ���Ҫ�жϣ���Ϊǰ���Ѿ��жϹ���
	        if (i==0 && pubData.getKType().equals(mainPubData.getKType()))
	        {
	        	//xulm ��ʱ���ж��Ƿ���������Ҫ����
//	        	if (dataDMO.loadTotalDataCount(mainPubData, pubData, strCond, strDBTable)<=0){
//	        		//û����Ҫ���ܵ����ݣ���MeasurePubDataVO��Ӧ�Ĺؼ���ֵ��ԭ
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

	    //�洢��ֵָ��
	    Set<IStoreCell> setNumStoreCell=vStoreCell.get(0);
	    //�洢����ֵָ��
	    Set<IStoreCell> setNoNumStoreCell=vStoreCell.get(1);
	    HBTotalRepDataDMO repdataDMO = new HBTotalRepDataDMO();
	    if (setNumStoreCell.size()>0){

		    //ִ�в����������ݵĲ���
		    //�����̬����˽�йؼ��֣����ڻ����¼����������ܣ�����õķ�����һ����
		    if(pubData.getKType().equals(strKeyGroupPK) == false){
		    	//xulm ˽�йؼ����Ѿ��������ˣ����Դ˴��ݲ�����
//		    	setNumMeasure.addAll(setNoNumMeasure);
//		    	setNoNumMeasure.clear();
//		    	dataDMO.createTotalDynDatas(strDBTable,strCond,new ArrayList<MeasureVO>(setNumMeasure),pubData,strKeyGroupPK);
		    }
		    //������������ܻ�ָ�����ڲμӻ��ܵı�������insert���ʵ��
//			else if (strTotalTimeID!=null || vRepID.contains(measure.getReportPK())==true)
//		    else if (strTotalTimeID!=null)
//			{
//		    	repdataDMO.createTotalNoDynDatasByInsert(strDBTable,strCond,new ArrayList<IStoreCell>(setNumStoreCell),mainPubData,vPubData);
//			}
			//�����Ӧ�����¼�������ָ����������ָ�꣬��Ҫ�ж�����insert������update���ʵ��
			else
			{
				repdataDMO.createTotalNoDynDatasWithHZSub(strDBTable,strCond,new ArrayList<IStoreCell>(setNumStoreCell),mainPubData,vPubData);
			}
	    }

	    //TODO:�ַ�����δ����أ������������չ���ַ����ͻ��ܺ���ʾ��ֵ�����⡣ modified by jiaah at 20110618
//	    if (setNoNumStoreCell.size()>0 && (pubData.getKType().equals(mainPubData.getKType())==false || pubData.getKType().equals(strKeyGroupPK) == false)){
	    if (setNoNumStoreCell.size()>0 ){
	    	if (setNumStoreCell.size()>0)
	    	{
	    		repdataDMO.createTotalNoDynDatasWithHZSub(strDBTable,strCond,new ArrayList<IStoreCell>(setNoNumStoreCell),mainPubData,vPubData);
	    	}
	    	else{
			    if(pubData.getKType().equals(strKeyGroupPK) == false)
			    {
			    	//xulm ˽�йؼ��ֵĴ�����ʱȥ��
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

	    //��MeasurePubDataVO��Ӧ�Ĺؼ���ֵ��ԭ
	    for (int i=0;i<vPubData.size();i++){
	    	String[] strKeyVal=vKeyVal.get(i);
	    	pubData=vPubData.get(i);
	    	pubData.setKeywords(strKeyVal,false);
	    }
	}
	
	/**
	 * ��ĳһ��MeasurePubDataVO���������йؼ���ֵ�滻���Ӧ�Ĺؼ���ֵ
	 * @param pubData��Ҫ�滻��MeasurePubDataVO
	 * @param mainPubData,����MeasurePubDataVO
	 * @param hashPos,����ؼ������ӱ�ؼ��ֵĶ��չ�ϵ
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
	 * �õ���̬���ؼ���������ؼ��ֵĶ�Ӧ��ϵ��������������ĳһ�ؼ��֣����ڶ�̬���ؼ�����λ�ڵڼ���λ��
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