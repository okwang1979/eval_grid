package nc.ui.hbbb.convert.avgrate.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.hbbb.convert.avgrate.IAvgRateQueryService;
import nc.itf.uif.pub.IUifService;
import nc.ui.bd.commoninfo.accperiod.view.AccperiodMthRefModel;
import nc.ui.bd.currinfo.model.CurrinfoAppModel;
import nc.ui.hbbb.convert.avgrate.model.AvgRateAppModel;
import nc.ui.hbbb.convert.avgrate.view.ComputeConditonDlg;
import nc.ui.hbbb.utils.HBBBAccPeriodUtil;
import nc.ui.iufo.dao.DAOAction_Client;
import nc.ui.pub.beans.UIDialog;
import nc.ui.pub.bill.BillModel;
import nc.ui.uif2.NCAction;
import nc.ui.uif2.UIState;
import nc.ui.uif2.editor.BillForm;
import nc.ui.uif2.model.BillManageModel;
import nc.vo.bd.currinfo.CurrinfoVO;
import nc.vo.bd.period.AccperiodVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.pub.lang.UFDate;
import nc.vo.ufoc.avgrateinfo.AvgRateVO;

//nc57 nc/util/iufo/cytranslation/ERateFuncCalcUtil.java
public class ComputeAction  extends NCAction{

