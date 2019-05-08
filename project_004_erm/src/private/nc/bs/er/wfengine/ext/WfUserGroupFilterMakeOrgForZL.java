package nc.bs.er.wfengine.ext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.bs.pf.usergroupmanager.UserGroupManager;
import nc.bs.pub.pf.IParticipantFilter;
import nc.bs.pub.pf.ParticipantFilterContext;
import nc.itf.uap.pf.IWfUserGroupQueryService;
import nc.itf.uap.rbac.IRoleManageQuery;
import nc.itf.uap.rbac.IUserManageQuery;
import nc.pubitf.rbac.IUserPubService;
import nc.vo.arap.pay.AggPayBillVO;
import nc.vo.ep.bx.JKBXHeaderVO;
import nc.vo.ep.bx.JKBXVO;
import nc.vo.org.OrgVO;
import nc.vo.pf.pub.util.ArrayUtil;
import nc.vo.pf.pub.util.UserUtil;
import nc.vo.pub.BusinessException;
import nc.vo.pub.workflowusergroup.WFUserGroupDetailVO;
import nc.vo.pub.workflowusergroup.WFUserGroupVO;
import nc.vo.sm.UserVO;
import nc.vo.uap.rbac.UserGroupVO;
import nc.vo.uap.rbac.role.RoleGroupVO;
import nc.vo.uap.rbac.role.RoleVO;
import nc.vo.wfengine.pub.WFTask;
import nc.vo.wfengine.pub.WfGroupType;
import nc.vo.wfengine.pub.WfUserGroupType;

import org.apache.commons.lang.StringUtils;

import nc.pub.iufo.basedoc.OrgUtil;
/**
 * 流程用户组制单组织限定类
 * @author zhouyy
 */
public class WfUserGroupFilterMakeOrgForZL implements IParticipantFilter {

	@Override
	public HashSet<String> filterUsers(ParticipantFilterContext pfc)
			throws BusinessException {
		HashSet<String> userIDs = pfc.getUserList();
		
		WFTask task = pfc.getTask();
		String orgType = task.getParticipantType();
		// 只处理流程用户组
		if(!"WFUSERGROUP".equals(orgType)) {
			return userIDs;
		}
		
		String wfUserGrouppk = task.getParticipantID();
		WFUserGroupVO wfGroupVO = UserGroupManager.getUserGroupVOByPK(wfUserGrouppk);
		// 只处理离散用户,不处理规则用户
		if(wfGroupVO.getDeftype().equals(Integer.valueOf(WfGroupType.FormulaType.getIntValue()).toString())) {
			return userIDs;
		}
		
		// 找到流程用户组配置的所有角色， 只保留属于制单组织的角色
		WFUserGroupDetailVO[] wfGroupDetailVOs = NCLocator.getInstance().lookup(IWfUserGroupQueryService.class)
				.getUserGroupDetailVOByParentPK(wfUserGrouppk);
		List<String> rolePKs = new ArrayList<String>();
		for (WFUserGroupDetailVO wfGroupDetailVO : wfGroupDetailVOs) {
			if(WfUserGroupType.Role.getValue().equals(wfGroupDetailVO.getRule_type())) {
				rolePKs.add(wfGroupDetailVO.getPk_member());
			}
		}
		if(rolePKs.isEmpty()) {
			return userIDs;
		}
		
		String pk_billorg = null;
		String pk_dept = null;
		if(pfc.getBillEntity() instanceof nc.vo.arap.basebill.BaseAggVO){
			nc.vo.arap.basebill.BaseAggVO billvo = (nc.vo.arap.basebill.BaseAggVO)pfc.getBillEntity();
			pk_billorg = (String)billvo.getParentVO().getAttributeValue("pk_org");
			pk_dept = (String)billvo.getParentVO().getAttributeValue("pk_deptid");
		}
		if(pfc.getBillEntity() instanceof JKBXVO){
			JKBXVO billvo = (JKBXVO) pfc.getBillEntity();
			pk_billorg = (String) billvo.getParentVO().getAttributeValue(JKBXHeaderVO.FYDWBM);
			//添加需求根据特定的报销单位走用户组 at:2019-4-10  by:王志强 
			//**start
			//264X 其他业务报销单    nc.bs.er.wfengine.ext.WfUserGroupFilterMakeOrgForZL
			//263x 业务借款单  nc.bs.er.wfengine.ext.WfUserGroupFilterMakeOrgForZL

			String pk_bxOrg =  (String) billvo.getParentVO().getAttributeValue("pk_org");
			if(pk_bxOrg!=null&&pk_bxOrg.trim().length()>0){
				OrgVO parentVo = OrgUtil.getOrgVOByPK("0001X110000000002RLA");
				OrgVO dmOrg = OrgUtil.getOrgVOByPK( pk_bxOrg);
				if(dmOrg!=null&&parentVo!=null){
					if(dmOrg.getInnercode().startsWith(parentVo.getInnercode())){
						pk_billorg = pk_bxOrg;
					}
				}
				 
			}
			//**end
			pk_dept = (String) billvo.getParentVO().getAttributeValue(JKBXHeaderVO.FYDEPTID);
		}
		
//		RoleVO[] roleVOs = NCLocator.getInstance().lookup(IRoleManageQuery.class).getRolesByPKs(rolePKs);
		RoleVO[] roleVOs = NCLocator.getInstance().lookup(IRoleManageQuery.class).getRolesAndAssignObjByPKs(rolePKs);
		//根据角色得到角色分配组织部门情况
		Set<String> rolePks = new HashSet<String>();
		//先找分配了部门的
		for(RoleVO roleVO : roleVOs) {
			if(roleVO.getAssignOrgPks() != null && roleVO.getAssignOrgPks().length>0){
				if(roleVO.getRole_code().endsWith("_dept")){
					for (String orgdeptid : roleVO.getAssignOrgPks()) {
						if(StringUtils.equals(pk_billorg, roleVO.getPk_org())
								&& orgdeptid.equals(pk_dept)){
							rolePks.add(roleVO.getPk_role());
							break;
						}
					}
				}
			}
		}
//		没有找到，再找同单位的
		if(rolePks.size() <= 0)
			for(RoleVO roleVO : roleVOs) {
				if(!roleVO.getRole_code().endsWith("_dept")){
					if(StringUtils.equals(pk_billorg, roleVO.getPk_org())) {
						rolePks.add(roleVO.getPk_role());
					}
				}
			}
		
		// 根据制单组织的角色重新查找用户
		List<WFUserGroupDetailVO> effectDetails = new ArrayList<WFUserGroupDetailVO>();
		for (WFUserGroupDetailVO wfGroupDetailVO : wfGroupDetailVOs) {
			if(WfUserGroupType.Role.getValue().equals(wfGroupDetailVO.getRule_type())
					&& !rolePks.contains(wfGroupDetailVO.getPk_member())) {
				continue;
			}
			effectDetails.add(wfGroupDetailVO);
		}
		
		HashSet<String> effectUserIDs = getDisperseUserByWfUserGroupID(effectDetails.toArray(new WFUserGroupDetailVO[0]), task.getParticipantBelongOrg());
		
		for(String userID : effectUserIDs) {
			if(!userIDs.contains(userID)) {
				effectUserIDs.remove(userID);
			}
		}
		
		return effectUserIDs;
	}
	
