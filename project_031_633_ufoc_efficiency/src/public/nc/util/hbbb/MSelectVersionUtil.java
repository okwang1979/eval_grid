package nc.util.hbbb;

import nc.bs.framework.common.NCLocator;
import nc.itf.iufo.calculate.IVersionFetcher;
import nc.itf.iufo.commit.ICommitQueryService;
import nc.pub.iufo.accperiod.AccPeriodSchemeUtil;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.util.hbbb.pub.HBPubItfService;
import nc.util.hbbb.service.HBBaseDocItfService;
import nc.vo.hbbb.adjustscheme.AdjustSchemeVO;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.iufo.calculate.DatePropVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.pub.date.UFODate;
import nc.vo.iufo.verctrl.IDataVersionConsts;
import nc.vo.pub.BusinessException;
import nc.vo.pub.ValidationException;
import nc.vo.pub.ValueObject;
import nc.vo.ufoc.adjreport.AdjReportVO;

import com.ufsoft.script.spreadsheet.UfoCalcEnv;

/**
 * MSELECT 公式取版本用
 * 
 * @date 20110711
 * @author liyra
 * @modify litfb@20120515 取版本添加偏移量参数,使正确判断是否有对应调整表版本
 * @modify litfb@20120515 添加成员调整方案,使之不用再次查询
 */

public class MSelectVersionUtil extends ValueObject implements IVersionFetcher {

    private static final long serialVersionUID = -5700491877849450982L;

    private String pk_report;

    private MeasurePubDataVO pubdata;

    private HBSchemeVO hbSchemeVo;

    private AdjustSchemeVO adjustSchemeVo;

    public MSelectVersionUtil(HBSchemeVO hbSchemeVo, AdjustSchemeVO adjustSchemeVo) {
        this.hbSchemeVo = hbSchemeVo;
        this.adjustSchemeVo = adjustSchemeVo;
    }

    public boolean isHBRep() {
        boolean result = false;
        if (Integer.valueOf(this.getPubdata().getVer()).toString()
                .startsWith(Integer.valueOf(IDataVersionConsts.VERTYPE_SEPERATE).toString())) {
            return false;
        } else if (Integer.valueOf(this.getPubdata().getVer()).toString()
                .startsWith(Integer.valueOf(IDataVersionConsts.VERTYPE_SEPERATE_ADJUST).toString())) {
            return false;
        } else if (Integer.valueOf(this.getPubdata().getVer()).toString()
                .startsWith(Integer.valueOf(IDataVersionConsts.VERTYPE_HBBB).toString())) {
            return true;
        } else if (Integer.valueOf(this.getPubdata().getVer()).toString()
                .startsWith(Integer.valueOf(IDataVersionConsts.VERTYPE_HBBB_ADJUST).toString())) {
            return true;
        }
        return result;
    }
    


    /**
     * 检查是否存在调整表
     * 
     * @param ver
     * @param offset
     * @return
     * @throws BusinessException
     */
    protected boolean isExisted(int ver, int offset) throws BusinessException {
        boolean result = false;
        String desAloneId = getDesAloneId(offset, ver);
        AdjReportVO repvo = new AdjReportVO();
        repvo.setAloneid(desAloneId);
        repvo.setPk_adjscheme(this.getHbSchemeVo().getPk_adjustscheme());
        repvo.setPk_hbscheme(this.getHbSchemeVo().getPk_hbscheme());
        repvo.setPk_keygroup(this.getHbSchemeVo().getPk_keygroup());
        repvo.setPk_report(this.getPk_report());

        result = HBPubItfService.getRemoteAdjReport().existAdjReport(repvo, this.getAdjustSchemeVo());

        return result;
    }
    
    
    
    

    /**
     * 获得目标pubdata
     * 
     * @param offset
     * @param ver
     * @return
     * @throws BusinessException
     */
    protected String getDesAloneId(int offset, int ver) throws BusinessException {
        MeasurePubDataVO result = (MeasurePubDataVO) this.getPubdata().clone();
        result.setAloneID(null);
        // 根据时间偏移量置换时间关键字
        if (offset != 0) {
            String strOffsetPeriod = getOffSetTime(offset, result);
            KeyVO[] keys = result.getKeyGroup().getKeys();
            for (int i = 0; i < keys.length; i++) {
                if (keys[i].getTTimeKeyIndex() >= 0) {
                    result.setKeywordByPK(keys[i].getPk_keyword(), strOffsetPeriod);
                    break;
                }
            }
        }
        //置换版本号为目标版本号
        result.setVer(ver);
        //获得aloneId
        String aloneid = null;
        try {
            aloneid = MeasurePubDataBO_Client.getAloneID(result);
        } catch (Exception e) {
            throw new BusinessException(e);
        }
        return aloneid;
    }

