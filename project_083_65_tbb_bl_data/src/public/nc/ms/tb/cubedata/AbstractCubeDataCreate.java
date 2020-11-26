package nc.ms.tb.cubedata;

import java.util.ArrayList;
import java.util.List;

import nc.bs.logging.Logger;
import nc.ms.mdm.dim.DimServiceGetter;
import nc.ms.tb.formula.core.CubeHelper;
import nc.vo.mdm.cube.CubeDef;
import nc.vo.mdm.cube.DimVector;
import nc.vo.mdm.dim.DimLevel;
import nc.vo.mdm.dim.DimMember;
import nc.vo.mdm.dim.IDimLevelCodeConst;
import nc.vo.mdm.dim.LevelValue;
import nc.vo.pub.BusinessRuntimeException;

public  abstract class AbstractCubeDataCreate {
	
	private CubeDef cd;
	
	public AbstractCubeDataCreate(String cubeCode){
		
		CubeDef cubeDef = CubeHelper.getCubeDefByCode(cubeCode);
		if(cubeCode!=null){
			cd = cubeDef;
		}else{
			throw new BusinessRuntimeException("��ѯcube���ݴ���");
		}
		
	}
	
	public  DimVector getDimVector(String entity,String year,String month,List<CreateMemberInfo> others){
		
		List<DimMember> members = new ArrayList<>();
		//Ĭ��,�汾,����,Ŀ�����,ҵ�񷽰�
		List<DimMember> defMembers = getDefMembers();
		members.addAll(defMembers);
		
		
		
		
 		DimVector dv = new DimVector(members);
 		return dv;
		
	}
	//����
	public DimMember getEnMember(String memberLevel){
		return getMemberByCode(IDimLevelCodeConst.ENTITY,memberLevel,null);
		
	}
	//���·��ص�dimMember,������Ҫ�Լ�ʵ��.
	public DimMember timeMember(String year,String month,String ...others){
		
		 
		DimLevel yearDl = DimServiceGetter.getDimManager().getDimLevelByBusiCode("YEAR");
		LevelValue yearLv = yearDl.getLevelValueByCode(year);
		if(yearLv==null){
			throw new BusinessRuntimeException("����YEAR��Ա����  user value is :"+year);
		}
		LevelValue monthLv = null;
		try{
			if(month!=null&&Integer.valueOf(month)>0){
				DimLevel monthDl = DimServiceGetter.getDimManager().getDimLevelByBusiCode("MONTH");
				monthLv = monthDl.getLevelValueByCode(month);
			}
		}catch(Exception ex){
			
		
		}
		DimMember timeMember = null;
		if(month!=null){
			 timeMember = cd.getDimHierarchy(yearDl.getDimDef()).getDimMemberByLevelValues(yearLv,monthLv);
		}else{
			 timeMember = cd.getDimHierarchy(yearDl.getDimDef()).getDimMemberByLevelValues(yearLv);
		}
		if(timeMember==null){
			throw new BusinessRuntimeException("����YEAR��Ա����  user value is :"+year);
		}
			
		
		return timeMember;
		
		
	}
	
	public List<DimMember> getDefMembers(){
		
		List<DimMember> members = new ArrayList<>();
		
		members.add(getMemberByCode("VERSION",null,"v0"));
		
		
		members.add(getMemberByCode("CURR",null,"CNY"));
		members.add(getMemberByCode("AIMCURR",null,"CNY"));
		members.add(getMemberByCode("MVTYPE",null,"Budget"));
		return members;
		
	}
	
	
	private DimMember getMemberByCode(String levelCode,String levelValue,String defCode){
		DimMember member = null;
		try{
			 
			DimLevel dl = DimServiceGetter.getDimManager().getDimLevelByBusiCode(levelCode);

			LevelValue lv = dl.getLevelValueByCode(levelValue);
			if(lv==null){
				lv = dl.getLevelValueByUniqCode(levelValue);
			}
		
			if(lv==null){
				lv = dl.getLevelValueByKey(levelValue);
			}
			if(lv==null){
				if(defCode==null){
					return null;	
				}else{
					lv = dl.getLevelValueByCode(defCode);
				}
				
			}
			if(lv==null){
				return null;
			}
			member = cd.getDimHierarchy(dl).getMemberReader().getMemberByLevelValues(lv);
		 
		}catch(Exception ex){
			String errInfo = "��ѯά�ȣ�"+levelCode+",���ֳ�Ա("+levelValue+")����"+ex.getMessage();
			Logger.error(errInfo,ex);
			throw new BusinessRuntimeException(errInfo,ex);
		}
	
		
		return member;
		
	}
	

}
