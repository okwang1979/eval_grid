package nc.ui.hbbb.hbreport.action;

import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.SwingWorker;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.hbbb.hbrepstru.HBRepStruUtil;
import nc.itf.hbbb.union.IUnionReport;
import nc.itf.iufo.data.IMeasureDataSrv;
import nc.itf.uap.IUAPQueryBS;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.MeasureCache;
import nc.ui.hbbb.hbreport.view.ExcuteHBReportDialog;
import nc.ui.hbbb.qrypanel.schemekey.SchemeKeyEditor;
import nc.ui.hbbb.stockinvestrela.action.InvestRelaGraphShowDelegator;
import nc.ui.hbbb.stockinvestrela.action.InvestRelaListShowDelegator;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.pub.beans.UIDialog;
import nc.ui.pub.beans.progress.DefaultProgressMonitor;
import nc.ui.pub.beans.progress.IProgressMonitor;
import nc.ui.uif2.NCAction;
import nc.ui.uif2.ShowStatusBarMsgUtil;
import nc.ui.uif2.components.progress.TPAProgressUtil;
import nc.ui.uif2.editor.BillForm;
import nc.ui.uif2.model.BillManageModel;
import nc.util.hbbb.MeasurePubDataUtil;
import nc.vo.hbbb.schemekey.SchemeKeyQryVO;
import nc.vo.hbbb.union.UnionReportQryVO;
import nc.vo.iufo.data.MeasureDataVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.para.SysInitVO;

/**
 * 合并Action
 * 
 * @version V6.1
 * @author litfb
 */
public class HBReportUnionAction extends NCAction {

    private static final long serialVersionUID = 5817723541094464094L;

    private BillManageModel model;

    private ExcuteHBReportDialog excutedialog;

    private BillForm schemeKeyEditor;
    
    private InvestRelaGraphShowDelegator investRelaGraphDelegator = null;
    private InvestRelaListShowDelegator investRelaListDelegator = null;

    public HBReportUnionAction() {
        this.setBtnName(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0276")/* @res "合并" */);
        this.setCode("hbaction");
    }

    @Override
    public void doAction(ActionEvent e) throws Exception {
//        ((SchemeKeyEditor) this.getSchemeKeyEditor()).initHBScheme();
    	
        this.getExcutedialog().showModal();
        if (UIDialog.ID_OK == this.getExcutedialog().getResult()) {
            SchemeKeyQryVO schemeqryvo = (SchemeKeyQryVO) this.getExcutedialog().getSchemekeyeditor().getValue();
            String pk_hbscheme = schemeqryvo.getSchemevo().getPk_hbscheme();
            if (null == pk_hbscheme) {
                throw new BusinessRuntimeException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0",
                        "01830001-0278")/* @res "请选择合并方案!" */);
            }
            Map<String, String> keyMap = schemeqryvo.getKeymap();
            for (String s : keyMap.values()) {
                if (null == s) {
                    throw new BusinessRuntimeException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0",
                            "01830001-0102")/* @res "关键字信息不能为空!" */);
                }
            }

            String pk_org = keyMap.get(KeyVO.CORP_PK);
            boolean isLeaf = HBRepStruUtil.isLeafMember(pk_org, schemeqryvo.getPk_hbrepstru());
            if (isLeaf) {
                throw new BusinessRuntimeException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0",
                        "01830003-0030")/* @res "没有下级单位不能合并报表!" */);
            }

            final UnionReportQryVO reportHbVO = new UnionReportQryVO();
            reportHbVO.setPk_hbscheme(pk_hbscheme);
			reportHbVO.setUnionorg(schemeqryvo.getContrastorg());
			reportHbVO.setKeymap(schemeqryvo.getKeymap());
			reportHbVO.setPk_hbrepstru(schemeqryvo.getPk_hbrepstru());
			reportHbVO.setPk_user(InvocationInfoProxy.getInstance().getUserId());
			
			
			//王志强 ：央客 国旅合并前查找
			String info = "个别表或者合并调整表有数据请检测";
			try{
//				 MeasurePubDataVO pubdata = MeasurePubDataUtil.getMeasurePubdata(0, true, reportHbVO.getUnionorg(), reportHbVO.getKeymap(), schemeqryvo.getSchemevo().getPk_keygroup());
//				 /*     */         
//				 /*  85 */         String uinoinAloneid = pubdata.getAloneID();
				 
				 IUAPQueryBS querys = NCLocator.getInstance().lookup(IUAPQueryBS.class);
//			 
				 
				 SysInitVO initVo =  (SysInitVO) querys.retrieveByPK(SysInitVO.class, "0001Z31000000001UFOC");
				 
				 if("3500".equals(initVo.getValue())){
					 if(schemeqryvo.getSchemevo().getCode()==313){
						 MeasurePubDataVO pubV0 = MeasurePubDataUtil.getMeasurePubdata(0, true, reportHbVO.getKeymap(),schemeqryvo.getSchemevo() );
							MeasurePubDataVO pub4313001 = MeasurePubDataUtil.getMeasurePubdata( pubV0,4313001 );
//						 
							
//							IStoreCellPackQrySrv cellQuser = NCLocator.getInstance().lookup(IStoreCellPackQrySrv.class);
//							 Hashtable<String, IStoreCell> cells =  cellQuser.getStoreCellsByRepID("1001A2100000000022UN");
//							IMeasureDataSrv measureDataSrv = NCLocator.getInstance().lookup(IMeasureDataSrv.class);                                                         // 1001A2100000000022UN
//							
//							 IStoreCell cell =  cells.get("I25");
//							 Hashtable<String, IStoreCell> storeCellMap = cellQuser.getStoreCellsByRepID("1001A2100000000022UN");
//							 
//							 
//								MeasureDataVO[] measureDataVos = measureDataSrv.getRepData(new String[]{pubV0.getAloneID()}, new IStoreCell[]{cell});
							MeasureCache cache = IUFOCacheManager.getSingleton().getMeasureCache();
				 
							 MeasureVO  m1 =  cache.getMeasure("s8xxmblhm02sznjka2txro60evob");
							 MeasureVO  m2 =  cache.getMeasure("s8xxmblhm02sznjka2txpj2xhi0f");
							 MeasureVO  m3 =  cache.getMeasure("s8xxmblhm02sznjka2txww92t5bl");
							 
							 MeasureVO[] measures = {m1,m2,m3};
							 
				 
							 IMeasureDataSrv ser =  NCLocator.getInstance().lookup(IMeasureDataSrv.class);
							 String[] aloneIds = {pubV0.getAloneID(),pub4313001.getAloneID()};
							 
							 MeasureDataVO[]  values =  ser.getRepData(aloneIds, measures);
							 boolean haveData = false;
							 for(MeasureDataVO mData:values){
								 if(mData.getUFDoubleValue()!=null&&mData.getUFDoubleValue().doubleValue()!=0){
									 haveData = true;
								 }
							 }
							 
							 
							 if(haveData){
								 throw new RuntimeException(info);
							 }
					 
						 
					 }
					}
				
			}catch(Exception ex){
				
				if(info.equals(ex.getMessage())){
					throw ex;
				}
			}

			
			 //************end
		 