    /**
     *  根据偏移量获取时间关键字
     * @param offset
     * @param result
     * @return
     */
	private String getOffSetTime(int offset, MeasurePubDataVO result) {
		String strOffsetPeriod;
		if(result.getKeyGroup().isTTimeTypeAcc()){
			//会计时间处理
		    KeyVO accKey = result.getKeyGroup().getAccKey();
			strOffsetPeriod = AccPeriodSchemeUtil.getInstance().getOffsetAccPeriod(
		        this.getHbSchemeVo().getPk_accperiodscheme(), accKey.getPk_keyword(), result.getInputDate(),
		        offset);
		}
		else{
			//自然时间的处理
			UFODate objDate = new UFODate(result.getInputDate());
			String strOffsetType= DatePropVO.getUfoDateTypeStr(result.getKeyGroup().getTimeKey().getDateType());
			if (strOffsetType != null){
				objDate = new nc.vo.iufo.pub.date.UFODate(objDate.getNextDate(strOffsetType, offset));
		    }
			strOffsetPeriod = objDate.getDateString();
//            	ReplenishKeyCondUtil.getTimeCode(result.getInputDate(), result.getKeyGroup().getTimeKey().getDateType(), offset);
		}
		return strOffsetPeriod;
	}

    /**
     * 获得目标版本
     * 
     * @param offset
     * @return
     */
    public int getVersion(int offset) {
        int result = 0;
        try {
            if (Integer.valueOf(this.getPubdata().getVer()).toString()
                    .startsWith(Integer.valueOf(IDataVersionConsts.VERTYPE_SEPERATE).toString())) {

                if (this.isExisted(this.getAdjustSchemeVo().getVersion(), offset)) {
                    return this.getAdjustSchemeVo().getVersion();
                } else {
                    return IDataVersionConsts.VERTYPE_SEPERATE;
                }
            } else if (Integer.valueOf(this.getPubdata().getVer()).toString()
                    .startsWith(Integer.valueOf(IDataVersionConsts.VERTYPE_SEPERATE_ADJUST).toString())) {
            	//modified by jiaah 当在调整表上定义mselect取当期数的时候，默认取调整数，取上期的时候在校验是否存在调整表
                if (offset == 0 || this.isExisted(this.getAdjustSchemeVo().getVersion(), offset)) {
                    return this.getAdjustSchemeVo().getVersion();
                } else {
                    return IDataVersionConsts.VERTYPE_SEPERATE;
                }
            } else if (Integer.valueOf(this.getPubdata().getVer()).toString()
                    .startsWith(Integer.valueOf(IDataVersionConsts.VERTYPE_HBBB).toString())) {

                int ver = HBVersionUtil.getHBAdjustByHBSchemeVO(this.getHbSchemeVo(), this.getAdjustSchemeVo());

                if (this.isExisted(ver, offset)) {
                    return ver;
                } else {
                    return this.getHbSchemeVo().getVersion();
                }
            } else if (Integer.valueOf(this.getPubdata().getVer()).toString()
                    .startsWith(Integer.valueOf(IDataVersionConsts.VERTYPE_HBBB_ADJUST).toString())) {

                int ver = HBVersionUtil.getHBAdjustByHBSchemeVO(this.getHbSchemeVo(), this.getAdjustSchemeVo());

                if (this.isExisted(ver, offset)) {
                    return ver;
                } else {
                    return this.getHbSchemeVo().getVersion();
                }
            }
        } catch (BusinessException e) {
            nc.bs.logging.Logger.error(e.getMessage(), e);
        }

        return result;
    }

    public String getPk_report() {
        return pk_report;
    }

    public void setPk_report(String pk_report) {
        this.pk_report = pk_report;
    }

    public MeasurePubDataVO getPubdata() {
        return pubdata;
    }

    public void setPubdata(MeasurePubDataVO pubdata) {
        this.pubdata = pubdata;
    }

    public HBSchemeVO getHbSchemeVo() {
        return hbSchemeVo;
    }

    public void setHbSchemeVo(HBSchemeVO hbSchemeVo) {
        this.hbSchemeVo = hbSchemeVo;
    }

    public AdjustSchemeVO getAdjustSchemeVo() {
        return adjustSchemeVo;
    }

    public void setAdjustSchemeVo(AdjustSchemeVO adjustSchemeVo) {
        this.adjustSchemeVo = adjustSchemeVo;
    }

    @Override
    public int getMselectVersion(UfoCalcEnv env, int offset) {
//        return this.getVersion(offset);
    	return this.getEnvVersion(env, offset);
    }
    
