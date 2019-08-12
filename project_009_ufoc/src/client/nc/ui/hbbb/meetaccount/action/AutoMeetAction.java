package nc.ui.hbbb.meetaccount.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import nc.bs.framework.common.NCLocator;
import nc.bs.framework.lock.LockService;
import nc.bs.hbbb.contrast.batch.ContrastExecJob;
import nc.itf.hbbb.contrast.IContrast;
import nc.pub.bi.clusterscheduler.SchedulerUtilities;
import nc.pub.iufo.exception.UFOSrvException;
import nc.ui.hbbb.meetaccount.model.MeetResultModelDataManager;
import nc.ui.hbbb.meetaccount.view.CombTmpEditor;
import nc.ui.hbbb.meetaccount.view.MeetExcuteDialog;
import nc.ui.hbbb.meetaccount.view.MeetResultDetailEditor;
import nc.ui.hbbb.meetaccount.view.MeetResultTotalEditor;
import nc.ui.hbbb.qrypanel.schemekey.SchemeKeyEditor;
import nc.ui.hbbb.stockinvestrela.action.InvestRelaGraphShowDelegator;
import nc.ui.hbbb.stockinvestrela.action.InvestRelaListShowDelegator;
import nc.ui.hbbb.util.IProgressRunner;
import nc.ui.hbbb.util.ProgressThread;
import nc.ui.pub.beans.UIDialog;
import nc.ui.uif2.NCAction;
import nc.ui.uif2.ShowStatusBarMsgUtil;
import nc.ui.uif2.editor.BillForm;
import nc.ui.uif2.editor.BillListView;
import nc.ui.uif2.model.BillManageModel;
import nc.util.hbbb.HBAloneIDUtil;
import nc.util.hbbb.pub.HBPubItfService;
import nc.vo.bi.clusterscheduler.SchedulerKeys;
import nc.vo.hbbb.contrast.ContrastQryVO;
import nc.vo.hbbb.dxrelation.DXContrastVO;
import nc.vo.hbbb.dxrelation.DXRelationHeadVO;
import nc.vo.hbbb.meetaccount.MeetResultHeadVO;
import nc.vo.hbbb.schemekey.SchemeKeyQryVO;
import nc.vo.pub.BusinessException;

import com.ufida.iufo.pub.tools.AppDebug;

/**
 * 自动对账
 * 
 * @version V6.1
 * @author litfb
 * @modify by litfb 修改tmpeditor基类
 */
public class AutoMeetAction extends NCAction implements IProgressRunner {

    private static final long serialVersionUID = 4798553533442140434L;

    private BillManageModel model;

	private BillListView tmpeditor;

	private BillForm schemekeyeditor;

	private MeetResultDetailEditor tmplateresulteditor;

	private MeetResultTotalEditor  taotaleditor;

	private MeetExcuteDialog tmpExDialog;

	private MeetResultModelDataManager modelDataManager;
	
	private SchemeKeyQryVO schemeqryvo;
	
	private InvestRelaGraphShowDelegator investRelaGraphDelegator = null;
    private InvestRelaListShowDelegator investRelaListDelegator = null;
    
    public static String LOCK_HB_KEY = "UFOC_CONTRAST";