	private BillManageModel model;
	private BillForm editor;
	private ManageModeActionInterceptor currinfoEditalbe;
	public ComputeAction(){
		super();
		this.setBtnName(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0164")/*@res "计算"*/);
		setCode("computeaction");
	}

	/**
	 *
	 */
	private static final long serialVersionUID = -3010573244353918566L;

	@Override
	public void doAction(ActionEvent e) throws Exception {
		
		
		//增加权限校验
		//if (getCurrinfoEditalbe().beforeDoAction(this, e)) {
			String pk_accperiod = ((AvgRateAppModel)model).getPk_accperiod();
			String pk_accperiodscheme = ((AvgRateAppModel)model).getPk_accperiodscheme();
			
			ComputeConditonDlg dlg = new ComputeConditonDlg(model.getContext().getEntranceUI(), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0165")/*@res "会计期间平均汇率录入"*/);
			dlg.initUI();
			dlg.getRateSchemeRefPane().setPK("");
			
			((AccperiodMthRefModel)dlg.getPeriodRefPane().getRefModel()).setClassWherePart("bd_accperiod.pk_accperiod='" + pk_accperiod + "' ");
			AccperiodVO accperiodVO = (AccperiodVO)DAOAction_Client.load(AccperiodVO.class, pk_accperiod);
			
			String accperiod = HBBBAccPeriodUtil.getDefaultAccPeriod(pk_accperiodscheme, KeyVO.ACC_MONTH_PK);
			if(accperiod.substring(0,4).equals(accperiodVO.getPeriodyear())) {
				dlg.getPeriodRefPane().setPK(accperiod);
			}
			else {
				dlg.getPeriodRefPane().setPK(accperiodVO.getPeriodyear() + "-" +new UFDate().toLocalString().substring(5,7));
			}
			
			dlg.getAccperiodschemeRefPane().setPK(pk_accperiodscheme);
			dlg.getAccperiodschemeRefPane().setEnabled(false);
			int showModal = dlg.showModal();
			if(showModal == UIDialog.ID_OK) {

				String month = dlg.getPeriodRefPane().getRefName();
				if(month == null) {
					return;
				}

				AvgRateVO computeRateVO  = null;
				int index = -1;
				for (int i = 0; i < ((AvgRateAppModel)model).getCurrentVOs().length; i++) {
					AvgRateVO rateVO = ((AvgRateAppModel)model).getCurrentVOs()[i];
					if(month.equals(rateVO.getRatemonth())) {
						computeRateVO = rateVO;
						index = i;
						break;
					}
				}
				if(computeRateVO == null) {
					return;
				}
				//AvgRateVO computeAResultvgRate = NCLocator.getInstance().lookup(IAvgRateQueryService.class).computeAvgRate(computeRateVO);
				
				try{
					
					String pk_exrate ="1001A210000000002YIF";
					//查询现有
					IUifService service = NCLocator.getInstance().lookup(IUifService.class	);
					AvgRateVO[] queryRates  = (AvgRateVO[])service.queryByCondition(AvgRateVO.class, "Ratemonth = '"+month+"' and pk_accperiod = '"+computeRateVO.getPk_accperiod()+"'");
					 
					 
					 Map<String ,AvgRateVO> avgMap = new HashMap<String, AvgRateVO>();
					 if(queryRates!=null&&queryRates.length>0){
						 for(AvgRateVO avgVo:queryRates){
							 avgMap.put(avgVo.getPk_currinfo(), avgVo);
						 }
					 }
					 List<AvgRateVO> filterVos = new ArrayList<AvgRateVO>();
					 //查询CurrinfoVO
					 CurrinfoVO[] currinfos  = (CurrinfoVO[])service.queryByCondition(CurrinfoVO.class,"pk_exratescheme = '"+pk_exrate+"'");
					 for(CurrinfoVO info:currinfos){
						 AvgRateVO vo = null;
						 if(avgMap.get(info.getPk_currinfo())!=null){
							 vo = avgMap.get(info.getPk_currinfo());
						 }else{
							 vo = new AvgRateVO();
							 vo.setPk_accperiod(computeRateVO.getPk_accperiod());
							 vo.setPk_accperiodmonth(computeRateVO.getPk_accperiodmonth());
							 vo.setPk_accperiodscheme(computeRateVO.getPk_accperiodscheme());
							 vo.setPk_currinfo(info.getPk_currinfo());
							 vo.setRatemonth(computeRateVO.getRatemonth());
						 }
						 filterVos.add(vo);
						 
					 }
					
					 
					 List<AvgRateVO> saveVo = new ArrayList<AvgRateVO>();
					 IAvgRateQueryService rateService = NCLocator.getInstance().lookup(IAvgRateQueryService.class);
					 for(AvgRateVO vo:filterVos){
						 try{
							 saveVo.add(rateService.computeAvgRate_gl(vo));
						 }catch(Exception ex){
							 Logger.error("计算汇率错误:"+ex.getMessage()+";Pk_currinfo is "+vo.getPk_currinfo());
						 }
					 }
					 AvgRateVO[] filterRates = filterValue(saveVo.toArray(new AvgRateVO[0]));
					 ((AvgRateAppModel)getModel()).save(filterRates);
					 ((CurrinfoAppModel)currinfoEditalbe.getModel()).setSelectedData(((AvgRateAppModel)model).getSelectedCurrinfoVO());
					 //queryRates = (AvgRateVO[])NCLocator.getInstance().lookup(IUifService.class	).queryByCondition(AvgRateVO.class, "Ratemonth = '08' and pk_accperiod = '"+computeRateVO.getPk_accperiod()+"'");
					
				}catch(Exception ex){
					
				}
				
//				AvgRateVO computeAResultvgRate = NCLocator.getInstance().lookup(IAvgRateQueryService.class).computeAvgRate_gl(computeRateVO);
//				
//			
//					
//				computeRateVO = computeAResultvgRate;
//
//				model.setUiState(UIState.EDIT);
//
//				 AvgRateVO[] vos = {computeAResultvgRate};
//				 ((AvgRateAppModel)getModel()).save(vos);
//				editor.getBillCardPanel().getBillModel().setBodyRowVO(computeRateVO, index);
//
//				//重置状态,确保save时获取到该行数据.不用管是新增还是更新,后台自动根据pk判断
//				editor.getBillCardPanel().getBillModel().setRowState(index, BillModel.MODIFICATION);
			}
	//	}
		
		
	}
	
	
	/*     */   private boolean isAllItemNull(AvgRateVO vo)
	/*     */   {
	/*  53 */     if (vo.getMonthrate() != null)
	/*  54 */       return false;
	/*  55 */     if (vo.getQuarterrate() != null)
	/*  56 */       return false;
	/*  57 */     if (vo.getHalfyearrate() != null)
	/*  58 */       return false;
	/*  59 */     if (vo.getYearrate() != null)
	/*  60 */       return false;
	/*  61 */     if (vo.getRate1() != null)
	/*  62 */       return false;
	/*  63 */     if (vo.getRate2() != null)
	/*  64 */       return false;
	/*  65 */     if (vo.getRate3() != null)
	/*  66 */       return false;
	/*  67 */     if (vo.getRate4() != null)
	/*  68 */       return false;
	/*  69 */     if (vo.getRate5() != null)
	/*  70 */       return false;
	/*  71 */     if (vo.getRate6() != null)
	/*  72 */       return false;
	/*  73 */     if (vo.getRate7() != null)
	/*  74 */       return false;
	/*  75 */     if (vo.getRate8() != null)
	/*  76 */       return false;
	/*  77 */     if (vo.getRate9() != null)
	/*  78 */       return false;
	/*  79 */     if (vo.getRate10() != null)
	/*  80 */       return false;
	/*  81 */     if (vo.getRate11() != null)
	/*  82 */       return false;
	/*  83 */     if (vo.getRate12() != null)
	/*  84 */       return false;
	/*  85 */     if (vo.getRate13() != null)
	/*  86 */       return false;
	/*  87 */     if (vo.getRate14() != null)
	/*  88 */       return false;
	/*  89 */     if (vo.getRate15() != null)
	/*  90 */       return false;
	/*  91 */     if (vo.getRate16() != null)
	/*  92 */       return false;
	/*  93 */     if (vo.getRate17() != null)
	/*  94 */       return false;
	/*  95 */     if (vo.getRate18() != null)
	/*  96 */       return false;
	/*  97 */     if (vo.getRate19() != null)
	/*  98 */       return false;
	/*  99 */     if (vo.getRate20() != null) {
	/* 100 */       return false;
	/*     */     }
	/* 102 */     return true;
	/*     */   }
	
	  private AvgRateVO[] filterValue(AvgRateVO[]  vos) {
//		  /*  36 */     AvgRateVO[] vos = (AvgRateVO[])value;
		  /*  37 */     ArrayList<AvgRateVO> returnVOs = new ArrayList();
		  /*  38 */     for (int i = 0; i < vos.length; i++) {
		  /*  39 */       if ((vos[i].getPrimaryKey() != null) || (!isAllItemNull(vos[i])))
		  /*     */       {
		  /*  41 */         vos[i].setPk_org("GLOBLE00000000000000");
		  /*  42 */         returnVOs.add(vos[i]);
		  /*     */       }
		  /*     */     }
		  /*  45 */     if (returnVOs.size() == 0) {
		  /*  46 */       return null;
		  /*     */     }
		  /*  48 */     return (AvgRateVO[])returnVOs.toArray(new AvgRateVO[returnVOs.size()]);
		  /*     */   }

	@Override
	protected boolean isActionEnable() {
		return ((AvgRateAppModel)model).getPk_accperiodscheme() != null && ((AvgRateAppModel)model).getPk_org() != null;
	}

	public BillManageModel getModel() {
		return model;
	}

	public void setModel(BillManageModel model) {
		this.model = model;
		this.model.addAppEventListener(this);
	}

	public BillForm getEditor() {
		return editor;
	}

	public void setEditor(BillForm editor) {
		this.editor = editor;
	}

	public ManageModeActionInterceptor getCurrinfoEditalbe() {
		return currinfoEditalbe;
	}

	public void setCurrinfoEditalbe(ManageModeActionInterceptor currinfoEditalbe) {
		this.currinfoEditalbe = currinfoEditalbe;
	}

}