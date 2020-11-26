package nc.ui.iufo.check.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import nc.bs.framework.common.NCLocator;
import nc.impl.iufo.utils.MultiLangTextUtil;
import nc.itf.iufo.check.ICheckManageQuery;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.TaskCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pub.iufo.data.thread.IufoThreadLocalUtil;
import nc.pub.iufo.exception.UFOSrvException;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.util.iufo.pub.UFOString;
import nc.util.iufo.pub.UfoException;
import nc.util.iufo.sysinit.UfobSysParamQueryUtil;
import nc.utils.iufo.CheckDetailViewUtil;
import nc.utils.iufo.CheckSrvUtil;
import nc.utils.iufo.TaskSrvUtils;
import nc.vo.iufo.check.CheckFormulaVO;
import nc.vo.iufo.check.CheckSchemaVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.param.FourTuple;
import nc.vo.iufo.param.ThreeTuple;
import nc.vo.iufo.task.RepDataTaskParam;
import nc.vo.iufo.task.TaskCheckInfoVO;
import nc.vo.iufo.task.TaskReportVO;
import nc.vo.iufo.task.TaskVO;
import nc.vo.pub.BusinessRuntimeException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.iufo.pub.tools.DateUtil;
import com.ufsoft.iufo.check.ui.CheckResultBO_Client;
import com.ufsoft.iufo.check.vo.CheckConVO;
import com.ufsoft.iufo.check.vo.CheckDetailVO;
import com.ufsoft.iufo.check.vo.CheckNoteVO;
import com.ufsoft.iufo.check.vo.CheckResultVO;
import com.ufsoft.iufo.fmtplugin.formatcore.UfoContextVO;
import com.ufsoft.iufo.fmtplugin.formula.IUfoCheckVO;
import com.ufsoft.iufo.fmtplugin.formula.IufoCheckFmlUtil;
import com.ufsoft.iufo.fmtplugin.service.ReportFormatSrv;
import com.ufsoft.iufo.func.excel.date.UfoExcelDateFuncDriver;
import com.ufsoft.iufo.func.excel.stat.UfoExcelStatFuncDriver;
import com.ufsoft.iufo.func.excel.text.UfoExcelTextFuncDriver;
import com.ufsoft.iuforeport.repdatainput.LoginEnvVO;
import com.ufsoft.iuforeport.repdatainput.TableInputHandlerHelper;
import com.ufsoft.iuforeport.tableinput.applet.IRepDataParam;
import com.ufsoft.script.UfoCmdProxy;
import com.ufsoft.script.extfunc.LoginInfoFuncDriver;
import com.ufsoft.script.extfunc.LookupFuncDriver;
import com.ufsoft.script.extfunc.MeasFuncDriver;
import com.ufsoft.script.extfunc.OtherFuncDriver;
import com.ufsoft.script.extfunc.StatisticFuncDriver;
import com.ufsoft.script.spreadsheet.UfoCalcEnv;
import com.ufsoft.script.util.ICheckResultStatus;
import com.ufsoft.table.CellsModel;
/**
 * check execute handler: for reduce the number of links between client and server
 * @author yp
 *
 */
public class CheckExecInputHandler  implements ICheckResultStatus{

