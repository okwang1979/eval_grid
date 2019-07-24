package nc.bs.hbbb.contrast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nc.util.hbbb.NumberFormatUtil;
import nc.vo.hbbb.dxrelation.DXContrastVO;
import nc.vo.hbbb.dxrelation.DXRelaDiffRuleVO;
import nc.vo.hbbb.dxrelation.IDXRelaConst;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.hbbb.meetaccount.AggMeetRltHeadVO;
import nc.vo.hbbb.meetaccount.MeetResultBodyVO;
import nc.vo.hbbb.meetaccount.MeetResultHeadVO;
import nc.vo.hbbb.meetdata.AggMeetdataVO;
import nc.vo.hbbb.meetdata.MeetdataVO;
import nc.vo.hbbb.meetdata.MeetdatasubVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;

public class GenContrastResultBO {

	//���ݲ����Ŀ���ж���
	public static void genContrastResultWithDifProject (AggMeetdataVO aggvo,Map<String, String> map,List<AggMeetRltHeadVO> resultList,Map<String, DXRelaDiffRuleVO> diffRule)
			throws BusinessException {
		MeetdatasubVO[] subvos = (MeetdatasubVO[]) aggvo.getChildrenVO();
		AggMeetRltHeadVO meetvo=new AggMeetRltHeadVO();

		MeetResultHeadVO headvo = new MeetResultHeadVO();
		MeetdataVO vo=(MeetdataVO) aggvo.getParentVO();
		headvo.setAlone_id(vo.getAloneid());
        headvo.setPk_keygroup(vo.getPk_keygroup());
		headvo.setIsmeetable(UFBoolean.TRUE);

		//�в����Ŀ���ǵ�ֵ�Է�
		headvo.setIsmeetequal(UFBoolean.TRUE);
		
		headvo.setNote(vo.getPk_self() + vo.getPk_opp());
		headvo.setPk_meetorg(vo.getPk_contrastorg());
		headvo.setPk_dxrelation(vo.getPk_dxrela());

//		String pk_difrule=new DXSchemeQrySrvImpl().getDifruleBySchemeAndRelation(vo.getPk_scheme(), vo.getPk_dxrela());
		DXRelaDiffRuleVO rulevo = diffRule.get(vo.getPk_dxrela());
		if(rulevo != null)
			headvo.setPk_difrule(rulevo.getPk_difrule());
		headvo.setPk_hbscheme(vo.getPk_scheme());
		headvo.setPk_selforg(vo.getPk_self());
		headvo.setPk_countorg(vo.getPk_opp());
		List<MeetResultBodyVO> volist = new ArrayList<MeetResultBodyVO>();
		double debit = 0;
		double credit = 0;
		for (MeetdatasubVO svo : subvos) {
			if (svo.getDirection().intValue() == IDXRelaConst.DEBIT) {
				debit = debit + NumberFormatUtil.Number2(svo.getAmount().doubleValue());
			} else {
				credit = credit + NumberFormatUtil.Number2(svo.getAmount().doubleValue());
			}

			MeetResultBodyVO bodyvo = new MeetResultBodyVO();
			bodyvo.setAdjust_amount(svo.getAmount());
			bodyvo.setMeet_amount(svo.getAmount());
			bodyvo.setDirection(svo.getDirection());
			bodyvo.setBself(svo.getBself());
			bodyvo.setPk_measure(svo.getPk_measure());
			bodyvo.setStatus(VOStatus.NEW);
			
			bodyvo.setAloneid(svo.getAloneid());
			bodyvo.setMeasurecode(svo.getMeasurecode());
			bodyvo.setPk_opporg(svo.getPk_opporg());
			bodyvo.setPk_selforg(svo.getPk_selforg());
			
			//���ö���˵��
			StringBuffer connectPK = new StringBuffer();
			connectPK.append(vo.getPk_self());
			connectPK.append(vo.getPk_opp());
			connectPK.append(vo.getAloneid());
			connectPK.append(vo.getPk_scheme());
			connectPK.append(vo.getPk_dxrela());
			connectPK.append(svo.getPk_measure());
			if(map != null && map.get(connectPK.toString()) != null){
				bodyvo.setMeetnote(map.get(connectPK.toString()));
			}
			if(svo.getMeetNode()!=null){
				bodyvo.setMeetnote(svo.getMeetNode());
			}
			
			volist.add(bodyvo);
		}
		headvo.setCreditamount(new UFDouble(credit));
		headvo.setDebitamount(new UFDouble(debit));
		meetvo.setParentVO(headvo);
		meetvo.setChildrenVO(volist.toArray(new MeetResultBodyVO[0]));
		
		resultList.add(meetvo);
//		HBProxy.getRemoteMeetResult().insert(meetvo);

	}


