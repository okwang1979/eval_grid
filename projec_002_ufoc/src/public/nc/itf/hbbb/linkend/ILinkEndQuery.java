package nc.itf.hbbb.linkend;

import java.util.Map;

import nc.vo.pub.BusinessRuntimeException;

/** 
 * 联查后天使用的远程查询接口
 * <b>Application name:</b>客开项目<br>
 * <b>Application describing:</b> <br>
 * <b>Copyright:</b>Copyright &copy; 2019 用友软件股份有限公司版权所有。<br>
 * <b>Company:</b>yonyou<br>
 * <b>Date:</b>2019-3-21<br>
 * @author：王志强
 * @version 客开
 */ 
public interface ILinkEndQuery {
 
	/**
	 * 查询指定主体的所有下级
	 * 
	 * @param pk_svid：版本
	 * @param parent_innercode：上级innercode
	 * @param level：查询级次。最小是1，可以为null，null查询所有下级
	 * @return
	 * @throws BusinessRuntimeException
	 * @author: 王志强
	 */
	public Map<String,String> getUnionOrgsOrderByCode(String pk_svid,String parent_innercode,Integer level) throws BusinessRuntimeException ;
	
	
	/**
	 * 查询指定InnerCodes的排序map,如果Idx为Null不加入。
	 * 
	 * @param innerCodes
	 * @return
	 * @throws BusinessRuntimeException
	 * @author: 王志强
	 */
	public Map<String,Integer> getOrgIndex(String pk_svid,String[] innerCodes)throws BusinessRuntimeException ;

}