	public CheckResultVO[] executeDefineCheck(Object paramp) throws Exception{
		
		try{
			IufoThreadLocalUtil.openCach();
			Object[] params = (Object[]) paramp;

			CheckConVO con = (CheckConVO)params[0];
			IRepDataParam param = (IRepDataParam) params[1];
			LoginEnvVO loginEnv = (LoginEnvVO) params[2];

			// ��ִ�����������л�ȡ��˲���
			String[] orgIds = con.getOrgIds();// ������˵���֯
			String accSchemaPk = con.getStrAccSchemePK();

			RepDataTaskParam paramt = getRepDataTaskParam(param)[0];

			String strKeyGroupPk = con.getStrKeyGroupPk();
			KeyGroupVO keyGroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(strKeyGroupPk);

			// ��ȡ���ڲ�ѯ��MeasurePubDataVO���Բ�ѯ�ùؼ��ֶ�Ӧ��aloneid
			MeasurePubDataVO pubVO = getQueryPubData(con, strKeyGroupPk, keyGroupVo);

			// ���ݲ�ѯ������ȡ��MeasurePubDataVO
			MeasurePubDataVO newPubVO = null;

			// �洢��˽��
			Vector<CheckResultVO> results = new Vector<CheckResultVO>();

			if(con.isFmlCheck()){// ����ʽ���

				Vector<CheckResultVO> tempResults = new Vector<CheckResultVO>();
				for(String orgId : orgIds){
					// ���ؼ����е���֯�滻Ϊ��ǰ��֯����ͨ���ؼ��ֲ�ѯaloneid TODO �����Ż�
					KeyVO[] keyVOs = keyGroupVo.getKeys();
					for (KeyVO keyVO : keyVOs) {
						if (KeyVO.CODE_TYPE_CORP.equals(keyVO.getCode())) {
							pubVO.setKeywordByPK(keyVO.getPk_keyword(), orgId);
						}
					}
					// ��ȡMeasurePubDataVO
					newPubVO = MeasurePubDataBO_Client.findByKeywords(pubVO);
					if(newPubVO == null){
						continue;
					}
					newPubVO.setAccSchemePK(accSchemaPk);

					// ��װ��˲���
					paramt.setAloneID(newPubVO.getAloneID());
					paramt.setPubData(newPubVO);
					paramt.setRepOrgPK(orgId);

					// ��˷����еĹ�ʽ���
					Map<Object, Vector<Object>> schemaFmls = con.getSchemaFmls();
					if(schemaFmls != null){
						Iterator<Map.Entry<Object, Vector<Object>>> it = schemaFmls.entrySet().iterator();
						while(it.hasNext()){
							// keyΪ��˷���pk, valueΪѡ��ĸ���˷����µ���˹�ʽ
							Map.Entry<Object, Vector<Object>> entry = it.next();
							Object schema = entry.getKey();
							Vector<Object> fmls = entry.getValue();

							String schemaPK = ((CheckSchemaVO)schema).getPk_check_schema();
							String schemaName = ((CheckSchemaVO)schema).getName();
							if(fmls != null && fmls.size() > 0){
								// ����������˹�ʽ
								CheckFormulaVO[] checkFmls = fmls.toArray(new CheckFormulaVO[0]);
								if(checkFmls != null && checkFmls.length > 0){
									// ִ�б����ˣ�ͨ��fmls���˲�����˵Ĺ�ʽ
									CheckResultVO resultVO = executeCheckFmlCheck(paramt, loginEnv, checkFmls);
									if(resultVO == null){
										continue;
									}
									resultVO.setSchemePK(schemaPK);
									resultVO.setSchemaName(schemaName);
									resultVO.setOrgId(orgId);
									tempResults.add(resultVO);
								}
							}
						}
					}


					// �����еĹ�ʽ���
					Map<String, Vector<IUfoCheckVO>> repFmls = con.getRepFormulas();
					if(repFmls!=null){
						Iterator<Map.Entry<String, Vector<IUfoCheckVO>>> repIt = repFmls.entrySet().iterator();
						while(repIt.hasNext()){
							// keyΪ����pk, valueΪѡ��ĸñ����µ���˹�ʽ
							Map.Entry<String, Vector<IUfoCheckVO>> entry = repIt.next();
							String repPk = entry.getKey();
							Vector<IUfoCheckVO> fmls = entry.getValue();
							if(fmls != null && fmls.size() > 0){
								// ���ò����еı���pk
								paramt.setReportPK(repPk);
								Vector<String> fmlIds = new Vector<String>();
								for(IUfoCheckVO object : fmls){
									fmlIds.add(object.getID());
								}
								// ִ�б�����ˣ�ͨ��fmlIds���˲�����˵Ĺ�ʽ
								CheckResultVO result = innerDoCheckInRep(paramt, loginEnv, null, true, fmlIds);
								if(result == null){
									continue;
								}
								result.setOrgId(orgId);
								tempResults.add(result);
							}
						}
					}
				}
				// ������˽��
				CheckResultVO[] returnResult = CheckResultBO_Client.createCheckNote(tempResults.toArray(new CheckResultVO[tempResults.size()]));
				// ���շ��ؽ��������
				if(returnResult != null && returnResult.length > 0){
					results.addAll(Arrays.asList(returnResult));
				}

			} else {// ���������˷������

				ICheckManageQuery checkQry=(ICheckManageQuery)NCLocator.getInstance().lookup(ICheckManageQuery.class.getName());
				String[] schemas = con.getSchemas();

				Map<CheckSchemaVO,CheckFormulaVO[]> schemaFmlMap = checkQry.queryCheckFormulasBySchemaPKs(schemas);
				for(String orgId : orgIds){
					// ���ؼ����е���֯�滻Ϊ��ǰ��֯����ͨ���ؼ��ֲ�ѯaloneid
					KeyVO[] keyVOs = keyGroupVo.getKeys();
					for (KeyVO keyVO : keyVOs) {
						if (KeyVO.CODE_TYPE_CORP.equals(keyVO.getCode())) {
							pubVO.setKeywordByPK(keyVO.getPk_keyword(), orgId);
						}
					}
					// ��ȡMeasurePubDataVO
					newPubVO = MeasurePubDataBO_Client.findByKeywords(pubVO);
					if(newPubVO == null){
						continue;
					}
					newPubVO.setAccSchemePK(accSchemaPk);

					// ��װ��˲���
					paramt.setAloneID(newPubVO.getAloneID());
					paramt.setPubData(newPubVO);
					paramt.setRepOrgPK(orgId);

					// ��ѡ�񱨱�������
					String[] reports = con.getReports();
					if(reports != null && reports.length > 0){
						for(String repPK : reports){
							paramt.setReportPK(repPK);

					    	CheckResultVO result = innerDoCheckInRep(paramt, loginEnv, null, false, null);
					    	result.setOrgId(orgId);
					    	result.setRepId(repPK);
					    	results.add(result);
						}
					}
					// ��ѡ�����˷����������
					if(schemaFmlMap != null && schemaFmlMap.size()>0){
						 Set<Entry<CheckSchemaVO, CheckFormulaVO[]>>  set = schemaFmlMap.entrySet();
						 for (Entry<CheckSchemaVO, CheckFormulaVO[]> entry : set) {
							 CheckSchemaVO schema = entry.getKey();
							 CheckFormulaVO[] fmls = entry.getValue();
							 CheckResultVO resultVO = executeCheckFmlCheck(paramt, loginEnv, fmls);
								resultVO.setSchemePK(schema.getPk_check_schema());
								resultVO.setSchemaName(schema.getName());
								resultVO.setOrgId(orgId);
								// @edit by wuyongc at 2011-6-6,����11:46:49 �����������Ϣ
								resultVO.setRepCheckPerson(paramt.getOperUserPK());
								resultVO.setTaskCheckPerson(paramt.getOperUserPK());
								results.add(resultVO);
						}
					}
				}
				if(results != null && results.size()>0)
					CheckResultBO_Client.creatCheckResults(results.toArray(new CheckResultVO[results.size()]));
			}
			return results.toArray(new CheckResultVO[results.size()]);
		}catch(Exception ex){
			throw new  BusinessRuntimeException(ex.getMessage(),ex);
			
		}finally{
			IufoThreadLocalUtil.closeCach();
			IufoThreadLocalUtil.clean();
		}


	}
	public CheckResultVO doCheckInRep(Object params) throws Exception {
		Object[] objs = (Object[]) params;
		IRepDataParam param  = (IRepDataParam) objs[0];
		LoginEnvVO loginEnv = (LoginEnvVO) objs[1];
		CellsModel cellsModel = (CellsModel)objs[2];
		boolean bSaveCheckResult = (Boolean) objs[3];
		Vector<String> fmlIds = (Vector<String>) objs[4];

		RepDataTaskParam paramt = getRepDataTaskParam(param)[0];

		return innerDoCheckInRep(paramt, loginEnv, cellsModel, bSaveCheckResult, fmlIds);
	}
	
