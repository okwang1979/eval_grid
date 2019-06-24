package nc.ui.hbbb.quickquery.common.filteritem;

import nc.ui.iufo.query.common.filteritem.AbsFilterItemByComb;
import nc.ui.pub.beans.constenum.DefaultConstEnum;
import nc.vo.iufo.commit.CommitStateEnum;
import nc.vo.iufo.query.IUfoQueryCondVO;

public class HBBBIntrFilterItem extends AbsFilterItemByComb {
	
	public HBBBIntrFilterItem(){
		super();
		name = "内部交易表类型";
	}


	@Override
	protected DefaultConstEnum[] getComboxItems() {
		DefaultConstEnum[] items=new DefaultConstEnum[1];
		items[0]=new DefaultConstEnum(0, "非内部交易采集表");
		 
		return items;
	}

	@Override
	protected int innerGetValueFromQueryCond(
			IUfoQueryCondVO paramIUfoQueryCondVO) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void innerSetValueFromQueryCond(
			IUfoQueryCondVO paramIUfoQueryCondVO, int paramInt) {
		// TODO Auto-generated method stub

	}

	@Override
	protected int getFieldValueFromResult(Object paramObject) {
		 
		return  -1;
	}
}
