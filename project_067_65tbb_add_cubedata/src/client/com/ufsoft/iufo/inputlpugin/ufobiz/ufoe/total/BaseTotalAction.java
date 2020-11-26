/**
 *
 */
package com.ufsoft.iufo.inputlpugin.ufobiz.ufoe.total;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import nc.bs.framework.common.NCLocator;
import nc.desktop.ui.WorkbenchEnvironment;
import nc.funcnode.ui.FuncletInitData;
import nc.funcnode.ui.FuncletWindowLauncher;
import nc.itf.iufo.approveset.IApproveQueryService;
import nc.itf.iufo.constants.IufoeFuncCodeConstants;
import nc.itf.iufo.total.ITotalQueryService;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.ui.iufo.input.control.base.AbsRepDataControler;
import nc.ui.iufo.input.edit.base.AbsBaseRepDataEditor;
import nc.ui.iufo.input.edit.base.IRepDataEditor;
import nc.ui.iufo.input.ufoe.comp.AbsCombRepDataEditor;
import nc.ui.iufo.input.ufoe.comp.AbsNotRepDataEditor;
import nc.ui.iufo.input.ufoe.control.IUFORepDataControler;
import nc.ui.iufo.input.ufoe.edit.IUFOCombRepDataEditor;
import nc.ui.iufo.input.ufoe.view.IUFOKeyCondPanel;
import nc.ui.iufo.input.view.base.AbsKeyCondPanel;
import nc.ui.iufo.input.view.base.AbsKeyCondPanel.ShowMode;
import nc.ui.sm.power.FuncRegisterCacheAccessor;
import nc.util.iufo.rms.RMSUtil;
import nc.util.iufo.total.TotalUtil;
import nc.utils.iufo.CommitUtil;
import nc.utils.iufo.TotalSrvUtils;
import nc.vo.iufo.commit.CommitStateEnum;
import nc.vo.iufo.commit.RepDataCommitVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.task.ApproveReportSet;
import nc.vo.iufo.task.TaskApproveVO;
import nc.vo.iufo.task.TaskVO;
import nc.vo.iufo.total.ITotalShowMsgConst;
import nc.vo.iufo.total.TotalSchemeVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.pf.IPfRetCheckInfo;
import nc.vo.sm.funcreg.FuncRegisterVO;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.report.plugin.AbstractRepPluginAction;
import com.ufida.zior.plugin.IPluginActionDescriptor;
import com.ufida.zior.plugin.PluginActionDescriptor;
import com.ufida.zior.plugin.PluginKeys;
import com.ufida.zior.plugin.PluginKeys.XPOINT;
import com.ufida.zior.view.Mainboard;
import com.ufida.zior.view.Viewer;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iufo.inputlpugin.ufobiz.ufoe.total.TotalCustomDlg.OrgReportType;
import com.ufsoft.report.util.UfoPublic;

/**
 * 汇总Action基类
 * @author xulm
 * @created at 2010-6-28,下午03:10:22
 *
 */
public class BaseTotalAction extends AbstractRepPluginAction{

	public enum TotalMenu{
		MENU_TOTAL,
		MENU_TOTALSUB,
		MENU_TASK_TOTAL,
		MENU_TASK_TOTALSUB,
		MENU_TOTAL_CUSTOM,
		MENU_TOTAL_SETTING,
		MENU_BALANCE_TOTAL;

	    public static TotalMenu getTotalMenu(String item){
	    	for (TotalMenu totalMenu: TotalMenu.values()){
	    		if (totalMenu.toString().equals(item)){
	    			return totalMenu;
	    		}
	    	}
            return MENU_TOTAL;
	    }