	public CheckResultVO[] doCheckInReps(Object params) throws Exception {
		Object[] objs = (Object[]) params;
		IRepDataParam[] paramArr  = (IRepDataParam[]) objs[0];
		LoginEnvVO loginEnv = (LoginEnvVO) objs[1];
		boolean bSaveCheckResult = (Boolean) objs[2];
		Vector<String> fmlIds = (Vector<String>) objs[3];

		RepDataTaskParam[] paramts = getRepDataTaskParam(paramArr);
		List<CheckResultVO> list = new ArrayList<CheckResultVO>();
		for (RepDataTaskParam param : paramts) {
			CheckResultVO checkResult = innerDoCheckInRep(param, loginEnv, null, bSaveCheckResult, fmlIds);
			if(checkResult!=null){
				list.add(checkResult);
			}
		}
		return list.toArray(new CheckResultVO[0]);
	}
	
	public  CheckResultVO[] checkSchemaForTaskCheck(Object params) throws UFOSrvException{
		Object[] objs = (Object[]) params;
		IRepDataParam param  = (IRepDataParam) objs[0];
		LoginEnvVO loginEnv = (LoginEnvVO) objs[1];
		boolean bLocation = (Boolean) objs[2];
		RepDataTaskParam paramt = getRepDataTaskParam(param)[0];
		return innerCheckSchemaForTaskCheck(paramt, loginEnv.getCurLoginDate(), bLocation);
	}
	/**
	 * �������
	 *
	 * @create by liuchuna at 2010-6-30,����02:50:28
	 *
	 * @param param
	 * @param loginEnv
	 * @return
	 * @throws Exception
	 */

	public CheckResultVO[] doCheckInTask(ThreeTuple<IRepDataParam,LoginEnvVO,Boolean> params) throws Exception {
		IRepDataParam param  = params.first;
		LoginEnvVO loginEnv = params.second;
		boolean bSaveResult = params.third;

		RepDataTaskParam paramt = getRepDataTaskParam(param)[0];

		return innerDoCheckInTask(paramt, loginEnv, bSaveResult);

	}

	public CheckResultVO[] doCheckInTaskMultParam(FourTuple<RepDataTaskParam[],TaskCheckInfoVO,LoginEnvVO,Boolean> params) throws Exception {
		RepDataTaskParam[] param  = params.first;
		TaskCheckInfoVO taskCheckInfo = params.second;
		LoginEnvVO loginEnv = params.third;
		boolean bSaveResult = params.four;

		return innerDoCheckInTaskMultParam(param,taskCheckInfo, loginEnv, bSaveResult);

	}



	public CheckResultVO[] checkRepForTaskCheck(Object params) throws Exception{
		Object[] objs = (Object[]) params;
		IRepDataParam param  = (IRepDataParam) objs[0];
		LoginEnvVO loginEnv = (LoginEnvVO) objs[1];

		RepDataTaskParam paramt = getRepDataTaskParam(param)[0];
		return innerCheckRepForTaskCheck(paramt, loginEnv);
	}

	/**
	 * @create by wuyongc at 2012-4-20,����4:45:18
	 *
	 * @param param
	 * @return
	 */
	private RepDataTaskParam[] getRepDataTaskParam(IRepDataParam... param){
		RepDataTaskParam[] paramt = new RepDataTaskParam[param.length];
		TaskVO taskVO = null;
		Map<String,TaskVO> taskMap = new HashMap<String,TaskVO>();
		TaskCache taskCache = IUFOCacheManager.getSingleton().getTaskCache();
		for(int i=0; i<param.length; i++){
			if(param[i] instanceof RepDataTaskParam){
				paramt[i] = (RepDataTaskParam) param[i];
			}else{
				paramt[i]= new RepDataTaskParam();
				BeanUtils.copyProperties(param[i], paramt[i]);
				if(taskMap.containsKey(param[i].getTaskPK())) {
					paramt[i].setTask(taskMap.get(param[i].getTaskPK()));
					
				}else{
					taskVO = taskCache.getTaskVO(param[i].getTaskPK());
					paramt[i].setTask(taskVO);
					taskMap.put(taskVO.getPk_task(), taskVO);
				}
			}
		}

		return paramt;
	}

	public CheckResultVO[] executeFmlReCheck(Object params) throws Exception {
		Object[] objs = (Object[]) params;

		Map<CheckResultVO, Vector<String>> map = (Map<CheckResultVO, Vector<String>>) objs[0];
		CheckConVO con = (CheckConVO) objs[1];
		IRepDataParam param = (IRepDataParam) objs[2];
		LoginEnvVO loginEnv = (LoginEnvVO) objs[3];

		RepDataTaskParam paramt = getRepDataTaskParam(param)[0];

		return innerExecuteFmlReCheck(map, con, paramt, loginEnv);
	}

