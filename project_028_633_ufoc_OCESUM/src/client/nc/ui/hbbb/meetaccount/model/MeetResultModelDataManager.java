package nc.ui.hbbb.meetaccount.model;

import nc.bs.framework.common.NCLocator;
import nc.itf.hbbb.meetresult.IMeetResultQueryService;
import nc.ui.querytemplate.querytree.IQueryScheme;
import nc.ui.uif2.AppEvent;
import nc.ui.uif2.IShowMsgConstant;
import nc.ui.uif2.ShowStatusBarMsgUtil;
import nc.ui.uif2.components.pagination.BillManagePaginationDelegator;
import nc.ui.uif2.components.pagination.IPaginationModelListener;
import nc.ui.uif2.components.pagination.IPaginationQueryService;
import nc.ui.uif2.components.pagination.PaginationModel;
import nc.ui.uif2.model.BillManageModel;
import nc.ui.uif2.model.IAppModelDataManagerEx;
import nc.ui.uif2.model.IQueryAndRefreshManagerEx;
import nc.ui.uif2.model.ModelDataDescriptor;
import nc.vo.hbbb.meetaccount.AggMeetRltHeadVO;
import nc.vo.hbbb.meetaccount.MeetResultBodyVO;
import nc.vo.hbbb.meetaccount.MeetResultHeadVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDouble;
import nc.vo.util.SqlWhereUtil;
/**
 * 
 * <p>
 * TODO 接口/类功能说明，使用说明（接口是否为服务组件，服务使用者，类是否线程安全等）。
 * </p>
 *
 * 修改记录：<br>
 * <li>修改人：修改日期：修改内容：</li>
 * <br><br>
 *
 * @see 
 * @author wangxwb
 * @version V6.0
 * @since V6.0 创建时间：2011-1-17 下午07:10:41
 */
public class MeetResultModelDataManager implements IAppModelDataManagerEx, IQueryAndRefreshManagerEx, IPaginationModelListener {
	
	public static final String MODELDATAMANAGERDATACHANGE = "modelDataManagerDataChange";

	//是否总报告
	private boolean istotal = true;
	
	
	private String sqlWhere;//总额查询条件缓存

	private String detailsqlWhere;//模板明细查询条件缓存

	//总报告model
	private BillManageModel totalmodel;
	
	//按模板
	private BillManageModel detailmodel;
	//分页模板
	private PaginationModel totalPaginationModel;
	private PaginationModel detailPaginationModel;
	//分页代理(总报告)
	private BillManagePaginationDelegator totalPaginationDelegator;
	//按模板查询时代理
	private BillManagePaginationDelegator detailPaginationDelegator;
	
	private AggMeetRltHeadVO[] totalObjs;
	private AggMeetRltHeadVO[] detailObjs;
	
	private meetRltPginQryService meetRltPginQryService;
	
