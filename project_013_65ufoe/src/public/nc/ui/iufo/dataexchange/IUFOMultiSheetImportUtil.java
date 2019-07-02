package nc.ui.iufo.dataexchange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.iufo.table.exarea.ExtendAreaCell;
import com.ufsoft.iufo.fmtplugin.datastate.CellsModelOperator;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iufo.fmtplugin.formatcore.UfoContextVO;
import com.ufsoft.iufo.fmtplugin.service.ReportFormatSrv;
import com.ufsoft.iufo.report.propertyoperate.PropertyOperate;
import com.ufsoft.report.ReportContextKey;
import com.ufsoft.table.AreaPosition;
import com.ufsoft.table.CellsModel;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.itf.iufo.commit.ICommitManageService;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.ReportCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pub.iufo.exception.CommonException;

import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.ui.iufo.data.RepDataBO_Client;
import nc.ui.iufo.input.InputUtil;
import nc.ui.iufo.input.ufoe.IUfoInputActionUtil;

import nc.util.iufo.pub.AuditUtil;
import nc.util.iufo.repdataright.RepDataAuthUtil;
import nc.util.iufo.sysinit.UfobSysParamQueryUtil;
import nc.vo.iufo.data.MeasureDataVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.RepDataVO;
import nc.vo.iufo.datasource.DataSourceVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.param.TwoTuple;
import nc.vo.iufo.pub.date.UFODate;
import nc.vo.iufo.task.TaskVO;
import nc.vo.iufo.task.TaskVO.DataRightControlType;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.pub.BusinessException;

public class IUFOMultiSheetImportUtil extends MultiSheetImportUtil {
	private final TaskVO task;
    public IUFOMultiSheetImportUtil(String strTaskPK,String strRepPK ,CellsModel cellsModel,String sheetname,int[] dynendrows,MeasurePubDataVO mainpubVo,String strUserPK,DataSourceVO dataSource,String strOrgPK,String strRmsPK,String strGroupPK,boolean bAutoCalc,String strLoginDate) {
        super(strRepPK,cellsModel,sheetname,dynendrows,mainpubVo,strUserPK,dataSource,strOrgPK,strRmsPK,strGroupPK,bAutoCalc,strLoginDate);
    	task = IUFOCacheManager.getSingleton().getTaskCache().getTaskVO(strTaskPK);
        m_bFormCanImport=getFormCanImport(strRepPK);
    }

    @Override
    public void reInit(String repcode, CellsModel cellsModel, String sheetname, int[] dynendrows){
    	super.reInit(repcode, cellsModel, sheetname, dynendrows);
//        m_bFormCanImport=getFormCanImport(repcode);
    }