	 private CheckResultVO[] innerExecuteFmlReCheck(
				Map<CheckResultVO, Vector<String>> map, CheckConVO con,
				IRepDataParam param, LoginEnvVO loginEnv) throws Exception {

			// ��ִ�����������л�ȡ��˲���
			String accSchemaPk = con.getStrAccSchemePK();
			String strKeyGroupPk = con.getStrKeyGroupPk();
			KeyGroupVO keyGroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(strKeyGroupPk);

			// ��ȡ���ڲ�ѯ��MeasurePubDataVO���Բ�ѯ�ùؼ��ֶ�Ӧ��aloneid
			MeasurePubDataVO pubVO = getQueryPubData(con, strKeyGroupPk, keyGroupVo);

			// ���ݲ�ѯ������ȡ��MeasurePubDataVO
			MeasurePubDataVO newPubVO = null;

			// �洢��˽��
			Vector<CheckResultVO> results = new Vector<CheckResultVO>();

			Iterator<Map.Entry<CheckResultVO, Vector<String>>> it = map.entrySet().iterator();
			Vector<CheckResultVO> tempResults = new Vector<CheckResultVO>();
			while(it.hasNext()){
				Map.Entry<CheckResultVO, Vector<String>> entry = it.next();
				CheckResultVO resultVO = entry.getKey();
				Vector<String> fmlIds = entry.getValue();

				String orgId = resultVO.getOrgId();
				String repId = resultVO.getRepId();
				String schemaId = resultVO.getSchemePK();

				// ���ؼ����е���֯�滻Ϊ��ǰ��֯����ͨ���ؼ��ֲ�ѯaloneid TODO �����Ż�
				KeyVO[] keyVOs = keyGroupVo.getKeys();
				for (KeyVO keyVO : keyVOs) {
					if (KeyVO.CODE_TYPE_CORP.equals(keyVO.getCode())) {
						pubVO.setKeywordByPK(keyVO.getPk_keyword(), orgId);
					}
				}
				// ��ȡMeasurePubDataVO
				newPubVO = MeasurePubDataBO_Client.findByKeywords(pubVO);
				if(newPubVO == null){
					continue;
				}
				newPubVO.setAccSchemePK(accSchemaPk);

				// ��װ��˲���
				param.setAloneID(newPubVO.getAloneID());
				param.setPubData(newPubVO);
				param.setRepOrgPK(orgId);

				if(!UFOString.isEmpty(repId)){
					// ���ò����еı���pk
					param.setReportPK(repId);
					// ִ�б�����ˣ�ͨ��fmlIds���˲�����˵Ĺ�ʽ
					CheckResultVO result = innerDoCheckInRep(param, loginEnv, null, false, fmlIds);
					result.setOrgId(orgId);
					tempResults.add(result);
				} else {
					// ��˷������
					CheckFormulaVO[] checkFmls = CheckSrvUtil.getCheckFormulaByPK(fmlIds.toArray(new String[0]));
					// ִ�б����ˣ�ͨ��fmls���˲�����˵Ĺ�ʽ
					CheckResultVO result = executeCheckFmlCheck(param, loginEnv, checkFmls);
					result.setSchemePK(schemaId);
					result.setSchemaName(resultVO.getSchemaName());
					result.setOrgId(orgId);
					tempResults.add(result);
				}
			}

			// ������˽��
			CheckResultVO[] returnResult = CheckResultBO_Client.createCheckNote(tempResults.toArray(new CheckResultVO[tempResults.size()]));
			// ���շ��ؽ��������
			if(returnResult != null && returnResult.length > 0){
				results.addAll(Arrays.asList(returnResult));
			}

			return results.toArray(new CheckResultVO[results.size()]);
		}

	/**
	 * ��ĳһ�����е����б���������
	 *
	 * @create by liuchuna at 2010-7-9,����04:05:16
	 *
	 * @param param
	 * @param loginEnv
	 * @return
	 * @throws Exception
	 */
	private CheckResultVO[] innerCheckRepForTaskCheck(IRepDataParam param,LoginEnvVO loginEnv) throws Exception{
		// ���������ȡ�����������б���
		String taskId = param.getTaskPK();
	    TaskReportVO[] taskReport= TaskSrvUtils.getTaskReportByTaskId(taskId);

	    Vector<CheckResultVO> results = new Vector<CheckResultVO>();

	    String curUserId =  param.getOperUserPK();
	    // ѭ��������б���
	    for(TaskReportVO vo : taskReport){
	    	String reportPK = vo.getPk_report();

	    	IRepDataParam newParam = (IRepDataParam)param.clone();
	    	newParam.setReportPK(reportPK);

	    	CheckResultVO result = innerDoCheckInRep(newParam, loginEnv, null, false, null);

	    	result.setRepCheckPerson(curUserId);
//			result.setTaskCheckPerson(curUserId);
	    	result.setOrgId(param.getRepOrgPK());
	    	result.setRepId(reportPK);

	    	results.add(result);

	    }

	    return results.toArray(new CheckResultVO[results.size()]);

	}

	private CheckResultVO[] innerDoCheckInTaskMultParam (RepDataTaskParam[] param,TaskCheckInfoVO taskCheckInfo,LoginEnvVO loginEnv, boolean bSaveResult) throws Exception {
		// ���������õ�������˷����������
		CheckResultVO[] schemaResults = MultiParamCheckSchemaForTaskCheck(param,taskCheckInfo, loginEnv.getCurLoginDate(),true);

        // �����������б�����б������
        CheckResultVO[] repResults = multiParamCheckRepForTaskCheck(param, loginEnv);

        // ��װ������˽��
        Vector<CheckResultVO> results = new Vector<CheckResultVO>();
        if(schemaResults != null && schemaResults.length > 0){
        	results.addAll(Arrays.asList(schemaResults));
        }
        if(repResults != null && repResults.length > 0){
        	results.addAll(Arrays.asList(repResults));
        }

        CheckResultVO[] resultArray = results.toArray(new CheckResultVO[results.size()]);

        if(bSaveResult){
        	// ������˽��
        	if(resultArray != null && resultArray.length>0){
        		// @edit by wuyongc at 2011-6-6,����11:36:51  ���� �������Ϣ
        		String curUserId =  param[0].getOperUserPK();
        		for(CheckResultVO checkResult : resultArray){
        			checkResult.setRepCheckPerson(curUserId);
        			checkResult.setTaskCheckPerson(curUserId);
        		}
        		CheckResultBO_Client.creatCheckResults(resultArray);
        	}

        }

        return resultArray;
	}

