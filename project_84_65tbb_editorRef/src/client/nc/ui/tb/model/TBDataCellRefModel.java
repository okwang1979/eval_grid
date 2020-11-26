package nc.ui.tb.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.tree.DefaultTreeModel;

import nc.ms.mdm.dim.DimServiceGetter;
import nc.ui.bd.ref.AbstractRefTreeModel;
import nc.ui.bd.ref.ExTreeNode;
import nc.ui.pub.beans.ValueChangedEvent;
import nc.vo.logging.Debug;
import nc.vo.mdm.dim.DimDefType;
import nc.vo.mdm.dim.DimLevel;
import nc.vo.mdm.dim.DimMember;
import nc.vo.mdm.dim.LevelValue;
import nc.vo.mdm.dim.MeasureUtil;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;
import nc.vo.tb.form.excel.ExVarDef;
import nc.vo.tb.obj.LevelValueOfDimLevelVO;

public class TBDataCellRefModel extends AbstractRefTreeModel{

	private List<DimMember> dimMembers = null;

	private Map<String,DimMember> dataMap = new HashMap<String,DimMember>();
	//对于指标的不同档案所做的缓存
	private Map<String,Vector<Vector>> docDataMap = new HashMap<String,Vector<Vector>>();
	//字典列表
	private List<String> dicStrList = new ArrayList<String>();
	private String displayDocName = null;
	private String rootName = null;
	private String canSelectLevelCode = null;

	private LevelValueOfDimLevelVO levelValueOfDimLevelVO;
	private String pk_user;
	private String pk_group;
	private String cubeCode;
	private ExVarDef exVarDef;
	private String parentKey;
	private String filterRefNodeName = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_bean_0","01050bean002-0000")/*@res "指标档案"*/;
	private Map<DimLevel, LevelValue> dvMap;



	public TBDataCellRefModel(String refNodeName) {
		setRefNodeName(refNodeName);
	}

