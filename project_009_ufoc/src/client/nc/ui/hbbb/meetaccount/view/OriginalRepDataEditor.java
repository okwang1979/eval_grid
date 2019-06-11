package nc.ui.hbbb.meetaccount.view;

import java.util.HashMap;
import java.util.Map;

import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.ReportCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.ui.iufo.input.control.base.AbsRepDataControler;
import nc.ui.iufo.input.ufoe.comp.AbsCombRepDataEditor;
import nc.ui.iufo.input.ufoe.comp.AbsRepDataEditorInComb;
import nc.ui.iufo.input.view.base.AbsKeyCondPanel.ShowMode;
import nc.util.hbbb.HBKeyGroupUtil;
import nc.util.hbbb.input.OriginalTableInputActionHandler;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iuforeport.rep.ReportVO;

import com.ufida.zior.console.ActionHandler;
import com.ufsoft.iuforeport.repdatainput.RepDataOperResultVO;

@SuppressWarnings("restriction")
public class OriginalRepDataEditor extends AbsRepDataEditorInComb {
	private static final long serialVersionUID = -8855857472285407635L;
	private MeasureVO mesrureVO;// 联查过来取数的指标

	public OriginalRepDataEditor(String strRepPK, AbsCombRepDataEditor parentEditor, MeasureVO mesrureVO) {
		super(strRepPK, parentEditor);
		this.mesrureVO = mesrureVO;
	}

	@Override
	protected RepDataOperResultVO innerOpenRepDataResult(String strBalCondPK) {
		OriginalRepDataControler controler = (OriginalRepDataControler) AbsRepDataControler.getInstance(getMainboard());
		//当动态区关键字不是对方单位编码时
		KeyGroupVO subKeyVo = IUFOCacheManager.getSingleton().getKeyGroupCache().getByPK(mesrureVO.getKeyCombPK());
		String reportPK = getRepDataParam().getReportPK();
		ReportCache repCache = UFOCacheManager.getSingleton().getReportCache();
		ReportVO repvo = repCache.getByPK(reportPK);
		
		String[] pk_dynvalues = HBKeyGroupUtil.getPk_dynKeyValues(subKeyVo,repvo.getPk_key_comb());
//		String pk_dynvalue = HBKeyGroupUtil.getPk_dynKeyValue(subKeyVo,repvo.getPk_key_comb());
		
		Map<String,String> values = new HashMap<>();
		
		for(String pk:pk_dynvalues){
			 Object value =  getMainboard().getContext().getAttribute(pk);
			 if(value!=null){
				 values.put(pk, String.valueOf(value));
			 }
		}
		
		Object[] objParams = {getRepDataParam(), controler.getLoginEnv(getMainboard()), strBalCondPK, 
				null, controler.isBFreeTotal(),mesrureVO,values};
		return (RepDataOperResultVO) ActionHandler.execWithZip(OriginalTableInputActionHandler.class.getName(), "proxyopenRepData", objParams);
	}

	@Override
	public boolean isDirty() {
		ShowMode showMode = this.getParentEditor().getKeyCondPane().getShowMode();
		if (showMode == ShowMode.TOTAL)
			return false;
		else
			return super.isDirty();
	}

	public MeasureVO getMesrureVO() {
		return mesrureVO;
	}

	public void setMesrureVO(MeasureVO mesrureVO) {
		this.mesrureVO = mesrureVO;
	}

}