	    @Override
		public String toString(){
	    	switch(this){
	    	   case MENU_TOTAL :  return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0065")/*@res "单表汇总"*/;
	    	   case MENU_TOTALSUB :  return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0066")/*@res "汇总下级"*/;
	    	   case MENU_TASK_TOTAL: return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0067")/*@res "任务汇总"*/;
	    	   case MENU_TASK_TOTALSUB: return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0068")/*@res "任务汇总下级"*/;
	    	   case MENU_TOTAL_CUSTOM: return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0069")/*@res "汇总..."*/;
	    	   case MENU_TOTAL_SETTING: return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0070")/*@res "汇总规则"*/;
	    	   case MENU_BALANCE_TOTAL: return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820012-0102")/*@res "倒挤差额"*/;
	    	   default: return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0065")/*@res "单表汇总"*/;
	    	}
		}

	  /**
	   * 返回菜单分组信息
	   */
	    public String toGroupPath(){
	    	switch(this){
	    	   case MENU_TOTAL :  return "0";
	    	   case MENU_TASK_TOTAL: return "0";
	    	   case MENU_TOTALSUB :  return "1";
	    	   case MENU_TASK_TOTALSUB: return "1";
	    	   case MENU_TOTAL_CUSTOM: return "2";
	    	   case MENU_TOTAL_SETTING: return "3";
	    	   case MENU_BALANCE_TOTAL: return "4";
               default: return "3";
	    	}
	    }

	}

	private TotalMenu m_menuItem=TotalMenu.MENU_TOTAL;

	//执行汇总的类型，主要用于汇总...
	private TotalMenu m_totalType=TotalMenu.MENU_TOTAL;


	private String code = null;

	private int Memonic = 0;

	private String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public TotalMenu getTotalType() {
		return m_totalType;
	}

	private int getMemonic() {
		return Memonic;
	}

	public void setMemonic(int memonic) {
		Memonic = memonic;
	}

	public void setTotalType(TotalMenu type) {
		m_totalType = type;
	}

	public TotalMenu getMenuItem() {
		return m_menuItem;
	}

	public void setMenuItem(TotalMenu item) {
		m_menuItem = item;
		m_totalType=item;
	}



	@Override
	public void execute(ActionEvent e){
		if (!isEnabled()) {
			return;
		}

		try {
			Object[] params=getParams(m_menuItem);
			if (params==null)
			{
				return ;
			}
			IUFOCombRepDataEditor repDataEditor=  (IUFOCombRepDataEditor)getEditor();
			new TotalCmd(repDataEditor.getActiveRepDataEditor(),m_totalType,m_menuItem).execute(params);

		} catch (Exception exception) {
			AppDebug.debug(exception);
			UfoPublic.sendMessage(exception.getMessage(),getMainboard());
		}
	}



	@Override
	public IPluginActionDescriptor getPluginActionDescriptor() {
		PluginActionDescriptor des = new PluginActionDescriptor(m_menuItem.toString());
		des.setExtensionPoints(XPOINT.MENU);
		des.setToolTipText(m_menuItem.toString());
		des.setGroupPaths(doGetTotalMenuPaths(m_menuItem.toGroupPath()));
		des.setCode(getCode());
		des.setMemonic(getMemonic());
		return des;
	}



	/**
	 * 获取"汇总"的菜单路径
	 * @create by xulm at 2010-6-28,下午04:51:00
	 *
	 * @param strGroup
	 * @return
	 */
    protected String[] doGetTotalMenuPaths(String strGroup){
        return new String[]{nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID(PluginKeys.MENU_PRODUCE_CODE,PluginKeys.MENU_TOTAL)/*@res "汇总"*/,strGroup};
    }