//				MeasureCache cache = IUFOCacheManager.getSingleton().getMeasureCache();
////				cache.getm
//				MeasureVO  vo = cache.getMeasure("");
			
			//风车等待……
			TPAProgressUtil tpaProgressUtil = new TPAProgressUtil();
			tpaProgressUtil.setContext(getModel().getContext());
			final DefaultProgressMonitor mon = tpaProgressUtil.getTPAProgressMonitor();
	        mon.beginTask(HBReportUnionAction.this.getBtnName() + "...", IProgressMonitor.UNKNOWN_TOTAL_TASK);
	        mon.setProcessInfo(HBReportUnionAction.this.getBtnName() + "...");
			SwingWorker<String[], Object> sw = new SwingWorker<String[], Object>() {
	            @Override 
	            protected String[] doInBackground() throws Exception {
					try {
						NCLocator.getInstance().lookup(IUnionReport.class).doUnion(reportHbVO);
                        HBReportUnionAction.this.showSuccessInfo();
					} catch (Exception e) {
						Logger.error(e.getMessage(), e);
	                    MessageDialog.showHintDlg(getModel().getContext().getEntranceUI(), nc.vo.ml.NCLangRes4VoTransl
	                                .getNCLangRes().getStrByID("pub_0", "01830002-0064")/* @res "警告" */, e.getMessage());
	                    ShowStatusBarMsgUtil.showStatusBarMsg(e.getMessage(), getModel().getContext());
					}
					return null;
	            }
	            
	            @Override
	            protected void done() {
	            	// 进度任务结束
					mon.done();
	            }
			};
			sw.execute();
        }
    }

    protected void showSuccessInfo() {
        ShowStatusBarMsgUtil.showStatusBarMsg(
                nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0279")/* @res "合并执行完毕" */,
                this.model.getContext());
    }

    public BillManageModel getModel() {
        return this.model;
    }

    public void setModel(BillManageModel model) {
        this.model = model;
    }

    public ExcuteHBReportDialog getExcutedialog() {
        if (this.excutedialog == null) {
            this.excutedialog = new ExcuteHBReportDialog(this.getModel().getContext().getEntranceUI(),
                    this.getSchemeKeyEditor(), investRelaGraphDelegator, investRelaListDelegator);
            ((SchemeKeyEditor)getSchemeKeyEditor()).loadInidvidual();
        }
        return this.excutedialog;
    }

    public void setExcutedialog(ExcuteHBReportDialog excutedialog) {
        this.excutedialog = excutedialog;
    }

    public BillForm getSchemeKeyEditor() {
        return schemeKeyEditor;
    }

    public void setSchemeKeyEditor(BillForm schemeKeyEditor) {
        this.schemeKeyEditor = schemeKeyEditor;
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