package nccloud.web.platform.attachment.action;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import nc.bs.pub.filesystem.IQueryFolderTreeNodeService;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;
import nc.vo.pub.filesystem.NCFileNode;
import nccloud.framework.core.exception.ExceptionUtils;
import nccloud.framework.core.json.IJson;
import nccloud.framework.service.ServiceLocator;
import nccloud.framework.web.action.itf.ICommonAction;
import nccloud.framework.web.container.IRequest;
import nccloud.framework.web.container.RequestSysJsonVO;
import nccloud.framework.web.container.SessionContext;
import nccloud.framework.web.json.JsonFactory;
import nccloud.web.platform.attachment.vo.LeftTreeNodeQueryVO;
import nccloud.web.platform.attachment.vo.LeftTreeNodeViewVO;

public class TreeNodeQueryAction implements ICommonAction {
	public Object doAction(IRequest request) {
		
		String str = request.read();
		IJson json = JsonFactory.create();
		LeftTreeNodeQueryVO para = (LeftTreeNodeQueryVO) json.fromJson(str, LeftTreeNodeQueryVO.class);
		if (para.getBillId() == null || para.getBillId().length() == 0) {
			ExceptionUtils
					.wrapBusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("1501003_0", "01501003-0117"));
		}

		IQueryFolderTreeNodeService ncservice = (IQueryFolderTreeNodeService) ServiceLocator
				.find(IQueryFolderTreeNodeService.class);
		NCFileNode rootNode = null;
		NCFileNode node = null;

		try {
			node = ncservice.getNCFileNodeTreeAndCreateAsNeed(para.getBillId(),
					SessionContext.getInstance().getClientInfo().getUserid());
			rootNode = this.findNode(node, para.getBillId());
		} catch (BusinessException var9) {
			ExceptionUtils.wrapException(var9);
		}
		String funCode = request.getFunCode();
		RequestSysJsonVO readSysParam = request.readSysParam();
		String appcode = readSysParam.getAppcode();
		if("400600200".equals(appcode) || "400400604".equals(appcode)) {
			LeftTreeNodeViewVO treeNode = this.specialConvert(rootNode, para.getBillId());
			return treeNode;
		}
		else {
			LeftTreeNodeViewVO treeNode = this.convert(rootNode, para.getBillId());
			return treeNode;
		}
	}

	private LeftTreeNodeViewVO specialConvert(NCFileNode node, String rootPath) {
		if (node == null) {
			return null;
		} else {
			LeftTreeNodeViewVO treeNode = new LeftTreeNodeViewVO();
			treeNode.setFullPath(node.getFullPath());
			if (rootPath.equals(node.getFullPath())) {
				treeNode.setLabel(NCLangRes4VoTransl.getNCLangRes().getStrByID("1501002_0", "01501002-0293"));
			} else {
				treeNode.setLabel(node.getName());
			}

//			if (node.getChildCount() != 0) {
				List<LeftTreeNodeViewVO> volist = new ArrayList();
//				Enumeration e = node.children();
//				while (e.hasMoreElements()) {
//					volist.add(this.convert((NCFileNode) e.nextElement(), rootPath));
//				}
				LeftTreeNodeViewVO vo1 = new LeftTreeNodeViewVO();
				vo1.setLabel("合同正文");
				vo1.setFullPath(node.getFullPath() + "/合同正文");
				volist.add(vo1);
				LeftTreeNodeViewVO vo2 = new LeftTreeNodeViewVO();
				vo2.setLabel("合同审批单");
				vo2.setFullPath(node.getFullPath() + "/合同审批单");
				volist.add(vo2);
				LeftTreeNodeViewVO vo3 = new LeftTreeNodeViewVO();
				vo3.setLabel("我方授权委托书");
				vo3.setFullPath(node.getFullPath() + "/我方授权委托书");
				volist.add(vo3);
				LeftTreeNodeViewVO vo4 = new LeftTreeNodeViewVO();
				vo4.setLabel("对方授权委托书");
				vo4.setFullPath(node.getFullPath() + "/对方授权委托书");
				volist.add(vo4);
				
				LeftTreeNodeViewVO vo5 = new LeftTreeNodeViewVO();
				vo5.setLabel("合同签署文本");
				vo5.setFullPath(node.getFullPath() + "/合同签署文本");
				volist.add(vo5);
				
				LeftTreeNodeViewVO vo6 = new LeftTreeNodeViewVO();
				vo6.setLabel("中标通知书");
				vo6.setFullPath(node.getFullPath() + "/中标通知书");
				volist.add(vo6);
				
				LeftTreeNodeViewVO vo7 = new LeftTreeNodeViewVO();
				vo7.setLabel("其它");
				vo7.setFullPath(node.getFullPath() + "/其它");
				volist.add(vo7);
				
				treeNode.setChildren((LeftTreeNodeViewVO[]) volist.toArray(new LeftTreeNodeViewVO[0]));
//			} 
//			else {
//				treeNode.setChildren(new LeftTreeNodeViewVO[0]);
//			}

			return treeNode;
		}
	}
	private LeftTreeNodeViewVO convert(NCFileNode node, String rootPath) {
		if (node == null) {
	         return null;
	      } else {
	         LeftTreeNodeViewVO treeNode = new LeftTreeNodeViewVO();
	         treeNode.setFullPath(node.getFullPath());
	         if (rootPath.equals(node.getFullPath())) {

	            treeNode.setLabel(NCLangRes4VoTransl.getNCLangRes().getStrByID("1501002_0", "01501002-0293"));


	         } else {
	            treeNode.setLabel(node.getName());
	         }

	         if (node.getChildCount() != 0) {
	            List<LeftTreeNodeViewVO> volist = new ArrayList();
	            Enumeration e = node.children();
	            while(e.hasMoreElements()) {
	               volist.add(this.convert((NCFileNode)e.nextElement(), rootPath));
	            }
	            treeNode.setChildren((LeftTreeNodeViewVO[])volist.toArray(new LeftTreeNodeViewVO[0]));

	         } else {
	            treeNode.setChildren(new LeftTreeNodeViewVO[0]);
	         }
	         return treeNode;
	      }
	   }
	private NCFileNode findNode(NCFileNode node, String path) {
		Enumeration<NCFileNode> enumer = node.breadthFirstEnumeration();
		NCFileNode retrNode = null;

		while (enumer.hasMoreElements()) {
			NCFileNode tempNode = (NCFileNode) enumer.nextElement();
			if (tempNode.getFullPath().equals(path)) {
				retrNode = tempNode;
				break;
			}
		}

		return retrNode;
	}
}