	//֧�ֲ�������ж���
	public void genContrastResult(AggMeetdataVO  aggvo, HBSchemeVO schemevo,
			DXContrastVO dxvo,Map<String, String> map,List<AggMeetRltHeadVO> resultList,Map<String, DXRelaDiffRuleVO> diffRule) throws BusinessException {
		MeetdatasubVO[] subvos = (MeetdatasubVO[]) aggvo.getChildrenVO();
		// IF������������ģ���޷��жϣ������κ�ģ�嶼�����ж϶���˫������
//		if(!(dxvo.getHeadvo().getType().equals(IDXRelaConst.RIGHTANDRIGHT_INVEST) 
//				||dxvo.getHeadvo().getType().equals(IDXRelaConst.RIGHTANDRIGHT_UNINVEST)
//				||dxvo.getHeadvo().getType().equals(IDXRelaConst.RIGHTANDRIGHT_ALLOWNERINVEST))){
//			this.validaty(subvos);
//		}
		MeetResultHeadVO headvo = new MeetResultHeadVO();

		AggMeetRltHeadVO meetvo =new AggMeetRltHeadVO();
		MeetdataVO vo=(MeetdataVO) aggvo.getParentVO();
		headvo.setAlone_id(vo.getAloneid());
		headvo.setPk_keygroup(vo.getPk_keygroup());
		headvo.setNote(vo.getPk_self() + vo.getPk_opp());
		headvo.setPk_meetorg(vo.getPk_contrastorg());
		headvo.setPk_dxrelation(vo.getPk_dxrela());
		headvo.setPk_hbscheme(vo.getPk_scheme());
		headvo.setPk_selforg(vo.getPk_self());
		headvo.setPk_countorg(vo.getPk_opp());
		List<MeetResultBodyVO> volist = new ArrayList<MeetResultBodyVO>();
		double debit = 0;
		double credit = 0;
		DXRelaDiffRuleVO difvo = diffRule.get(dxvo.getHeadvo().getPk_dxrela_head());
		
		if(null!=difvo){
			headvo.setPk_difrule(difvo.getPk_difrule());
		}
		

		if (null == difvo /*|| difvo.getDiffvalue().intValue()==0*/  || (difvo.getUsedflag()!=null && difvo.getUsedflag().booleanValue()==false)) {
			for (MeetdatasubVO svo : subvos) {
				if (svo.getDirection().intValue() == IDXRelaConst.DEBIT) {
					debit = debit + NumberFormatUtil.Number2(svo.getAmount().doubleValue());
				} else {
					credit = credit + NumberFormatUtil.Number2(svo.getAmount().doubleValue());
				}
				MeetResultBodyVO bodyvo = new MeetResultBodyVO();
				bodyvo.setAdjust_amount(svo.getAmount());
				bodyvo.setMeet_amount(svo.getAmount());
				bodyvo.setDirection(svo.getDirection());
				bodyvo.setBself(svo.getBself());
				bodyvo.setPk_measure(svo.getPk_measure());
				bodyvo.setStatus(VOStatus.NEW);
				

				bodyvo.setAloneid(svo.getAloneid());
				bodyvo.setMeasurecode(svo.getMeasurecode());
				bodyvo.setPk_opporg(svo.getPk_opporg());
				bodyvo.setPk_selforg(svo.getPk_selforg());
				
				//���ö���˵��
				StringBuffer connectPK = new StringBuffer();
				connectPK.append(vo.getPk_self());
				connectPK.append(vo.getPk_opp());
				connectPK.append(vo.getAloneid());
				connectPK.append(vo.getPk_scheme());
				connectPK.append(vo.getPk_dxrela());
				connectPK.append(svo.getPk_measure());
				if(map != null && map.get(connectPK.toString()) != null){
					bodyvo.setMeetnote(map.get(connectPK.toString()));
				}
				if(svo.getMeetNode()!=null){
					bodyvo.setMeetnote(svo.getMeetNode());
				}
				volist.add(bodyvo);
			}
			
			if (NumberFormatUtil.Number2(credit) == NumberFormatUtil.Number2(debit)) {
				// ��ֵ�Է�
				headvo.setIsmeetable(UFBoolean.TRUE);
				headvo.setIsmeetequal(UFBoolean.TRUE);
			} else {
				// δ�Է�
				headvo.setIsmeetable(UFBoolean.FALSE);
				headvo.setIsmeetequal(null);
			}
			
			headvo.setCreditamount(new UFDouble(credit));
			headvo.setDebitamount(new UFDouble(debit));
			 meetvo.setParentVO(headvo);
			 meetvo.setChildrenVO(volist.toArray(new MeetResultBodyVO[0]));
			 
			resultList.add(meetvo);
//			HBProxy.getRemoteMeetResult().insert(meetvo);

		} else { // ��ʼ�߲�����
			new GenDifContrastResultBO(difvo,headvo,subvos,dxvo).genResultHead();

		}


	}


//	private boolean validaty(MeetdatasubVO[] subvos) throws BusinessException {
//		boolean result = false;
//		boolean self = false;
//		boolean opp = false;
//		if(subvos != null) {
//
//			for (MeetdatasubVO vo : subvos) {
//				if (vo.getBself().booleanValue()) {
//					self = true;
//				} else {
//					opp = true;
//				}
//			}
//		}
//		if (self && opp) {
//			result = true;
//		} else {
//			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0453")/*@res "���ڶ��˲�˫����������ͬһ��������!!"*/);
//		}
//		return result;
//	}

}