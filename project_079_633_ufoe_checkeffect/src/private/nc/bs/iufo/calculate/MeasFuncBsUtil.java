package nc.bs.iufo.calculate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.naming.NamingException;

import nc.bs.dao.DAOException;
import nc.bs.pub.SystemException;
import nc.jdbc.framework.SQLParameter;
import nc.pub.iufo.cache.KeyGroupCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pub.iufo.data.thread.AbstractQueryData;
import nc.pub.iufo.data.thread.IufoThreadLocalUtil;
import nc.vo.iufo.calculate.DatePropVO;
import nc.vo.iufo.calculate.KeyWordVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.MeasureTraceVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iufo.pub.IDatabaseType;
import nc.vo.iufo.storecell.IStoreCell;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.pub.lang.UFDouble;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.iuforeport.reporttool.temp.KeyDataGroup;
import com.ufsoft.script.base.CommonExprCalcEnv;
import com.ufsoft.script.base.ICalcEnv;
import com.ufsoft.script.exception.OprException;
import com.ufsoft.script.exception.ParseException;
import com.ufsoft.script.exception.TranslateException;
import com.ufsoft.script.expression.ExprUtil;
import com.ufsoft.script.expression.UfoExpr;
import com.ufsoft.script.extfunc.ExtFuncIUfoConst;
import com.ufsoft.script.extfunc.MeasCondExprUtil;
import com.ufsoft.script.extfunc.MeasFunc;
import com.ufsoft.script.extfunc.MeasFuncDriver;
import com.ufsoft.script.extfunc.MeasOperand;
import com.ufsoft.script.extfunc.ReplenishKeyCondUtil;
import com.ufsoft.script.extfunc.UserCalcRightUtil;
import com.ufsoft.script.spreadsheet.ReportDynCalcEnv;
import com.ufsoft.script.spreadsheet.UfoCalcEnv;
import com.ufsoft.script.util.TaskCheckEnv;

/**
 * 此类用于指标函数后台计算
 *
 * @author liuchuna
 * @created at 2010-6-8,上午11:24:30
 *
 */
@SuppressWarnings("unchecked")
public class MeasFuncBsUtil {

	private MeasFuncDMO m_objMeasFuncDMO;

	public final static java.lang.String COLALIASPRFIX = "M_";

