package nc.impl.iufo.storecell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import com.ufida.iufo.pub.tools.AppDebug;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.itf.iufo.storecell.IStoreCellPackQrySrv;
import nc.jdbc.framework.SQLParameter;
import nc.pub.iufo.cache.KeyGroupCache;
import nc.pub.iufo.data.thread.AbstractQueryData;
import nc.pub.iufo.data.thread.IufoThreadLocalUtil;
import nc.pub.iufo.exception.UFOSrvException;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.storecell.IStoreCell;
import nc.vo.iufo.storecell.StoreCellPackVO;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.pub.BusinessRuntimeException;
/**
 * 存储单元服务实现类
 * @author yp
 *
 */
public class StoreCellPackSrvImpl implements IStoreCellPackQrySrv{

	
	@SuppressWarnings("unchecked")
	@Override
	public Hashtable<String, StoreCellPackVO> getStoreCellPackByRepID(
			String pk_report) throws UFOSrvException {
		
		
		
		final String finalKey = pk_report;
    	String key = "nc.impl.iufo.storecell.StoreCellPackSrvImpl  getStoreCellPackByRepID | "+finalKey;
    	Hashtable<String, StoreCellPackVO>   table = (Hashtable<String, StoreCellPackVO>  )IufoThreadLocalUtil.getValue(key, new AbstractQueryData() {
					
					@Override
					public Object qqueryData() {
						BaseDAO dao = new BaseDAO();
						Hashtable<String, StoreCellPackVO>  table = new Hashtable<String, StoreCellPackVO>();
						try {
							SQLParameter param = new SQLParameter();
							param.addParam(finalKey);
				
							Collection<StoreCellPackVO> results = (Collection<StoreCellPackVO>) dao
									.retrieveByClause(StoreCellPackVO.class, "pk_report = ?",param);
							for (StoreCellPackVO packVo : results) {
								table.put(packVo.getPk_keygroup(), packVo);
								fillStoreCellRepPK(packVo);
							}
						} catch (Exception e) {
							AppDebug.debug(e);
							throw new BusinessRuntimeException(e.getMessage(),e);
						}
						return table;
					}
				});
    	return table;
		
		
		
//		BaseDAO dao = new BaseDAO();
//		Hashtable<String, StoreCellPackVO>  table = new Hashtable<String, StoreCellPackVO>();
//		try {
//			SQLParameter param = new SQLParameter();
//			param.addParam(pk_report);
//
//			Collection<StoreCellPackVO> results = (Collection<StoreCellPackVO>) dao
//					.retrieveByClause(StoreCellPackVO.class, "pk_report = ?",param);
//			for (StoreCellPackVO packVo : results) {
//				table.put(packVo.getPk_keygroup(), packVo);
//				fillStoreCellRepPK(packVo);
//			}
//		} catch (DAOException e) {
//			AppDebug.debug(e);
//			throw new UFOSrvException(e.getMessage(),e);
//		}
//		return table;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Hashtable<String, IStoreCell> getStoreCellsByRepID(String pk_report)
			throws UFOSrvException {
	
    	final String finalKey = pk_report;
    	String key = "nc.impl.iufo.storecell.StoreCellPackSrvImpl  pk_report | "+finalKey;
    	Hashtable<String, IStoreCell>  table = (Hashtable<String, IStoreCell> )IufoThreadLocalUtil.getValue(key, new AbstractQueryData() {
					
					@Override
					public Object qqueryData() {
						BaseDAO dao = new BaseDAO();
						Hashtable<String, IStoreCell>  table = new Hashtable<String,IStoreCell>();
						try {
							SQLParameter param = new SQLParameter();
							param.addParam(finalKey);
				
							Collection<StoreCellPackVO> results = (Collection<StoreCellPackVO>) dao
									.retrieveByClause(StoreCellPackVO.class, "pk_report = ?",param);
							for (StoreCellPackVO packVo : results) {
								fillStoreCellRepPK(packVo);
								table.putAll(packVo.getStorecells());
							}
						} catch (Exception e) {
							AppDebug.debug(e);
							throw new BusinessRuntimeException(e.getMessage(),e);
						}
						return table;
					}
				});
    	return table;
		
//		BaseDAO dao = new BaseDAO();
//		Hashtable<String, IStoreCell>  table = new Hashtable<String,IStoreCell>();
//		try {
//			SQLParameter param = new SQLParameter();
//			param.addParam(pk_report);
//
//			Collection<StoreCellPackVO> results = (Collection<StoreCellPackVO>) dao
//					.retrieveByClause(StoreCellPackVO.class, "pk_report = ?",param);
//			for (StoreCellPackVO packVo : results) {
//				fillStoreCellRepPK(packVo);
//				table.putAll(packVo.getStorecells());
//			}
//		} catch (DAOException e) {
//			AppDebug.debug(e);
//			throw new UFOSrvException(e.getMessage(),e);
//		}
//		return table;
	}

	@Override
	public IStoreCell getStoreCellsByPos(String pk_report, String pos)
			throws UFOSrvException {
		return getStoreCellsByRepID(pk_report).get(pos);
	}

	@SuppressWarnings("unchecked")
	@Override
	public  Hashtable<String, IStoreCell> getStoreCellPackByRepKeyGroupID(
			String pk_report, String pk_keygroup) throws UFOSrvException {
		BaseDAO dao = new BaseDAO();
		Hashtable<String, IStoreCell>  table = new Hashtable<String,IStoreCell>();
		try {
			SQLParameter param = new SQLParameter();
			param.addParam(pk_report);
			param.addParam(pk_keygroup);

			Collection<StoreCellPackVO> results = (Collection<StoreCellPackVO>) dao
					.retrieveByClause(StoreCellPackVO.class, "pk_report = ? and pk_keygroup = ?",param);
			for (StoreCellPackVO packVo : results) {
				fillStoreCellRepPK(packVo);
				table.putAll(packVo.getStorecells());
			}
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage(),e);
		}
		return table;
	}

	/* (non-Javadoc)
	 * @see nc.itf.iufo.storecell.IStoreCellPackQrySrv#getStoreCellsByPosAry(java.lang.String, java.lang.String[])
	 */
	@Override
	public IStoreCell[] getStoreCellsByPosAry(String pk_report, String[] posAry)
			throws UFOSrvException {
		List<IStoreCell> lstStoreCells = new ArrayList<IStoreCell>();
		Hashtable<String, IStoreCell> storecells = getStoreCellsByRepID(pk_report);
		for(String pos : posAry){
			lstStoreCells.add(storecells.get(pos));
		}
		return lstStoreCells.toArray(new IStoreCell[0]);
	}
	
	private void fillStoreCellRepPK(StoreCellPackVO storeCellPackVo){
		String pk_report = storeCellPackVo.getPk_report();
		Hashtable<String, IStoreCell> mapStoreCells = storeCellPackVo.getStorecells();
		for(IStoreCell storeCell : mapStoreCells.values()){
			storeCell.setReportPK(pk_report);
		}
	}

}
