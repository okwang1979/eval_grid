package nc.ui.hbbb.quickquery.common.filteritem;

import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.ReportCache;
import nc.ui.iufo.query.common.filteritem.AbsFilterItemByComb;
import nc.ui.pub.beans.UIComboBox;
import nc.ui.pub.beans.constenum.DefaultConstEnum;
import nc.vo.iufo.commit.CommitStateEnum;
import nc.vo.iufo.query.IUfoQueryCondVO;
import nc.vo.iufo.repdataquery.RepDataQueryResultVO;
import nc.vo.iuforeport.rep.ReportVO;

/**
 * 内部交易表在合并执行中添加的过滤条件。 <b>Application name:</b>客开项目<br>
 * <b>Application describing:</b> <br>
 * <b>Copyright:</b>Copyright &copy; 2019 用友软件股份有限公司版权所有。<br>
 * <b>Company:</b>yonyou<br>
 * <b>Date:</b>2019-6-24<br>
 * 
 * @author：王志强
 * @version 客开
 */
public class HBBBIntrFilterItem extends AbsFilterItemByComb {

	public HBBBIntrFilterItem() {
		super();
		name = "报表类型";
		metaFieldName = RepDataQueryResultVO.REPCOMMITSTATE;
	}

	public UIComboBox getEditComponent() {
		return super.getEditComponent();
	}

	@Override
	protected DefaultConstEnum[] getComboxItems() {
		DefaultConstEnum[] items = new DefaultConstEnum[2];
		items[0] = new DefaultConstEnum(0, "财务主表");
		items[1] = new DefaultConstEnum(1, "内部交易表");

		return items;
	}

	@Override
	protected int innerGetValueFromQueryCond(
			IUfoQueryCondVO paramIUfoQueryCondVO) {
		// TODO Auto-generated method stub
		return -2;
	}

	@Override
	protected void innerSetValueFromQueryCond(
			IUfoQueryCondVO paramIUfoQueryCondVO, int paramInt) {
		// TODO Auto-generated method stub

	}

	protected boolean isAcceptResultByInt(int iInputVal, Object result) {

		if (result instanceof RepDataQueryResultVO) {
			RepDataQueryResultVO resultVo = (RepDataQueryResultVO) result;
			ReportCache repCache = IUFOCacheManager.getSingleton()
					.getReportCache();
			ReportVO repVO = repCache.getByPK(resultVo.getPk_report());
			if (repVO != null) {
				if (repVO.getIsintrade() == null) {
					 return true;
				}
				if (repVO.getIsintrade().booleanValue() == false) {
					 return iInputVal==0;
				} else {
					 return iInputVal==1; 
				}
			}
		}
		return true;

	}

	@Override
	protected int getFieldValueFromResult(Object paramObject) {
		return 0;
		// if(paramObject instanceof RepDataQueryResultVO){
		// RepDataQueryResultVO resultVo = (RepDataQueryResultVO) paramObject;
		// ReportCache repCache =
		// IUFOCacheManager.getSingleton().getReportCache();
		// ReportVO repVO = repCache.getByPK(resultVo.getPk_report());
		// if(repVO!=null){
		// if(repVO.getIsintrade()==null){
		// return 0;
		// }
		// if(repVO.getIsintrade().booleanValue()==false){
		// return 0;
		// }else{
		// return -1;
		// }
		// }
		// }
		// return 0;
		//

	}
}
