package nc.util.hbbb.input.hbreportdraft;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import nc.vo.iufo.data.MeasureDataVO;
import nc.vo.iufo.measure.MeasureVO;


/**
 * <b>Application name:</b>NC63<br>
 * <b>Application describing:</b> <br>
 * <b>Copyright:</b>Copyright &copy; 2019 用友软件股份有限公司版权所有。<br>
 * <b>Company:</b>yonyou<br>
 * <b>Date:</b>2019-3-20<br>
 * 
 * @author：王志强
 * @version 客开
 */
public class LinkEndTreeModel implements Serializable {

	/**
	 * {字段功能描述}
	 */
	private static final long serialVersionUID = -2874454833399660165L;
	private DefaultMutableTreeNode root;
	public List<MeasureVO> measures;

	public LinkEndTreeModel(DefaultMutableTreeNode root) {
		this.root = root;
	}

	public DefaultMutableTreeNode getRoot() {
		return root;
	}

	public void setRoot(DefaultMutableTreeNode root) {
		this.root = root;
	}
	
	

	public List<MeasureVO> getMeasures() {
		return measures;
	}

	public void setMeasures(List<MeasureVO> measures) {
		this.measures = measures;
	}
	
	
	
	
	
	
	

}
