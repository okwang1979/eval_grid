package nc.impl.iufo.cacu;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.itf.corg.IReportManaStruQryService;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pubitf.para.SysInitQuery;
import nc.vo.corg.ReportManaStruVO;
import nc.vo.iufo.data.MeasureDataUtil;
import nc.vo.iufo.data.MeasurePubDataUtil;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iufo.task.TaskVO;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.org.AccountingBookVO;
import nc.vo.org.SetOfBookVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.param.TempParamVO;

import com.ufida.dataset.Context;
import com.ufida.dataset.IContext;
import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.iufo.fmtplugin.datastate.CellsModelOperator;
import com.ufsoft.iufo.fmtplugin.measure.MeasureModel;
import com.ufsoft.iuforeport.tableinput.applet.IRepDataParam;
import com.ufsoft.iuforeport.tableinput.applet.RepDataParam;
import com.ufsoft.report.ReportContextKey;
import com.ufsoft.table.CellPosition;
import com.ufsoft.table.CellsModel;

public class BaseService {
	

	private BaseDAO dao = null;
	
	public  BaseDAO getBaseDAO(){
		if(dao == null){
			dao = new BaseDAO();
		}
		return dao;
	}
	
	/**
	 * 
	 * 获取企业报表参数
	 * @author xulink
	 * @Email xulink@yonyou.com
	 * @Date 2020-7-10 上午8:54:42
	 */
	protected IRepDataParam getReportDataParam(TempParamVO param) throws BusinessException{
		TaskVO taskVO = IUFOCacheManager.getSingleton().getTaskCache().getTaskVO(param.getTaskid());
		String pk_keygroup = taskVO.getPk_keygroup();
		KeyGroupVO keygroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(pk_keygroup);
		IRepDataParam repParam = new RepDataParam();
		//取第一个报表获取alone_id
		String pk_report = param.getRepPks()[0];
		repParam.setReportPK(pk_report);
		repParam.setRepOrgPK(param.getPk_org());
		repParam.setTaskPK(param.getTaskid());
		repParam.setRepMngStructPK(param.getCurRmsPK());
		repParam.setVer("0");
		repParam.setOperUserPK(param.getUserid());
		MeasurePubDataVO measurePubDataVO = new MeasurePubDataVO();
		measurePubDataVO.setAccSchemePK(taskVO.getPk_accscheme());
		
		measurePubDataVO.setKeyGroup(keygroupVo);
		measurePubDataVO.setKType(pk_keygroup);
		measurePubDataVO.setKeywords(param.getInputKeys());
		MeasurePubDataUtil.initMeasurePubDataKeyData(new MeasurePubDataVO[] { measurePubDataVO });
		repParam.setAloneID(MeasureDataUtil.getAloneID(measurePubDataVO));
		measurePubDataVO.setAloneID(MeasureDataUtil.getAloneID(measurePubDataVO));
		
		repParam.setPubData(measurePubDataVO);
		return repParam;
		
	}
	
	/**
	 * 获取指标MeasurePubDataVO
	 * @param taskid
	 * @param inputkeys
	 * @return
	 */
	protected MeasurePubDataVO getMeasurePubDataVO(String taskid,String[] inputkeys){
		TaskVO taskVO = IUFOCacheManager.getSingleton().getTaskCache().getTaskVO(taskid);
		String pk_keygroup = taskVO.getPk_keygroup();
		KeyGroupVO keygroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(pk_keygroup);
		
		MeasurePubDataVO measurePubDataVO = new MeasurePubDataVO();
		measurePubDataVO.setAccSchemePK(taskVO.getPk_accscheme());
		measurePubDataVO.setKeyGroup(keygroupVo);
		measurePubDataVO.setKType(pk_keygroup);
		measurePubDataVO.setKeywords(inputkeys);
		MeasurePubDataUtil.initMeasurePubDataKeyData(new MeasurePubDataVO[] { measurePubDataVO });
		measurePubDataVO.setAloneID(MeasureDataUtil.getAloneID(measurePubDataVO));
		return measurePubDataVO;
	}
	
	
	protected TempParamVO getTempParam(String pk_accountingbook,String year,String month,String taskcode,String manastrucode) throws BusinessException{
		TempParamVO param = new TempParamVO();
		//账簿
		AccountingBookVO book = (AccountingBookVO) getBaseDAO().retrieveByPK(AccountingBookVO.class, pk_accountingbook);
		//账簿类型
		SetOfBookVO  setBook = (SetOfBookVO) getBaseDAO().retrieveByPK(SetOfBookVO.class, book.getPk_setofbook());
		//当前组织
		String pk_org = book.getPk_relorg();
		//币种
		String pk_currency = setBook.getPk_standardcurr();
		//期间
		String period = year+"-"+month;
		String groupId = InvocationInfoProxy.getInstance().getGroupId();
		String langCode = InvocationInfoProxy.getInstance().getLangCode();
		String curRmsPK = getCurRmsPKByCode(manastrucode);
		String selTask = getTaskBycode(taskcode);
		String[] inputKeys = new String []{pk_org,pk_currency,period};
		String[] repPks  = getTaskreportByTaskId(selTask).toArray(new String[0]);
		String userId = InvocationInfoProxy.getInstance().getUserId();
		
		param.setCurRmsPK(curRmsPK);
		param.setInputKeys(inputKeys);
		param.setLangCode(langCode);
		param.setPeriod(period);
		param.setPk_currency(pk_currency);
		param.setPk_group(groupId);
		param.setPk_org(pk_org);
		param.setTaskcode(taskcode);
		param.setTaskid(selTask);
		param.setRepPks(repPks);
		param.setUserid(userId);
		return param;
	}
	