	public AutoMeetAction() {
		setBtnName(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0326")/* @res "自动对账" */);
		setCode("automeeetaction");
	}

	@SuppressWarnings({ "unchecked", "restriction" })
    @Override
    public void doAction(ActionEvent e) throws Exception {
        // 重新初始化面板
        List<DXRelationHeadVO> data = (List<DXRelationHeadVO>) ((CombTmpEditor) this.getTmpeditor()).getModel().getData();
        ((CombTmpEditor) this.getTmpeditor()).getModel().initModel(data.toArray());

        getTmpExDialog().showModal();
        
        if (UIDialog.ID_OK == getTmpExDialog().getResult()) {
            schemeqryvo = (SchemeKeyQryVO) getTmpExDialog().getSchemekeyeditor().getValue();
            
  			/*if (isStartSchedule(schemeqryvo.getPk_hbrepstru(),
  					schemeqryvo.getContrastorg())) {*/
//            if (false) {
//
//				// 对账单位大于200,启用调度
//				ContrastQryVO qryVO = new ContrastQryVO();
//				if (schemeqryvo == null
//						|| schemeqryvo.getSchemevo().getPk_hbscheme() == null) {
//					return;
//				}
//
//				qryVO.setSchemevo(schemeqryvo.getSchemevo());
//				qryVO.setContrastorg(schemeqryvo.getContrastorg());
//				qryVO.setPk_hbrepstru(schemeqryvo.getPk_hbrepstru());
//
//				BillManageModel model = getTmpeditor().getModel();
//				List<DXContrastVO> list = new ArrayList<DXContrastVO>();
//
//				Object[] array = model.getSelectedOperaDatas();
//				if (array != null) {
//					for (int i = 0; i < array.length; i++) {
//						DXContrastVO contrastvo = new DXContrastVO();
//						contrastvo.setHeadvo((DXRelationHeadVO) array[i]);
//						list.add(contrastvo);
//					}
//				}
//
//				qryVO.setDxmodels(list.toArray(new DXContrastVO[0]));
//				qryVO.setKeymap(schemeqryvo.getKeymap());
//				
//				doExecutedBySchedule(qryVO);
//
//				ShowStatusBarMsgUtil.showStatusBarMsg(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830010-0016")/** @res "本次执行超过200家对账单位，已启用本地调度执行" */, getModel().getContext());
//			} else {//小于5万
            ProgressThread progressThread = new ProgressThread(this);
            progressThread.start();
//			}
           
        }
    }
	
    @Override
    public void runProgressTask() {
        try {
            ContrastQryVO qryVO = new ContrastQryVO();
            if (schemeqryvo == null || schemeqryvo.getSchemevo().getPk_hbscheme() == null)
                return;
            qryVO.setSchemevo(schemeqryvo.getSchemevo());
            qryVO.setContrastorg(schemeqryvo.getContrastorg());
            qryVO.setPk_hbrepstru(schemeqryvo.getPk_hbrepstru());

            BillManageModel model = getTmpeditor().getModel();
            List<DXContrastVO> list = new ArrayList<DXContrastVO>();

            Object[] array = model.getSelectedOperaDatas();
            if (array != null) {
                for (int i = 0; i < array.length; i++) {
                    DXContrastVO contrastvo = new DXContrastVO();
                    contrastvo.setHeadvo((DXRelationHeadVO) array[i]);
                    list.add(contrastvo);
                }
            }

            qryVO.setDxmodels(list.toArray(new DXContrastVO[0]));
            qryVO.setKeymap(schemeqryvo.getKeymap());
            
            NCLocator.getInstance().lookup(IContrast.class).doContrast(qryVO);
            this.showSuccessInfo();
            // 对账完毕后进行查询
            queryData(qryVO);
        } catch (Exception e) {
            AppDebug.debug(e);
            ShowStatusBarMsgUtil.showErrorMsgWithClear(
                    nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0004")/* @res "提示" */,
                    e.getMessage(), getModel().getContext());
        }
    }

    private boolean isStartSchedule(String pk_hbrepstru, String pk_contrastorg) throws BusinessException {
		return NCLocator.getInstance().lookup(IContrast.class).isStartSchedule(pk_hbrepstru, pk_contrastorg);
	}
    
	/**
	 * 时间调度执行方式（只有选中的步骤数量超过100才会使用这种方式）
	 * @param keyQryVO
	 * @param execAllVOs
	 * @param execNotLeafVos
	 * @param stepVOs
	 * @throws Exception
	 * @throws UFOSrvException
	 */
	private void doExecutedBySchedule(final ContrastQryVO qryVO) throws Exception {
		ContrastExecJob job = new ContrastExecJob(qryVO);
		
		// zhaojian8
		String alone_id = HBAloneIDUtil.getAdjustVoucherAlone_id(qryVO,false);
		final String pkLock = qryVO.getSchemevo().getPk_hbscheme() + alone_id;// 方案+aloneid唯一确定
		try {
			boolean suc = NCLocator.getInstance().lookup(LockService.class).lock(pkLock);
			if (!suc){
				throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0141")/* @res "其他用户正在执行该操作,请稍后再试!"*/);
			}
			HBPubItfService.getRemoteContrastSrv().clearContrastedData(qryVO);
			
		} catch (Exception e) {
			// TODO: handle exception
			AppDebug.debug(e);
			ShowStatusBarMsgUtil.showStatusBarMsg(e.getMessage(), getModel().getContext());;
            throw new BusinessException(e);
		}

		
		final String jobID = SchedulerUtilities.addJob(job);
		Runnable runnable=new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					waitForJobComplete(jobID,pkLock,qryVO);
				} catch (BusinessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		new Thread(runnable).start();
		
	}
	
	/**
	 * 等待任务完成
	 * @param jobId
	 * @throws BusinessException 
	 */
	private void waitForJobComplete(String jobId,String pkLock,ContrastQryVO qryVO) throws BusinessException{
		int state = -1;
		try{
			state = NCLocator.getInstance().lookup(IContrast.class).waitForJobComplete(jobId);
		}catch(Exception e){

		}finally{
			NCLocator.getInstance().lookup(LockService.class).unlock(pkLock);
			if(state == SchedulerKeys.STATE_COMPLETE){
				this.showSuccessInfo();
				queryData(qryVO);
			}else if(state == SchedulerKeys.STATE_ERROR){
				HBPubItfService.getRemoteContrastSrv().clearContrastedData(qryVO);
				throw new BusinessException("对账失败,请重新对账");
			}

		}

	}
    
	private void queryData(ContrastQryVO qryVO) {
		String where = " pk_hbscheme = '" + qryVO.getSchemevo().getPk_hbscheme() + "' ";
		if (qryVO.getDxmodels() == null) {
			return;
		}
		String Alone_id = HBAloneIDUtil.getAdjustVoucherAlone_id(qryVO, true);
		where = where + " and "+MeetResultHeadVO.ALONE_ID+"='"+Alone_id+"'";
		String sql = " and pk_dxrelation  in (";
		for (int i = 0; i < qryVO.getDxmodels().length; i++) {
			if (i == 0) {
				sql += "'" + qryVO.getDxmodels()[i].getHeadvo().getPk_dxrela_head() + "'";
			} else {
				sql += ", '" + qryVO.getDxmodels()[i].getHeadvo().getPk_dxrela_head() + "'";
			}
		}
		sql += ") ";
		where += sql;

		getModelDataManager().setIstotal(true);
		getModelDataManager().initModelBySqlWhere(where);
		getTaotaleditor().showMeUp();
	}

	protected void showSuccessInfo() {
		ShowStatusBarMsgUtil.showStatusBarMsg(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0327")/*
																															 * @res "对账完毕"
																															 */, model.getContext());
	}

	public MeetExcuteDialog getTmpExDialog() {
		if (tmpExDialog == null) {
			tmpExDialog = new MeetExcuteDialog(getModel().getContext().getEntranceUI(), 
					getTmpeditor(), this.getSchemekeyeditor(), 
					investRelaGraphDelegator, investRelaListDelegator);
			  ((SchemeKeyEditor) this.getSchemekeyeditor()).loadInidvidual();
		}
		return tmpExDialog;
	}
	
	public BillManageModel getModel() {
		return model;
	}

	public void setModel(BillManageModel model) {
		this.model = model;
	}

	public BillListView getTmpeditor() {
		return tmpeditor;
	}

	public void setTmpeditor(BillListView tmpeditor) {
		this.tmpeditor = tmpeditor;
	}

	public BillForm getSchemekeyeditor() {
		return schemekeyeditor;
	}

	public void setSchemekeyeditor(BillForm schemekeyeditor) {
		this.schemekeyeditor = schemekeyeditor;
	}

	public MeetResultModelDataManager getModelDataManager() {
		return modelDataManager;
	}

	public void setModelDataManager(MeetResultModelDataManager modelDataManager) {
		this.modelDataManager = modelDataManager;
	}

	public MeetResultDetailEditor getTmplateresulteditor() {
		return tmplateresulteditor;
	}

	public void setTmplateresulteditor(MeetResultDetailEditor tmplateresulteditor) {
		this.tmplateresulteditor = tmplateresulteditor;
	}

	public MeetResultTotalEditor getTaotaleditor() {
		return taotaleditor;
	}

	public void setTaotaleditor(MeetResultTotalEditor taotaleditor) {
		this.taotaleditor = taotaleditor;
	}

	public InvestRelaGraphShowDelegator getInvestRelaGraphDelegator() {
		return investRelaGraphDelegator;
	}

	public void setInvestRelaGraphDelegator(
			InvestRelaGraphShowDelegator investRelaGraphDelegator) {
		this.investRelaGraphDelegator = investRelaGraphDelegator;
	}

	public InvestRelaListShowDelegator getInvestRelaListDelegator() {
		return investRelaListDelegator;
	}

	public void setInvestRelaListDelegator(
			InvestRelaListShowDelegator investRelaListDelegator) {
		this.investRelaListDelegator = investRelaListDelegator;
	}

}