	@Override
	public void initModel() {
		try {
			if(istotal) {
				getTotalPaginationModel().setObjectPks(null);
			}else {
				getDetailPaginationModel().setObjectPks(null);
			}
		} catch (BusinessException e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void refresh() {
		if(istotal){
			initModelBySqlWhere(sqlWhere);
		}else{
			initModelBySqlWhere(detailsqlWhere);
		}
	}
	

	@Override
	public void initModelBySqlWhere(String sqlWhere) {
		if(sqlWhere != null){
			if(istotal){
				this.sqlWhere = sqlWhere;
			}else{
				this.detailsqlWhere = sqlWhere;
			}
			String[] pks = null;
			// @edit by zhoushuang at 2015-5-30,上午11:13:05 凭证查询状态栏显示结果 
			//start
			String schemeName = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0220")/*@res "查询结果"*/;
			ModelDataDescriptor descriptor = new ModelDataDescriptor(schemeName);
			
			try {
				pks = NCLocator.getInstance().lookup(IMeetResultQueryService.class).queryMeetResultPKsByCondition(sqlWhere);
				if(istotal){
					getTotalPaginationModel().setObjectPks(pks,descriptor);
					if (pks == null || pks.length == 0) {
						ShowStatusBarMsgUtil.showStatusBarMsg(IShowMsgConstant.getQueryNullInfo(), getTotalmodel().getContext());
					} else {
						ShowStatusBarMsgUtil.showStatusBarMsg(IShowMsgConstant.getQuerySuccessInfo(pks.length), getTotalmodel().getContext());
					}
				}
				else {
					//Modified by sunzeg 按模板抵消查询取消分页，解决对账及对账数据查询与合并报表数据中心抵消分录结果不一致_通版
					//getDetailPaginationModel().setObjectPks(pks,descriptor);
					setMeetResultData(pks, descriptor);
					if (pks == null || pks.length == 0) {
						ShowStatusBarMsgUtil.showStatusBarMsg(IShowMsgConstant.getQueryNullInfo(), getDetailmodel().getContext());
					} else {
						ShowStatusBarMsgUtil.showStatusBarMsg(IShowMsgConstant.getQuerySuccessInfo(pks.length), getDetailmodel().getContext());
					}
				}
			//end
			} catch (BusinessException e) {
				nc.bs.logging.Logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void setShowSealDataFlag(boolean showSealDataFlag) {

	}

	public BillManageModel getTotalmodel() {
		return totalmodel;
	}

	public void setTotalmodel(BillManageModel totalmodel) {
		this.totalmodel = totalmodel;
	}

	public BillManageModel getDetailmodel() {
		return detailmodel;
	}

	public void setDetailmodel(BillManageModel detailmodel) {
		this.detailmodel = detailmodel;
	}

	@Override
	public void onStructChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDataReady() {
		if(istotal) {
			getTotalPaginationDelegator().onDataReady();
		}
		else {
			getDetailPaginationDelegator().onDataReady();
		}
	}

	@Override
	public void initModelBySqlWhere(IQueryScheme queryScheme) {
		String sql = new SqlWhereUtil(queryScheme.getWhereSQLOnly()).getSQLWhere();
		initModelBySqlWhere(sql);
	}
	public boolean isIstotal() {
		return istotal;
	}
	
	public void setIstotal(boolean istotal) {
		this.istotal = istotal;
	}

	public PaginationModel getTotalPaginationModel() {
		return totalPaginationModel;
	}

	public void setTotalPaginationModel(PaginationModel paginationModel) {
		this.totalPaginationModel = paginationModel;
		this.totalPaginationModel.addPaginationModelListener(this);
		this.totalPaginationModel.setPaginationQueryService(new meetRltPginQryService());
	}

	public PaginationModel getDetailPaginationModel() {
		return detailPaginationModel;
	}

	public void setDetailPaginationModel(PaginationModel paginationModel) {
		this.detailPaginationModel = paginationModel;
		this.detailPaginationModel.addPaginationModelListener(this);
		this.detailPaginationModel.setPaginationQueryService(new meetRltPginQryService());
	}
	
	private class meetRltPginQryService implements IPaginationQueryService {
		@Override
		public Object[] queryObjectByPks(String[] pks)
				throws BusinessException {
			AggMeetRltHeadVO[] aggvos = NCLocator.getInstance().lookup(IMeetResultQueryService.class).queryAggMeetResultByPKs(pks);
			if(istotal) {
				if(aggvos != null && aggvos.length > 0){
					for (AggMeetRltHeadVO aggVo : aggvos) {
						MeetResultHeadVO headVO = (MeetResultHeadVO) aggVo.getParentVO();
						MeetResultBodyVO[] childrenVO = (MeetResultBodyVO[]) aggVo.getChildrenVO();
						UFDouble debit = new UFDouble(UFDouble.ZERO_DBL);
						UFDouble credit = new UFDouble(UFDouble.ZERO_DBL);
						for (MeetResultBodyVO bodyVO : childrenVO) {
							if(bodyVO.getDirection() == 0){
								debit = debit.add(bodyVO.getMeet_amount());
							}else{
								credit = credit.add(bodyVO.getMeet_amount());
							}
						}
						headVO.setVoucherdebitamount(debit);
						headVO.setVouchercreditamount(credit);
					}
				}
				setTotalObjs(aggvos);
			}
			else {
				setDetailObjs(aggvos);
			}
			return aggvos;
		}
	}
	
	public BillManagePaginationDelegator getTotalPaginationDelegator() {
		if(totalPaginationDelegator == null) {
			totalPaginationDelegator = new BillManagePaginationDelegator(getTotalmodel(), getTotalPaginationModel());
		}
		return totalPaginationDelegator;
	}

	public BillManagePaginationDelegator getDetailPaginationDelegator() {
		if(detailPaginationDelegator == null) {
			detailPaginationDelegator = new BillManagePaginationDelegator(getDetailmodel(), getDetailPaginationModel());
		}
		return detailPaginationDelegator;
	}

	public AggMeetRltHeadVO[] getTotalObjs() {
		return totalObjs;
	}

	public void setTotalObjs(AggMeetRltHeadVO[] totalObjs) {
		this.totalObjs = totalObjs;
		getTotalmodel().fireEvent(new AppEvent(MeetResultModelDataManager.MODELDATAMANAGERDATACHANGE, getTotalmodel(), null));
	}

	public AggMeetRltHeadVO[] getDetailObjs() {
		return detailObjs;
	}

	public void setDetailObjs(AggMeetRltHeadVO[] detailObjs) {
		this.detailObjs = detailObjs;
		getDetailmodel().fireEvent(new AppEvent(MeetResultModelDataManager.MODELDATAMANAGERDATACHANGE, getDetailmodel(), null));
	}

	public String getSqlWhere() {
		return sqlWhere;
	}

	public void setSqlWhere(String sqlWhere) {
		this.sqlWhere = sqlWhere;
	}	
	
	private meetRltPginQryService getMeetRltPginQryService(){
		if(meetRltPginQryService == null){
			meetRltPginQryService = new meetRltPginQryService();
		}
		return meetRltPginQryService;
	}
	
	/**
	 * 直接更新单据模板的数据，跳过分页
	 * @author sunzeg
	 * @param pks
	 * @param descriptor
	 * @throws BusinessException
	 */
	@SuppressWarnings("restriction")
	private void setMeetResultData(String[] pks, ModelDataDescriptor descriptor) throws BusinessException{
		Object[] objs = getMeetRltPginQryService().queryObjectByPks(pks);		
		getDetailmodel().initModel(objs, descriptor);
	}
}
