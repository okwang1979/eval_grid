package nc.util.hbbb.input.hbreportdraft;

import java.io.Serializable;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import nc.vo.iufo.data.MeasureDataVO;


/**
 * <b>Application name:</b>NC63<br>
 * <b>Application describing:</b> <br>
 * <b>Copyright:</b>Copyright &copy; 2019 ��������ɷ����޹�˾��Ȩ���С�<br>
 * <b>Company:</b>yonyou<br>
 * <b>Date:</b>2019-3-20<br>
 * 
 * @author����־ǿ
 * @version �Ϳ�
 */
public class LinkEndTreeModel implements Serializable {

	/**
	 * {�ֶι�������}
	 */
	private static final long serialVersionUID = -2874454833399660165L;
	private DefaultMutableTreeNode root;

	public LinkEndTreeModel(DefaultMutableTreeNode root) {
		this.root = root;
	}

	public DefaultMutableTreeNode getRoot() {
		return root;
	}

	public void setRoot(DefaultMutableTreeNode root) {
		this.root = root;
	}
	
	
	

}
