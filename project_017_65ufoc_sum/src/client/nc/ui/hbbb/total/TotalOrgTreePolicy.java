package nc.ui.hbbb.total;

import nc.ms.tb.tree.ITreeBuildPolicy;
import nc.vo.hbbb.total.HbTotalOrgTreeVO;
import nc.vo.hbbb.total.HbTotalSchemeVO;

public class TotalOrgTreePolicy implements ITreeBuildPolicy{

	@Override
	public String getPkTree(Object arg0) {
		if(arg0 instanceof HbTotalOrgTreeVO){
			HbTotalOrgTreeVO vo = (HbTotalOrgTreeVO)arg0;
			return vo.getPk_org();
		}
		return String.valueOf(arg0);
	
//		return null;
	}

	@Override
	public String getPkTreeParent(Object arg0) {
		if(arg0 instanceof HbTotalOrgTreeVO){
			HbTotalOrgTreeVO vo = (HbTotalOrgTreeVO)arg0;
			if(vo.getInnercode().length()>4){
				return vo.getPk_fatherorg();
			}
		}
		return null;
	}

	@Override
	public String toShowString(Object arg0) {
		if(arg0 instanceof HbTotalOrgTreeVO){
			HbTotalOrgTreeVO vo = (HbTotalOrgTreeVO)arg0;
			return vo.getName();
		}
		return String.valueOf(arg0);
	}

}