	/**
	 * FuncToSqlTranslator 构造子注解。
	 */
	public MeasFuncBsUtil() {
		super();
	}
	private String getDBType(UfoCalcEnv env){
		try{
			String dbType = env.getDbType();
			
			if(dbType == null) {
				
//				final	MeasFuncDMO dmo = getMeasFuncDMO();
				
//				String key = "nc.bs.iufo.calculate.MeasFuncBsUtil  dbtype ";
//		    	String type = (String)IufoThreadLocalUtil.getValue(key, new AbstractQueryData() {
//							
//							@Override
//							public Object qqueryData() {
//								 
//									return dmo.getDBType();
//							 
//							}
//						});
//		     
//				env.setDbType(type);
 
				
//				dbType = getMeasFuncDMO().getDBType();
				dbType="oracle";
				env.setDbType(dbType);
			}
			return dbType;
		}catch(Exception ex){
			return IDatabaseType.DATABASE_DEFAULTE;
		}
	}
	/**
	 * 批量计算与当前报表区域无关的指标统计函数的值，返回结果。这里的指标都是保存在一张数据表中的 创建日期：(2002-5-24 13:04:38)
	 *
	 * @return double
	 * @param nFid
	 *            int
	 * @param mVO
	 *            nc.vo.iufo.measure.MeasureVO
	 * @param strCondSql
	 *            java.lang.String
	 * @param env
	 *            UfoCalcEnv
	 */
	private Hashtable calcAggrValue(short nFID, String strKeyGroupPK,
			IStoreCell[] objMeasures, UfoExpr objCondExpr, UfoCalcEnv env,
			String strDbType) throws TranslateException {
		// 组成SQL语句
		try {

			if (strKeyGroupPK == null) {
				throw new TranslateException(TranslateException.ERR_KEYGROUP);
			}
			String strCondSql = getAggrSQL(nFID, objMeasures, strKeyGroupPK,
					objCondExpr, env, strDbType);

			//公式追踪。 liuyy。
			if(env.getMeasureTraceVOs() != null){
				String where = strCondSql.substring(strCondSql
						.indexOf("from") + 4);
				KeyGroupVO keyGroup=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(strKeyGroupPK);
				String strAccSchemePK=env.getMeasureEnv()!=null?env.getMeasureEnv().getAccSchemePK():null;
				MeasureTraceVO[] tvos = getMeasFuncDMO().measureTraces(env, objCondExpr, objMeasures, where,keyGroup,strAccSchemePK);

				Vector<MeasureTraceVO> allTraceVos = new Vector<MeasureTraceVO>();
				// 取得旧的追踪VO，将新的数据与旧数据合并
				MeasureTraceVO[] hasTraceVos = env.getMeasureTraceVOs();
				if(hasTraceVos != null) {
					allTraceVos.addAll(Arrays.asList(hasTraceVos));
				}
				if(tvos != null) {
					allTraceVos.addAll(Arrays.asList(tvos));
				}
				MeasureTraceVO[] aryAllVos = allTraceVos.toArray(new MeasureTraceVO[0]);

				env.setMeasureTraceVOs(aryAllVos);
			}

			return getMeasFuncDMO().getAggrValue(objMeasures, strCondSql);

		} catch (java.sql.SQLException e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_NODATA);
		} catch (javax.naming.NamingException ne) {
			AppDebug.debug(ne);//@devTools ne.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_NAMING);
		} catch (nc.bs.pub.SystemException se) {
			AppDebug.debug(se);//@devTools se.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_PUBSYSTEM);
		}
	}

	/**
	 * 计算单个统计函数的值，返回结果。这里不支持计算当前报表动态区域关联的指标的统计函数值。 创建日期：(2002-5-24 13:04:38)
	 *
	 * @return double
	 * @param nFid
	 *            int 统计函数类型id
	 * @param mVO
	 *            nc.vo.iufo.measure.MeasureVO
	 * @param strCondSql
	 *            java.lang.String
	 * @param env
	 *            UfoCalcEnv
	 */
	private Object calcAggrValue(short nFID, IStoreCell mVO,
			UfoExpr objCondExpr, UfoCalcEnv env, String strDbType)
			throws TranslateException {
		// 组成SQL语句
		try {

			String strKeyGroupPK = getMeasureKeyGroupPK(mVO, env);

			IStoreCell[] objMeasures = { mVO };
			KeyGroupVO objKG = env.getKeyGroupCache().getByPK(strKeyGroupPK);
			if (objKG == null) {
				throw new TranslateException(TranslateException.ERR_KEYGROUP);
			}
			nc.vo.iufo.keydef.KeyVO[] objKeys = objKG.getKeys();
			
			SQLParameter sqlParam = new SQLParameter();
			String strCondSql = getAggrSQLWithParam(objMeasures, null, strKeyGroupPK, objCondExpr, env, false, strDbType,false, sqlParam);
			
//			SQLParameter sqlParam = getSqlParam(objMeasures, null, strKeyGroupPK, objCondExpr, env, false, strDbType,false);

			//tianchuan 这里做一次真实调度的公式追踪
			if(env.getMeasureTraceVOs() != null){
				String where = strCondSql.substring(strCondSql
						.indexOf("from") + 4);
				KeyGroupVO keyGroup=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(strKeyGroupPK);
				String strAccSchemePK=env.getMeasureEnv()!=null?env.getMeasureEnv().getAccSchemePK():null;
				//tianchuan 
				MeasureTraceVO[] tvos = null;
				if(sqlParam.getCountParams()>0){
					tvos = getMeasFuncDMO().measureTracesWithSqlParam(env, objCondExpr, objMeasures, where,sqlParam,keyGroup,strAccSchemePK);
				}else{
					tvos = getMeasFuncDMO().measureTraces(env, objCondExpr, objMeasures, where,keyGroup,strAccSchemePK);
				}

				Vector<MeasureTraceVO> allTraceVos = new Vector<MeasureTraceVO>();
				//tianchuan 注：真实调度的追踪，以数据库中取出的为准
				// 取得旧的追踪VO，将新的数据与旧数据合并
//				MeasureTraceVO[] hasTraceVos = env.getMeasureTraceVOs();
				
//				if(hasTraceVos != null) {
//					allTraceVos.addAll(Arrays.asList(hasTraceVos));
//				}
				if(tvos != null) {
					allTraceVos.addAll(Arrays.asList(tvos));
				}
				MeasureTraceVO[] aryAllVos = allTraceVos.toArray(new MeasureTraceVO[0]);

				env.setMeasureTraceVOs(aryAllVos);
			}
			
			Hashtable hashValue = getMeasFuncDMO().getAggrValues(objMeasures,
					objKeys, strCondSql, sqlParam);

			if(mVO.getType() == IStoreCell.TYPE_BIGDECIMAL) {
				BigDecimal nMeasVal = (BigDecimal) env.getExEnv(MeasFunc.MEASEXENVPREFIX + mVO.getCode());
				return dealAggrValueNoPrvKeyByUFDouble(mVO.getCode(), hashValue, nMeasVal, nFID, env);
			} else {
				Double nMeasVal = (Double) env.getExEnv(MeasFunc.MEASEXENVPREFIX + mVO.getCode());
				// 调整值
				return dealAggrValueNoPrvKey(mVO.getCode(), hashValue, nMeasVal, nFID, env);
			}
		} catch (java.sql.SQLException e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_NODATA);
		} catch (javax.naming.NamingException ne) {
			AppDebug.debug(ne);//@devTools ne.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_NAMING);
		} catch (nc.bs.pub.SystemException se) {
			AppDebug.debug(se);//@devTools se.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_PUBSYSTEM);
		} catch (DAOException se) {
			AppDebug.debug(se);//@devTools se.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_PUBSYSTEM);
		}
	}

	private String getMeasureKeyGroupPK(IStoreCell mVO, UfoCalcEnv env)
			throws TranslateException {

		String strKeyGroupPK = null;
		if(mVO instanceof MeasureVO) {
			strKeyGroupPK = env.getMeasureCache().getKeyCombPk(mVO.getCode());
		} else {
			strKeyGroupPK = mVO.getKeyCombPK();
		}

		if (strKeyGroupPK == null) {
			throw new TranslateException(TranslateException.ERR_KEYGROUP);
		}
		return strKeyGroupPK;
	}

	/**
	 * 计算统计函数的值，返回结果。 创建日期：(2002-5-24 13:04:38)
	 *
	 * @return double
	 * @param mVO
	 *            nc.vo.iufo.measure.MeasureVO
	 * @param strCondSql
	 *            java.lang.String
	 * @param env
	 *            UfoCalcEnv
	 */
	private double calcCountValue(IStoreCell mVO, UfoExpr objCondExpr,
			UfoCalcEnv env, String strDbType) throws TranslateException {
		// 组成SQL语句
		try {

			String strKeyGroupPK = getMeasureKeyGroupPK(mVO, env);

			String strCondSql = getCountSQL(mVO, strKeyGroupPK, objCondExpr,
					env, strDbType);
			return ((Double) getMeasFuncDMO().getValue(strCondSql, true, false))
					.doubleValue();

		} catch (java.sql.SQLException e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_NODATA);
		} catch (javax.naming.NamingException ne) {
			AppDebug.debug(ne);//@devTools ne.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_NAMING);
		} catch (nc.bs.pub.SystemException se) {
			AppDebug.debug(se);//@devTools se.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_PUBSYSTEM);
		}
	}

	/**
	 * 有私有关键字时用此方法计算统计函数的值。
	 *
	 * 创建日期：(2003-8-13 11:46:33)
	 *
	 * @return double
	 * @param hashMeasValue
	 *            java.util.Hashtable
	 * @param nMeasValInArea
	 *            double
	 * @param nFID
	 *            short
	 */
	private double dealAggrValueNoPrvKey(String strMeasurePK,
			Hashtable hashMeasValue, Double nMeasValInArea, short nFID,
			UfoCalcEnv objEnv) {
		// String strName = FuncToSqlTranslator.getDBFuncName(nFID);
		// if (nFID == MeasFuncDriver.FMAVG) {
		// strName = FuncToSqlTranslator.getDBFuncName(MeasFuncDriver.FMSUM);
		// }
		if (hashMeasValue==null || hashMeasValue.size()<=0)
			return 0;

		double nVal = 0;
		int nCount = 0;
		if (nFID == MeasFuncDriver.FMMIN) {
			nVal = Double.MAX_VALUE;
		} else if (nFID == MeasFuncDriver.FMMAX) {
			nVal = Double.MIN_VALUE;
		}
		Enumeration enKey = hashMeasValue.keys();
		while (enKey.hasMoreElements()) {
			MeasurePubDataVO objMeasurepubdata = (MeasurePubDataVO) enKey
					.nextElement();
			nCount++;
			double nCurVal = 0;
			if (objMeasurepubdata.equals(objEnv.getMeasureEnv())
					&& nMeasValInArea != null) {
				nCurVal = nMeasValInArea.doubleValue();
			} else {
				nCurVal = ((Double) ((Hashtable) hashMeasValue
						.get(objMeasurepubdata)).get(strMeasurePK))
						.doubleValue();
			}

			switch (nFID) {
			case MeasFuncDriver.FMSUM:
			case MeasFuncDriver.FMAVG:
				nVal += nCurVal;
				break;
			case MeasFuncDriver.FMMIN:
				if (nCurVal < nVal) {
					nVal = nCurVal;
				}
				break;
			case MeasFuncDriver.FMMAX:
				if (nCurVal > nVal) {
					nVal = nCurVal;
				}
				break;
			}
		}
		if (nFID == MeasFuncDriver.FMAVG) {
			if (nCount == 0) {
				nVal = 0;
			} else {
				nVal /= nCount;
			}
		}
		return nVal;
	}
	
	private UFDouble dealAggrValueNoPrvKeyByUFDouble(String strMeasurePK,
			Hashtable hashMeasValue, BigDecimal nMeasValInArea, short nFID,
			UfoCalcEnv objEnv) {
		// String strName = FuncToSqlTranslator.getDBFuncName(nFID);
		// if (nFID == MeasFuncDriver.FMAVG) {
		// strName = FuncToSqlTranslator.getDBFuncName(MeasFuncDriver.FMSUM);
		// }
		if (hashMeasValue==null || hashMeasValue.size()<=0)
			return new UFDouble(0);

		UFDouble nVal = new UFDouble(0);
		UFDouble maxValue = new UFDouble(Double.MAX_VALUE);
		UFDouble minValue = new UFDouble(Double.MIN_VALUE);
		int nCount = 0;
		if (nFID == MeasFuncDriver.FMMIN) {
			nVal = maxValue;
		} else if (nFID == MeasFuncDriver.FMMAX) {
			nVal = minValue;
		}
		Enumeration enKey = hashMeasValue.keys();
		while (enKey.hasMoreElements()) {
			MeasurePubDataVO objMeasurepubdata = (MeasurePubDataVO) enKey
					.nextElement();
			nCount++;
			UFDouble nCurVal = new UFDouble(0);
			if (objMeasurepubdata.equals(objEnv.getMeasureEnv())
					&& nMeasValInArea != null) {
				nCurVal = new UFDouble(nMeasValInArea);
			} else {
				nCurVal = ((UFDouble) ((Hashtable) hashMeasValue
						.get(objMeasurepubdata)).get(strMeasurePK));
			}

			switch (nFID) {
			case MeasFuncDriver.FMSUM:
			case MeasFuncDriver.FMAVG:
				nVal = nVal.add(nCurVal);
				break;
			case MeasFuncDriver.FMMIN:
				if (nCurVal.compareTo(nVal) < 0) {
					nVal = nCurVal;
				}
				break;
			case MeasFuncDriver.FMMAX:
				if (nCurVal.compareTo(nVal) > 0) {
					nVal = nCurVal;
				}
				break;
			}
		}
		if (nFID == MeasFuncDriver.FMAVG) {
			if (nCount == 0) {
				nVal = new UFDouble(0);
			} else {
				nVal = nVal.div(nCount);
			}
		}
		return nVal;
	}

	/**
	 * 计算指定指标在指定条件下涉及的值。函数包括 msum,mmax,mmin,mavg. 创建日期：(2003-8-11 14:03:30)
	 *
	 * @return java.util.Hashtable
	 *         key=(指标pk,CURKEY),value=hashtable(key=MSUM,MMAX,MMIN,MAVG,
	 *         value=排除当前关键值外的合计、最大、最小、计数,当前关键字值字串）
	 *
	 * @param hashMeasureSQL
	 *            Hashtable[] [0]是表对应的from之前的字段（key=dbtable,value=columns）
	 *            [1]表示在一个表中的指标（key=dbtable,
	 *            value=ArrayList(MeasureVO)）[0]与[1]的key一致
	 * @param measFuncType
	 *            key=指标pk,value=对应的指标统计函数类型集合(Vector 元素为Short)
	 * @param strKeyGroupPK
	 *            String
	 * @param objCondExpr
	 * @param objEnv
	 *
	 * @exception 异常说明。
	 */
	private Hashtable getAggrDatas(Hashtable[] hashMeasureSQL,
			Map measFuncType, String strKeyGroupPK, UfoExpr objCondExpr,
			UfoCalcEnv objEnv, String strDbType, boolean removeZero) throws TranslateException {
		try {

			// 指标没有与数据库表关联时，返回0
			if (hashMeasureSQL == null) {
				return null;
			}

			if (strKeyGroupPK == null) {
				throw new TranslateException(TranslateException.ERR_KEYGROUP);
			}
			// 处理条件
			// 在统计条件中追加用户权限
			// objCondExpr =
			// MeasFunc.applyUserRightToCond(
			// objCondExpr,
			// objEnv,
			// strKeyGroupPK);

			nc.vo.iufo.keydef.KeyGroupVO objKeyGroup = objEnv
					.getKeyGroupCache().getByPK(strKeyGroupPK);
			if (objKeyGroup == null) {
				throw new TranslateException(TranslateException.ERR_KEYGROUP);
			}

			nc.vo.iufo.keydef.KeyVO[] objKeys = objKeyGroup.getKeys();
			// hashDatas
			// (key=指标pk,value=hashtable(key=MSUM,MMAX,MMIN,MAVG,CURKEY,value=排除当前关键值外的合计、最大、最小、计数,当前关键字值字串）)
			Hashtable hashDatas = new Hashtable();

			Enumeration enMeasKey = hashMeasureSQL[1].keys();
			MeasFuncDMO objMeasFuncDmo = getMeasFuncDMO();
			while (enMeasKey.hasMoreElements()) {
				// 某一数据表对应的指标集合
				ArrayList alMeas = (ArrayList) hashMeasureSQL[1].get(enMeasKey
						.nextElement());
				IStoreCell[] objMeasures = new IStoreCell[alMeas.size()];
				alMeas.toArray(objMeasures);
				if (objMeasures[0].getDbtable() == null) {
					continue;
				}
				// hashDBFunc (key=指标pk
				// value=hashtable(key=指标统计函数名,value=数据库函数名))
				Hashtable hashDBFunc = new Hashtable();
				for (int i = 0, size = objMeasures.length; i < size; i++) {

					if (measFuncType != null
							&& measFuncType.get(objMeasures[i].getCode()) != null) {
						Hashtable hashDB = new Hashtable();
						Vector vecTemp = (Vector) measFuncType
								.get(objMeasures[i].getCode());
						for (int j = 0, len = vecTemp.size(); j < len; j++) {
							String strFuncName = (String) vecTemp.get(j);
							String strDBName = getStatDBFuncName(strFuncName);
							if (strDBName != null)
								hashDB.put(strFuncName, strDBName);
							if (strFuncName.equals(MeasFuncDriver.MAVG)) {
								hashDB.put(MeasFuncDriver.MSUM,
										getStatDBFuncName(MeasFuncDriver.MSUM));
							}
						}
						if (hashDB.size() > 0)
							hashDBFunc.put(objMeasures[i].getCode(), hashDB);
					}
				}

				String strCondSql = getAggrSQL(objMeasures, hashDBFunc,
						strKeyGroupPK, objCondExpr, objEnv, true, strDbType,removeZero);

				//liuyy. 处理指标公式追踪
				if(objEnv.isMeasureTrace()){
					String where = strCondSql.substring(strCondSql
							.indexOf("from") + 4);
					KeyGroupVO keyGroup=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(strKeyGroupPK);
					String strAccSchemePK=objEnv.getMeasureEnv()!=null?objEnv.getMeasureEnv().getAccSchemePK():null;
					MeasureTraceVO[] tracevos = getMeasFuncDMO().measureTraces(objEnv, objCondExpr, objMeasures, where,keyGroup,strAccSchemePK);
					Vector<MeasureTraceVO> allTraceVos = new Vector<MeasureTraceVO>();

					// 取得旧的追踪VO，将新的数据与旧数据合并
					MeasureTraceVO[] hasTraceVos = objEnv.getMeasureTraceVOs();
					if(hasTraceVos != null) {
						allTraceVos.addAll(Arrays.asList(hasTraceVos));
					}
					if(tracevos != null) {
						allTraceVos.addAll(Arrays.asList(tracevos));
					}
					MeasureTraceVO[] aryAllVos = allTraceVos.toArray(new MeasureTraceVO[0]);

					objEnv.setMeasureTraceVOs(aryAllVos);
					//借hashtable返回追踪信息。
					hashDatas.put(ICalcEnv.MEASURE_TRACE_FLAG, aryAllVos);

				}

				// hashMeasValue
				// (key=指标pk,value=hashtable(key=MSUM,MMAX,MMIN,MAVG,value=排除当前关键值外的合计、最大、最小、计数）)
				Hashtable hashMeasValue = objMeasFuncDmo.getAggrDatas(
						objMeasures, hashDBFunc, strCondSql);

				if (hashMeasValue != null && hashMeasValue.size() > 0) {
					hashDatas.putAll(hashMeasValue);
				} else {
					// 没有符合条件的指标数据时
					if (objMeasures != null && objMeasures.length > 0) {
						for (int m = 0, size = objMeasures.length; m < size; m++) {
							if (objMeasures[m].getType() != MeasureVO.TYPE_NUMBER && 
									objMeasures[m].getType() != MeasureVO.TYPE_BIGDECIMAL)
								continue;

							Hashtable hashStatTemp = (Hashtable) measFuncType
									.get(objMeasures[m].getCode());
							if (hashStatTemp != null && hashStatTemp.size() > 0) {
								Hashtable hashMeasState = new Hashtable();

								Iterator iter = hashStatTemp.keySet()
										.iterator();
								while (iter.hasNext()) {
									String strFuncName = (String) iter.next();
									if (strFuncName.equals(MeasFuncDriver.MAVG)) {
										hashMeasState.put(MeasFuncDriver.MAVG,
												Integer.valueOf(0));
									} else
										hashMeasState.put(strFuncName,
												Double.valueOf(0));
								}
								hashDatas.put(objMeasures[m].getCode(),
										hashMeasState);
							}
						}
					}
				}
			}
			// 获得当前主表关键字值字串
			String strCurKeyValue = getCurKeyValues(objEnv, objKeys);

			// 处理返回,返回当前主表关键字值,及除当前环境值之外的各条件合计、数量、最大、最小值。
			if (strCurKeyValue != null)
				hashDatas.put(MeasFunc.TAG_CURKEYVALUE, strCurKeyValue);

			return hashDatas;
		}/*
			 * catch (ParseException e) { e.printStackTrace(System.out); throw
			 * new TranslateException(e.getMessage()); } catch (OprException
			 * ope) { ope.printStackTrace(System.out); throw new
			 * TranslateException(ope.getMessage()); }
			 */catch (java.sql.SQLException e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_NODATA);
		} catch (javax.naming.NamingException ne) {
			AppDebug.debug(ne);//@devTools ne.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_NAMING);
		} catch (nc.bs.pub.SystemException se) {
			AppDebug.debug(se);//@devTools se.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_PUBSYSTEM);
		}
	}

	/**
	 * 批量读取指标的统计函数值对。 在参数alMeas中的第一个元素是一个Object[]对象，
	 * Object[0]是这组指标的关键字组和PK，Object[1]是UfoExpr是这祖指标的条件，
	 * alMeas从第一个位置之后全部是MeasureVO对象
	 * 这个方法会读出符合函数条件的所有值，并以key=MeasurePubDataVO,value=Hashtable(key=measurePK,
	 * value= Double|String)的形式保存。 创建日期：(2003-8-11 11:16:17)
	 *
	 * @return java.util.Hashtable
	 * @param alMeas
	 *            java.util.ArrayList
	 * @param env
	 *            UfoCalcEnv
	 * @exception TranslateException
	 *                异常说明。
	 */
	public Hashtable getAggrDatas(ArrayList alMeas, UfoCalcEnv env, boolean removeZero)
			throws TranslateException {

		Object[] objCond = (Object[]) alMeas.get(0);
		Map measFuncType = (Map) alMeas.get(1);

		IStoreCell[] objMeasures = new IStoreCell[alMeas.size() - 2];
		for (int i = 2; i < alMeas.size(); i++) {
			objMeasures[i - 2] = (IStoreCell) alMeas.get(i);
		}
		// [0]是表对应的from之前的字段（key=dbtable,value=columns）
		// [1]表示在一个表中的指标（key=dbtable, value=ArrayList(MeasureVO)）[0]与[1]的key一致
		Hashtable[] hashMeasSQL = getMeasureSQL(objMeasures);

		Hashtable hashValues = getAggrDatas(hashMeasSQL, measFuncType,
				(String) objCond[0], (UfoExpr) objCond[1], env, getDBType(env),removeZero);

		return hashValues;
	}

	/**
	 * 批量获得指标统计函数的数据
	 *
	 * @param objMeasure
	 * @param objCondExpr
	 * @param strExprRefDynKeys
	 * @param objEnv
	 * @param batchKeyDatas
	 * @return
	 * @throws TranslateException
	 */
	public Hashtable batchGetAggrDatas(IStoreCell objMeasure,
			UfoExpr objCondExpr, String[] strExprRefDynKeys,
			String[] strExprMeasKeys, String strMainTimeKey,
			ReportDynCalcEnv objEnv, KeyDataGroup[] batchKeyDatas)
			throws TranslateException {
		try {
			// 1.检查
			nc.vo.iufo.keydef.KeyGroupVO objKeyGroup = checkAggrMeasure(
					objMeasure, objEnv);
			if (objMeasure == null || objMeasure.getDbtable() == null) {
//			if (objMeasure != null && objMeasure.getDbtable() == null) {
				return null;
			}

			// 2.获得查询sql并返回数据
			nc.vo.iufo.keydef.KeyVO[] objKeys = objKeyGroup.getKeys();
			String strCondSql = getAggrSQL(objMeasure, objKeyGroup
					.getKeyGroupPK(), objCondExpr, objEnv, batchKeyDatas,
					strExprRefDynKeys, strMainTimeKey, getDBType(objEnv));

			Hashtable aggrDatas = getMeasFuncDMO().batchGetAggrDatas(objMeasure, objKeys,
					strCondSql, strExprMeasKeys);

			//liuyy. 处理指标公式追踪
			if(objEnv.isMeasureTrace()){
				String where = strCondSql.substring(strCondSql
						.indexOf("from") + 4);
				String strAccSchemePK=objEnv.getMeasureEnv()!=null?objEnv.getMeasureEnv().getAccSchemePK():null;
				MeasureTraceVO[] tracevos = getMeasFuncDMO().measureTraces(objEnv,objCondExpr,new IStoreCell[]{objMeasure}, where,objKeyGroup,strAccSchemePK);
				objEnv.setMeasureTraceVOs(tracevos);
				//借hashtable返回追踪信息。
				aggrDatas.put(ICalcEnv.MEASURE_TRACE_FLAG, tracevos);

			}
			return aggrDatas;

		} catch (java.sql.SQLException e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_NODATA);
		} catch (javax.naming.NamingException ne) {
			AppDebug.debug(ne);//@devTools ne.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_NAMING);
		} catch (nc.bs.pub.SystemException se) {
			AppDebug.debug(se);//@devTools se.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_PUBSYSTEM);
		}
	}

	/**
	 * 批量获得指标统计函数的数据
	 *
	 * @param objMeasure
	 * @param objCondExpr
	 * @param strExprRefDynKeys
	 * @param objEnv
	 * @param batchKeyDatas
	 * @return
	 * @throws TranslateException
	 */
	public Hashtable batchGetAggrDatas(IStoreCell[] objMeasures,
			UfoExpr objCondExpr, String[] strExprRefDynKeys,
			String[] strExprMeasKeys, String strMainTimeKey,
			ReportDynCalcEnv objEnv, KeyDataGroup[] batchKeyDatas)
			throws TranslateException {
		try {
			IStoreCell objMeasure = null;
			Hashtable multiAggrDatas = new Hashtable();
			for(int i = 0; i< objMeasures.length; i++){
				// 1.检查
				objMeasure = objMeasures[i];
				nc.vo.iufo.keydef.KeyGroupVO objKeyGroup = checkAggrMeasure(
						objMeasure, objEnv);
				if (objMeasure == null || objMeasure.getDbtable() == null) {
//				if (objMeasure != null && objMeasure.getDbtable() == null) {
					return null;
				}

				// 2.获得查询sql并返回数据
				nc.vo.iufo.keydef.KeyVO[] objKeys = objKeyGroup.getKeys();
				String strCondSql = getAggrSQL(objMeasure, objKeyGroup
						.getKeyGroupPK(), objCondExpr, objEnv, batchKeyDatas,
						strExprRefDynKeys, strMainTimeKey, getDBType(objEnv));

				Hashtable aggrDatas = getMeasFuncDMO().batchGetAggrDatas(objMeasure, objKeys,
						strCondSql, strExprMeasKeys);

				//liuyy. 处理指标公式追踪
				if(objEnv.isMeasureTrace()){
					String where = strCondSql.substring(strCondSql
							.indexOf("from") + 4);
					String strAccSchemePK=objEnv.getMeasureEnv()!=null?objEnv.getMeasureEnv().getAccSchemePK():null;
					MeasureTraceVO[] tracevos = getMeasFuncDMO().measureTraces(objEnv,objCondExpr,new IStoreCell[]{objMeasure}, where,objKeyGroup,strAccSchemePK);
					objEnv.setMeasureTraceVOs(tracevos);
					//借hashtable返回追踪信息。
					aggrDatas.put(ICalcEnv.MEASURE_TRACE_FLAG, tracevos);

				}

				multiAggrDatas.put(objMeasure, aggrDatas);
			}
			return multiAggrDatas;

		} catch (java.sql.SQLException e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_NODATA);
		} catch (javax.naming.NamingException ne) {
			AppDebug.debug(ne);//@devTools ne.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_NAMING);
		} catch (nc.bs.pub.SystemException se) {
			AppDebug.debug(se);//@devTools se.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_PUBSYSTEM);
		}
	}

	private nc.vo.iufo.keydef.KeyGroupVO checkAggrMeasure(IStoreCell objMeasure,
			UfoCalcEnv env) throws TranslateException {
		// 1.检查

		if (objMeasure == null) {
			throw new TranslateException(TranslateException.ERR_FUNC);
		}

		String strKeyGroupPK = null;
		if(objMeasure instanceof MeasureVO) {
			strKeyGroupPK = env.getMeasureCache().getKeyCombPk(objMeasure.getCode());
		} else {
			strKeyGroupPK = objMeasure.getKeyCombPK();
		}

		if (strKeyGroupPK == null) {
			throw new TranslateException(TranslateException.ERR_KEYGROUP);
		}
		nc.vo.iufo.keydef.KeyGroupVO objKeyGroup = env.getKeyGroupCache()
				.getByPK(strKeyGroupPK);
		if (objKeyGroup == null) {
			throw new TranslateException(TranslateException.ERR_KEYGROUP);
		}
		return objKeyGroup;
	}

	/**
	 * 计算指定指标在指定条件下制定函数的值。函数包括mcount, msum,mmax,mmin,mavg，env中保存了指标对应区域的值
	 * 创建日期：(2003-8-11 14:03:30)
	 *
	 * @return java.util.Hashtable key=(keyValue+\r\n)* value = Double|String
	 * @param objMeasure
	 *            nc.vo.iufo.measure.MeasureVO
	 * @param objCondExpr
	 *            com.ufsoft.iufo.util.expression.UfoExpr
	 * @param objEnv
	 *            UfoCalcEnv
	 * @param batchKeyDatas
	 *            批量计算的动态区多行关键字值数组
	 * @exception TranslateException
	 *                异常说明。
	 */
	public Hashtable getAggrDatas(IStoreCell objMeasure, UfoExpr objCondExpr,
			ReportDynCalcEnv objEnv) throws TranslateException {
		try {
			// 1.检查
			nc.vo.iufo.keydef.KeyGroupVO objKeyGroup = checkAggrMeasure(
					objMeasure, objEnv);

			if (objMeasure == null || objMeasure.getDbtable() == null) {
//			if (objMeasure != null && objMeasure.getDbtable() == null) {
				return null;
			}

			// 2.获得查询sql并返回数据
			nc.vo.iufo.keydef.KeyVO[] objKeys = objKeyGroup.getKeys();
			String strCondSql = getAggrSQL(objMeasure, objKeyGroup
					.getKeyGroupPK(), objCondExpr, objEnv, null, null, null,
					getDBType(objEnv));

			Hashtable aggrDatas = getMeasFuncDMO().getAggrDatas(objMeasure, objKeys,
					strCondSql);


			//liuyy. 处理指标公式追踪
			if(objEnv.isMeasureTrace()){
				String where = strCondSql.substring(strCondSql.indexOf("from") + 4);
				String strAccSchemePK=objEnv.getMeasureEnv()!=null?objEnv.getMeasureEnv().getAccSchemePK():null;
				MeasureTraceVO[] tracevos = getMeasFuncDMO().measureTraces(objEnv, objCondExpr, new IStoreCell[]{objMeasure}, where,objKeyGroup,strAccSchemePK);
				objEnv.setMeasureTraceVOs(tracevos);
				//借hashtable返回追踪信息。
				if(aggrDatas != null) aggrDatas.put(ICalcEnv.MEASURE_TRACE_FLAG, tracevos);
			}

			return aggrDatas;

		} catch (java.sql.SQLException e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_NODATA);
		} catch (javax.naming.NamingException ne) {
			AppDebug.debug(ne);//@devTools ne.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_NAMING);
		} catch (nc.bs.pub.SystemException se) {
			AppDebug.debug(se);//@devTools se.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_PUBSYSTEM);
		}
	}

	/**
	 * 计算一组指标在指定条件下制定函数的值。函数包括mcount, msum,mmax,mmin,mavg，env中保存了指标对应区域的值
	 *
	 * @return java.util.Hashtable key=指标编码，value=Hashtable(key=(keyValue+\r\n)* value = Double|String)
	 * @param objMeasure
	 *            nc.vo.iufo.measure.MeasureVO
	 * @param objCondExpr
	 *            com.ufsoft.iufo.util.expression.UfoExpr
	 * @param objEnv
	 *            UfoCalcEnv
	 * @param batchKeyDatas
	 *            批量计算的动态区多行关键字值数组
	 * @exception TranslateException
	 *                异常说明。
	 */
	public Hashtable getAggrDatas(IStoreCell[] objMeasures, UfoExpr objCondExpr,
			ReportDynCalcEnv objEnv) throws TranslateException {
		try {
			IStoreCell objMeasure = null;
			Hashtable multiAggrDatas = new Hashtable();
			for(int i = 0; i< objMeasures.length; i++){
				//1.检查
				objMeasure = objMeasures[i];
				nc.vo.iufo.keydef.KeyGroupVO objKeyGroup = checkAggrMeasure(
						objMeasure, objEnv);

				if (objMeasure == null || objMeasure.getDbtable() == null) {
//				if (objMeasure != null && objMeasure.getDbtable() == null) {
					return null;
				}

				// 2.获得查询sql并返回数据
				nc.vo.iufo.keydef.KeyVO[] objKeys = objKeyGroup.getKeys();
				String strCondSql = getAggrSQL(objMeasure, objKeyGroup
						.getKeyGroupPK(), objCondExpr, objEnv, null, null, null,
						getDBType(objEnv));

				Hashtable aggrDatas = getMeasFuncDMO().getAggrDatas(objMeasure, objKeys,
						strCondSql);

				//liuyy. 处理指标公式追踪
				if(objEnv.isMeasureTrace()){
					String where = strCondSql.substring(strCondSql.indexOf("from") + 4);
					String strAccSchemePK=objEnv.getMeasureEnv()!=null?objEnv.getMeasureEnv().getAccSchemePK():null;
					MeasureTraceVO[] tracevos = getMeasFuncDMO().measureTraces(objEnv, objCondExpr, new IStoreCell[]{objMeasure}, where,objKeyGroup,strAccSchemePK);
					objEnv.setMeasureTraceVOs(tracevos);
					//借hashtable返回追踪信息。
					aggrDatas.put(ICalcEnv.MEASURE_TRACE_FLAG, tracevos);
				}
				if(objMeasure != null && aggrDatas != null){
					multiAggrDatas.put(objMeasure, aggrDatas);
				}
			}
			return multiAggrDatas;

		} catch (java.sql.SQLException e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_NODATA);
		} catch (javax.naming.NamingException ne) {
			AppDebug.debug(ne);//@devTools ne.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_NAMING);
		} catch (nc.bs.pub.SystemException se) {
			AppDebug.debug(se);//@devTools se.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_PUBSYSTEM);
		}
	}

	/**
	 * 得到统计函数的SQL。其中的指标都是在一个表中保存数据。 创建日期：(2002-5-24 13:04:38)
	 *
	 * @return double
	 * @param mVOs
	 *            nc.vo.iufo.measure.MeasureVO[]这些指标的数据应该是保存在一个表内的。
	 * @param objCondExpr
	 *            UfoExpr
	 * @param env
	 *            UfoCalcEnv
	 */
	private String getAggrSQL(IStoreCell[] mVOs, Map measAggrFunc,
			String strKeyGroupPK, UfoExpr objCondExpr, UfoCalcEnv env,
			boolean bStatic, String strDbType, boolean removeZero) throws TranslateException {
		// 组成SQL语句
		// 得到任务类型
		String strEnvRepPK = env.getRepPK();
		nc.vo.iufo.keydef.KeyVO[] objEnvKeys = env.getKeys();

		ArrayList alMeasInEnv = (ArrayList) env
				.getExEnv(CommonExprCalcEnv.EX_MEASVOINSQL);
		env.removeExEnv(CommonExprCalcEnv.EX_MEASVOINSQL);
		try {

			nc.vo.iufo.keydef.KeyGroupVO objKG = env.getKeyGroupCache()
					.getByPK(strKeyGroupPK);
			if (objKG == null) {
				throw new TranslateException(TranslateException.ERR_KEYGROUP);
			}

			nc.vo.iufo.keydef.KeyVO[] objKeys = objKG.getKeys();
			if(mVOs[0] instanceof MeasureVO) {
				env.setRepPK(((MeasureVO)mVOs[0]).getReportPK());
			} else {
				env.setRepPK(mVOs[0].getReportPK());
			}
			if (objKeys != null) {
				for (int i = 0; i < objKeys.length; i++) {
					if (objKeys[i].getCode() != null
							&& objKeys[i].getCode().equals(
									nc.vo.iufo.keydef.KeyVO.CODE_TYPE_CORP)) {
						objCondExpr = UserCalcRightUtil.applyUserRightToCond(
								objCondExpr, env, strKeyGroupPK);
						break;
					}
				}
			}
			env.setKeys(objKeys);
			// 转换条件 select alone_id from iufo_measure_pub_data where XXX
			Hashtable<String,String> hashTables = new Hashtable<String,String>();// 条件中引用的表名
			Hashtable<Integer,String> hashTableByKeyPos=new Hashtable<Integer,String>();
			StringBuffer sbSQL = new StringBuffer();

			ArrayList alMeasOperand = new ArrayList();
			if (objCondExpr != null) {
				ExprUtil.getElementsByClass(objCondExpr, alMeasOperand,
						MeasOperand.class);
				if (alMeasOperand != null && alMeasOperand.size() > 0) {
					for (int i = 0; i < alMeasOperand.size(); i++) {
						alMeasOperand.set(i, ((MeasOperand) alMeasOperand
								.get(i)).getMeasureVO());
					}
				}
			}
			alMeasOperand.add(mVOs[0]);
			env.setExEnv(CommonExprCalcEnv.EX_MEASVOINSQL, alMeasOperand);

			hashTables.put("meas_table_column", mVOs[0].getDbtable() + "." + mVOs[0].getDbcolumn());
			ExprToSqlTranslator.toSQLCommon(strKeyGroupPK, objCondExpr,
					hashTables, hashTableByKeyPos,sbSQL, env, strDbType);
			hashTables.remove("meas_table_column");


			String strPubTable=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(strKeyGroupPK).getTableName();
			if (!hashTables
					.contains(strPubTable)) {
				hashTables.put(strPubTable,strPubTable);
			}
			StringBuffer sbFromWhere = new StringBuffer();

			int iSelectLen = "select ".length();
			sbFromWhere.append("select ");

			if (bStatic == false) {
//				sbFromWhere.append(IDatabaseNames.IUFO_MEASURE_PUBDATA);
				sbFromWhere.append("'"+env.getMeasureEnv().getKeyGroup().getKeyGroupPK()+"' ktype, ");

//				sbFromWhere.append(IDatabaseNames.IUFO_MEASURE_PUBDATA);
//				sbFromWhere.append(".formula_id, ");

				sbFromWhere.append(strPubTable);
				sbFromWhere.append(".ver ");

				if (objKeys != null) {
					for (int i = 0; i < objKeys.length; i++) {
						sbFromWhere.append(',');
						int nPos = env.getKeyGroupCache().getIndexOfKey(
								strKeyGroupPK, objKeys[i].getPk_keyword());
						if (nPos == -1) {
							throw new TranslateException(
									TranslateException.ERR_KEYNOTFOUND);
						}
//						if (objKeys[i].isPrivate()) {
//							sbFromWhere.append(mVOs[0].getDbtable());
//							sbFromWhere.append('.');
//							sbFromWhere.append(KeyWordVO.PRVKEYCOLEMNNAME
//									+ nPos);
//						} else
						{
							sbFromWhere
									.append(strPubTable);
							sbFromWhere.append('.');
							sbFromWhere.append(KeyWordVO.PUBKEYCOLEMNNAME
									+ nPos);
						}
						sbFromWhere.append(" ");
						sbFromWhere.append(COLALIASPRFIX);
						sbFromWhere.append(objKeys[i].getPk_keyword());
					}
				}
			}

			Hashtable hashDbFuncName = null;
			for (int i = 0; i < mVOs.length; i++) {
				IStoreCell mVO = mVOs[i];
				if (!hashTables.containsKey(mVO.getDbtable())) {
					hashTables.put(mVO.getDbtable(), mVO.getDbtable());
				}
				// NEW_ADD
				if (bStatic == true && measAggrFunc != null) {
					hashDbFuncName = (Hashtable) measAggrFunc
							.get(mVO.getCode());
					if (hashDbFuncName != null && hashDbFuncName.size() > 0) {
						Iterator iter = hashDbFuncName.keySet().iterator();
						while (iter.hasNext()) {
							String strFuncName = (String) iter.next();
							String strDBFuncName = (String) hashDbFuncName
									.get(strFuncName);

							if (sbFromWhere.length() > iSelectLen)
								sbFromWhere.append(',');

							sbFromWhere.append(strDBFuncName);
							sbFromWhere.append("(");
							sbFromWhere.append(mVO.getDbtable());
							sbFromWhere.append('.');
							sbFromWhere.append(mVO.getDbcolumn());
							sbFromWhere.append(")");
							sbFromWhere.append(" ");
							sbFromWhere.append(COLALIASPRFIX);
							sbFromWhere.append(strFuncName);
							sbFromWhere.append("_");
							sbFromWhere.append(i + 1);
						}

					}
				} else {
					if (sbFromWhere.length() > iSelectLen)
						sbFromWhere.append(',');

					sbFromWhere.append(mVO.getDbtable());
					sbFromWhere.append('.');
					sbFromWhere.append(mVO.getDbcolumn());
					sbFromWhere.append(" ");
					sbFromWhere.append(COLALIASPRFIX);
					sbFromWhere.append(i + 1);
				}
			}

//			sbFromWhere.append(nc.vo.iufo.pub.IDatabaseNames.IUFO_MEASURE_PUBDATA
//					+ ".ktype");

			StringBuffer sbWhere = new StringBuffer();
			boolean bWhereAdded = ExprToSqlTranslator.toFromWhereClause(
					hashTables, hashTableByKeyPos,sbWhere,strPubTable);

			if (bWhereAdded) {
				sbFromWhere.append(sbWhere.substring(0, sbWhere.toString()
						.indexOf("where")));
			} else {
				sbFromWhere.append(sbWhere);
			}
			sbFromWhere.append(" where ");
			if (env.getMeasureEnv().getVer() != -100) {
				sbFromWhere
						.append(strPubTable);
				sbFromWhere.append(".ver=" + env.getMeasureEnv().getVer());
			}
//			sbFromWhere.append(" and ");
////			if (objKG.isContainsPrivatekey()) {
////				sbFromWhere.append(mVOs[0].getDbtable() + ".pk_key_comb");
////			} else {
//				sbFromWhere
//						.append(nc.vo.iufo.pub.IDatabaseNames.IUFO_MEASURE_PUBDATA
//								+ ".ktype");
////			}
//			sbFromWhere.append("='");
//			sbFromWhere.append(strKeyGroupPK);
//			sbFromWhere.append('\'');
//			sbFromWhere.append(" and ");
//			sbFromWhere
//					.append(nc.vo.iufo.pub.IDatabaseNames.IUFO_MEASURE_PUBDATA);
//			sbFromWhere.append(".formula_id ");
//			if (env.getMeasureEnv().getFormulaID() == null) {
//				sbFromWhere.append(" is null ");
//			} else {
//				sbFromWhere.append("='");
//				sbFromWhere.append(env.getMeasureEnv().getFormulaID());
//				sbFromWhere.append("' ");
//			}

			if (bStatic == true && !env.isMeasureTrace()) {// && !env.isMeasureTrace() //liuyy 添加!env.isMeasureTrace()条件，用以在本表msuma时也查询出本表数据。 2008-01-27
				String strAloneId = env.getMeasureEnv().getAloneID();
				if (strAloneId != null) {
					sbFromWhere.append(" and ");
					sbFromWhere.append(strPubTable);
					sbFromWhere.append(".alone_id <>");
					sbFromWhere.append("'");
					sbFromWhere.append(strAloneId);
					sbFromWhere.append("' ");
				}
			}

			if (objCondExpr != null && sbSQL.length() > 0) { // 逻辑表达式
				sbFromWhere.append(" and ");
				sbFromWhere.append("  (");
				sbFromWhere.append(sbSQL.toString());
				// 下面是专门为了处理oracle8.0.5的一个bug而加的
				sbFromWhere.append(')');
			}

			if (bWhereAdded) {
				sbFromWhere.append(" and ");
				sbFromWhere.append(sbWhere.substring(sbWhere.toString()
						.indexOf("where") + 5));
			}
			//统计个数，如果不包括零值的指标
			if(removeZero){
				for (int i = 0; i < mVOs.length; i++) {
					IStoreCell mVO = mVOs[i];
					sbFromWhere.append(" and ");
					sbFromWhere.append(mVO.getDbtable());
					sbFromWhere.append('.');
					sbFromWhere.append(mVO.getDbcolumn());
					sbFromWhere.append("<>0");
				}
			}
			
			return sbFromWhere.toString();
		} catch (ParseException pe) {
			AppDebug.debug(pe);//@devTools pe.printStackTrace(System.out);
			throw new TranslateException(pe);
		} catch (OprException ope) {
			AppDebug.debug(ope);//@devTools ope.printStackTrace(System.out);
			throw new TranslateException(ope);
		} catch (Exception e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new TranslateException(e.getMessage());
		} finally {

			env.setKeys(objEnvKeys);
			env.setRepPK(strEnvRepPK);
			if (alMeasInEnv == null) {
				env.removeExEnv(CommonExprCalcEnv.EX_MEASVOINSQL);
			} else {
				env.setExEnv(CommonExprCalcEnv.EX_MEASVOINSQL, alMeasInEnv);
			}
		}
	}
	
	private String getAggrSQLWithParam(IStoreCell[] mVOs, Map measAggrFunc,
			String strKeyGroupPK, UfoExpr objCondExpr, UfoCalcEnv env,
			boolean bStatic, String strDbType, boolean removeZero, SQLParameter sqlParam) throws TranslateException {
		// 组成SQL语句
		// 得到任务类型
		String strEnvRepPK = env.getRepPK();
		nc.vo.iufo.keydef.KeyVO[] objEnvKeys = env.getKeys();

		ArrayList alMeasInEnv = (ArrayList) env
				.getExEnv(CommonExprCalcEnv.EX_MEASVOINSQL);
		env.removeExEnv(CommonExprCalcEnv.EX_MEASVOINSQL);
		try {

			nc.vo.iufo.keydef.KeyGroupVO objKG = env.getKeyGroupCache()
					.getByPK(strKeyGroupPK);
			if (objKG == null) {
				throw new TranslateException(TranslateException.ERR_KEYGROUP);
			}

			nc.vo.iufo.keydef.KeyVO[] objKeys = objKG.getKeys();
			if(mVOs[0] instanceof MeasureVO) {
				env.setRepPK(((MeasureVO)mVOs[0]).getReportPK());
			} else {
				env.setRepPK(mVOs[0].getReportPK());
			}
			if (objKeys != null) {
				for (int i = 0; i < objKeys.length; i++) {
					if (objKeys[i].getCode() != null
							&& objKeys[i].getCode().equals(
									nc.vo.iufo.keydef.KeyVO.CODE_TYPE_CORP)) {
						objCondExpr = UserCalcRightUtil.applyUserRightToCond(
								objCondExpr, env, strKeyGroupPK);
						break;
					}
				}
			}
			env.setKeys(objKeys);
			// 转换条件 select alone_id from iufo_measure_pub_data where XXX
			Hashtable<String,String> hashTables = new Hashtable<String,String>();// 条件中引用的表名
			Hashtable<Integer,String> hashTableByKeyPos=new Hashtable<Integer,String>();
			StringBuffer sbSQL = new StringBuffer();

			ArrayList alMeasOperand = new ArrayList();
			if (objCondExpr != null) {
				ExprUtil.getElementsByClass(objCondExpr, alMeasOperand,
						MeasOperand.class);
				if (alMeasOperand != null && alMeasOperand.size() > 0) {
					for (int i = 0; i < alMeasOperand.size(); i++) {
						alMeasOperand.set(i, ((MeasOperand) alMeasOperand
								.get(i)).getMeasureVO());
					}
				}
			}
			alMeasOperand.add(mVOs[0]);
			env.setExEnv(CommonExprCalcEnv.EX_MEASVOINSQL, alMeasOperand);

			hashTables.put("meas_table_column", mVOs[0].getDbtable() + "." + mVOs[0].getDbcolumn());
			ExprToSqlTranslator.toSQLWithParamCommon(strKeyGroupPK, objCondExpr,
					hashTables, hashTableByKeyPos,sbSQL, env, strDbType, sqlParam);
			hashTables.remove("meas_table_column");


			String strPubTable=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(strKeyGroupPK).getTableName();
			if (!hashTables
					.contains(strPubTable)) {
				hashTables.put(strPubTable,strPubTable);
			}
			StringBuffer sbFromWhere = new StringBuffer();

			int iSelectLen = "select ".length();
			sbFromWhere.append("select ");

			if (bStatic == false) {
//				sbFromWhere.append(IDatabaseNames.IUFO_MEASURE_PUBDATA);
				sbFromWhere.append("'"+env.getMeasureEnv().getKeyGroup().getKeyGroupPK()+"' ktype, ");

//				sbFromWhere.append(IDatabaseNames.IUFO_MEASURE_PUBDATA);
//				sbFromWhere.append(".formula_id, ");

				sbFromWhere.append(strPubTable);
				sbFromWhere.append(".ver ");

				if (objKeys != null) {
					for (int i = 0; i < objKeys.length; i++) {
						sbFromWhere.append(',');
						int nPos = env.getKeyGroupCache().getIndexOfKey(
								strKeyGroupPK, objKeys[i].getPk_keyword());
						if (nPos == -1) {
							throw new TranslateException(
									TranslateException.ERR_KEYNOTFOUND);
						}
//						if (objKeys[i].isPrivate()) {
//							sbFromWhere.append(mVOs[0].getDbtable());
//							sbFromWhere.append('.');
//							sbFromWhere.append(KeyWordVO.PRVKEYCOLEMNNAME
//									+ nPos);
//						} else
						{
							sbFromWhere
									.append(strPubTable);
							sbFromWhere.append('.');
							sbFromWhere.append(KeyWordVO.PUBKEYCOLEMNNAME
									+ nPos);
						}
						sbFromWhere.append(" ");
						sbFromWhere.append(COLALIASPRFIX);
						sbFromWhere.append(objKeys[i].getPk_keyword());
					}
				}
			}

			Hashtable hashDbFuncName = null;
			for (int i = 0; i < mVOs.length; i++) {
				IStoreCell mVO = mVOs[i];
				if (!hashTables.containsKey(mVO.getDbtable())) {
					hashTables.put(mVO.getDbtable(), mVO.getDbtable());
				}
				// NEW_ADD
				if (bStatic == true && measAggrFunc != null) {
					hashDbFuncName = (Hashtable) measAggrFunc
							.get(mVO.getCode());
					if (hashDbFuncName != null && hashDbFuncName.size() > 0) {
						Iterator iter = hashDbFuncName.keySet().iterator();
						while (iter.hasNext()) {
							String strFuncName = (String) iter.next();
							String strDBFuncName = (String) hashDbFuncName
									.get(strFuncName);

							if (sbFromWhere.length() > iSelectLen)
								sbFromWhere.append(',');

							sbFromWhere.append(strDBFuncName);
							sbFromWhere.append("(");
							sbFromWhere.append(mVO.getDbtable());
							sbFromWhere.append('.');
							sbFromWhere.append(mVO.getDbcolumn());
							sbFromWhere.append(")");
							sbFromWhere.append(" ");
							sbFromWhere.append(COLALIASPRFIX);
							sbFromWhere.append(strFuncName);
							sbFromWhere.append("_");
							sbFromWhere.append(i + 1);
						}

					}
				} else {
					if (sbFromWhere.length() > iSelectLen)
						sbFromWhere.append(',');

					sbFromWhere.append(mVO.getDbtable());
					sbFromWhere.append('.');
					sbFromWhere.append(mVO.getDbcolumn());
					sbFromWhere.append(" ");
					sbFromWhere.append(COLALIASPRFIX);
					sbFromWhere.append(i + 1);
				}
			}

//			sbFromWhere.append(nc.vo.iufo.pub.IDatabaseNames.IUFO_MEASURE_PUBDATA
//					+ ".ktype");

			StringBuffer sbWhere = new StringBuffer();
			boolean bWhereAdded = ExprToSqlTranslator.toFromWhereClause(
					hashTables, hashTableByKeyPos,sbWhere,strPubTable);

			if (bWhereAdded) {
				sbFromWhere.append(sbWhere.substring(0, sbWhere.toString()
						.indexOf("where")));
			} else {
				sbFromWhere.append(sbWhere);
			}
			sbFromWhere.append(" where ");
			if (env.getMeasureEnv().getVer() != -100) {
				sbFromWhere
						.append(strPubTable);
				sbFromWhere.append(".ver= ? ");
			}
//			sbFromWhere.append(" and ");
////			if (objKG.isContainsPrivatekey()) {
////				sbFromWhere.append(mVOs[0].getDbtable() + ".pk_key_comb");
////			} else {
//				sbFromWhere
//						.append(nc.vo.iufo.pub.IDatabaseNames.IUFO_MEASURE_PUBDATA
//								+ ".ktype");
////			}
//			sbFromWhere.append("='");
//			sbFromWhere.append(strKeyGroupPK);
//			sbFromWhere.append('\'');
//			sbFromWhere.append(" and ");
//			sbFromWhere
//					.append(nc.vo.iufo.pub.IDatabaseNames.IUFO_MEASURE_PUBDATA);
//			sbFromWhere.append(".formula_id ");
//			if (env.getMeasureEnv().getFormulaID() == null) {
//				sbFromWhere.append(" is null ");
//			} else {
//				sbFromWhere.append("='");
//				sbFromWhere.append(env.getMeasureEnv().getFormulaID());
//				sbFromWhere.append("' ");
//			}

			if (bStatic == true && !env.isMeasureTrace()) {// && !env.isMeasureTrace() //liuyy 添加!env.isMeasureTrace()条件，用以在本表msuma时也查询出本表数据。 2008-01-27
				String strAloneId = env.getMeasureEnv().getAloneID();
				if (strAloneId != null) {
					sbFromWhere.append(" and ");
					sbFromWhere.append(strPubTable);
					sbFromWhere.append(".alone_id <>");
					sbFromWhere.append("'");
					sbFromWhere.append(strAloneId);
					sbFromWhere.append("' ");
				}
			}

			if (objCondExpr != null && sbSQL.length() > 0) { // 逻辑表达式
				sbFromWhere.append(" and ");
				sbFromWhere.append("  (");
				sbFromWhere.append(sbSQL.toString());
				// 下面是专门为了处理oracle8.0.5的一个bug而加的
				sbFromWhere.append(')');
			}

			if (bWhereAdded) {
				sbFromWhere.append(" and ");
				sbFromWhere.append(sbWhere.substring(sbWhere.toString()
						.indexOf("where") + 5));
			}
			//统计个数，如果不包括零值的指标
			if(removeZero){
				for (int i = 0; i < mVOs.length; i++) {
					IStoreCell mVO = mVOs[i];
					sbFromWhere.append(" and ");
					sbFromWhere.append(mVO.getDbtable());
					sbFromWhere.append('.');
					sbFromWhere.append(mVO.getDbcolumn());
					sbFromWhere.append("<>0");
				}
			}
			
			return sbFromWhere.toString();
		} catch (ParseException pe) {
			AppDebug.debug(pe);//@devTools pe.printStackTrace(System.out);
			throw new TranslateException(pe);
		} catch (OprException ope) {
			AppDebug.debug(ope);//@devTools ope.printStackTrace(System.out);
			throw new TranslateException(ope);
		} catch (Exception e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new TranslateException(e.getMessage());
		} finally {

			env.setKeys(objEnvKeys);
			env.setRepPK(strEnvRepPK);
			if (alMeasInEnv == null) {
				env.removeExEnv(CommonExprCalcEnv.EX_MEASVOINSQL);
			} else {
				env.setExEnv(CommonExprCalcEnv.EX_MEASVOINSQL, alMeasInEnv);
			}
		}
	}
	
	private static String combineStringBuf(StringBuffer[] subBufs) {
		int iSize = subBufs.length;
		StringBuffer strBufRet = new StringBuffer();
		for (int i = 0; i < iSize; i++) {
			if (subBufs[i] != null && subBufs[i].length() > 0) {
				strBufRet.append("(");
				strBufRet.append(subBufs[i].toString());
				strBufRet.append(")");
			}
			if (i != (iSize - 1) && subBufs[i + 1] != null
					&& subBufs[i + 1].length() > 0) {
				strBufRet.append(" or ");
			}
		}
		return strBufRet.toString();
	}

	/**
	 * 得到统计函数的SQL。 创建日期：(2002-5-24 13:04:38)
	 *
	 * @return double
	 * @param mVOs
	 *            nc.vo.iufo.measure.MeasureVO[]这些指标的数据应该是保存在一个表内的。
	 * @param objCondExpr
	 *            UfoExpr
	 * @param env
	 *            UfoCalcEnv
	 */
	private String getAggrSQL(IStoreCell mVO, String strKeyGroupPK,
			UfoExpr objCondExpr, ReportDynCalcEnv env,
			KeyDataGroup[] batchKeyDatas, String[] strExprRefDynKeys,
			String strMainTimeKey, String strDbType) throws TranslateException {
		// 组成SQL语句

		// 1.保留原env中的内容
		String strEnvRepPK = env.getRepPK();
		nc.vo.iufo.keydef.KeyVO[] objEnvKeys = env.getKeys();
		ArrayList alMeasInEnv = (ArrayList) env
				.getExEnv(CommonExprCalcEnv.EX_MEASVOINSQL);
		KeyDataGroup curKeyData = env.getKeyDatas();

		env.removeExEnv(CommonExprCalcEnv.EX_MEASVOINSQL);
		try {

			nc.vo.iufo.keydef.KeyGroupVO objKG = env.getKeyGroupCache()
					.getByPK(strKeyGroupPK);
			if (objKG == null) {
				throw new TranslateException(TranslateException.ERR_KEYGROUP);
			}

			String strPubTable=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(strKeyGroupPK).getTableName();

			// 2.设置计算环境为指标的关键字和对应报表内容
			nc.vo.iufo.keydef.KeyVO[] objKeys = objKG.getKeys();
			if(mVO instanceof MeasureVO) {
				env.setRepPK(((MeasureVO)mVO).getReportPK());
			} else {
				env.setRepPK(mVO.getReportPK());
			}
			if (objKeys != null) {
				for (int i = 0; i < objKeys.length; i++) {
					if (objKeys[i].getCode() != null
							&& objKeys[i].getCode().equals(
									nc.vo.iufo.keydef.KeyVO.CODE_TYPE_CORP)) {
						objCondExpr = UserCalcRightUtil.applyUserRightToCond(
								objCondExpr, env, strKeyGroupPK);
						break;
					}
				}
			}
			env.setKeys(objKeys);

			// 3.获得引用的指标列表
			ArrayList alMeasOperand = new ArrayList();
			if (objCondExpr != null) {
				ExprUtil.getElementsByClass(objCondExpr, alMeasOperand,
						MeasOperand.class);
				if (alMeasOperand != null && alMeasOperand.size() > 0) {
					for (int i = 0; i < alMeasOperand.size(); i++) {
						alMeasOperand.set(i, ((MeasOperand) alMeasOperand
								.get(i)).getMeasureVO());
					}
				}
			}
			alMeasOperand.add(mVO);
			env.setExEnv(CommonExprCalcEnv.EX_MEASVOINSQL, alMeasOperand);

			// 4.生成条件表达式对应的sql语句，但不是完整的sql语句,只是列名组成的逻辑表达式。如:iufo_measure_pubdata.keyword1='0'
			// AND iufo_measure_pubdata.keyword4='a1'
			Hashtable<String,String> hashTables = new Hashtable<String,String>();// 条件中引用的表名
			Hashtable<Integer,String> hashTableByKeyPos=new Hashtable<Integer,String>();
			StringBuffer sbExprSQL = new StringBuffer();

			if (!hashTables.contains(strPubTable)) {
				hashTables.put(strPubTable,strPubTable);
			}

			// 4.1如果批量进行指标统计函数，则分割表达式中同动态区域关键字相关的条件部分。
			if (batchKeyDatas != null && batchKeyDatas.length > 0) {
				UfoExpr[] exprNews = MeasCondExprUtil.seperateExpr(
						objCondExpr, strExprRefDynKeys, strMainTimeKey);
				StringBuffer strBufMain = new StringBuffer();
				if (exprNews[0] != null)
					ExprToSqlTranslator.toSQLCommon(strKeyGroupPK, exprNews[0],
							hashTables, hashTableByKeyPos,strBufMain, env, strDbType);
				String strSubTemp = null;
				if (exprNews[1] != null) {
					int iRowSize = batchKeyDatas.length;
					StringBuffer[] strBufTemp = new StringBuffer[iRowSize];
					for (int i = 0; i < iRowSize; i++) {
						env.setDynAreaInfo(null, batchKeyDatas[i]);
						strBufTemp[i] = new StringBuffer();
						ExprToSqlTranslator.toSQLCommon(strKeyGroupPK,
								exprNews[1], hashTables,hashTableByKeyPos, strBufTemp[i], env,
								strDbType);
					}
					// 组织各子条件
					strSubTemp = combineStringBuf(strBufTemp);
				}
				sbExprSQL.append(strBufMain);
				if (strSubTemp != null && strSubTemp.length() > 0) {
					if (strBufMain.length() > 0)
						sbExprSQL.append(" and (");
					sbExprSQL.append(strSubTemp);
					if (strBufMain.length() > 0)
						sbExprSQL.append(")");
				}
			} else {
				// 该方法的调用太多，无法确保添加参数后不会带来问题，暂且通过hashTables
				// 传递参数，执行完成之后再将该参数删除 TODO
				hashTables.put("meas_table_column", mVO.getDbtable() + "." + mVO.getDbcolumn());
				ExprToSqlTranslator.toSQLCommon(strKeyGroupPK, objCondExpr,
						hashTables,hashTableByKeyPos, sbExprSQL, env, strDbType);
				hashTables.remove("meas_table_column");
			}

			// 5.组织select语句中select子句部分
			StringBuffer sbSqlReturn = new StringBuffer();
			sbSqlReturn.append("select ");
//			sbSqlReturn.append(strPubData);
//			sbSqlReturn.append(".ktype, ");
//			sbSqlReturn
//					.append(nc.vo.iufo.pub.IDatabaseNames.IUFO_MEASURE_PUBDATA);
//			sbSqlReturn.append(".formula_id, ");
			sbSqlReturn.append(strPubTable);
			sbSqlReturn.append(".ver ");

			if (objKeys != null) {
				for (int i = 0; i < objKeys.length; i++) {
					sbSqlReturn.append(',');
					int nPos = env.getKeyGroupCache().getIndexOfKey(
							strKeyGroupPK, objKeys[i].getPk_keyword());
					if (nPos == -1) {
						throw new TranslateException(
								TranslateException.ERR_KEYNOTFOUND);
					}
//					if (objKeys[i].isPrivate()) {
//						sbSqlReturn.append(mVO.getDbtable());
//						sbSqlReturn.append('.');
//						sbSqlReturn.append(KeyWordVO.PRVKEYCOLEMNNAME + nPos);
//					} else
					{
						sbSqlReturn.append(strPubTable);
						sbSqlReturn.append('.');
						sbSqlReturn.append(KeyWordVO.PUBKEYCOLEMNNAME + nPos);
					}
					sbSqlReturn.append(" ");
					sbSqlReturn.append(COLALIASPRFIX);
					sbSqlReturn.append(objKeys[i].getPk_keyword());
				}
			}
			if (!hashTables.containsKey(mVO.getDbtable())) {
				hashTables.put(mVO.getDbtable(), mVO.getDbtable());
			}
			if (!hashTables
					.containsKey(strPubTable)) {
				hashTables.put(strPubTable,strPubTable);
			}
			sbSqlReturn.append(',');
			sbSqlReturn.append(mVO.getDbtable());
			sbSqlReturn.append('.');
			sbSqlReturn.append(mVO.getDbcolumn());
			sbSqlReturn.append(" ");
			sbSqlReturn.append(COLALIASPRFIX);
			sbSqlReturn.append(1);

			// 6.加入from子句
			StringBuffer sbFrom = new StringBuffer();
			boolean bWhereAdded = ExprToSqlTranslator.toFromWhereClause(
					hashTables, hashTableByKeyPos,sbFrom,strPubTable);
			if (bWhereAdded) {
				sbSqlReturn.append(sbFrom.substring(0, sbFrom.toString()
						.indexOf("where")));
			} else {
				sbSqlReturn.append(sbFrom);
			}

			// 7.加入where子句
			sbSqlReturn.append(" where ");
			if (env.getMeasureEnv().getVer() != -100) {
				sbSqlReturn.append(strPubTable);
				sbSqlReturn.append(".ver=" + env.getMeasureEnv().getVer()); // 决算版本
			}
//			sbSqlReturn.append(" and ");
////			if (objKG.isContainsPrivatekey()) {
////				sbSqlReturn.append(mVO.getDbtable() + ".pk_key_comb");
////			} else {
//				sbSqlReturn.append(strPubData+ ".ktype");
////			}
//			sbSqlReturn.append("='");
//			sbSqlReturn.append(strKeyGroupPK);
//			sbSqlReturn.append('\'');
//			sbSqlReturn.append(" and ");
//			sbSqlReturn
//					.append(nc.vo.iufo.pub.IDatabaseNames.IUFO_MEASURE_PUBDATA);
//			sbSqlReturn.append(".formula_id ");
//			if (env.getMeasureEnv().getFormulaID() == null) {
//				sbSqlReturn.append(" is null ");
//			} else {
//				sbSqlReturn.append("='");
//				sbSqlReturn.append(env.getMeasureEnv().getFormulaID());
//				sbSqlReturn.append("' ");
//			}
			if (objCondExpr != null && sbExprSQL.length() > 0) { // 逻辑表达式
				sbSqlReturn.append(" and ");
				sbSqlReturn.append("  (");
				sbSqlReturn.append(sbExprSQL.toString());
				// 下面是专门为了处理oracle8.0.5的一个bug而加的
				sbSqlReturn.append(')');
			}
			if (bWhereAdded) {
				sbSqlReturn.append(" and ");
				sbSqlReturn.append(sbFrom.substring(sbFrom.toString().indexOf(
						"where") + 5));
			}

			return sbSqlReturn.toString();
		} catch (ParseException pe) {
			AppDebug.debug(pe);//@devTools pe.printStackTrace(System.out);
			throw new TranslateException(pe.getMessage());
		} catch (OprException ope) {
			AppDebug.debug(ope);//@devTools ope.printStackTrace(System.out);
			throw new TranslateException(ope);
		} catch (Exception e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new TranslateException(e.getMessage());
		} finally {
			env.setDynAreaInfo(null, curKeyData);
			env.setKeys(objEnvKeys);
			env.setRepPK(strEnvRepPK);
			if (alMeasInEnv == null) {
				env.removeExEnv(CommonExprCalcEnv.EX_MEASVOINSQL);
			} else {
				env.setExEnv(CommonExprCalcEnv.EX_MEASVOINSQL, alMeasInEnv);
			}

		}
	}

	/**
	 * 得到统计函数的值，这是指标统计函数中的指标与当前区域无关。 创建日期：(2002-5-24 13:04:38)
	 *
	 * @return double
	 * @param mVOs
	 *            nc.vo.iufo.measure.MeasureVO[]这些指标的数据应该是保存在一个表内的。
	 * @param objCondExpr
	 *            UfoExpr
	 * @param env
	 *            UfoCalcEnv
	 */
	private String getAggrSQL(short nFID, IStoreCell[] mVOs,
			String strKeyGroupPK, UfoExpr objCondExpr, UfoCalcEnv env,
			String strDbType) throws TranslateException {
		// 组成SQL语句
		// 得到任务类型
		String strEnvRepPK = env.getRepPK();
		nc.vo.iufo.keydef.KeyVO[] objEnvKeys = env.getKeys();

		ArrayList alMeasInEnv = (ArrayList) env
				.getExEnv(CommonExprCalcEnv.EX_MEASVOINSQL);
		env.removeExEnv(CommonExprCalcEnv.EX_MEASVOINSQL);
		try {
			if(mVOs[0] instanceof MeasureVO) {
				env.setRepPK(((MeasureVO)mVOs[0]).getReportPK());
			} else {
				env.setRepPK(mVOs[0].getReportPK());
			}
			nc.vo.iufo.keydef.KeyGroupVO objKG = env.getKeyGroupCache()
					.getByPK(strKeyGroupPK);
			if (objKG == null) {
				throw new TranslateException(TranslateException.ERR_KEYGROUP);
			}
			env.setKeys(objKG.getKeys());
			nc.vo.iufo.keydef.KeyVO[] objKeys = env.getKeys();
			if (objKeys != null) {
				for (int i = 0; i < objKeys.length; i++) {
					if (objKeys[i].getCode() != null
							&& objKeys[i].getCode().equals(
									nc.vo.iufo.keydef.KeyVO.CODE_TYPE_CORP)) {
						objCondExpr = UserCalcRightUtil.applyUserRightToCond(
								objCondExpr, env, strKeyGroupPK);
					}
				}
			}
			// 转换条件 select alone_id from iufo_measure_pub_data where XXX
			Hashtable<String,String> hashTables = new Hashtable<String,String>();// 条件中引用的表名
			Hashtable<Integer,String> hashTableByKeyPos=new Hashtable<Integer,String>();
			StringBuffer sbSQL = new StringBuffer();

			ArrayList alMeasOperand = new ArrayList();
			if (objCondExpr != null) {
				ExprUtil.getElementsByClass(objCondExpr, alMeasOperand,
						MeasOperand.class);
				if (alMeasOperand != null && alMeasOperand.size() > 0) {
					for (int i = 0; i < alMeasOperand.size(); i++) {
						alMeasOperand.set(i, ((MeasOperand) alMeasOperand
								.get(i)).getMeasureVO());
					}
				}
			}
			alMeasOperand.add(mVOs[0]);
			env.setExEnv(CommonExprCalcEnv.EX_MEASVOINSQL, alMeasOperand);
			ExprToSqlTranslator.toSQLCommon(strKeyGroupPK, objCondExpr,
					hashTables,hashTableByKeyPos, sbSQL, env, strDbType);
			StringBuffer sbFromWhere = new StringBuffer();
			sbFromWhere.append("select ");
			String strFuncDBName = MeasFuncDriver.getDBFuncName(nFID, strDbType);

			String strPubTable=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(strKeyGroupPK).getTableName();
			if (!hashTables.contains(strPubTable)) {
				hashTables.put(strPubTable,strPubTable);
			}

			for (int i = 0; i < mVOs.length; i++) {
				if (i > 0) {
					sbFromWhere.append(',');
				}
				IStoreCell mVO = mVOs[i];
				if (!hashTables.containsKey(mVO.getDbtable())) {
					hashTables.put(mVO.getDbtable(), mVO.getDbtable());
				}
				sbFromWhere.append(strFuncDBName);
				sbFromWhere.append('(');
				sbFromWhere.append(mVO.getDbtable());
				sbFromWhere.append('.');
				sbFromWhere.append(mVO.getDbcolumn());
				sbFromWhere.append(") ");
				sbFromWhere.append(COLALIASPRFIX);
				sbFromWhere.append(i + 1);
			}
			StringBuffer sbWhere = new StringBuffer();
			boolean bWhereAdded = ExprToSqlTranslator.toFromWhereClause(
					hashTables,hashTableByKeyPos, sbWhere,strPubTable);

			if (bWhereAdded) {
				sbFromWhere.append(sbWhere.substring(0, sbWhere.toString()
						.indexOf("where")));
			} else {
				sbFromWhere.append(sbWhere);
			}
			sbFromWhere.append(" where ");
			if (env.getMeasureEnv().getVer() != -100) {
				sbFromWhere.append(strPubTable);
				sbFromWhere.append(".ver=" + env.getMeasureEnv().getVer()); // 决算版本
			}
//			sbFromWhere.append(" and ");
////			if (objKG.isContainsPrivatekey()) {
////				sbFromWhere.append(mVOs[0].getDbtable() + ".pk_key_comb");
////			} else {
//				sbFromWhere
//						.append(nc.vo.iufo.pub.IDatabaseNames.IUFO_MEASURE_PUBDATA
//								+ ".ktype");
////			}
//			sbFromWhere.append("='");
//			sbFromWhere.append(strKeyGroupPK);
//			sbFromWhere.append('\'');
//			sbFromWhere.append(" and ");
//			sbFromWhere
//					.append(nc.vo.iufo.pub.IDatabaseNames.IUFO_MEASURE_PUBDATA);
//			sbFromWhere.append(".formula_id ");
//			if (env.getMeasureEnv().getFormulaID() == null) {
//				sbFromWhere.append(" is null ");
//			} else {
//				sbFromWhere.append("='");
//				sbFromWhere.append(env.getMeasureEnv().getFormulaID());
//				sbFromWhere.append("' ");
//			}
			//tianchuan ++ 在这里进行一次容错的操作，如果"("或")"配对有问题，则自动补充缺失的括号
			checkAndCorrectExprSql(sbSQL);
			
			if (objCondExpr != null && sbSQL.length() > 0) { // 逻辑表达式
				sbFromWhere.append(" and ");
				sbFromWhere.append("  (");
				sbFromWhere.append(sbSQL.toString());
				// 下面是专门为了处理oracle8.0.5的一个bug而加的
				sbFromWhere.append(')');
			}
			if (bWhereAdded) {
				sbFromWhere.append(" and ");
				sbFromWhere.append(sbWhere.substring(sbWhere.toString()
						.indexOf("where") + 5));
			}

			return sbFromWhere.toString();
		} catch (ParseException pe) {
			AppDebug.debug(pe);//@devTools pe.printStackTrace(System.out);
			throw new TranslateException(pe.getMessage());
		} catch (OprException ope) {
			AppDebug.debug(ope);//@devTools ope.printStackTrace(System.out);
			throw new TranslateException(ope);
		} catch (Exception e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new TranslateException(e.getMessage());
		} finally {

			env.setKeys(objEnvKeys);
			env.setRepPK(strEnvRepPK);
			if (alMeasInEnv == null) {
				env.removeExEnv(CommonExprCalcEnv.EX_MEASVOINSQL);
			} else {
				env.setExEnv(CommonExprCalcEnv.EX_MEASVOINSQL, alMeasInEnv);
			}
		}
	}

	/*
	 * tianchuan ++
	 * 检查并更正条件SQL语句。该方法是针对一些特定的括号缺失错误而设立。
	 */
	private void checkAndCorrectExprSql(StringBuffer sbSQL){
		if(sbSQL!=null){
			//多余的左括号数
			int moreLeft=0;
			for(int i=0;i<sbSQL.length();i++){
				if(sbSQL.charAt(i)=='('){
					moreLeft++;
				}else if(sbSQL.charAt(i)==')'){
					moreLeft--;
				}
			}
			if(moreLeft>0){	//大于0，说明缺失右括号，补充相应数量的右括号
				for(int i=0;i<moreLeft;i++){
					sbSQL.append(')');
				}
			}else if(moreLeft<0){//小于0，说明缺失左括号，补充相应数量的左括号
				int count=Math.abs(moreLeft);
				for(int i=0;i<count;i++){
					sbSQL.insert(0, '(');
				}
			}
		}
	}
	
	/**
	 * 批量读取指标的统计函数值对。 在参数alMeas中的第一个元素是一个Object[]对象，
	 * Object[0]是这组指标的关键字组和PK，Object[1]是UfoExpr是这祖指标的条件，
	 * alMeas从第一个位置之后全部是MeasureVO对象
	 * 这个方法会读出符合函数条件的所有值，并以key=MeasurePubDataVO,value=Hashtable(key=measurePK,
	 * value= Double|String)的形式保存。 创建日期：(2003-8-11 11:16:17)
	 *
	 * @return java.util.Hashtable
	 * @param alMeas
	 *            java.util.ArrayList
	 * @param env
	 *            UfoCalcEnv
	 * @exception TranslateException
	 *                异常说明。
	 */
	public Hashtable getAggrValue(short nFID, ArrayList alMeas, UfoCalcEnv env)
			throws TranslateException {
		Object[] objCond = (Object[]) alMeas.get(0);
		IStoreCell[] objMeasures = new IStoreCell[alMeas.size() - 1];
		for (int i = 1; i < alMeas.size(); i++) {
			objMeasures[i - 1] = (IStoreCell) alMeas.get(i);
		}
		// [0]是表对应的from之前的字段（key=dbtable,value=columns）
		// [1]表示在一个表中的指标（key=dbtable, value=ArrayList(MeasureVO)）[0]与[1]的key一致
		Hashtable[] hashMeasSQL = getMeasureSQL(objMeasures);

		return getAggrValues(nFID, hashMeasSQL, (String) objCond[0],
				(UfoExpr) objCond[1], env, getDBType(env));
	}

	/**
	 * 读取msum,mcount,mmin,mmax,mavg函数的相关值，返回结果。 创建日期：(2002-5-17 9:37:51)
	 *
	 * @return java.util.Hashtable key=(keyValue+\r\n)* value = Double|String *
	 *         创建日期：(2003-8-11 14:03:30)
	 * @return double
	 * @param nFuncID
	 *            short
	 * @param objMeasure
	 *            nc.vo.iufo.measure.MeasureVO
	 * @param objCondExpr
	 *            com.ufsoft.iufo.util.expression.UfoExpr
	 * @param objEnv
	 *            UfoCalcEnv
	 * @exception TranslateException
	 *                异常说明。
	 */
	public Object getAggrValue(short nFuncID, IStoreCell objMeasure,
			UfoExpr objCondExpr, UfoCalcEnv objEnv) throws TranslateException {
		try {

			// 指标没有与数据库表关联时，返回0
			if (objMeasure != null && objMeasure.getDbtable() == null) {
				return 0;
			}
			if (objMeasure == null) {
				throw new TranslateException(TranslateException.ERR_FUNC);
			}

			// 处理条件
			// 在统计条件中追加用户权限
			String repPk = null;
			if(objMeasure instanceof MeasureVO) {
				repPk = ((MeasureVO)objMeasure).getReportPK();
			} else {
//				repPk = objEnv.getRepPK();
				repPk = objMeasure.getReportPK();
			}
			ReportVO[] objReps = objEnv.getReportCache().getByPks(
					new String[] { repPk });
			if (objReps == null || objReps[0] == null) {
				throw new TranslateException(TranslateException.ERR_KEYGROUP);
			}
			KeyGroupVO objKG = objEnv.getKeyGroupCache().getByPK(
					objReps[0].getPk_key_comb());
			if (objKG == null) {
				throw new TranslateException(TranslateException.ERR_KEYGROUP);
			}

			if (nFuncID == MeasFuncDriver.FMCOUNT) {
				return calcCountValue(objMeasure, objCondExpr, objEnv, getDBType(objEnv));
			} else
				return calcAggrValue(nFuncID, objMeasure, objCondExpr, objEnv,
						getDBType(objEnv));

		} catch (Exception e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new TranslateException(e.getMessage());
		}
	}

	/**
	 * 计算指定指标在指定条件下涉及的值。函数包括mcount, msum,mmax,mmin,mavg 创建日期：(2003-8-11
	 * 14:03:30)
	 *
	 * @return java.util.Hashtable key=measurePK,value = 指标统计值,
	 * @param hashMeasureSQL
	 *            Hashtable[] //[0]是表对应的from之前的字段（key=dbtable,value=columns）
	 *            //[1]表示在一个表中的指标（key=dbtable,
	 *            value=ArrayList(MeasureVO)）[0]与[1]的key一致
	 * @param strKeyGroupPK
	 *            String
	 * @param objCondExpr
	 *            com.ufsoft.iufo.util.expression.UfoExpr
	 * @param objEnv
	 *            UfoCalcEnv
	 * @exception TranslateException
	 *                异常说明。
	 */
	private Hashtable getAggrValues(short nFID, Hashtable[] hashMeasureSQL,
			String strKeyGroupPK, UfoExpr objCondExpr, UfoCalcEnv objEnv,
			String strDbType) throws TranslateException {

		if (hashMeasureSQL == null) {
			return null;
		}

		if (strKeyGroupPK == null) {
			throw new TranslateException(TranslateException.ERR_KEYGROUP);
		}

		Hashtable hashDatas = new Hashtable();
		Enumeration enMeasKey = hashMeasureSQL[1].keys();
		while (enMeasKey.hasMoreElements()) {
			ArrayList alMeas = (ArrayList) hashMeasureSQL[1].get(enMeasKey
					.nextElement());
			IStoreCell[] objMeasures = new IStoreCell[alMeas.size()];
			alMeas.toArray(objMeasures);
			if (objMeasures[0].getDbtable() != null) {
				hashDatas.putAll(calcAggrValue(nFID, strKeyGroupPK,
						objMeasures, objCondExpr, objEnv, strDbType));
			}
		}

		return hashDatas;

	}

	/**
	 * 根据条件生成where 条件。这里的条件表达式已经不租了默认项 创建日期：(2003-8-11 14:07:17)
	 *
	 * @return String
	 * @param objKeyCondExpr
	 *            com.ufsoft.iufo.util.expression.UfoExpr
	 * @param nVer
	 *            int
	 * @param objEnv
	 *            UfoCalcEnv
	 * @param exprKeyCond
	 *            当batchKeyDatas不为空时则表示非时间条件；batchKeyDatas时则表示所有条件
	 * @param exprTimeKeyCond
	 *            时间条件
	 * @exception TranslateException
	 *                异常说明。
	 */
	public StringBuffer getCondSQL(String strKeyGroupPK, UfoExpr exprKeyCond,
			UfoExpr exprTimeKeyCond, DatePropVO objDateProp, Double nOffset,
			Integer nVer, UfoCalcEnv objEnv,
			Map<String,String> hashTables,
			Hashtable<Integer,String> hashTableByKeyPos,
			KeyDataGroup[] batchKeyDatas, String[] strExprRefDynKeys,
			String strMainTimeKey, String strDbType) throws TranslateException {

		try {

//			UfoEElement[] objEles = exprKeyCond == null ? null : exprKeyCond
//					.getElements();
//			String strUnitID = null;
			//TODO:实在不知道此处是如何处理的，暂时注释掉
//			if (objEles != null) {
//				for (int i = 0; i < objEles.length; i++) {
//					if (objEles[i].getType() == UfoEElement.OPR
//							&& objEles[i].getObj() instanceof KFunc) {
//						KFunc objFunc = (KFunc) objEles[i].getObj();
////						if (((KeyWordVO) objFunc.getParams().get(0)).getKey()
////								.getName().equals(
////										nc.vo.iufo.unit.UnitPropVO.DWBM)) {
//							strUnitID = ((StrOperand) objEles[i + 1].getObj())
//									.toString(null);
//							break;
////						}
//					}
//				}
//			}
//			if (strUnitID != null) {
				exprKeyCond = UserCalcRightUtil.applyUserRightToCond(
						exprKeyCond, objEnv, strKeyGroupPK);
//			}

			String strPubTable=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(strKeyGroupPK).getTableName();

			StringBuffer sbSQL = new StringBuffer();
			if (exprKeyCond != null && exprKeyCond.getElementLength() > 0) {

				// 如果批量进行指标取数函数，则分割表达式中同动态区域关键字相关的条件部分。 add by ljhua 2005-6-1
				if (batchKeyDatas != null && batchKeyDatas.length > 0
						&& objEnv instanceof ReportDynCalcEnv) {

					KeyDataGroup curKeyData = ((ReportDynCalcEnv) objEnv)
							.getKeyDatas();
					Object oldExZKeyValue = objEnv
							.getExEnv(CommonExprCalcEnv.EX_ZKEYVALUES);

					try {
						objEnv.removeExEnv(CommonExprCalcEnv.EX_ZKEYVALUES);

						// UfoExpr[]
						// exprNews=exprNews=MeasCondExprUtil.seperateExpr(objKeyCondExpr,strExprRefDynKeys,strMainTimeKey);
						UfoExpr[] exprNews = MeasCondExprUtil.seperateExpr(
								exprKeyCond, strExprRefDynKeys, strMainTimeKey);

						// 判断是否引用动态区时间关键字
						ArrayList listRefKey = new ArrayList();
						listRefKey.addAll(Arrays.asList(strExprRefDynKeys));
						String strDynTimeKey =DatePropVO.getTTimeKey(listRefKey);

						StringBuffer strBufMain = new StringBuffer();
//						if (true) {
							// 获得与动态区无关的条件sql
							UfoExpr exprCondTemp = exprNews[0];
							if (strDynTimeKey == null) {
								// 补充时间条件
								nc.vo.iufo.keydef.KeyVO measTimeKey = ReplenishKeyCondUtil
										.getMeasTimeKey(objEnv, strKeyGroupPK);
								;

								UfoExpr timeCondTemp = ReplenishKeyCondUtil
										.getAppendTimeExpr(
												exprKeyCond,
												exprTimeKeyCond,
												objDateProp,
												nOffset,
												measTimeKey,
												objEnv,
												ReplenishKeyCondUtil.TIMECOND_TYEP_SQL);
								exprCondTemp = ReplenishKeyCondUtil
										.combineBoolExpr(exprNews[0],
												timeCondTemp);
							}

							ExprToSqlTranslator.toSQLCommon(strKeyGroupPK,
									exprCondTemp, hashTables,hashTableByKeyPos, strBufMain,
									objEnv, strDbType);
//						}
						String strSubTemp = null;
						if (exprNews[1] != null) {
							int iRowSize = batchKeyDatas.length;
							StringBuffer[] strBufTemp = new StringBuffer[iRowSize];
							for (int i = 0; i < iRowSize; i++) {
								((ReportDynCalcEnv) objEnv).setDynAreaInfo(
										null, batchKeyDatas[i]);

								// 补充时间条件
								UfoExpr exprCondTempNew = exprNews[1];
								if (strDynTimeKey != null) {
									// 获得指标的时间关键字
									nc.vo.iufo.keydef.KeyVO measTimeKey = ReplenishKeyCondUtil
											.getMeasTimeKey(objEnv,
													strKeyGroupPK);

									UfoExpr timeCondTemp = ReplenishKeyCondUtil
											.getAppendTimeExpr(
													exprKeyCond,
													exprTimeKeyCond,
													objDateProp,
													nOffset,
													measTimeKey,
													objEnv,
													ReplenishKeyCondUtil.TIMECOND_TYEP_SQL);
									exprCondTempNew = ReplenishKeyCondUtil
											.combineBoolExpr(exprCondTempNew,
													timeCondTemp);
								}

								strBufTemp[i] = new StringBuffer();
								ExprToSqlTranslator.toSQLCommon(strKeyGroupPK,
										exprCondTempNew, hashTables,hashTableByKeyPos,
										strBufTemp[i], objEnv, strDbType);
							}
							// 组织各子条件
							strSubTemp = combineStringBuf(strBufTemp);
						}
						sbSQL.append(strBufMain);
						if (strSubTemp != null && strSubTemp.length() > 0) {
							if (strBufMain.length() > 0)
								sbSQL.append(" and (");
							sbSQL.append(strSubTemp);
							if (strBufMain.length() > 0)
								sbSQL.append(")");
						}

					} finally {

						if (oldExZKeyValue != null)
							objEnv.setExEnv(CommonExprCalcEnv.EX_ZKEYVALUES,
									oldExZKeyValue);
						if (objEnv instanceof ReportDynCalcEnv)
							((ReportDynCalcEnv) objEnv).setDynAreaInfo(null,
									curKeyData);
					}

				} else {
					ExprToSqlTranslator.toSQLCommon(strKeyGroupPK, exprKeyCond,
							hashTables, hashTableByKeyPos,sbSQL, objEnv, strDbType);
				}
			}

			if (sbSQL.length() > 0) {
				sbSQL.append(" and ");
			}
			sbSQL.append(strPubTable);
			sbSQL.append('.');
			sbSQL.append("ver=");
			if (nVer == null) {
				sbSQL.append(objEnv.getMeasureEnv().getVer());
			} else {
				sbSQL.append(nVer);
			}
//			sbSQL.append(" and ");
//			sbSQL.append(nc.vo.iufo.pub.IDatabaseNames.IUFO_MEASURE_PUBDATA);
//			sbSQL.append(".formula_id ");
//			//yp 如果版本号为0,则formulaid为空
//			if (objEnv.getMeasureEnv().getFormulaID() != null ){
//				sbSQL.append("='");
//				sbSQL.append(objEnv.getMeasureEnv().getFormulaID());
//				sbSQL.append("' ");
//			}
//			else if (objEnv.getMeasureEnv().getFormulaID() == null && (nVer != null && nVer > HBBBSysParaUtil.VER_HEBING_FIRST && nVer < HBBBSysParaUtil.VER_HEBING_LAST )) {
////				if(Util.isHBRepDataRelatingWithTask())
////				sbSQL.append(" ='").append(objEnv.getTaskPK()).append("' ");
////				else
//					sbSQL.append(" is null ");//(nVer != null && nVer == 0)
//			}
//			else if (objEnv.getMeasureEnv().getFormulaID() == null ){
//				sbSQL.append(" is null ");
//			}
			//TODO:报表管理结构中权限的判断，待完善
//			if (strUnitID != null && objEnv.getMeasureEnv().getUnitPK() != null) {
//				sbSQL.append(" and ");
//				sbSQL.append(strPubTable);
//				sbSQL.append('.');
//				sbSQL.append("keyword1 in (select code from ");
//				sbSQL.append(nc.vo.iufo.pub.IDatabaseNames.IUFO_UNIT_INFO_TABLE);
//				sbSQL.append(" where ");
//				sbSQL.append(" level_code like '");
//				String strLevelCode = null;
//				strLevelCode = objEnv.getUnitCache().getUnitInfoByPK(
//						objEnv.getMeasureEnv().getUnitPK()).getPropValue(
//						UnitPropVO.BASEORGPK);
//				sbSQL.append(strLevelCode);
//				sbSQL.append("%')");
//			}
			if (!hashTables
					.containsKey(strPubTable)) {
				hashTables.put(strPubTable,strPubTable);
			}
			return sbSQL;
		} catch (OprException ope) {
			AppDebug.debug(ope);//@devTools ope.printStackTrace(System.out);
			throw new TranslateException(ope);
		} catch (ParseException e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new TranslateException(e.getMessage());
		} finally {

		}
	}

	/**
	 * 得到统计函数的SQL。 创建日期：(2002-5-24 13:04:38)
	 *
	 * @return double
	 * @param mVOs
	 *            nc.vo.iufo.measure.MeasureVO[]这些指标的数据应该是保存在一个表内的。
	 * @param objCondExpr
	 *            UfoExpr
	 * @param env
	 *            UfoCalcEnv
	 */
	private String getCountSQL(IStoreCell mVO, String strKeyGroupPK,
			UfoExpr objCondExpr, UfoCalcEnv env, String strDbType)
			throws TranslateException {
		// 组成SQL语句
		// 得到任务类型
		String strEnvRepPK = env.getRepPK();
		nc.vo.iufo.keydef.KeyVO[] objEnvKeys = env.getKeys();

		ArrayList alMeasInEnv = (ArrayList) env
				.getExEnv(CommonExprCalcEnv.EX_MEASVOINSQL);
		env.removeExEnv(CommonExprCalcEnv.EX_MEASVOINSQL);
		try {

			if(mVO instanceof MeasureVO) {
				env.setRepPK(((MeasureVO)mVO).getReportPK());
			} else {
				env.setRepPK(mVO.getReportPK());
			}

			nc.vo.iufo.keydef.KeyGroupVO objKG = env.getKeyGroupCache()
					.getByPK(strKeyGroupPK);
			if (objKG == null) {
				throw new TranslateException(TranslateException.ERR_KEYGROUP);
			}
			env.setKeys(objKG.getKeys());
			nc.vo.iufo.keydef.KeyVO[] objKeys = objKG.getKeys();
			if (objKeys != null) {
				for (int i = 0; i < objKeys.length; i++) {
					if (objKeys[i].getCode() != null
							&& objKeys[i].getCode().equals(
									nc.vo.iufo.keydef.KeyVO.CODE_TYPE_CORP)) {
						objCondExpr = UserCalcRightUtil.applyUserRightToCond(
								objCondExpr, env, strKeyGroupPK);
						break;
					}
				}
			}

			String strPubTable=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(strKeyGroupPK).getTableName();

			// 转换条件 select alone_id from iufo_measure_pub_data where XXX

			Hashtable<String,String> hashTables = new Hashtable<String,String>();// 条件中引用的表名
			Hashtable<Integer,String> hashTableByKeyPos=new Hashtable<Integer,String>();
			StringBuffer sbSQL = new StringBuffer();

			ArrayList alMeasOperand = new ArrayList();
			if (objCondExpr != null) {
				ExprUtil.getElementsByClass(objCondExpr, alMeasOperand,
						MeasOperand.class);
				if (alMeasOperand != null && alMeasOperand.size() > 0) {
					for (int i = 0; i < alMeasOperand.size(); i++) {
						alMeasOperand.set(i, ((MeasOperand) alMeasOperand
								.get(i)).getMeasureVO());
					}
				}
			}
			alMeasOperand.add(mVO);
			env.setExEnv(CommonExprCalcEnv.EX_MEASVOINSQL, alMeasOperand);

			hashTables.put("meas_table_column", mVO.getDbtable() + "." + mVO.getDbcolumn());
			ExprToSqlTranslator.toSQLCommon(strKeyGroupPK, objCondExpr,
					hashTables,hashTableByKeyPos, sbSQL, env, strDbType);
			hashTables.remove("meas_table_column");

			StringBuffer sbFromWhere = new StringBuffer();
			sbFromWhere.append("select count(");
			sbFromWhere.append(mVO.getDbtable());
			sbFromWhere.append('.');
			sbFromWhere.append(mVO.getDbcolumn());
			sbFromWhere.append(") ");
			StringBuffer sbWhere = new StringBuffer();

			if (env.getMeasureEnv().getVer() != -100) {
				hashTables.put(strPubTable, strPubTable);
			}

			hashTables.put(mVO.getDbtable(), mVO.getDbtable());
			boolean bWhereAdded = ExprToSqlTranslator.toFromWhereClause(
					hashTables,hashTableByKeyPos, sbWhere,strPubTable);
			if (bWhereAdded) {
				sbFromWhere.append(sbWhere.substring(0, sbWhere.toString()
						.indexOf("where")));
			} else {
				sbFromWhere.append(sbWhere);
			}
			sbFromWhere.append(" where ");
			if (env.getMeasureEnv().getVer() != -100) {
				sbFromWhere
						.append(strPubTable);
				sbFromWhere.append(".ver=" + env.getMeasureEnv().getVer());
			}
//			sbFromWhere.append(" and ");
////			if (objKG.isContainsPrivatekey()) {
////				sbFromWhere.append(mVO.getDbtable() + ".pk_key_comb");
////			} else {
//				sbFromWhere
//						.append(strPubTable
//								+ ".ktype");
////			}
//			sbFromWhere.append("='");
//			sbFromWhere.append(strKeyGroupPK);
//			sbFromWhere.append('\'');
//			sbFromWhere.append(" and ");
//			sbFromWhere
//					.append(nc.vo.iufo.pub.IDatabaseNames.IUFO_MEASURE_PUBDATA);
//			sbFromWhere.append(".formula_id ");
//			if (env.getMeasureEnv().getFormulaID() == null) {
//				sbFromWhere.append(" is null ");
//			} else {
//				sbFromWhere.append("='");
//				sbFromWhere.append(env.getMeasureEnv().getFormulaID());
//				sbFromWhere.append("' ");
//			}

			if (objCondExpr != null && sbSQL.length() > 0) { // 逻辑表达式
				sbFromWhere.append(" and ");
				sbFromWhere.append("  (");
				sbFromWhere.append(sbSQL.toString());
				// 下面是专门为了处理oracle8.0.5的一个bug而加的
				sbFromWhere.append(')');
			}
			if (bWhereAdded) {
				sbFromWhere.append(" and ");
				sbFromWhere.append(sbWhere.substring(sbWhere.toString()
						.indexOf("where") + 5));
			}

			return sbFromWhere.toString();
		} catch (ParseException e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new TranslateException(e.getMessage());
		} catch (OprException ope) {
			AppDebug.debug(ope);//@devTools ope.printStackTrace(System.out);
			throw new TranslateException(ope);
		} catch (Exception e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new TranslateException(e.getMessage());
		} finally {
			env.setRepPK(strEnvRepPK);
			env.setKeys(objEnvKeys);

			if (alMeasInEnv == null) {
				env.removeExEnv(CommonExprCalcEnv.EX_MEASVOINSQL);
			} else {
				env.setExEnv(CommonExprCalcEnv.EX_MEASVOINSQL, alMeasInEnv);
			}
		}

	}

	/**
	 * 批量计算指标的计算函数值。 在参数alMeas中的第一个元素是一个Object[]对象，
	 * Object[0]是这组指标的关键字组和PK，Object[1]是UfoExpr是这祖指标的条件，
	 * alMeas从第一个位置之后全部是MeasureVO对象 这个方法会直接计算出函数值。 创建日期：(2003-8-11 11:16:17)
	 *
	 * @return java.util.Hashtable
	 * @param alMeas
	 *            java.util.ArrayList
	 * @param env
	 *            UfoCalcEnv
	 * @exception TranslateException
	 *                异常说明。
	 */
	public Hashtable getCountValue(ArrayList alMeas, UfoCalcEnv env)
			throws TranslateException {
		try {
			if (alMeas == null) {
				return new Hashtable();
			}
			Object[] objConds = (Object[]) alMeas.get(0);
			if (objConds == null) {
				return new Hashtable();
			}
			String strKeyGroupPK = (String) objConds[0];
			if (strKeyGroupPK == null) {
				throw new TranslateException(TranslateException.ERR_KEYGROUP);
			}
			// UfoExpr objCond =
			// MeasFunc.applyUserRightToCond(
			// (UfoExpr) objConds[1],
			// env,
			// strKeyGroupPK);
			UfoExpr objCond = (UfoExpr) objConds[1];
			MeasureVO[] objMeasures = new MeasureVO[alMeas.size() - 1];
			for (int i = 0; i < objMeasures.length; i++) {
				objMeasures[i] = (MeasureVO) alMeas.get(i + 1);
			}
			Hashtable[] hashMeasSQL = getMeasureSQL(objMeasures);
			Hashtable hashData = new Hashtable();
			Enumeration enKey = hashMeasSQL[1].elements();
			MeasFuncDMO objMeasFuncDMO = getMeasFuncDMO();
			while (enKey.hasMoreElements()) {
				ArrayList alMeases = (ArrayList) enKey.nextElement();
				if (alMeases == null || alMeases.size() == 0) {
					continue;
				}
				MeasureVO[] objMeases = new MeasureVO[alMeases.size()];
				alMeases.toArray(objMeases);
				Double nValue = new Double(1);
				if (objMeases[0].getDbtable() != null) {
					String strCondSql = getCountSQL(objMeases[0],
							strKeyGroupPK, objCond, env, getDBType(env));
					nValue = (Double) objMeasFuncDMO.getValue(strCondSql, true,
							false);
				}
				for (int i = 0; i < objMeases.length; i++) {
					hashData.put(objMeases[i].getCode(), nValue);
				}
			}
			return hashData;
		} catch (java.sql.SQLException e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_NODATA);
		} catch (javax.naming.NamingException ne) {
			AppDebug.debug(ne);//@devTools ne.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_NAMING);
		} catch (nc.bs.pub.SystemException se) {
			AppDebug.debug(se);//@devTools se.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_PUBSYSTEM);
		}/*
			 * catch (ParseException e) { e.printStackTrace(System.out); throw
			 * new TranslateException(e.getMessage()); } catch (OprException
			 * ope) { ope.printStackTrace(System.out); throw new
			 * TranslateException(ope.getMessage()); }
			 */
	}

	/**
	 * 此处插入方法描述。 创建日期：(2003-8-20 19:55:46)
	 *
	 * @return nc.bs.iufo.calculate.MeasFuncDMO
	 */
	private MeasFuncDMO getMeasFuncDMO() throws javax.naming.NamingException,
			nc.bs.pub.SystemException {
		if (m_objMeasFuncDMO == null) {
			m_objMeasFuncDMO = new MeasFuncDMO();
		}
		return m_objMeasFuncDMO;
	}

	/**
	 * 对指标编写from之前的sql，目的是将不同数据库表中的指标用不同的sql语句查询。 创建日期：(2003-8-11 16:29:44)
	 *
	 * @return Hashtable[][0]是表对应的from之前的字段（key=dbtable,value=columns）[1]表示在一个表中的指标（key=dbtable,
	 *         value=ArrayList(MeasureVO)）[0]与[1]的key一致
	 * @param objMeasures
	 *            nc.vo.iufo.measure.MeasureVO[]
	 */
	public Hashtable[] getMeasureSQL(IStoreCell[] objMeasures) {
		if (objMeasures == null) {
			return null;
		}
		Hashtable hashSQL = new Hashtable();
		Hashtable hashMeas = new Hashtable();
		KeyGroupVO keyGroup=null;
		for (int i = 0; i < objMeasures.length; i++) {
			if (objMeasures[i] == null) {
				continue;
			}
			if (keyGroup==null){
				keyGroup=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(objMeasures[i].getKeyCombPK());
			}
			if (objMeasures[i].getDbcolumn() == null) {
				if (!hashMeas.contains(keyGroup.getTableName())) {
					hashMeas.put(keyGroup.getTableName(),
							new ArrayList());
					hashSQL.put(keyGroup.getTableName(),
							keyGroup.getTableName());
				}
				ArrayList alMeas = (ArrayList) hashMeas
						.get(keyGroup.getTableName());
				alMeas.add(objMeasures[i]);
				continue;
			}
			if (!hashSQL.containsKey(objMeasures[i].getDbtable())) {
				hashSQL.put(objMeasures[i].getDbtable(), new StringBuffer());
				hashMeas.put(objMeasures[i].getDbtable(), new ArrayList());
			}
			ArrayList alMeas = (ArrayList) hashMeas.get(objMeasures[i]
					.getDbtable());
			if(alMeas.contains(objMeasures[i])) {
				continue;
			}
			alMeas.add(objMeasures[i]);
			StringBuffer sbSQL = (StringBuffer) hashSQL.get(objMeasures[i]
					.getDbtable());
			if (alMeas.size() > 1) {
				sbSQL.append(',');
			}
			sbSQL.append(objMeasures[i].getDbtable());
			sbSQL.append('.');
			sbSQL.append(objMeasures[i].getDbcolumn());
			sbSQL.append(' ');
			sbSQL.append(COLALIASPRFIX);
			sbSQL.append(alMeas.size());
		}

		return new Hashtable[] { hashSQL, hashMeas };
	}

	/**
	 * 批量处理动态区域指标取数函数的计算
	 *
	 * @param objMeasures
	 * @param strKeyGroupPK
	 * @param objDateProp
	 * @param nOffset
	 * @param objKeyCondExpr
	 * @param nVer
	 * @param objEnv
	 * @param batchKeyDatas
	 * @param strExprRefDynKeys
	 * @param strMainTimeKey
	 * @return
	 * @throws TranslateException
	 */
	private Hashtable batchDynSelectValue(IStoreCell[] objMeasures,
			String strKeyGroupPK, UfoExpr exprNotTimeKeyCond,
			UfoExpr exprTimeKeyCond, DatePropVO objDateProp, Double nOffset,
			Integer nVer, UfoCalcEnv objEnv, KeyDataGroup[] batchKeyDatas,
			String[] strExprRefDynKeys, String strMainTimeKey, String strDbType)
			throws TranslateException {

		// 得到指标对应的sql,[0]是表对应的from之前的字段（key=dbtable,value=columns）
		// [1]表示在一个表中的指标（key=dbtable, value=ArrayList(MeasureVO)）[0]与[1]的key一致
		Hashtable[] hashMeasSqls = getMeasureSQL(objMeasures);
		
		//tianchuan 指标追踪处理
		boolean doTraceMeasure = objEnv.getExEnv(ICalcEnv.MEASURE_TRACE_FLAG) != null;
		HashMap<String, MeasureTraceVO> mtmap = null;
		if(doTraceMeasure){
			mtmap = new HashMap<String, MeasureTraceVO>();
		}
		
		Hashtable hashTemp = null;
		Hashtable hashReturn = new Hashtable();
		String strPubTable=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(strKeyGroupPK).getTableName();

		java.util.Enumeration enKey = hashMeasSqls[0].keys();
		while (enKey.hasMoreElements()) {
			String strTable = (String) enKey.nextElement();
			if (strTable.equals(strPubTable)) {
				continue;
			}
			java.util.ArrayList alMeas = (java.util.ArrayList) hashMeasSqls[1]
					.get(strTable);
			IStoreCell[] objMeases = new IStoreCell[alMeas.size()];
			alMeas.toArray(objMeases);

			// 计算同一张数据表的指标数值
			hashTemp = batchDynSelectFromOneTable(
					((StringBuffer) hashMeasSqls[0].get(strTable)).toString(),
					objMeases, strKeyGroupPK, exprNotTimeKeyCond,
					exprTimeKeyCond, objDateProp, nOffset, nVer, objEnv,
					batchKeyDatas, strExprRefDynKeys, strMainTimeKey, strDbType);

/*			//chxw. 处理指标公式追踪
			if(objEnv.isMeasureTrace()){
				MeasureTraceVO[] ttracevos = (MeasureTraceVO[])hashTemp.get(ICalcEnv.MEASURE_TRACE_FLAG);
				if(ttracevos != null){
//					int i =0;
//					MeasureTraceVO[] tracevos = new MeasureTraceVO[objMeasures.length];
//					for(MeasureVO mvo:objMeasures){
//						if(mvo == null){
//							tracevos[i++] = null;
//						} else{
//							for(MeasureTraceVO mtvo:ttracevos){
//								if(mtvo != null && mtvo.getMeasurePK().equals(mvo.getCode())){
//									tracevos[i++] = mtvo;
//									break;
//								}
//							}
//						}
//					}
					hashTemp.remove(ICalcEnv.MEASURE_TRACE_FLAG);
					hashReturn.put(ICalcEnv.MEASURE_TRACE_FLAG, ttracevos);
				}
			}*/
			//tianchuan 正确处理指标追踪
			if(doTraceMeasure){
				MeasureTraceVO[] ttracevos = (MeasureTraceVO[])hashTemp.get(ICalcEnv.MEASURE_TRACE_FLAG);
				if(ttracevos != null){
					for(MeasureTraceVO mt: ttracevos){
						if(mt!=null){
							mtmap.put(mt.getMeasurePK(), mt);
						}
					}
					hashTemp.remove(ICalcEnv.MEASURE_TRACE_FLAG);
					hashReturn.put(ICalcEnv.MEASURE_TRACE_FLAG, ttracevos);
				}
			}
			
			// 处理合并各表结果
			if (hashTemp != null && hashTemp.size() > 0) {
				Iterator iterTemp = hashTemp.keySet().iterator();
				while (iterTemp.hasNext()) {
					Object objKey = iterTemp.next();
					Hashtable hashSecond = (Hashtable) hashReturn.get(objKey);
					if (hashSecond == null) {
						hashSecond = new Hashtable();
						hashReturn.put(objKey, hashSecond);
					}
					hashSecond.putAll((Hashtable) hashTemp.get(objKey));
				}
			}

		}
		//tianchuan 正确处理指标追踪
		if(doTraceMeasure){
			int len = objMeasures.length;
			MeasureTraceVO[] vos = new MeasureTraceVO[len];
			for (int i = 0; i < len; i++) {
				if(objMeasures[i] != null){
					String mpk = objMeasures[i].getCode();
					MeasureTraceVO mt = mtmap.get(mpk);
					vos[i] = mt;
				}
			}
			hashReturn.put(ICalcEnv.MEASURE_TRACE_FLAG, vos);
		}
		
		if (hashReturn.size() == 0)
			hashReturn = null;

		return hashReturn;

	}

	/**
	 * 读取值定指标在制定条件下的值，env中保存了指标对应区域的值。 创建日期：(2003-8-11 14:07:17)
	 *
	 * @return Hashtable
	 * @param objMeasures
	 *            nc.vo.iufo.measure.MeasureVO[]
	 * @param objDateProp
	 *            nc.vo.iufo.keyword.DatePropVO
	 * @param nOffset
	 *            double
	 * @param objKeyCondExpr
	 *            com.ufsoft.iufo.util.expression.UfoExpr
	 * @param nVer
	 *            int
	 * @param objEnv
	 *            UfoCalcEnv
	 * @exception TranslateException
	 *                异常说明。
	 */
	private Hashtable getSelectValue(IStoreCell[] objMeasures,
			String strKeyGroupPK, DatePropVO objDateProp, Double nOffset,
			UfoExpr objKeyCondExpr, Integer nVer, UfoCalcEnv objEnv,
			String strDbType) throws TranslateException {

		// 补充条件
		objKeyCondExpr = ReplenishKeyCondUtil.replenishValueKeyCond(objEnv,
				strKeyGroupPK, objKeyCondExpr, objDateProp, nOffset,true);

		// 得到指标对应的sql,[0]是表对应的from之前的字段（key=dbtable,value=columns）
		// [1]表示在一个表中的指标（key=dbtable, value=ArrayList(MeasureVO)）[0]与[1]的key一致
		Hashtable[] hashMeasSqls = getMeasureSQL(objMeasures);

		java.util.Hashtable hashValue = new java.util.Hashtable();
		java.util.Enumeration enKey = hashMeasSqls[0].keys();

		String strPubTable=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(strKeyGroupPK).getTableName();

		//指标追踪处理 涉及循环，只好加此处理分支。 以后维护此代码的同辈们多多包涵：） v502 liuyy 2007-11-28
		boolean doTraceMeasure = objEnv.getExEnv(ICalcEnv.MEASURE_TRACE_FLAG) != null;
		HashMap<String, MeasureTraceVO> mtmap = null;
		if(doTraceMeasure){
			mtmap = new HashMap<String, MeasureTraceVO>();
		}

		while (enKey.hasMoreElements()) {
			String strTable = (String) enKey.nextElement();
			java.util.ArrayList alMeas = (java.util.ArrayList) hashMeasSqls[1]
					.get(strTable);
			IStoreCell[] objMeases = new IStoreCell[alMeas.size()];
			alMeas.toArray(objMeases);
			if (strTable.equals(strPubTable)) {
				for (int i = 0; i < objMeases.length; i++) {
					if (objMeases[i] == null) {
						continue;
					}
					if (objMeases[i].getType() == MeasureVO.TYPE_NUMBER) {
						hashValue.put(objMeases[i].getCode(), new Double(0));
					} else if(objMeases[i].getType() == MeasureVO.TYPE_BIGDECIMAL) {
						hashValue.put(objMeases[i].getCode(), new UFDouble(0));
					} else {
						hashValue.put(objMeases[i].getCode(), "");
					}
				}
				continue;
			}

			hashValue.putAll(getSelectValueFromOneTable(
					((StringBuffer) hashMeasSqls[0].get(strTable)).toString(),
					objMeases, strKeyGroupPK, objKeyCondExpr, nVer, objEnv,
					strDbType));


			if(doTraceMeasure){
				MeasureTraceVO[] vos = objEnv.getMeasureTraceVOs();
				for(MeasureTraceVO mt: vos){
					if(mt!=null){
						mtmap.put(mt.getMeasurePK(), mt);
					}
				}
			}
		}

		if(doTraceMeasure){
			int len = objMeasures.length;
			MeasureTraceVO[] vos = new MeasureTraceVO[len];
			for (int i = 0; i < len; i++) {
				if(objMeasures[i] != null){
					String mpk = objMeasures[i].getCode();
					MeasureTraceVO mt = mtmap.get(mpk);
					vos[i] = mt;
				}
			}
			objEnv.setMeasureTraceVOs(vos);//.setExEnv(ICalcEnv.MEASURE_TRACE_FLAG, vos);
		}

		return hashValue;
	}

	/**
	 * 批量处理动态区域指标取数函数的计算
	 *
	 * @param objMeasures
	 * @param objDateProp
	 * @param nOffset
	 * @param objKeyCondExpr
	 * @param nVer
	 * @param objEnv
	 * @param batchKeyDatas
	 * @param strExprRefDynKeys
	 * @param strMainTimeKey
	 * @return
	 * @throws TranslateException
	 */
	public Hashtable batchDynSelectValue(IStoreCell[] objMeasures,
			UfoExpr exprNotTimeKeyCond, UfoExpr exprTimeKeyCond,
			DatePropVO objDateProp, Double nOffset, Integer nVer,
			UfoCalcEnv objEnv, KeyDataGroup[] batchKeyDatas,
			String[] strExprRefDynKeys, String strMainTimeKey)
			throws TranslateException {

		if (objMeasures == null || objMeasures.length == 0)
			return null;

		// 预处理

		String strKeyGroupPK = null;
		boolean bHaveKeyCombPK = false;
		int iLen = objMeasures.length;

		for (int i = 0; i < iLen; i++) {
			if (objMeasures[i] != null) {
				if(objMeasures[i] instanceof MeasureVO) {
					strKeyGroupPK = objEnv.getMeasureCache().getKeyCombPk(
							objMeasures[i].getCode());
				} else {
					strKeyGroupPK = objMeasures[i].getKeyCombPK();
				}

				if (strKeyGroupPK == null) {
					objMeasures[i] = null;
				} else {
					bHaveKeyCombPK = true;
				}
			}
		}
		// 当所有的指标都没有对应的关键字组合时，返回空
		if (bHaveKeyCombPK == false) {
			return null;
		}

		// 返回值 key=引用动态区域关键字值字串, value=(hashtable key=指标code, value=指标取数函数值)
		Hashtable hashValue = batchDynSelectValue(objMeasures, strKeyGroupPK,
				exprNotTimeKeyCond, exprTimeKeyCond, objDateProp, nOffset,
				nVer, objEnv, batchKeyDatas, strExprRefDynKeys, strMainTimeKey,
				getDBType(objEnv));

		return hashValue;

	}

	/**
	 * 读取值定指标在指定条件下的值，env中保存了指标对应区域的值。 创建日期：(2003-8-11 14:07:17)
	 *
	 * @return java.lang.Object[]
	 * @param objMeasures
	 *            nc.vo.iufo.measure.MeasureVO[]
	 * @param objDateProp
	 *            nc.vo.iufo.keyword.DatePropVO
	 * @param nOffset
	 *            double
	 * @param objKeyCondExpr
	 *            com.ufsoft.iufo.util.expression.UfoExpr
	 * @param nVer
	 *            int
	 * @param objEnv
	 *            UfoCalcEnv
	 * @exception TranslateException
	 *                异常说明。
	 */
	public Hashtable getSelectValue(IStoreCell[] objMeasures,
			DatePropVO objDateProp, Double nOffset, UfoExpr objKeyCondExpr,
			Integer nVer, UfoCalcEnv objEnv) throws TranslateException {

//		// 预处理
//
		String strKeyGroupPK = null;
//		Object[] objValues = new Object[objMeasures.length];
		for (int i = 0; i < objMeasures.length; i++) {
//			if (objMeasures[i] == null) {
//				objValues[i] = null;
//			}
			if (objMeasures[i] != null) {
				if(objMeasures[i] instanceof MeasureVO) {
					strKeyGroupPK = objEnv.getMeasureCache().getKeyCombPk(objMeasures[i].getCode());
				} else {
//					String repPk = objEnv.getRepPK();
//					strKeyGroupPK = UFOCacheManager.getSingleton().getReportCache().getByPK(repPk).getPk_key_comb();
					strKeyGroupPK = objMeasures[i].getKeyCombPK();
				}

//				if (strKeyGroupPK == null) {
//					objMeasures[i] = null;
//					objValues[i] = null;
//				}
			}
//			if (objMeasures[i].getType() == MeasureVO.TYPE_NUMBER) {
//				objValues[i] = new Double(0);
//			} else {
//				objValues[i] = "";
//			}
		}
//		if (strKeyGroupPK == null) {
//			return objValues;
//		}

		// 取数
		Hashtable hashValue = getSelectValue(objMeasures, strKeyGroupPK,
				objDateProp, nOffset, objKeyCondExpr, nVer, objEnv, getDBType(objEnv));

//		// 处理返回
//		for (int i = 0; i < objMeasures.length; i++) {
//			if (objMeasures[i] != null) {
//				if (objMeasures[i].getType() == MeasureVO.TYPE_NUMBER) {
//					objValues[i] = (Double) hashValue.get(objMeasures[i]
//							.getCode());
//				} else {
//					objValues[i] = (String) hashValue.get(objMeasures[i]
//							.getCode());
//				}
//			}
//		}

		//处理指标公式追踪结果返回值
		if(objEnv.isMeasureTrace()){
			MeasureTraceVO[] mtvos = (MeasureTraceVO[]) objEnv.getExEnv(ICalcEnv.MEASURE_TRACE_FLAG);
			if(mtvos != null){
				hashValue.put(ICalcEnv.MEASURE_TRACE_FLAG, mtvos);
			}
		}
		return hashValue;

	}

	/**
	 * 批量读取指标取数函数值。这里计算的取数函数与区域无关 在参数alMeas中的第一个元素是一个Object[]对象，
	 * Object[0]是这组指标的关键字组和PK，Object[1]是UfoExpr是这祖指标的条件，
	 * alMeas从第一个位置之后全部是MeasureVO对象 这个方法会读出符合函数条件的所有值，并以key=measurePK, value=
	 * Double|String的形式保存。 创建日期：(2003-8-11 11:16:17)
	 *
	 * @return java.util.Hashtable
	 * @param alMeas
	 *            java.util.ArrayList
	 * @param env
	 *            UfoCalcEnv
	 * @exception TranslateException
	 *                异常说明。
	 */
	public Hashtable getSelectValue(ArrayList alMeas, UfoCalcEnv env)
			throws TranslateException {
		Object[] objConds = (Object[]) alMeas.get(0);
		if (objConds == null) {
			throw new TranslateException("miufocalc000130"/* "批量计算条件丢失！" */);
		}
		IStoreCell[] objMeasures = new IStoreCell[alMeas.size() - 1];
		for (int i = 0; i < alMeas.size() - 1; i++) {
			objMeasures[i] = (IStoreCell) alMeas.get(i + 1);
		}
		return getSelectValue(objMeasures, (String) objConds[0],
				(DatePropVO) objConds[1], (Double) objConds[2],
				(UfoExpr) objConds[3], (Integer) objConds[4], env, getDBType(env));
	}

	/**
	 * 获得引用动态区域关键字的select子句
	 *
	 * @param strExprRefDynKeys
	 *            指标取数函数中条件引用动态区域的关键字
	 * @param strKeyGroupPK
	 *            指标的关键字组合pk
	 * @param strMeasDbTable
	 *            指标数据库表
	 * @return
	 */
	private String getSelectKeyPart(UfoCalcEnv env, String strKeyGroupPK,
			String strMeasDbTable) {

		KeyGroupCache keyGroupCache = env.getKeyGroupCache();

		KeyGroupVO keyGroupVO = keyGroupCache.getByPK(strKeyGroupPK);
		if (keyGroupVO == null)
			return null;

		String strPubTable=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(strKeyGroupPK).getTableName();

		nc.vo.iufo.keydef.KeyVO[] keyVOs = keyGroupVO.getKeys();

		StringBuffer sbSqlReturn = new StringBuffer();

		int iLen = keyVOs.length;
		for (int i = 0; i < iLen; i++) {
			if (keyVOs[i] == null)
				continue;

			int nPos = keyGroupCache.getIndexOfKey(strKeyGroupPK, keyVOs[i]
					.getPk_keyword());

			if (nPos == -1) {
				continue;
			}
//			if (keyVOs[i].isPrivate()) {
//				sbSqlReturn.append(strMeasDbTable);
//				sbSqlReturn.append('.');
//				sbSqlReturn.append(KeyWordVO.PRVKEYCOLEMNNAME + nPos);
//			} else
			{
				sbSqlReturn.append(strPubTable);
				sbSqlReturn.append('.');
				sbSqlReturn.append(KeyWordVO.PUBKEYCOLEMNNAME + nPos);
			}
			sbSqlReturn.append(" ");
			sbSqlReturn.append(COLALIASPRFIX);
			sbSqlReturn.append(keyVOs[i].getPk_keyword());

			sbSqlReturn.append(',');

		}
		String strReturn = sbSqlReturn.toString();
		if (sbSqlReturn.toString().endsWith(",")) {
			strReturn = strReturn.substring(0, strReturn.length() - 1);
		}

		return strReturn;

	}

	/**
	 * 获得单个指标取数函数的sql语句
	 *
	 * @param strCols
	 * @param objMeasures
	 * @param strKeyGroupPK
	 * @param objKeyCondExpr
	 * @param nVer
	 * @param objEnv
	 * @param batchKeyDatas
	 * @param strExprRefDynKeys
	 * @param strMainTimeKey
	 * @return
	 * @throws TranslateException
	 */
	private String getSelectSqlFromOneTable(String strCols,
			IStoreCell[] objMeasures, String strKeyGroupPK, UfoExpr exprKeyCond,
			UfoExpr exprTimeKeyCond, DatePropVO objDateProp, Double nOffset,
			Integer nVer, UfoCalcEnv objEnv, KeyDataGroup[] batchKeyDatas,
			String[] strExprRefDynKeys, String strMainTimeKey, String strDbType)
			throws TranslateException {

		StringBuffer strWhereSql = null;
		String strEnvRepPK = objEnv.getRepPK();
		nc.vo.iufo.keydef.KeyVO[] objEnvKeys = objEnv.getKeys();

		String strPubTable=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(strKeyGroupPK).getTableName();

		ArrayList alMeasInEnv = (ArrayList) objEnv.getExEnv(CommonExprCalcEnv.EX_MEASVOINSQL);
		objEnv.removeExEnv(CommonExprCalcEnv.EX_MEASVOINSQL);
		String strRepPK = null;
		String strDbTable = null;
		Hashtable<String,String> hashTables = new Hashtable<String,String>();// 条件中引用的表名
		Hashtable<Integer,String> hashTableByKeyPos=new Hashtable<Integer,String>();
		try {
			IStoreCell objMeasure = null;
			for (int i = 0; i < objMeasures.length; i++) {
				if (objMeasures[i] != null) {
					objMeasure = objMeasures[i];
					if(objMeasure instanceof MeasureVO) {
						strRepPK = ((MeasureVO)objMeasure).getReportPK();
					} else {
//						strRepPK = objEnv.getRepPK();
						strRepPK = objMeasure.getReportPK();
					}

					strDbTable = objMeasures[i].getDbtable();
					break;
				}
			}
			objEnv.setRepPK(strRepPK);

			nc.vo.iufo.keydef.KeyGroupVO objKG = objEnv.getKeyGroupCache()
					.getByPK(strKeyGroupPK);
			if (objKG == null) {
				throw new TranslateException(TranslateException.ERR_KEYGROUP);
			}
			objEnv.setKeys(objKG.getKeys());

			// 第条件生成sql
			ArrayList alMeasOperand = new ArrayList();
			if (exprKeyCond != null) {
				ExprUtil.getElementsByClass(exprKeyCond, alMeasOperand,
						MeasOperand.class);
			}
			if (exprTimeKeyCond != null) {
				ExprUtil.getElementsByClass(exprTimeKeyCond, alMeasOperand,
						MeasOperand.class);
			}
			if (alMeasOperand != null && alMeasOperand.size() > 0) {
				for (int i = 0; i < alMeasOperand.size(); i++) {
					alMeasOperand.set(i, ((MeasOperand) alMeasOperand.get(i))
							.getMeasureVO());
				}
			}
			alMeasOperand.add(objMeasure);
			objEnv.setExEnv(CommonExprCalcEnv.EX_MEASVOINSQL, alMeasOperand);

			// modify by ljhua 2005-6-2
			strWhereSql = getCondSQL(strKeyGroupPK, exprKeyCond,
					exprTimeKeyCond, objDateProp, nOffset, nVer, objEnv,
					hashTables,hashTableByKeyPos, batchKeyDatas, strExprRefDynKeys,
					strMainTimeKey, strDbType);

			StringBuffer sbTables = new StringBuffer();
			Vector datatables = new Vector();
			// 整理from后面的表列表，并对这些表产生的关联拼接在条件语句之后
			hashTables.remove(strDbTable);

			if (hashTables.size() > 0) {
				boolean bFirst = true;

				// 组成From 语句，并得到每个指标数据表列表
				java.util.Enumeration alltable = hashTables.keys();
				while (alltable.hasMoreElements()) {
					String dbtableKey = (String) alltable.nextElement();
					if (ExtFuncIUfoConst.KEY_COL_DIC_CORP.equals(dbtableKey))
						continue;

					String dbtable = (String) hashTables.get(dbtableKey);

					if (bFirst == false) {
						sbTables.append(',');
					} else {
						bFirst = false;
					}

					sbTables.append(dbtable);
					if (!dbtable.equals(ExtFuncIUfoConst.NEW_TABLE_UNIT_INFO)
							&& !dbtable
									.equals(strPubTable)
							&& !dbtable
									.equals(ExtFuncIUfoConst.TABLE_DIC_UNIT_INFO)
							/*&& !dbtable
									.equals(ExtFuncIUfoConst.TABLE_PARENT_UNIT_INFO)*/) {
						datatables.addElement(dbtable);
					}
				}
			}


			for (int iPos:hashTableByKeyPos.keySet()){
				String strTableName=hashTableByKeyPos.get(iPos);
				sbTables.append(","+strTableName+" "+ExtFuncIUfoConst.KEYDETAIL_PREFIX+iPos);
			}

			boolean bWhereAdded = strWhereSql.length() > 0;
			// 对指标数据表，需要增加条件, 如 a.alone_id = iufo_measure_pubdata.alone_id
			if (datatables.size() > 0) {
				for (int i = 0; i < datatables.size(); i++) {
					if (bWhereAdded) {
						strWhereSql.append(" and ");
					} else {
						bWhereAdded = true;
					}
					strWhereSql.append((String) datatables.get(i));
					strWhereSql.append(".alone_id = ");
					strWhereSql.append(strPubTable);
					strWhereSql.append(".alone_id  ");
				}
			}

			for (int iPos:hashTableByKeyPos.keySet()){
			  	if( bWhereAdded ){
			  		strWhereSql.append(" and ");
			  	}else{
				  	bWhereAdded = true;
				  	strWhereSql.append(" where ");
			  	}

			  	strWhereSql.append(ExtFuncIUfoConst.KEYDETAIL_PREFIX+iPos+".keyval="+strPubTable+".keyword"+iPos);
			}

			// 如果引用了单元信息表，需要增加条件 iufo_unit_info.unit_id =
			// iufo_measure_pubdata.code
			if (hashTables.get(ExtFuncIUfoConst.NEW_TABLE_UNIT_INFO) != null) {
				if (bWhereAdded) {
					strWhereSql.append(" and ");
				} else {
					bWhereAdded = true;
				}
				strWhereSql.append(ExtFuncIUfoConst.NEW_TABLE_UNIT_INFO);
				strWhereSql.append(".pk_vid= ");
				strWhereSql.append(strPubTable);
				strWhereSql.append(".keyword1 ");
			}

			// 如果条件中包括k(对方单位)，需要添加 dic_corp.unit_id =
			// iufo_measure_pubdata.keywordx
			// add by ljhua 2006-8-22
			if (hashTables.containsKey(ExtFuncIUfoConst.KEY_COL_DIC_CORP)) {
				if (bWhereAdded) {
					strWhereSql.append(" and ");
				} else {
					bWhereAdded = true;
				}
				strWhereSql.append(ExtFuncIUfoConst.TABLE_DIC_UNIT_ALIAS);
				strWhereSql.append(".pk_vid = ");
				strWhereSql.append(strPubTable);
				strWhereSql.append(".");
				strWhereSql.append((String) hashTables
						.get(ExtFuncIUfoConst.KEY_COL_DIC_CORP));
				strWhereSql.append(" ");
			}
			//此处代码注释掉，从5.02开始，没有parent_code字段，所以这种处理是有问题的
//			// 如果条件中包括k("单位->上级单位编码")，需要添加
//			// iufo_unit_info.parent_code=parent.unit_id
//			// add by ljhua 2006-10-13
//			if (hashTables.containsKey(ExtFuncIUfoConst.TABLE_PARENT_UNIT_INFO)) {
//				if (bWhereAdded) {
//					strWhereSql.append(" and ");
//				} else {
//					bWhereAdded = true;
//				}
//				strWhereSql.append(ExtFuncIUfoConst.TABLE_UNIT_INFO);
//				strWhereSql.append(".parent_code = ");
//				strWhereSql.append(ExtFuncIUfoConst.TABLE_PARENT_UNIT_ALIAS);
//				strWhereSql.append(".unit_id ");
//			}

			String strTables = sbTables.toString();

			StringBuffer sqlStatement = new StringBuffer();
			sqlStatement.append("select ");

			// add by ljhua 2005-6-2
			if (strExprRefDynKeys != null && strExprRefDynKeys.length > 0) {
				// 对于批量动态区域指标取数函数，则添加选取引用的动态区域关键字列
				sqlStatement.append(getSelectKeyPart(objEnv, strKeyGroupPK,
						strDbTable));
				sqlStatement.append(",");
			}

			sqlStatement.append(strCols);
			sqlStatement.append(" from ");
			sqlStatement.append(strTables);
			sqlStatement.append(",");
			sqlStatement.append(strDbTable);
			sqlStatement.append(" where ");
////			if (objKG.isContainsPrivatekey()) {
////				sqlStatement.append(strDbTable);
////				sqlStatement.append(".pk_key_comb");
////			} else {
//				sqlStatement
//						.append(nc.vo.iufo.pub.IDatabaseNames.IUFO_MEASURE_PUBDATA);
//				sqlStatement.append(".ktype");
////			}
//
//			sqlStatement.append("='");
//			sqlStatement.append(strKeyGroupPK);
//			sqlStatement.append("' and ");
			if (strWhereSql != null && strWhereSql.length() > 0) {
				sqlStatement.append(" (");
				sqlStatement.append(strWhereSql.toString());
				sqlStatement.append(" ) and ");
			}

			sqlStatement.append(strDbTable);
			sqlStatement.append(".alone_id=");
			sqlStatement.append(strPubTable);
			sqlStatement.append(".alone_id");

			return sqlStatement.toString();
		}catch(TranslateException ex){
			AppDebug.debug(ex);
			throw new TranslateException(ex);
		}catch(Exception ex){
			AppDebug.debug(ex);
			throw new TranslateException(ex.getMessage());
		}
		finally {
			objEnv.setRepPK(strEnvRepPK);
			objEnv.setKeys(objEnvKeys);
			if (alMeasInEnv == null) {
				objEnv.removeExEnv(CommonExprCalcEnv.EX_MEASVOINSQL);
			} else {
				objEnv.setExEnv(CommonExprCalcEnv.EX_MEASVOINSQL, alMeasInEnv);
			}

		}

	}

	private Hashtable batchDynSelectFromOneTable(String strCols,
			IStoreCell[] objMeasures, String strKeyGroupPK,
			UfoExpr exprNotTimeKeyCond, UfoExpr exprTimeKeyCond,
			DatePropVO objDateProp, Double nOffset, Integer nVer,
			UfoCalcEnv objEnv, KeyDataGroup[] batchKeyDatas,
			String[] strExprRefDynKeys, String strMainTimeKey, String strDbType)
			throws TranslateException {

		try {

			KeyGroupCache keyGroupCache = objEnv.getKeyGroupCache();
			KeyGroupVO keyGroupVO = keyGroupCache.getByPK(strKeyGroupPK);
			if (keyGroupVO == null)
				return null;
			nc.vo.iufo.keydef.KeyVO[] keyVOs = keyGroupVO.getKeys();

			String sqlStatement = getSelectSqlFromOneTable(strCols,
					objMeasures, strKeyGroupPK, exprNotTimeKeyCond,
					exprTimeKeyCond, objDateProp, nOffset, nVer, objEnv,
					batchKeyDatas, strExprRefDynKeys, strMainTimeKey, strDbType);

			Hashtable hashValue = getMeasFuncDMO().batchDynSelectValues(objMeasures,
					sqlStatement, keyVOs);

			//chxw. 处理指标公式追踪
			if(objEnv.isMeasureTrace()){
				String where = sqlStatement.substring(sqlStatement.indexOf("from") + 4);
				String strAccSchemePK=objEnv.getMeasureEnv()!=null?objEnv.getMeasureEnv().getAccSchemePK():null;

				UfoExpr objKeyCondJudge=ReplenishKeyCondUtil.replenishTimeValueKeyCond(exprNotTimeKeyCond,exprTimeKeyCond,objEnv,strKeyGroupPK,objDateProp,nOffset);

				MeasureTraceVO[] tracevos = getMeasFuncDMO().measureTraces(objEnv,objKeyCondJudge,objMeasures, where,keyGroupVO,strAccSchemePK);
				objEnv.setMeasureTraceVOs(tracevos);
				//借hashtable返回追踪信息。
				hashValue.put(ICalcEnv.MEASURE_TRACE_FLAG, tracevos);

			}

			return hashValue;

		} catch (java.sql.SQLException e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_NODATA);
		} catch (javax.naming.NamingException ne) {
			AppDebug.debug(ne);//@devTools ne.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_NAMING);
		} catch (nc.bs.pub.SystemException se) {
			AppDebug.debug(se);//@devTools se.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_PUBSYSTEM);
		}

	}

	/**
	 * 读取值定指标在制定条件下的值，env中保存了指标对应区域的值。 创建日期：(2003-8-11 14:07:17)
	 *
	 * @return Hashtable
	 * @param objMeasures
	 *            nc.vo.iufo.measure.MeasureVO[]
	 * @param objDateProp
	 *            nc.vo.iufo.keyword.DatePropVO
	 * @param nOffset
	 *            double
	 * @param objKeyCondExpr
	 *            com.ufsoft.iufo.util.expression.UfoExpr
	 * @param nVer
	 *            int
	 * @param objEnv
	 *            UfoCalcEnv
	 * @exception TranslateException
	 *                异常说明。
	 */
	private Hashtable getSelectValueFromOneTable(String strCols,
			IStoreCell[] objMeasures, String strKeyGroupPK,
			UfoExpr objKeyCondExpr, Integer nVer, UfoCalcEnv objEnv,
			String strDbType) throws TranslateException {

		try {
			String sqlStatement = getSelectSqlFromOneTable(strCols,
					objMeasures, strKeyGroupPK, objKeyCondExpr, null, null,
					null, nVer, objEnv, null, null, null, strDbType);

			// 指标公式追踪。 liuyy.
			MeasureTraceVO[] tracevos = objEnv.getMeasureTraceVOs();
			if (tracevos != null) {
				String where = sqlStatement.substring(sqlStatement
						.indexOf("from") + 4);
				KeyGroupVO keyGroup=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(strKeyGroupPK);
				String strAccSchemePK=objEnv.getMeasureEnv()!=null?objEnv.getMeasureEnv().getAccSchemePK():null;
				MeasureTraceVO[] tracevos2 = getMeasFuncDMO().measureTraces(objEnv, objKeyCondExpr, objMeasures, where,keyGroup,strAccSchemePK);
				objEnv.setMeasureTraceVOs(tracevos2);
			}
			// 指标公式追踪。end.

			Object extEnv = objEnv.getExEnv(TaskCheckEnv.TASKENV_KEY);
			if(extEnv != null){
				TaskCheckEnv tEnv = (TaskCheckEnv)extEnv;
			    return getMeasFuncDMO().getSelectValuesForTask(objMeasures, sqlStatement, tEnv.getMeasData());
			}
			else
				return getMeasFuncDMO().getSelectValues(objMeasures, sqlStatement);

		} catch (java.sql.SQLException e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_NODATA);
		} catch (javax.naming.NamingException ne) {
			AppDebug.debug(ne);//@devTools ne.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_NAMING);
		} catch (nc.bs.pub.SystemException se) {
			AppDebug.debug(se);//@devTools se.printStackTrace(System.out);
			throw new TranslateException(TranslateException.ERR_PUBSYSTEM);
		}

	}

	/**
	 * 获得当前计算表页主表关键字值字串
	 *
	 * @param objEnv
	 * @param objKeys
	 *            要求关键字集合就是或少于报表主表关键字集合
	 * @return
	 * @i18n miufo1001491=关键字
	 * @i18n miufo00896=没有设置计算环境值
	 */
	private String getCurKeyValues(UfoCalcEnv objEnv,
			nc.vo.iufo.keydef.KeyVO[] objKeys) throws TranslateException {
		// NEW_ADD
		String strCurKeyValue = "";
		if (objEnv != null && objEnv.getMeasureEnv() != null && objKeys != null) {
			nc.vo.iufo.data.MeasurePubDataVO objPubData = objEnv
					.getMeasureEnv();

			String strKeyValue = null;
			for (int i = 0; i < objKeys.length; i++) {
				strKeyValue = objPubData.getKeywordByName(objKeys[i].getName());
				if (strKeyValue == null) {
					throw new TranslateException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0084")/*@res "关键字"*/ + objKeys[i]
							+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0844")/*@res "没有设置计算环境值"*/);

				} else {
					strCurKeyValue += strKeyValue + "\r\n";
				}
			}
		}
		return strCurKeyValue;
	}

	/**
	 * 通过指标统计函数名，获得对应数据库统计函数名
	 *
	 * @param strFuncName
	 * @return
	 */

	private String getStatDBFuncName(String strFuncName) {
		String strReturn = null;
		if (MeasFuncDriver.MAVG.equals(strFuncName)) {
			strReturn = "count";
		} else if (MeasFuncDriver.MSUM.equals(strFuncName)) {
			strReturn = "sum";
		} else if (MeasFuncDriver.MMAX.equals(strFuncName)) {
			strReturn = "max";
		} else if (MeasFuncDriver.MMIN.equals(strFuncName)) {
			strReturn = "min";
		}
		return strReturn;
	}

	/**
	 * 获得指标值 for vlookup
	 * 
	 * @param measureVOs
	 * @param objEnv
	 * @param strKeyGroupPk
	 * @param ver
	 * @return
	 * @throws TranslateException
	 */
	public Hashtable<String, Object> getLookUpMeasValue(List<MeasureVO> measureVOs, UfoCalcEnv objEnv, String strKeyGroupPk, Integer ver)
			throws TranslateException {
		if (measureVOs == null || measureVOs.size() == 0) {
			return null;
		}
		// 指标
		MeasureVO[] objMeasures = measureVOs.toArray(new MeasureVO[measureVOs.size()]);
		// 补充条件
		UfoExpr objKeyCondExpr = ReplenishKeyCondUtil.replenishValueKeyCond(objEnv, strKeyGroupPk, null, null, null,true);
		// 得到指标对应的sql,[0]是表对应的from之前的字段（key=dbtable,value=columns）
		// [1]表示在一个表中的指标（key=dbtable, value=ArrayList(MeasureVO)）[0]与[1]的key一致
		Hashtable[] hashMeasSqls = getMeasureSQL(objMeasures);

		Hashtable<String, Object> hashValue = new Hashtable<String, Object>();
		Enumeration enKey = hashMeasSqls[0].keys();

		while (enKey.hasMoreElements()) {
			String strTable = (String) enKey.nextElement();
			ArrayList alMeas = (ArrayList) hashMeasSqls[1].get(strTable);
			MeasureVO[] objMeases = new MeasureVO[alMeas.size()];
			alMeas.toArray(objMeases);
//			if (strTable.equals(IDatabaseNames.IUFO_MEASURE_PUBDATA)) {
//				for (int i = 0; i < objMeases.length; i++) {
//					if (objMeases[i] == null) {
//						continue;
//					}
//					if (objMeases[i].getType() == MeasureVO.TYPE_NUMBER) {
//						hashValue.put(objMeases[i].getCode(), new Double(0));
//					} else {
//						hashValue.put(objMeases[i].getCode(), "");
//					}
//				}
//				continue;
//			}

			hashValue.putAll(getSelectValueFromOneTable(((StringBuffer) hashMeasSqls[0].get(strTable)).toString(), objMeases,
					strKeyGroupPk, objKeyCondExpr, ver, objEnv, getDBType(objEnv)));
		}
		return hashValue;
	}

	/**
	 * 获得动态区关键字和指标值 for vlookup
	 * 
	 * @param measureVOs
	 * @param objEnv
	 * @param strKeyGroupPk
	 * @param strDynKeys
	 * @param ver
	 * @return
	 * @throws TranslateException
	 */
	public Hashtable<String, Hashtable<String, Object>> getLookUpDynMeasValue(List<MeasureVO> measureVOs, UfoCalcEnv objEnv,
			String strKeyGroupPk, String[] strDynKeys, Integer ver) throws TranslateException {
		if (measureVOs == null || measureVOs.size() == 0) {
			return null;
		}

		// 指标
		MeasureVO[] objMeasures = measureVOs.toArray(new MeasureVO[measureVOs.size()]);

		// 得到指标对应的sql,[0]是表对应的from之前的字段（key=dbtable,value=columns）
		// [1]表示在一个表中的指标（key=dbtable, value=ArrayList(MeasureVO)）[0]与[1]的key一致
		Hashtable[] hashMeasSqls = getMeasureSQL(objMeasures);

		Hashtable<String, Hashtable<String, Object>> hashTemp = null;
		Hashtable<String, Hashtable<String, Object>> hashReturn = new Hashtable<String, Hashtable<String, Object>>();

		Enumeration enKey = hashMeasSqls[0].keys();
		while (enKey.hasMoreElements()) {
			String strTable = (String) enKey.nextElement();
//			if (strTable.equals(IDatabaseNames.IUFO_MEASURE_PUBDATA)) {
//				continue;
//			}
			ArrayList alMeas = (ArrayList) hashMeasSqls[1].get(strTable);
			MeasureVO[] objMeases = new MeasureVO[alMeas.size()];
			alMeas.toArray(objMeases);

			hashTemp = getLookUpDynMeasFromOneTable(((StringBuffer) hashMeasSqls[0].get(strTable)).toString(), objMeases, strKeyGroupPk,
					ver, objEnv, strDynKeys, getDBType(objEnv));

			// 处理合并各表结果
			if (hashTemp != null && hashTemp.size() > 0) {
				Iterator<Entry<String, Hashtable<String, Object>>> iterTemp = hashTemp.entrySet().iterator();
				while (iterTemp.hasNext()) {
					Entry<String, Hashtable<String, Object>> entry = iterTemp.next();
					Hashtable<String, Object> hashSecond = hashReturn.get(entry.getKey());
					if (hashSecond == null) {
						hashSecond = new Hashtable<String, Object>();
						hashReturn.put(entry.getKey(), hashSecond);
					}
					hashSecond.putAll(entry.getValue());
				}
			}
		}
		if (hashReturn.size() == 0) {
			hashReturn = null;
		}
		return hashReturn;
	}

	/**
	 * 获得动态区关键字和指标值 for vlookup
	 * 
	 * @param strCols
	 * @param objMeasures
	 * @param strKeyGroupPK
	 * @param nVer
	 * @param objEnv
	 * @param strExprRefDynKeys
	 * @param strDbType
	 * @return
	 * @throws TranslateException
	 */
	private Hashtable<String, Hashtable<String, Object>> getLookUpDynMeasFromOneTable(String strCols, MeasureVO[] objMeasures,
			String strKeyGroupPK, Integer nVer, UfoCalcEnv objEnv, String[] strExprRefDynKeys, String strDbType) throws TranslateException {
		try {
			KeyGroupVO keyGroupVO = objEnv.getKeyGroupCache().getByPK(strKeyGroupPK);
			if (keyGroupVO == null) {
				return null;
			}
			KeyVO[] keyVOs = keyGroupVO.getKeys();

			String sql = getSelectSqlFromOneTable(strCols, objMeasures, strKeyGroupPK, null, null, null, null, nVer, objEnv, null,
					strExprRefDynKeys, null, strDbType);

			Hashtable<String, Hashtable<String, Object>> hashValue = getMeasFuncDMO().getLookUpDynMeasValues(objMeasures, sql, keyVOs);

			return hashValue;
		} catch (java.sql.SQLException e) {
			AppDebug.debug(e);
			throw new TranslateException(TranslateException.ERR_NODATA);
		} catch (javax.naming.NamingException ne) {
			AppDebug.debug(ne);
			throw new TranslateException(TranslateException.ERR_NAMING);
		} catch (nc.bs.pub.SystemException se) {
			AppDebug.debug(se);
			throw new TranslateException(TranslateException.ERR_PUBSYSTEM);
		}
	}
	
	
	
}