    @Override
	protected String checkKeyDateStr(String strValue,KeyVO key,AreaPosition area)
    {
    	strValue=super.checkKeyDateStr(strValue, key, area);
    	if (strValue==null || strValue.length()<=0)
    		return strValue;

    	String timeProp=key.getTimeProperty();
        UFODate ufoDate = new nc.vo.iufo.pub.date.UFODate(strValue);
        String strInputDate = ufoDate.getEndDay(timeProp).toString();

        //对任务的有效期限作判断
        if(task != null){
        	//tianchuan 2012.6.19，先toString()，再equals
            if(task.getStart_date() != null && !task.getStart_date().toString().equals("")){
                UFODate inputUfoDate = new UFODate(strInputDate);
                UFODate startUfoDate = new UFODate(task.getStart_date().toString());
                startUfoDate = startUfoDate.getEndDay(timeProp);
                if(inputUfoDate.compareTo(startUfoDate) < 0){
                    addErrDataTime(area.toString(),nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1162")/*@res "录入日期不在任务有效期限范围内"*/);  //"录入日期不在任务有效期限范围内"
                    return null;
                }
            }
            //tianchuan 2012.6.19，先toString()，再equals
            if(task.getEnd_date() != null && !task.getEnd_date().toString().equals("")){
                UFODate inputUfoDate = new UFODate(strInputDate);
                UFODate endUfoDate = new UFODate(task.getEnd_date().toString());
                endUfoDate = endUfoDate.getEndDay(timeProp);
                if(endUfoDate.compareTo(inputUfoDate) < 0){
                    addErrDataTime(area.toString(),nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1163")/*@res "录入日期不在任务有效期范围内"*/);  //"录入日期不在任务有效期范围内"
                    return null;
                }
            }
        }
        return strInputDate;
    }

    protected boolean isCommitedOrHaveNoRight(String strUserPK,String strTaskPK,String strRepPK,String strRmsPK,String strMainOrgPK){
        try
        {
            String aloneId = mainPubDataVO.getAloneID();
            if (IUfoInputActionUtil.isCommit(strUserPK,strTaskPK,aloneId)){
                addErrCommited();
                return true;
            }

            ReportVO repVo = UFOCacheManager.getSingleton().getReportCache().getByPK(strRepPK);
            MeasurePubDataVO pubData=MeasurePubDataBO_Client.findByAloneID(repVo.getPk_key_comb(),aloneId);
            if (isNeedFilterByDataRight() && pubData.getUnitPK()!=null && pubData.getUnitPK().trim().length()>0){
            	//TODO: 此方法没有使用  modified by jiaah
            	int iRepDataAuthType = 0;
        		//判断是否需要进行权限控制 add by jiaah at 20110615
        		boolean bControlAuth = false;
        		if(task != null && task.getData_contype() != DataRightControlType.TYPE_NOTCONTROL.ordinal())
        			bControlAuth = true;

        		if(!bControlAuth){
        			iRepDataAuthType = IUfoContextKey.RIGHT_DATA_WRITE;
        		}
        		else{
        			iRepDataAuthType=RepDataAuthUtil.getAuthType(strUserPK,strRepPK,pubData.getUnitPK(),strRmsPK, strMainOrgPK, strTaskPK).ordinal();
        		}
            	if (iRepDataAuthType<IUfoContextKey.RIGHT_DATA_WRITE)
            		return true;
            }
        }
        catch(nc.pub.iufo.exception.UFOSrvException e){
          AppDebug.debug(e);
          if (e.detail!=null && e.detail instanceof nc.util.iufo.pub.UfoException)
            //throw new CommonException(((nc.util.iufo.pub.UfoException)e.detail).getExResourceId(),((nc.util.iufo.pub.UfoException)e.detail).getParams());
          throw new CommonException(((nc.util.iufo.pub.UfoException)e.detail).getExResourceId());
          else
            throw new CommonException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1270"));// 未定义的错误！
        }
        catch (Exception e) {
          throw new CommonException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1270"));//未定义的错误！
        }
        return false;
    }

    @Override
    protected boolean getFormCanImport(String strRepId) {

    	// 读取规则：按照 报表-〉任务-〉系统参数
		// 的顺序来读取这个属性，若属性设置为“是”或“否”，则按照此属性处理；若设置为“不控制”，则读取下一级的属性设置，直至系统参数。
    	if(task == null) {
    		return false;
    	}
    	// 第一级参数控制:报表中设置公式单元是否可"录入"数据
    	report = UFOCacheManager.getSingleton().getReportCache().getByCode(repcode);
		if (report == null) {
			return false;
		}
		int iReportIsEditFormula = report.getFmledit_type();// 获得属性值
		if (iReportIsEditFormula == PropertyOperate.FORMULA_EDIT_NO) {// 否：不能编辑
			return false;
		}
		if (iReportIsEditFormula == PropertyOperate.FORMULA_EDIT_YES) {// 是：可以编辑
			return true;
		}

    	// 第二级参数控制:任务中设置公式单元是否可以导入数据
    	if(task == null) {
    		return false;
    	}
    	int importType = task.getIscellimport().intValue();
    	if(importType == TaskVO.YES) {
    		return true;
    	}
    	if(importType == TaskVO.NO) {
    		return false;
    	}

    	// 第三级参数控制:全局参数,公式单元是否可以导入数据
    	try{
    		return UfobSysParamQueryUtil.getFormCellCanImport();
		}catch(Exception e){
			AppDebug.debug(e);
			return true;
		}

	}

    @Override
	public int processImportData(boolean isNeedSave) throws CommonException, BusinessException
	{

		// 如果工作表或报表为空，则无法导入数据，直接返回
		if (excelCellsModel == null || report == null)
			return 0;

		this.repName = report.getChangeName();
		// 判断是否该报表上报了，如果上报了的话，则不能进行导入
		if (isCommitedOrHaveNoRight(m_strUserPK,report.getPk_report()))
			return errCount;

		UfoContextVO context = new UfoContextVO();
		context.setAttribute(ReportContextKey.REPORT_PK, report.getPk_report());

		String curUserId = InvocationInfoProxy.getInstance().getUserId();

		context.setAttribute(CUR_USER_ID, curUserId);

		// 由于打开的是格式，所以，对格式应该有写的权限，对数据只有查看的权限
		repFormatSrv = new ReportFormatSrv(context, false);

		CellsModelOperator.getFormulaModel(repFormatSrv.getCellsModel()).setUnitID(m_strDataUnitPK);
		ExtendAreaCell[] dynAreas = repFormatSrv.getDynAreas();


		List<String> warnMsgList = new ArrayList<String>();
		List vecDataList = null;
		//记录动态区重复关键字位置，动态区时间区间不在主表时间关键字区间内。
		List<String> warnList = new ArrayList<String>();
		// 处理动态区的指标和关键字数据
		ArrayList dynaListData = new ArrayList();
		if (dynAreas != null && dynAreas.length > 0){
			int inputDynEndsLen = dynEnds.length; // 输入的动态区结束行的个数
			int dynAreasLen = dynAreas.length;    //动态区的个数
			int[] dynEndLines = new int[dynAreasLen];

			if(inputDynEndsLen<dynAreasLen){// 如果输入的动态区结束行的个数 小于实际动态区的个数，则补齐数组，缺少的填充为-1
				System.arraycopy(dynEnds, 0, dynEndLines, 0, inputDynEndsLen);
				Arrays.fill(dynEndLines, inputDynEndsLen, dynAreasLen, -1);
				dynEnds = dynEndLines;
			}
			/**
			 * TODO  如果 多个动态区同时向下扩展，他们的横向区域有交叉，这样的情况，
			 * 后面的动态与位置可能需要向下扩展
			 * 会造成第二个动态区不能导入
			 *
			 * 后续需要做的是把动态区扩展的区域 行数或者列数，及他们的区域记录下来，
			 *
			 * 这样后面的动态区找起始位置的时候可以直接找到。
			 *
			 */
			dynAddRowActIndex = 0;
			
			
			
			for(int i=0; i<Math.min(dynAreasLen,dynEnds.length); i++)
				if(dynAreas[i] != null){
					try{
						TwoTuple<List<MeasureDataVO>,List<String>> twoTuple = importDynRepData(dynAreas[i],i,warnMsgList);
						if(twoTuple != null){
							vecDataList = twoTuple.first;
							warnList.addAll(twoTuple.second);
							if (vecDataList != null){
								// 校验数据合法性
								checkPubDataVO(dynPubDatavoList, vecDataList);
								dynaListData.addAll(vecDataList);
							}
						}
					} catch (CommonException ce) {
						addErr(ce.getMessage());
					}
				}
		}
		if(!warnList.isEmpty()){
			StringBuilder sb = new StringBuilder();
			for (String string : warnList) {
				if(sb.length() == 0){
					sb.append(string);
				}else{
					sb.append("\r\n").append(string);
				}
			}
			throw new BusinessException(sb.toString());
		}
		if (errCount > 0){
			addMsg(warnMsgList);
			return errCount;
		}
		// 构造MeasurePubDataVO数组，包含动态区
		MeasurePubDataVO[] mpdatas = new MeasurePubDataVO[dynPubDatavoList
				.size() + 1];
		mpdatas[0] = mainPubDataVO;
		if (dynPubDatavoList.size() > 0)
		{
			for (int i = 0; i < dynPubDatavoList.size(); i++)
				mpdatas[i + 1] = (MeasurePubDataVO) dynPubDatavoList.get(i);
		}

		int dynListLen = 0;
		if (dynaListData != null && dynaListData.size() > 0)
			dynListLen = dynaListData.size();

		// 得到主表的指标数据
		ArrayList mainListData = importMainRepData(report, warnMsgList);

		int mainDataCount = 0;
		if (mainListData != null && mainListData.size() > 0)
			mainDataCount = mainListData.size();

		int len = mainDataCount + dynListLen - 1;
		MeasureDataVO[] mdatas = new MeasureDataVO[len + 1];
		for (; len >= 0; len--){
			if (len >= mainDataCount)
				mdatas[len] = (MeasureDataVO) dynaListData.get(len
						- mainDataCount);
			else
				mdatas[len] = (MeasureDataVO) mainListData.get(len);
		}
		MeasurePubDataVO[] measurePubDatas = dynPubDatavoList.toArray(new MeasurePubDataVO[0]);
		try {
			MeasurePubDataBO_Client.createFilterMeasurePubDatas(measurePubDatas);
		} catch (Exception e1) {
			AppDebug.error(e1);
		}
		RepDataVO repData = new RepDataVO(report.getPk_report(), report
				.getPk_key_comb());
		repData.setDatas(mpdatas, mdatas);
		repData.setUserID(curUserId);

		// 保存指标数据
		try
		{
			// @edit by wuyongc at 2013-6-8,上午10:07:48
//			boolean isOnServer = context.getAttribute(ON_SERVER) == null ? false
//					: Boolean.parseBoolean(context.getAttribute(ON_SERVER)
//							.toString());
//			if (isOnServer)
//				RepDataBO_Client.createRepData(repData,
//						m_strRmsPK);
//			else
//				RepDataBO_Client.createRepData(repData, m_strRmsPK);
			RepDataBO_Client.createRepData(repData, m_strRmsPK);

			ICommitManageService commitSrv=NCLocator.getInstance().lookup(ICommitManageService.class);
			if (bAutoCalc){
				String strtaskId = task == null ? null : task.getPk_task();
				commitSrv.addRepInputSate(strtaskId, repData.getMainPubData().getAloneID(), repData.getReportPK(),
		        		repData.getUserID(), true, AuditUtil.getCurrentTime());

				ReportFormatSrv calRepFormatSrv = InputUtil.getReportFormatSrv(
						report.getPk_report(), null, repData.getMainPubData()
								.getAloneID(), repData.getMainPubData()
								.getAccSchemePK(), repData.getMainPubData()
								.getUnitPK(), m_strUserPK, true, false,
						strLoginDate);
				// liuchun 20110527 修改，数据态导入数据时，上下文中设置状态
				calRepFormatSrv.getContextVO().setAttribute(OPERATION_STATE,OPERATION_INPUT);
				// @edit by wuyongc at 2012-8-7,下午3:24:31 外部函数计算，需要数据源信息。
				calRepFormatSrv.getContextVO().setAttribute(IUfoContextKey.DATA_SOURCE, m_voDataSource);
				
				calRepFormatSrv.getContextVO().setAttribute(IUfoContextKey.CUR_GROUP_PK, InvocationInfoProxy.getInstance().getGroupId());
				// end modify
				InputUtil.calculate(calRepFormatSrv, m_strUserPK, false, strLoginDate);
				calRepFormatSrv.saveReportData();
			} else {
				String strtaskId = task == null ? null : task.getPk_task();
				commitSrv.addRepInputSate(strtaskId, repData.getMainPubData().getAloneID(), repData.getReportPK(),
		        		repData.getUserID(), true, null);
			}

		} catch (Exception e)
		{
			AppDebug.debug(e);
			addErrSave();
		}
		dynAddRowActIndex = 0;
		addMsg(warnMsgList);
		return errCount;
	}
}