	private CheckResultVO[] multiParamCheckRepForTaskCheck(IRepDataParam[] params,LoginEnvVO loginEnv) throws Exception{
		// ���������ȡ�����������б���
		String taskId = params[0].getTaskPK();
	    TaskReportVO[] taskReport= TaskSrvUtils.getTaskReportByTaskId(taskId);

	    Vector<CheckResultVO> results = new Vector<CheckResultVO>();

	    String curUserId =  params[0].getOperUserPK();
	    // ѭ��������б���
	    for(TaskReportVO vo : taskReport){
	    	String reportPK = vo.getPk_report();
	    	for (IRepDataParam param : params) {
	    		IRepDataParam newParam = (IRepDataParam)param.clone();
	    		newParam.setReportPK(reportPK);
	    		CheckResultVO result = innerDoCheckInRep(newParam, loginEnv, null, false, null);
		    	result.setRepCheckPerson(curUserId);
//				result.setTaskCheckPerson(curUserId);
		    	result.setOrgId(newParam.getRepOrgPK());
		    	result.setRepId(reportPK);
		    	results.add(result);
			}
	    }

	    return results.toArray(new CheckResultVO[results.size()]);

	}

	private CheckResultVO[] innerDoCheckInTask(IRepDataParam param,LoginEnvVO loginEnv, boolean bSaveResult) throws Exception {

		// ���������õ�������˷����������
		CheckResultVO[] schemaResults = innerCheckSchemaForTaskCheck(param, loginEnv.getCurLoginDate(),true);

        // �����������б�����б������
        CheckResultVO[] repResults = innerCheckRepForTaskCheck(param, loginEnv);

        // ��װ������˽��
        Vector<CheckResultVO> results = new Vector<CheckResultVO>();
        if(schemaResults != null && schemaResults.length > 0){
        	results.addAll(Arrays.asList(schemaResults));
        }
        if(repResults != null && repResults.length > 0){
        	results.addAll(Arrays.asList(repResults));
        }

        CheckResultVO[] resultArray = results.toArray(new CheckResultVO[results.size()]);

        if(bSaveResult){
        	// ������˽��
        	if(resultArray != null && resultArray.length>0){
        		// @edit by wuyongc at 2011-6-6,����11:36:51  ���� �������Ϣ
        		String curUserId =  param.getOperUserPK();
        		for(CheckResultVO checkResult : resultArray){
        			checkResult.setRepCheckPerson(curUserId);
        			checkResult.setTaskCheckPerson(curUserId);
        		}
        		CheckResultBO_Client.creatCheckResults(resultArray);
        	}

        }

        return resultArray;
	}