	// 拷贝nc.impl.uap.pf.WfUserGroupQueryServiceImpl.getDisperseUserByWfUserGroupID
	private HashSet<String> getDisperseUserByWfUserGroupID(WFUserGroupDetailVO[] vos, String pk_org)
			throws BusinessException {
		HashSet<String> list = new HashSet<String>();
		for (int n = 0; n < vos.length; n++) {
			/** 得到该成员的PK */
			String pk_member = ((WFUserGroupDetailVO) vos[n]).getPk_member();
			/** 得到该成员的类型,是角色,还是角色组,还是用户和用户组 */
			String pk_type = ((WFUserGroupDetailVO) vos[n]).getRule_type();
			// USERTYPE = "01"; USERGROUPTYPE = "02"; ROLETYPE = "03";
			// ROLEGROUPTYPE = "04";
			/** 用户 */
			IUserManageQuery userManageQueryService = (IUserManageQuery) NCLocator
					.getInstance().lookup(IUserManageQuery.class.getName());
			IRoleManageQuery roleManageQueryService = (IRoleManageQuery) NCLocator
					.getInstance().lookup(IRoleManageQuery.class.getName());
			if (pk_type.equals(WfUserGroupType.User.getValue())) {
				UserVO user = userManageQueryService.getUser(pk_member);
				if (UserUtil.isUserEnable(user)) {
					list.add(user.getCuserid());
				}
			} else if (pk_type.equals(WfUserGroupType.UserGroup.getValue())) {
				UserGroupVO groupVO = (UserGroupVO) new BaseDAO().retrieveByPK(
						UserGroupVO.class, pk_member);
				if (groupVO != null) {
					UserVO[] users = userManageQueryService
							.queryAllUserinUserGroup(groupVO.getPk_usergroup(),
									true, false);
					addUserPksToSet(list, users);
				}
			} else if (pk_type.equals(WfUserGroupType.Role.getValue())) {
				UserVO[] users = userManageQueryService.queryUserByRole(
						pk_member, pk_org);
				addUserPksToSet(list, users);
			} else if (pk_type.equals(WfUserGroupType.RoleGroup.getValue())) {
				RoleGroupVO rolegroups = roleManageQueryService
						.queryRoleGroupByID(pk_member);
				RoleVO[] roles = rolegroups.getRoles();
				for (int a = 0; a < roles.length; a++) {
					UserVO[] users = userManageQueryService.queryUserByRole(
							roles[a].getPk_role(), pk_org);
					addUserPksToSet(list, users);
				}
			} else if (pk_type.equals(WfUserGroupType.Person.getValue())) {
				IUserPubService userPubService = NCLocator.getInstance()
						.lookup(IUserPubService.class);
				UserVO uvo = userPubService.queryUserVOByPsnDocID(pk_member);

				if (uvo != null) {
					addUserPksToSet(list, uvo);
				}
			}

		}
		return list;
	}
	
	private void addUserPksToSet(Set<String> userSet, UserVO... users) {
		users = UserUtil.filtDisableUsers(users);
		if (!ArrayUtil.isNull(users)) {
			for (int b = 0; b < users.length; b++) {
				userSet.add(users[b].getCuserid());
			}
		}
	}
}