	/**
	 * 
	 * 获取组织参数.
	 * @author xulink
	 * @Email xulink@yonyou.com
	 * @Date 2020-7-8 下午7:41:39
	 */
	protected Map<String, String> getParaString(List<String> orgList,String paraCode) {
		Map<String, String> paraValueMap = new HashMap<String,String>();
		try {
			paraValueMap = SysInitQuery.getBatchParaString(orgList.toArray(new String[orgList.size()]), paraCode);
		} catch (BusinessException e) {
			AppDebug.debug(e);
			e.printStackTrace();
		}
		return paraValueMap;
	}
	
	
	/**
	 * 
	 * 根据任务id获取下面所有报表
	 * @author xulink
	 * @Email xulink@yonyou.com
	 * @Date 2020-7-9 下午3:57:01
	 */
	protected List<String> getTaskreportByTaskId(String taskid) throws BusinessException{
		String[] pk_reports = IUFOCacheManager.getSingleton().getTaskCache().getReportIdsByTaskId(taskid);
		return  Arrays.asList(pk_reports);
	}
	
	/**
	 * 
	 * 根据任务编码获取任务主键
	 * @author xulink
	 * @Email xulink@yonyou.com
	 * @Date 2020-7-9 下午3:39:12
	 */
	protected String getTaskBycode(String code) throws BusinessException{
		List<TaskVO> taskvos = (List<TaskVO>)getBaseDAO().retrieveByClause(TaskVO.class, "code = '"+code+"'");
		if(taskvos==null ||taskvos.isEmpty()){
			throw new BusinessException("没有任务编码为‘"+code+"’的任务");
		}
		return taskvos.get(0).getPk_task();
	}
	
	
	/**
	 * 
	 * 根据组织体系code获取组织
	 * @author xulink
	 * @Email xulink@yonyou.com
	 * @Date 2020-7-9 下午4:12:52
	 */
	protected String getCurRmsPKByCode(String code) throws BusinessException {
		IReportManaStruQryService service = NCLocator.getInstance().lookup(IReportManaStruQryService.class);
		String pk_group = InvocationInfoProxy.getInstance().getGroupId();
		ReportManaStruVO[] manaStruVOS = service.queryReportManaStruVOsByGroupIDAndClause(pk_group,"code = '"+code+"'");
		if(manaStruVOS == null || manaStruVOS.length<1){
			throw new BusinessException("报表组织体系编码为‘"+code+"’不存在");
		}
		return manaStruVOS[0].getPk_reportmanastru();
	}
	
	
	/**
	 * @author xulink
	 * 根据报表编码获取
	 * @param reportCode
	 * @return
	 */
	protected Map<String,MeasureVO> getReportMeasureVOPosition(String reportCode) {
		ReportVO reportVO = IUFOCacheManager.getSingleton().getReportCache().getByCode(reportCode);
		IContext reportContext = new Context(); 
		reportContext.setAttribute(ReportContextKey.REPORT_PK, reportVO.getPk_report());
		CellsModel formatModel =CellsModelOperator.getFormatModelByPKWithDataProcess(reportContext, true);
		MeasureModel measure =    (MeasureModel)formatModel.getExtProp(MeasureModel.class.getName());
		Hashtable<CellPosition, MeasureVO> measureMap = measure.getMeasureVOPosByAll();
		Map<String,MeasureVO> cellPosition = new HashMap<String, MeasureVO>();
		for(Entry<CellPosition, MeasureVO> entry : measureMap.entrySet()){
			cellPosition.put(entry.getKey().toString(), entry.getValue());
		}
		return cellPosition;
	}
	
	protected String getLastPriod(String priod) throws Exception{

		Date date = new SimpleDateFormat("yyyy-MM-dd").parse(priod+"-01");
		Calendar calendar = Calendar.getInstance();    
		calendar.setTime(date);  
		calendar.add(Calendar.MONTH,-1);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH)+1;
		String lastPriod = year+"-"+String.format("%02d", month);  
		return lastPriod; 
	}
}