	private CheckResultVO[] MultiParamCheckSchemaForTaskCheck(RepDataTaskParam[] param, TaskCheckInfoVO taskCheckInfo,String strLoginDate, boolean bLocation)throws UFOSrvException {

		try {
			String strUserID = param[0].getOperUserPK();
			List<CheckResultVO> vResult=new ArrayList<CheckResultVO>();
			Map<CheckSchemaVO,CheckFormulaVO[]> map =  taskCheckInfo.getSchemaFmlMap();
			if(map != null){
				Set<Map.Entry<CheckSchemaVO,CheckFormulaVO[]>> set = map.entrySet();
				for (Map.Entry<CheckSchemaVO,CheckFormulaVO[]> entry : set) {
					CheckFormulaVO[] fmls = entry.getValue();
					int fmlLen = fmls.length;
					String[] formulas = new String[fmlLen];
					String[] strNames = new String[fmlLen];
					String[] strFormulaIDs = new String[fmlLen];
					Boolean[] isMatchFmlCheck = new Boolean[fmlLen];
					 List<String>[] parsedMatchFmls = new ArrayList[fmlLen];
					 for (int i = 0; i < fmlLen; i++) {
						 formulas[i] = fmls[i].getFormula();
						 strNames[i] = MultiLangTextUtil.getCurLangText(fmls[i]);
						 strFormulaIDs[i] = fmls[i].getPk_check_formula();
						 isMatchFmlCheck[i] = Boolean.valueOf(false);
						 parsedMatchFmls[i] = null;
					}
						for (RepDataTaskParam p : param) {
							CheckResultVO resultVO = execCheck(p.getPubData(),p.getTask(), formulas, strNames,
									strFormulaIDs, p.getAloneID(), bLocation, strUserID,
									strLoginDate, isMatchFmlCheck, parsedMatchFmls);
							// û��¼������ݵģ�������pubvo�����ʱֱ�ӷ���Null
							if(resultVO == null)
								continue;
							resultVO.setSchemePK(entry.getKey().getPk_check_schema());
							resultVO.setSchemaName(entry.getKey().getName());
							resultVO.setOrgId(p.getRepOrgPK());
							// @edit by wuyongc at 2011-6-6,����11:42:53 ���� ���������
							resultVO.setTaskCheckPerson(strUserID);
							vResult.add(resultVO);
						}
				}
			}

			return vResult.toArray(new CheckResultVO[0]);
		} catch (UFOSrvException re) {
			AppDebug.debug(re);
			throw re;
		} catch (Exception ex) {
			AppDebug.debug(ex);
			throw new UFOSrvException("CheckBO->runTaskCheck:", ex);
		}

	}
	/**
	 * ��ĳһ�����е�������˷����������
	 *
	 * @create by liuchuna at 2010-7-9,����04:06:04
	 *
	 * @param strTaskId
	 * @param aloneIds
	 * @param bLocation
	 * @param strUserID
	 * @param strLoginDate
	 * @return
	 * @throws UFOSrvException
	 */
	@SuppressWarnings("unchecked")
	private CheckResultVO[] innerCheckSchemaForTaskCheck(IRepDataParam param, String strLoginDate, boolean bLocation)
			throws UFOSrvException {
		try {
			// ��ȡ����
			String strTaskId = param.getTaskPK();
			String[] aloneIds = new String[] { param.getAloneID() };
			String strUserID = param.getOperUserPK();

			// ��ȡ�������õ�����
			CheckSchemaVO[] checkSchemes=TaskSrvUtils.getTaskCheckSchemeVOsByTaskId(strTaskId);
			if (checkSchemes==null || checkSchemes.length<=0)
				return new CheckResultVO[0];

			ICheckManageQuery checkQry=(ICheckManageQuery)NCLocator.getInstance().lookup(ICheckManageQuery.class.getName());

			List<CheckResultVO> vResult=new ArrayList<CheckResultVO>();
			for (CheckSchemaVO checkScheme : checkSchemes) {
				CheckFormulaVO[] checkForms=checkQry.queryCheckFormulas(checkScheme.getPk_check_schema());
				String[] formulas = new String[checkForms.length];
				String[] strNames = new String[checkForms.length];
				String[] strFormulaIDs = new String[checkForms.length];

				Boolean[] isMatchFmlCheck = new Boolean[checkForms.length];
		        List<String>[] parsedMatchFmls = new ArrayList[checkForms.length];
				for (int j = 0; j< checkForms.length; j++){
					formulas[j] = checkForms[j].getFormula();
					strNames[j] = checkForms[j].getName();
					strFormulaIDs[j] = checkForms[j].getPk_check_formula();

					isMatchFmlCheck[j] = Boolean.valueOf(false);
					parsedMatchFmls[j] = null;
				}

				for (String aloneId : aloneIds) {
					CheckResultVO resultVO = execCheck(param.getPubData(),((RepDataTaskParam)param).getTask(), formulas, strNames,
							strFormulaIDs, aloneId, bLocation, strUserID,
							strLoginDate, isMatchFmlCheck, parsedMatchFmls);
					// û��¼������ݵģ�������pubvo�����ʱֱ�ӷ���Null
					if(resultVO == null)
						continue;
					resultVO.setSchemePK(checkScheme.getPk_check_schema());
					resultVO.setSchemaName(checkScheme.getName());
					resultVO.setOrgId(param.getRepOrgPK());
					// @edit by wuyongc at 2011-6-6,����11:42:53 ���� ���������
					resultVO.setTaskCheckPerson(param.getOperUserPK());
					vResult.add(resultVO);
				}
			}

			return vResult.toArray(new CheckResultVO[0]);
		} catch (UFOSrvException re) {
			AppDebug.debug(re);
			throw re;
		} catch (Exception ex) {
			AppDebug.debug(ex);
			throw new UFOSrvException("CheckBO->runTaskCheck:", ex);
		}
	}