    /**
     * 获取指定汇总菜单项的执行参数
     * @create by xulm at 2010-6-28,下午04:50:34
     *
     * @param Item
     * @return
     * @throws Exception
     */
	protected Object[] getParams(TotalMenu item) throws Exception
	{
		Object[] params=null;
		String rmsId=(String)getMainboard().getContext().getAttribute(IUfoContextKey.CUR_REPMNGSTRUCT_PK);
//		String userId = (String)getMainboard().getContext().getAttribute(IUfoContextKey.CUR_USER_ID);
		IUFORepDataControler repDataControler = (IUFORepDataControler) (AbsRepDataControler.getInstance(getMainboard()));

		//单表汇总组织为当前选择的组织；汇总下级汇总组织设为当前的报表主组织
		String orgId = null;
		//主组织
//		String mainOrgId = (String)getMainboard().getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK);
		if (item==TotalMenu.MENU_TOTAL ||item==TotalMenu.MENU_TASK_TOTAL
				||item==TotalMenu.MENU_TOTAL_CUSTOM || item==TotalMenu.MENU_BALANCE_TOTAL) {
			orgId = repDataControler.getSelectedUnitPK();
		}
		else if (item==TotalMenu.MENU_TOTALSUB ||item==TotalMenu.MENU_TASK_TOTALSUB ){
			orgId = (String)getMainboard().getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK);
		}

//		String reportId = ((IUFOCombRepDataEditor)getEditor()).getKeyCondPane().getRepPK();
		String reportId = ((IUFOCombRepDataEditor)getEditor()).getActiveRepDataEditor().getRepPK();
		
		String taskId = repDataControler.getSelectedTaskPK();
		TaskVO taskVO = repDataControler.getSelectedTaskInfo().getTaskVO();
		
		String[] reportIds=null;
		boolean[] extendParams=null;
		
		MeasurePubDataVO pubData=null;
		pubData = getMeasurePubDataFromKeyCondPanel(taskVO);
		
		//edit by congdy 2015.5.23 如果是单表汇总或汇总下级，判断当前表是否已上报或审批，如果已上报或审批就不再往下走了
		if(item == TotalMenu.MENU_TOTAL) {
			ITotalQueryService srv = NCLocator.getInstance().lookup(ITotalQueryService.class);
			Object[] msg = srv.getCommitAndApproveState(pubData, taskId, new String[]{ reportId });
			RepDataCommitVO[] lstReportCommit = (RepDataCommitVO[])msg[0];
			Map<String,Integer> reportToStatesMap = (Map<String,Integer>)msg[1];
			if(lstReportCommit != null && lstReportCommit.length > 0) {
				if(lstReportCommit[0].getCommit_state() >= CommitStateEnum.STATE_COMMITED.getIntValue()) {
					throw new Exception(ITotalShowMsgConst.STATE_COMMIT);
				}
			}
			if(reportToStatesMap.get(reportId) != null && reportToStatesMap.get(reportId).equals(IPfRetCheckInfo.NOSTATE)) {
				throw new Exception(ITotalShowMsgConst.STATE_APPROVE);
			}
		}

		//获取个性化中心设置的扩展参数：是否包含未上报、遇异常是否继续执行；
		extendParams = TotalSrvUtils.getTotalIndividualParams();

		//获取本组织应用的汇总方案，如果本组织没有则采用父级的
//		TotalSchemeVO totalScheme = TotalSrvUtils.getAppTotalScheme(rmsId, orgId);

		String date = WorkbenchEnvironment.getInstance().getBusiDate().toString();

		Object[] results = TotalSrvUtils.getTotalSchemeInfo(rmsId, orgId, taskId, pubData, date);

		TotalSchemeVO totalScheme = (TotalSchemeVO)results[0];
		
		boolean isLeafOrg = ((Boolean)results[4]).booleanValue();
		
		if(isLeafOrg) {
			throw new Exception(ITotalShowMsgConst.STATE_LEAFORG_NOTEXECUTE);//末级组织不执行汇总
		}

		if (totalScheme==null ){
			//如果为叶子节点
			if(isLeafOrg)
				throw new Exception(ITotalShowMsgConst.STATE_LEAFORG_NOTEXECUTE);//末级组织不执行汇总
			else
				throw new Exception(ITotalShowMsgConst.STATE_NOSCHEME);//请设置汇总方案
		}
		
