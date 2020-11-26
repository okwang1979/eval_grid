package nc.bs.tbb.imp;

import nc.itf.mdm.dim.IDimManager;
import nc.ms.mdm.dim.DimServiceGetter;
import nc.vo.mdm.cube.DimVector;
import nc.vo.mdm.dim.DimDef;
import nc.vo.mdm.dim.DimMember;
import nc.vo.mdm.dim.IDimDefPKConst;

public class GroupDim {
	
	private DimMember entity;
	private DimMember time;
	
	
	public GroupDim(DimVector dv){
		IDimManager dm = DimServiceGetter.getDimManager();
		DimDef entityDef = dm.getDimDefByPK(IDimDefPKConst.ENT);
		DimDef timeDef = dm.getDimDefByPK(IDimDefPKConst.TIME);
		this.entity = dv.getDimMember(entityDef);
		this.time = dv.getDimMember(timeDef);
	}
	
	public DimMember getEntity() {
		return entity;
	}
	public void setEntity(DimMember entity) {
		this.entity = entity;
	}
	public DimMember getTime() {
		return time;
	}
	public void setTime(DimMember time) {
		this.time = time;
	}
	@Override
	public boolean equals(Object param) {
		
		if(param instanceof GroupDim){
			GroupDim groupDim = (GroupDim)param;
			return groupDim.getEntity().equals(this.entity)&&groupDim.getTime().equals(this.getTime());
			
		}
		return false;
	}
	@Override
	public int hashCode() {
		 
		return this.getEntity().hashCode()+this.getTime().hashCode()*37;
	}
	
	
	
	
	

}