	private CheckResultVO innerDoCheckInRep(IRepDataParam param,
			LoginEnvVO loginEnv, CellsModel cellsModel, boolean bSaveCheckResult, Vector<String> fmlIds)
			throws Exception {
		try{
			// ��ȡ���������Ļ�����Ϣ
			UfoContextVO context = TableInputHandlerHelper.getContextVO(param,loginEnv);

			// ���ر����ʽ�����࣬��ʼ������ģ��
			ReportFormatSrv reportFormatSrv = null;
			if (cellsModel != null) {
				reportFormatSrv = new ReportFormatSrv(context, cellsModel);
			} else {
				reportFormatSrv = new ReportFormatSrv(context, true);
			}



//			TaskVO task = IUFOCacheManager.getSingleton().getTaskCache().getTaskVO(param.getTaskPK());
			TaskVO task = ((RepDataTaskParam)param).getTask();
			int checkWarn = task.getIswarnchecked();

			//  ���ȶ�ȡ������� Ȼ���ٶ�ȡϵͳ����      ����Ƿ�ͨ��
			boolean warnPass = getWarnPass(checkWarn);

			// ͨ���������࣬��ȡ��ʽִ������ִ�е������
			CheckResultVO repCheckVO = reportFormatSrv.getFormulaHandler().execRepCheck(context, true, fmlIds,warnPass);
			//TODO  û����˽������������� δ¼�룿
			String curUserId =  param.getOperUserPK();
			if(repCheckVO != null){
				repCheckVO.setRepCheckPerson(curUserId);
				repCheckVO.setCheckTime(DateUtil.getCurTime());
			}

			// ������˽��
			if (bSaveCheckResult) {
				if(repCheckVO != null){
					CheckResultBO_Client.creatCheckResult(repCheckVO);

				}
			}

			// ������˽�������ڽ���չʾ
			return repCheckVO;
		} catch (Exception e){
			AppDebug.debug(e);
			throw e;
		}
	}
	/**
	 * ִ����˹�ʽ�����
	 *
	 * @create by liuchuna at 2010-7-12,����09:59:42
	 *
	 * @param param
	 * @param loginEnv
	 * @param taskPk
	 * @param checkForms
	 * @return
	 * @throws UFOSrvException
	 */
	@SuppressWarnings("unchecked")
	private  CheckResultVO executeCheckFmlCheck(IRepDataParam param,
			LoginEnvVO loginEnv, CheckFormulaVO[] checkForms)
			throws UFOSrvException {
		// ����˹�ʽ���ݡ���˹�ʽ���ơ���˹�ʽid��װ����
		String[] formulas = new String[checkForms.length];
		String[] strNames = new String[checkForms.length];
		String[] strFormulaIDs = new String[checkForms.length];

		Boolean[] isMatchFmlCheck = new Boolean[checkForms.length];
        List<String>[] parsedMatchFmls = new ArrayList[checkForms.length];
		for (int j = 0; j< checkForms.length; j++){
			formulas[j] = checkForms[j].getFormula();
			strNames[j] = checkForms[j].getName();
			strFormulaIDs[j] = checkForms[j].getPk_check_formula();

			isMatchFmlCheck[j] = Boolean.valueOf(false);
			parsedMatchFmls[j] = null;
		}
		// ����װ�õ�������Ϊ���������÷���ִ�����
		CheckResultVO resultVO = execCheck(param.getPubData(),((RepDataTaskParam)param).getTask(), formulas, strNames,
				strFormulaIDs, param.getAloneID(), true, param.getOperUserPK(),loginEnv.getCurLoginDate(), isMatchFmlCheck, parsedMatchFmls);
		return resultVO;
	}
	/**
	 *������˹�ʽ���ݡ�aloneIdִ�����
	 */
	private CheckResultVO execCheck(MeasurePubDataVO pubVO,TaskVO taskVO, String[] formulas,String[] strNames,String[] strFormulaIDs, String aloneId,boolean bLocation,
			String strUserID,String strLoginDate, Boolean[] isMatchFmlCheck, List<String>[] parsedMatchFmls) throws UFOSrvException {
	    try {

//	        MeasurePubDataVO pubVO = MeasurePubDataBO_Client.findByAloneID(taskVO.getPk_keygroup(),aloneId);
//	        if(pubVO == null){
//	        	return null;
//	        }
	        pubVO.setAccSchemePK(taskVO.getPk_accscheme());

	    	UfoCalcEnv env = new UfoCalcEnv(taskVO.getPk_task(),pubVO,false,null);
	        env.setLoginDate(strLoginDate);
	        //TODO:���ǵ�¼��Ϣ�������⣬��userid��ȡ�õ�ǰ��¼��λ
//	        env.setLoginUnitId(strLoginUnitID);
	    	env.loadFuncListInst().registerExtFuncs(new MeasFuncDriver(env));
	    	env.loadFuncListInst().registerExtFuncs(new OtherFuncDriver());
	    	env.loadFuncListInst().registerExtFuncs(new LoginInfoFuncDriver());
	    	env.loadFuncListInst().registerExtFuncs(new UfoExcelTextFuncDriver());
	    	env.loadFuncListInst().registerExtFuncs(new UfoExcelDateFuncDriver());
	    	env.loadFuncListInst().registerExtFuncs(new UfoExcelStatFuncDriver());
	    	env.loadFuncListInst().registerExtFuncs(new StatisticFuncDriver());
	    	env.loadFuncListInst().registerExtFuncs(new LookupFuncDriver());
	    	
	        IufoCheckFmlUtil checkUtil=new IufoCheckFmlUtil(new UfoCmdProxy(env),bLocation);



			int checkWarn = taskVO.getIswarnchecked();

			boolean warnPass = getWarnPass(checkWarn);

	        checkUtil.setWarnPass(warnPass);

	        int[] iCheckStates=new int[formulas.length];
	        String[] strNotes=new String[formulas.length];

	        boolean bPass=checkUtil.execCheck(formulas,strNames,strFormulaIDs,strNotes,iCheckStates, isMatchFmlCheck, parsedMatchFmls);

	        CheckResultVO resultVO = new CheckResultVO();
	        resultVO.setAloneId(aloneId);
	        resultVO.setCheckTime(DateUtil.getCurTime());

	        List<CheckNoteVO> vCheckNote=new ArrayList<CheckNoteVO>();
	        for (int i=0;i<iCheckStates.length;i++){
	        	CheckNoteVO checkNote=new CheckNoteVO();
	        	checkNote.setCheckState(iCheckStates[i]);
	        	checkNote.setNote(strNotes[i]);
	        	checkNote.setFormulaID(strFormulaIDs[i]);
	        	checkNote.setFormulaName(strNames[i]);
	        	vCheckNote.add(checkNote);
	        }
	        resultVO.setNote(vCheckNote);

	        resultVO.setDetailVO(checkUtil.getDetailVO());
	        if(!bPass){
	        	resultVO.setCheckState(NOPASS);
	        }else
	        	resultVO.setCheckState(PASS);


	        return resultVO;
	    } catch (Exception ex) {
	    	AppDebug.debug(ex);//@devTools         ex.printStackTrace(System.out);
	        String errorMsg = ex.getMessage();
	        throw new UFOSrvException("CheckBO:execCheck Exception",new UfoException(errorMsg));
	    }
	}
	/**
	 * @create by wuyongc at 2011-6-18,����04:51:10
	 *
	 * @param warnPass
	 * @param checkWarn
	 * @return
	 * @throws Exception
	 */
	private static boolean getWarnPass(int checkWarn)
			throws Exception {
		boolean warnPass = true;
		switch(checkWarn){
			case TaskVO.WARN_NOT_CONTROL : warnPass = UfobSysParamQueryUtil.getCheckPassOnAlarm(); break;
			case TaskVO.WARN_NOT_PASS : warnPass = false;break;
			case TaskVO.WARN_PASS : warnPass = true;break;
		}
		return warnPass;
	}