    private int getEnvVersion(UfoCalcEnv env, int offset){
    	
    	
        int result = 0;
        try {
            if (Integer.valueOf(this.getPubdata().getVer()).toString()
                    .startsWith(Integer.valueOf(IDataVersionConsts.VERTYPE_SEPERATE).toString())) {

                if (this.isExisted(this.getAdjustSchemeVo().getVersion(), offset,env)) {
                    return this.getAdjustSchemeVo().getVersion();
                } else {
                    return IDataVersionConsts.VERTYPE_SEPERATE;
                }
            } else if (Integer.valueOf(this.getPubdata().getVer()).toString()
                    .startsWith(Integer.valueOf(IDataVersionConsts.VERTYPE_SEPERATE_ADJUST).toString())) {
            	//modified by jiaah 当在调整表上定义mselect取当期数的时候，默认取调整数，取上期的时候在校验是否存在调整表
                if (offset == 0 || this.isExisted(this.getAdjustSchemeVo().getVersion(), offset,env)) {
                    return this.getAdjustSchemeVo().getVersion();
                } else {
                    return IDataVersionConsts.VERTYPE_SEPERATE;
                }
            } else if (Integer.valueOf(this.getPubdata().getVer()).toString()
                    .startsWith(Integer.valueOf(IDataVersionConsts.VERTYPE_HBBB).toString())) {

                int ver = HBVersionUtil.getHBAdjustByHBSchemeVO(this.getHbSchemeVo(), this.getAdjustSchemeVo());

                if (this.isExisted(ver, offset,env)) {
                    return ver;
                } else {
                    return this.getHbSchemeVo().getVersion();
                }
            } else if (Integer.valueOf(this.getPubdata().getVer()).toString()
                    .startsWith(Integer.valueOf(IDataVersionConsts.VERTYPE_HBBB_ADJUST).toString())) {

                int ver = HBVersionUtil.getHBAdjustByHBSchemeVO(this.getHbSchemeVo(), this.getAdjustSchemeVo());

                if (this.isExisted(ver, offset,env)) {
                    return ver;
                } else {
                    return this.getHbSchemeVo().getVersion();
                }
            }
        } catch (BusinessException e) {
            nc.bs.logging.Logger.error(e.getMessage(), e);
        }

        return result;
    	
    }
    
    

    /**
     * 检查是否存在调整表
     * 
     * @param ver
     * @param offset
     * @return
     * @throws BusinessException
     */
    protected boolean isExisted(int ver, int offset,UfoCalcEnv env) throws BusinessException {
    	if(env==null){
    		return this.isExisted(ver, offset);
    	}
    	if(this.getPubdata()!=null){
    		
    		
    		String key = "nc.util.hbbb.mselectversion_verKey"+this.getPubdata().toString()+"_ver:"+ver+";off:"+offset+"report:"+this.getPk_report();
        	if(env.getExEnv().get(key)!=null){
        		return (Boolean)env.getExEnv().get(key);
        		
        	}else{
        		
        		
        	    boolean result = false;
                String desAloneId = getDesAloneId(offset, ver);
                AdjReportVO repvo = new AdjReportVO();
                repvo.setAloneid(desAloneId);
                repvo.setPk_adjscheme(this.getHbSchemeVo().getPk_adjustscheme());
                repvo.setPk_hbscheme(this.getHbSchemeVo().getPk_hbscheme());
                repvo.setPk_keygroup(this.getHbSchemeVo().getPk_keygroup());
                repvo.setPk_report(this.getPk_report());

                result = HBPubItfService.getRemoteAdjReport().existAdjReport(repvo, this.getAdjustSchemeVo());
                
                env.getExEnv().put(key, result);

                return result;
//        		
//        	    String aloneid = vo.getAloneid();
//                String pk_adjscheme = vo.getPk_adjscheme();
//                if(pk_adjscheme == null)
//                    return false;
//                
//                if (adjustSchemeVo == null || !pk_adjscheme.equals(adjustSchemeVo.getPk_adjustscheme())) {
//                    adjustSchemeVo = (AdjustSchemeVO)HBBaseDocItfService.getRemoteUAPQueryBS().retrieveByPK(AdjustSchemeVO.class, pk_adjscheme);
//                }
//                ICommitQueryService commitQrySrv = NCLocator.getInstance().lookup(ICommitQueryService.class);
//                boolean bInput = commitQrySrv.isRepInput(aloneid, vo.getPk_report());
//                return bInput;
        	}
    		
    	}
    	
        boolean result = false;
        String desAloneId = getDesAloneId(offset, ver);
        AdjReportVO repvo = new AdjReportVO();
        repvo.setAloneid(desAloneId);
        repvo.setPk_adjscheme(this.getHbSchemeVo().getPk_adjustscheme());
        repvo.setPk_hbscheme(this.getHbSchemeVo().getPk_hbscheme());
        repvo.setPk_keygroup(this.getHbSchemeVo().getPk_keygroup());
        repvo.setPk_report(this.getPk_report());

        result = HBPubItfService.getRemoteAdjReport().existAdjReport(repvo, this.getAdjustSchemeVo());

        return result;
    }

    @Override
    public String getEntityName() {
        return null;
    }

    @Override
    public void validate() throws ValidationException {

    }

}