		if (item==TotalMenu.MENU_TOTAL_CUSTOM)
	    {

			//如果是父级的，重置组织
			totalScheme.setPk_org(orgId);

			final TotalCustomDlg dlg = new TotalCustomDlg(getMainboard(),item.toString());

			//关键字的值，传入到dlg中
			String[] keyValues = ((IUFOCombRepDataEditor)getEditor()).getKeyCondPane().getInputKeyValues();
			dlg.setKeyValues(keyValues);
			dlg.setRmsId(rmsId);
//			dlg.setOrgId((String)getMainboard().getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK));
			dlg.setOrgId(orgId);
			dlg.setTaskId(taskId);
			dlg.setReportId(reportId);
			dlg.initUI();

			if (dlg.showModal() == TotalCustomDlg.ID_OK)
			{
				pubData=dlg.getMeasurePubData();
				reportIds = dlg.getReportIds();
				if (dlg.getSelectOrgReportType()==OrgReportType.OrgReport||dlg.getSelectOrgReportType()==OrgReportType.OrgTaskReport||dlg.getSelectOrgReportType()==OrgReportType.OrgTaskSortReport)
				{
					//edit by congdy 进行单表汇总时需对汇总规则进行判断
					if (totalScheme.getOrg_type() == TotalSchemeVO.TYPE_NONE){
						throw new Exception(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1095")/*@res "当前组织汇总规则为不汇总"*/);
					}
					m_totalType=TotalMenu.MENU_TOTAL;
					params=new Object[]{pubData,totalScheme,reportIds,extendParams,taskId};
				}
			    else if (dlg.getSelectOrgReportType()==OrgReportType.OrgSubReport||dlg.getSelectOrgReportType()==OrgReportType.OrgSubTaskReport||dlg.getSelectOrgReportType()==OrgReportType.OrgSubTaskSortReport)
				{
			    	m_totalType=TotalMenu.MENU_TOTALSUB;
			    	params=new Object[]{pubData,rmsId,orgId,reportIds,extendParams,taskId};
				}
//				//任务启用数据权限，则进行报表过滤，只对具有修改权限的报表执行汇总
//				if(isFilterRep(taskVO))
//					reportIds = filterTaskReportByRepAuth(reportIds, userId, orgId, rmsId, mainOrgId, taskId);
//				if (reportIds == null ||reportIds.length == 0){
//					throw new Exception(ITotalShowMsgConst.STATE_NOEDITAUTH);
//				}
			}
		}
		else
		{

			//如果是父级的，重置组织
			totalScheme.setPk_org(orgId);

//			pubData=getMeasurePubDataFromKeyCondPanel(taskVO);
			String aloneId = (String)results[1];
			pubData.setAloneID(aloneId);

			if (item==TotalMenu.MENU_TOTAL||item==TotalMenu.MENU_TOTALSUB){
				reportIds=new String[]{reportId};

			}
			else{
//				reportIds= TaskSrvUtils.getReceiveReportId(taskId);
				reportIds = (String[])results[2];
				if (reportIds==null ||reportIds.length==0){
					throw new Exception(ITotalShowMsgConst.STATE_NOREPORT);
				}
			}
//			//任务启用数据权限，则进行报表过滤，只对具有修改权限的报表执行汇总
//			if(isFilterRep(taskVO))
//				reportIds = filterTaskReportByRepAuth(reportIds, userId, orgId, rmsId, mainOrgId, taskId);
//			if (reportIds == null ||reportIds.length == 0){
//				throw new Exception(ITotalShowMsgConst.STATE_NOEDITAUTH);
//			}

	        //汇总和任务汇总对于本组织不汇总的处理
			if (item==TotalMenu.MENU_TOTAL ||item==TotalMenu.MENU_TASK_TOTAL || item==TotalMenu.MENU_BALANCE_TOTAL) {
				//单表或汇总下级时判断
//				if (totalScheme == null ){
//					//如果为叶子节点
//					if(isLeafOrg)
//						throw new Exception(ITotalShowMsgConst.STATE_LEAFORG_NOTEXECUTE);
//					else
//						throw new Exception(ITotalShowMsgConst.STATE_NOSCHEME);
//				}
				if (totalScheme.getOrg_type()==TotalSchemeVO.TYPE_NONE){
					throw new Exception(ITotalShowMsgConst.STATE_NOTTOTAL);
				}
				if(item==TotalMenu.MENU_TOTAL ||item==TotalMenu.MENU_TASK_TOTAL){
					params=new Object[]{pubData,totalScheme,reportIds,extendParams,taskId};
				}else{	//差额汇总
					params=new Object[]{pubData,totalScheme,reportIds,extendParams,taskId,rmsId};
				}

			}
			else if (item==TotalMenu.MENU_TOTALSUB ||item==TotalMenu.MENU_TASK_TOTALSUB)
			{
				params=new Object[]{pubData,rmsId,orgId,reportIds,extendParams,taskId};
			}
		}

		return params;
	}

//	/**
//	 * 是否过滤报表
//	 * @create by jiaah at 2011-6-21,下午02:18:43
//	 * @return
//	 */
//	private boolean isFilterRep(TaskVO taskVO){
//		if(taskVO != null && taskVO.getData_contype() == DataRightControlType.TYPE_CONTROL.ordinal()){
//			return true;
//		}
//		return false;
//	}
//
//	/**
//	 * 根据数据权限过滤任务下的报表，只保留具有修改权限的报表
//	 * @throws Exception
//	 * @create by jiaah at 2011-6-21,下午02:03:54
//	 *
//	 */
//	private String[] filterTaskReportByRepAuth(String[] reportIds,String userId,String orgId,String rmsId,String mainOrgId,String pk_task){
//		List<String> lstReportIds = new ArrayList<String>();
//		try {
//			Map<String, RepDataAuthType> map = RepDataAuthUtil.getAuthType_Client(userId,reportIds,orgId,rmsId,mainOrgId);
//			for(int i = 0 ; i < reportIds.length ; i++){
//				try {
//					int iAuthType = map.get(reportIds[i]).ordinal();
//					if(iAuthType > RepDataAuthType.VIEW.ordinal()){
//						lstReportIds.add(reportIds[i]);
//					}
//				} catch (Exception e) {
//					AppDebug.debug(e.getMessage());
//				}
//			}
//		} catch (Exception e1) {
//			AppDebug.debug(e1);
//		}
//
//		return lstReportIds.toArray(new String[0]);
//	}