	public void setRefNodeName(String refNodeName) {
		m_strRefNodeName = refNodeName;
		// *根据需求设置相应参数
		setFieldCode(new String[] { "objcode", "objname","","","" });
		setFieldName(new String[] {
				NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_integration",
						"01420int_000037")/* 编码 */,
				NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_integration",
						"01420int_000038") /* 名称 */});
		setHiddenFieldCode(new String[] { "pk_obj" });
		setPkFieldCode("pk_obj");
		setRefCodeField("objcode");
		setRefNameField("objname");
		setChildField("pk_obj");
		setFilterRefNodeName(new String[] { getFilterRefNodeName() });/*-=notranslate=-*/
		// 使用启用条件
		setAddEnableStateWherePart(false);
		resetFieldName();
	}

	@Override
	public DefaultTreeModel getTreeModel() {
		return constructModel();
	}
	public String getFilterRefNodeName() {
		return filterRefNodeName;
	}

	public void setFilterRefNodeName(String filterRefNodeName) {
		this.filterRefNodeName = filterRefNodeName;
	}
	 /**
	 * 构造树的Model
	 */
	private DefaultTreeModel constructModel() {
		Vector vAllTreeNode = new Vector();
		int[] cols = {0, 1};

//	        createTreeNode(root,docNames);
        Vector vecData = null;
        if(this.displayDocName != null){
        	getData();
        	vecData= docDataMap.get(displayDocName);
        }else{
        	vecData= getData();
      }
        ExTreeNode root = new ExTreeNode(rootName, true);
        HashMap<String, ExTreeNode> hmPk2Node = new HashMap<String, ExTreeNode>();
        HashMap<String,ExTreeNode> hm=new HashMap<String,ExTreeNode>();
        root.setIsCanSelected(false);
		DefaultTreeModel tm = new DefaultTreeModel(root, false);
        if(vecData == null){
        	return tm;
        }
        for(int i=0;i<vecData.size();i++){
        	Vector row = (Vector) vecData.elementAt(i);
        	ExTreeNode nodepar = new ExTreeNode(row,cols);
        	nodepar.setMainClass(true);
			nodepar.setIsCanSelected(true);
			vAllTreeNode.add(nodepar);
			if(row.elementAt(2)!=null&&!row.elementAt(2).equals(""))
			 {
				hm.put(row.elementAt(2).toString(), nodepar);
			 }
			if(row.elementAt(3)==null){
				hmPk2Node.put(null, nodepar);
			}else{
				hmPk2Node.put(row.elementAt(3).toString(), nodepar);
			}
        }
        for(int i=0;i<vecData.size();i++){
        	ExTreeNode nodepar = (ExTreeNode) vAllTreeNode.get(i);
        	Vector row = (Vector) vecData.elementAt(i);
        	String fathreCodeValue =  row.elementAt(3) == null? null :row.elementAt(3).toString();
        	Object pk = row.elementAt(2);
        	pk = pk == null ? "" : pk;
        	String docName=row.elementAt(4).toString();
        	getModelPkToNode().put(pk, nodepar);
        	if(MeasureUtil.getMeasureName(docName)==null||MeasureUtil.getMeasureName(docName).equals("")){
        		String levelCode = row.elementAt(6).toString();
        		if(levelCode != null ){
        			if(canSelectLevelCode == null){
        				nodepar.setIsCanSelected(true);
        			}else{
        				if(canSelectLevelCode.equals(levelCode)){
        					nodepar.setIsCanSelected(true);
        				}else{
        					nodepar.setIsCanSelected(false);
        				}
        			}
        		}
        		if (fathreCodeValue == null ) {
	        		root.insert(nodepar, root.getChildCount());
				} else {
					ExTreeNode nodeparFather = (ExTreeNode) hm.get(fathreCodeValue);
					if (nodeparFather == null||nodeparFather==nodepar) {
						Debug.debug("to find father error:" + fathreCodeValue + ":"
								+ nodepar);
						// 插入到根节点
						root.insert(nodepar, root.getChildCount());
					} else {
						nodeparFather
								.insert(nodepar, nodeparFather.getChildCount());
					}
				}
        	}else{
        		int count=0;
        		Vector v=new Vector();
				v.add("");
				v.add(MeasureUtil.getMeasureName(docName));
				v.add("");
				v.add("");
				v.add(docName);
				ExTreeNode newNode = new ExTreeNode(
						v, cols);
//	        		ExTreeNode newNode=new ExTreeNode(MeasureUtil.getMeasureName(docName),true);
        		newNode.setIsCanSelected(false);
        		int treeChild=root.getChildCount();
        		if(treeChild==0)
        			root.insert(newNode, root.getChildCount());
        		else{
        			for(int j=0;j<treeChild;j++){
        				if((newNode.toString()).equals(root.getChildAt(j).toString())){
        					newNode=(ExTreeNode) root.getChildAt(j);
        					continue;
        				}else{
        					count++;
        				}
        			}
        			if(count==treeChild){
        				root.insert(newNode, root.getChildCount());
        				count=0;
        			}
        		}
	        	if (fathreCodeValue == null ) {
	        		newNode.insert(nodepar, newNode.getChildCount());
				} else {
					ExTreeNode nodeparFather = (ExTreeNode) hm.get(fathreCodeValue);
					if (nodeparFather == null||nodeparFather==nodepar) {
						Debug.debug("to find father error:" + fathreCodeValue + ":"
								+ nodepar);
						// 插入到根节点
						newNode.insert(nodepar, newNode.getChildCount());
					} else {
						nodeparFather
								.insert(nodepar, nodeparFather.getChildCount());
					}
				}
        	}
        }
		return tm;
	}


	@Override
	public Vector getData(){
		docDataMap.clear();
		Vector vectorLsit = new Vector();
		if(dimMembers != null){
			for(DimMember dimMember :dimMembers){
				Vector dataVector = new Vector();
				//设置UniqCode
				dataVector.add(dimMember.getLevelValue().getUniqCode());
				//设置名称
				dataVector.add(dimMember.getLevelValue().getName());

			    //设置本级TreeKey
			    DimDefType dimType = dimMember.getDimDef().getDimType();
//			    if(dimType == DimDefType.ENTITY /*|| dimType == DimDefType.ENTITY_OP*/){
//		    		 dataVector.add(dimMember.getLevelValue().getTreeKey());
//		    		 dataMap.put((String)(dimMember.getLevelValue().getTreeKey()), dimMember);
//
//			     }else if(dimType == DimDefType.TIME){
//		    		 dataVector.add(dimMember.getUniqKey());
//		    		 dataMap.put((String)(dimMember.getUniqKey()), dimMember);
//
//			     }else{
			    	 dataVector.add(dimMember.getUniqKey());
			    	 dataMap.put(dimMember.getUniqKey(), dimMember);
//			     }

			     //设置上级TreeKey即ParentKey
			    if(dimMember.getParentMember() != null){
			    	dataVector.add(dimMember.getParentMember().getUniqKey());
			     }else{
			    	 dataVector.add(null);
			     }
			    //档案表名
			    if(dimMember.getLevelValue().getDocName()==null||(dimMember.getLevelValue().getDocName().isEmpty())){
			    	dataVector.add("");
			     }else{
			    	 if(dimType == DimDefType.MEASURE){
			    		 if(docDataMap.get(dimMember.getLevelValue().getDocName()) != null){
				    		 docDataMap.get(dimMember.getLevelValue().getDocName()).add(dataVector);
				    	 }else{
				    		 Vector<Vector> tempList = new Vector<Vector>();
				    		 tempList.add(dataVector);
				    		 docDataMap.put(dimMember.getLevelValue().getDocName(), tempList);
				    	 }
			    	 }else{
			    		 if(docDataMap.get(dimMember.getDimLevel().getObjCode()) != null){
				    		 docDataMap.get(dimMember.getDimLevel().getObjCode()).add(dataVector);
				    	 }else{
				    		 Vector<Vector> tempList = new Vector<Vector>();
				    		 tempList.add(dataVector);
				    		 docDataMap.put(dimMember.getDimLevel().getObjCode(), tempList);
				    	 }
			    	 }
			    	 dataVector.add(dimMember.getLevelValue().getDocName());
			     }
			    dataVector.add(dimMember);
			    dataVector.add(dimMember.getDimLevel().getBusiCode());
			    vectorLsit.add(dataVector);
			}
		}else if(dicStrList != null){
			if(dicStrList != null){
				for (String str : dicStrList) {
					if(str != null){
						Vector data = new Vector();
						data.add(str);
						data.add(str);
						data.add(str);
						data.add(str);
						data.add("");
						data.add(str);
						data.add(str);
						vectorLsit.add(data);
					}
				}
			}
		}
		return vectorLsit;
	}

	public void setDimMembers(List<DimMember> dimMembers) {
		this.dimMembers = dimMembers;
	}

	public void setDisplayDocName(String displayDocName) {
		this.displayDocName = displayDocName;
	}

	public void setRootName(String rootName) {
		this.rootName = rootName;
	}

	public void setCanSelectLevelCode(String canSelectLevelCode) {
		this.canSelectLevelCode = canSelectLevelCode;
	}

	public DimMember getDimMember(String pkKey){
		return dataMap == null ? null:dataMap.get(pkKey);
	}

	public void setDicStrList(List<String> dicStrList) {
		this.dicStrList = dicStrList;
	}

	@Override
	public void filterValueChanged(ValueChangedEvent changedValue) {
		Object object = changedValue.getNewValue();
		if(object != null){
			String pk = ((String[])object)[0].toString();
			if(pk != null){
				if(this.rootName.equals(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_bean_0","01050bean002-0002")/*@res "部门"*/)){
					try {
						List<DimMember> dimMembers = DimServiceGetter.getVarMemberService().getVarMemberByTask(new LevelValueOfDimLevelVO[]{levelValueOfDimLevelVO}, cubeCode,pk_user,pk_group,exVarDef.mesType == null ? null:new String[]{exVarDef.mesType.name()},pk,dvMap);
					//	List<DimMember> dimMembers = DimServiceGetter.getVarMemberService().getVarMemberByTask(new LevelValueOfDimLevelVO[]{this.levelValueOfDimLevelVO}, this.cubeCode,this.pk_user,this.pk_group,null,pk);
						setDimMembers(dimMembers);
					} catch (BusinessException e) {
						NtbLogger.print(e.getMessage());
					}

				}else{
					displayDocName = MeasureUtil.getTableNameByPk(pk);
				}

			}
		}
		setPk_org(null);
	}

	public void setRaradims(LevelValueOfDimLevelVO levelValueOfDimLevelVO,
			String cubeCode, String pk_user, String pk_group, ExVarDef exVarDef,
			String parentKey,Map<DimLevel, LevelValue> dvMap) {
		this.levelValueOfDimLevelVO=levelValueOfDimLevelVO;
		this.cubeCode=cubeCode;
		this.pk_user=pk_user;
		this.pk_group=pk_group;
		this.exVarDef=exVarDef;
		this.parentKey=parentKey;
		this.dvMap=dvMap;

	}



}