	/**
	 * ��װMeasurePubDataVO�����ڲ�ѯaloneid
	 *
	 * @create by liuchuna at 2010-7-12,����09:52:23
	 *
	 * @param con
	 * @param strKeyGroupPk
	 * @param keyGroupVo
	 * @return
	 */
	private MeasurePubDataVO getQueryPubData(CheckConVO con,
			String strKeyGroupPk, KeyGroupVO keyGroupVo) {

		String strAccSchemePK = con.getStrAccSchemePK();
		String[] keyValues = con.getKeyValues();

		MeasurePubDataVO pubVO = new MeasurePubDataVO();
		pubVO.setKType(strKeyGroupPk);
		pubVO.setVer(0);
		pubVO.setKeyGroup(keyGroupVo);
		pubVO.setAccSchemePK(strAccSchemePK);
		pubVO.setKeywords(keyValues);

		return pubVO;
	}

	@SuppressWarnings("unchecked")
	public Map<String,CheckDetailVO[]> formatCheckDetail(CheckConVO con, CheckResultVO[] results,IRepDataParam param,LoginEnvVO loginEnv,Map<String,CellsModel> cellsModelsMap) throws Exception{
		try{
//			CheckSchemaVO[] checkSchemes = null;
			Set<String> setRepIds = new HashSet<String>();
			Map<String,CheckDetailVO[]> detailMap = new HashMap<String,CheckDetailVO[]>();
			List<CheckResultVO> schemeList = new ArrayList<CheckResultVO>();
			List<CheckResultVO> repList = new ArrayList<CheckResultVO>();

			Set<String> schemePKs = new HashSet<String>();
			for(CheckResultVO resultVo : results){
				if(StringUtils.isEmpty(resultVo.getRepId())){
					schemeList.add(resultVo);
					schemePKs.add(resultVo.getSchemePK());
				}else{
					setRepIds.add(resultVo.getRepId());
					repList.add(resultVo);
				}
			}

			CellsModel cellsModel = null;
				for(CheckResultVO vo : repList){

					// ��װ���������Ϣ
					param.setAloneID(vo.getAloneId());
					param.setPubData(vo.getNewPubVO());

					// ���ñ���pk
					param.setReportPK(vo.getRepId());

					// ��ȡ���������Ļ�����Ϣ
					UfoContextVO context = TableInputHandlerHelper.getContextVO(param,loginEnv);

					if(cellsModelsMap != null){
						cellsModel = cellsModelsMap.get(vo.getRepId());
					}
					if(cellsModel == null){
						// ���ر����ʽ�����࣬��ʼ������ģ��
						ReportFormatSrv reportFormatSrv = new ReportFormatSrv(context, true);
						cellsModel = reportFormatSrv.getCellsModel();
					}

						CheckDetailViewUtil executor = new CheckDetailViewUtil(cellsModel,context);


					// ͨ���������࣬��ȡ��ʽִ��������װ�������
					CheckDetailVO[] details = executor.generateCheckDetail(vo);

					// ��װ��˽����ϸ
					detailMap.put(vo.getResultPK(), details);
					cellsModel = null;

				}
//			}

			if(schemeList.size()>0){
				ICheckManageQuery checkQry=(ICheckManageQuery)NCLocator.getInstance().lookup(ICheckManageQuery.class.getName());
				Map<String,List<CheckFormulaVO>> map = checkQry.queryCheckFormulasMap(schemePKs.toArray(new String[0]));

				for(CheckResultVO vo : schemeList){
					UfoCalcEnv env = new UfoCalcEnv(con.getTaskId(), vo.getNewPubVO(), false, null);
					env.setLoginDate(loginEnv.getCurLoginDate());
					env.loadFuncListInst().registerExtFuncs(new MeasFuncDriver(env));
			    	env.loadFuncListInst().registerExtFuncs(new OtherFuncDriver());
			    	env.loadFuncListInst().registerExtFuncs(new LoginInfoFuncDriver());
			    	env.loadFuncListInst().registerExtFuncs(new UfoExcelTextFuncDriver());
			    	env.loadFuncListInst().registerExtFuncs(new UfoExcelDateFuncDriver());
			    	env.loadFuncListInst().registerExtFuncs(new UfoExcelStatFuncDriver());
			    	env.loadFuncListInst().registerExtFuncs(new StatisticFuncDriver());
			    	env.loadFuncListInst().registerExtFuncs(new LookupFuncDriver());
					
					// ��ʼ����˹�����
					IufoCheckFmlUtil checkUtil = new IufoCheckFmlUtil(new UfoCmdProxy(env),true);

					// ��װ��˹�ʽ
					CheckFormulaVO[] checkForms = map.get(vo.getSchemePK()).toArray(new CheckFormulaVO[0]);
					String[] formulas = new String[checkForms.length];
					String[] strNames = new String[checkForms.length];
					String[] strFormulaIDs = new String[checkForms.length];
					Boolean[] isMatchFmlCheck = new Boolean[checkForms.length];
                    List<String>[] parsedMatchFmls = new ArrayList[checkForms.length];
					for (int j = 0; j< checkForms.length; j++){
						formulas[j] = checkForms[j].getFormula();
						strNames[j] = checkForms[j].getName();
						strFormulaIDs[j] = checkForms[j].getPk_check_formula();

						isMatchFmlCheck[j] = Boolean.valueOf(false);
						parsedMatchFmls[j] = null;
					}
					// ���������ϸ��Ϣ
					CheckDetailVO[] details = checkUtil.generateCheckDetail(formulas, strNames, strFormulaIDs, vo,
							isMatchFmlCheck, parsedMatchFmls);
					// ��װ��˽����ϸ
					detailMap.put(vo.getResultPK(), details);
				}
			}
			return detailMap;

		} finally {
		}
	}

/*	public Map<String,CheckDetailVO[]> formatCheckDetail(CheckConVO con, CheckResultVO[] results,
			IRepDataParam param, LoginEnvVO loginEnv) throws Exception {
		return formatCheckDetail(con,results,param,loginEnv,null);
	}*/
}