	/**
	 * 从关键字面板中获取MeasurePubDataVO对象
	 * @create by xulm at 2010-6-24,下午07:01:53
	 *
	 * @return
	 * @throws Exception
	 */
	private  MeasurePubDataVO getMeasurePubDataFromKeyCondPanel(TaskVO taskVO) throws Exception
	{
		//得到关键字面板的值
		IUFOKeyCondPanel keyCondPanel=(IUFOKeyCondPanel) ((IUFOCombRepDataEditor)getEditor()).getKeyCondPane();
	   	String[] inputKeyVals=keyCondPanel.getInputKeyValues();

		String currOrgPk = (String)getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK);
		inputKeyVals[0] = currOrgPk;

    	boolean hasInput = AbsKeyCondPanel.checkKeyInput(inputKeyVals);
    	if (hasInput==false)
    	{
    		throw new Exception(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0072")/*@res "关键字面板中未录入完全"*/);
    	}

    	//关键字有效性验证
		String strKeyGroupPk = taskVO.getPk_keygroup();
		KeyGroupVO keyGroup = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(strKeyGroupPk);
        if (keyGroup.getKeyByKeyPk(KeyVO.CORP_PK)==null){
        	throw new Exception(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0073")/*@res "当前任务没有包含单位关键字，无法进行汇总"*/);
        }
//		if (!inputKeyVals[0].trim().equals(strOrgPK))
//		{
//			throw new Exception("关键字面板中单位信息与选择的单位不匹配");
//		}

        MeasurePubDataVO pubVO = new MeasurePubDataVO();
        pubVO.setKType(strKeyGroupPk);
        pubVO.setVer(0);
        pubVO.setKeyGroup(keyGroup);
        pubVO.setKeywords(inputKeyVals);
        pubVO.setAccSchemePK(((IUFOCombRepDataEditor)getEditor()).getAccSchemePK());
//        String strAloneID = MeasurePubDataBO_Client.getAloneID(pubVO);
//        String strAloneID = null;
//        String[] aloneIds = MeasurePubDataBO_Client.getAloneIDs(new MeasurePubDataVO[]{pubVO});
//        if(aloneIds != null && aloneIds.length > 0) {
//        	strAloneID = aloneIds[0];
//        }
//        IUFORepDataControler repDataControler = (IUFORepDataControler) (AbsRepDataControler.getInstance(getMainboard()));
//		String strAloneID = repDataControler.getAloneId(getMainboard(), inputKeyVals[0]);//inputKeyVals[0]关键字面板单位id

//        pubVO.setAloneID(strAloneID);
        return pubVO;
	}

	/**
	 * 打开汇总设置对话框
	 * @throws BusinessException
	 * @create by xulm at 2010-6-24,下午06:54:36
	 *
	 */
	protected void openTotalSettingDlg(Mainboard md, IUFOCombRepDataEditor editor) throws BusinessException {
		FuncletInitData data = new FuncletInitData();
		String rmsID = (String) (md.getContext().getAttribute(IUfoContextKey.CUR_REPMNGSTRUCT_PK));
		String orgID = (String) (md.getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK));
		// editor tianjlc 2015-03-24 适配报表组织体系多版本修改
		// String date=((AbsCombRepDataEditor) getMainboard().getView("iufo.input.data.view")).getKeyCondPane().getDate();
		// edit by liuweiu 2015-04-07 上一种方法可能会导致空指针
		Viewer currentViewer = getMainboard().getCurrentView();
		if (currentViewer instanceof AbsCombRepDataEditor) {
			String date = ((AbsCombRepDataEditor) currentViewer).getKeyCondPane().getDate();
			data.setInitData(new Object[] { rmsID, orgID, md, editor, date });
			FuncRegisterVO function = FuncRegisterCacheAccessor.getInstance().getFuncRegisterVOByFunCode(
					IufoeFuncCodeConstants.FUNC_TOTALRULE);
			FuncletWindowLauncher.openFuncNodeDialog(getMainboard(), function, data, null, false, false, new Dimension(
					800, 600));
		} else {
			return;
		}
	}

	@Override
	public boolean isEnabled() {
		if (getCurrentView() instanceof AbsBaseRepDataEditor) {
			IRepDataEditor editor=((AbsBaseRepDataEditor)getCurrentView()).getActiveRepDataEditor();
			// 来自于分布式上传的报表数据不做汇总
			return isRepOpened() && editor != null && editor.getMenuState() != null && !editor.getMenuState().beDisTrans();
		} else {
			return false;
		}
	}

	/**
	 * 报表是否打开
	 * @create by jiaah at 2011-3-8,下午04:47:15
	 * @return
	 */
	private boolean isRepOpened(){
		//判断是否是自由汇总
    	Boolean freeTotal = false;
    	Viewer curView = getCurrentView();
    	if(curView != null && curView instanceof AbsBaseRepDataEditor && ((AbsBaseRepDataEditor)curView).getKeyCondPane() != null){
    		ShowMode showMode = ((AbsBaseRepDataEditor)curView).getKeyCondPane().getShowMode();
        	if(showMode == ShowMode.TOTAL)
        		freeTotal = true;
    	}

    	if (curView == null || curView instanceof AbsBaseRepDataEditor == false || freeTotal == true)
    		return false;
    	IRepDataEditor editor=((AbsBaseRepDataEditor)curView).getActiveRepDataEditor();
    	if(editor instanceof AbsNotRepDataEditor){
    		return false;
    	}
    	return editor!=null && editor.getAloneID()!=null && editor.isSWReport()==false;
    }

}
