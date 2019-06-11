package nc.ui.hbbb.meetaccount.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.logging.Logger;
import nc.ui.pub.bill.BillItem;
import nc.ui.pub.bill.BillListPanel;
import nc.ui.pub.bill.BillModel;
import nc.ui.uif2.editor.BillListView.IBillListPanelValueSetter;
import nc.vo.hbbb.meetaccount.AggMeetRltHeadVO;
import nc.vo.hbbb.meetaccount.MeetResultBodyVO;
import nc.vo.hbbb.meetaccount.MeetResultHeadVO;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.lang.UFDouble;
/**
 *
 * <p>
 * 对账结果明细设置策略
 * </p>
 *
 * 修改记录：<br>
 * <li>修改人：修改日期：修改内容：</li>
 * <br><br>
 *
 * @see
 * @author wangxwb
 * @version V6.0
 * @since V6.0 创建时间：2011-1-26 上午10:29:10
 */
public class MeetResultSetter implements IBillListPanelValueSetter {

	/**
	 * 设置表头的值
	 */
	@Override
	public void setHeaderDatas(BillListPanel listPanel, Object[] allDatas) {
		if (allDatas == null || allDatas.length == 0) {
			listPanel.setHeaderValueVO(null);
		} else {
			listPanel.setHeaderValueVO(getMeetRedultDetail(allDatas));
			BillModel headModel = listPanel.getHeadBillModel();
			if(headModel != null)
			{
				//处理参照信息
				if(listPanel.getBillListData().isMeataDataTemplate())
					headModel.loadLoadRelationItemValue();
				//处理装载公式
				headModel.execLoadFormula();
			}
		}
	}
	/**
	 *
	 * 获得查询的小计合计列表
	 * <p>修改记录：</p>
	 * @param allDatas
	 * @return
	 * @see
	 * @since V6.0
	 */
	public static CircularlyAccessibleValueObject[] getMeetRedultDetail(Object[] allDatas){
		List<CircularlyAccessibleValueObject> cirlst = new ArrayList<CircularlyAccessibleValueObject>();

		HashMap<String, Collection<MeetResultHeadVO>> totalMap = getTotal(allDatas);
		
		HashMap<String, Collection<AggMeetRltHeadVO>> map = new HashMap<String, Collection<AggMeetRltHeadVO>> ();
		//先将具体值按照MAP分组
		for (int i = 0; i < allDatas.length; i++) {
			AggMeetRltHeadVO meetVO = (AggMeetRltHeadVO) (allDatas[i]);
			MeetResultBodyVO[] bodyVos = (MeetResultBodyVO[]) meetVO.getChildrenVO();
			
			if(bodyVos == null) {
				continue;
			}
			MeetResultHeadVO headVO=(MeetResultHeadVO) meetVO.getParentVO();
			if(!map.containsKey(headVO.getPk_dxrelation())){
				ArrayList<AggMeetRltHeadVO>  list = new ArrayList<AggMeetRltHeadVO>();
				list.add(meetVO);
				map.put(headVO.getPk_dxrelation(), list);
			}else{
				map.get(headVO.getPk_dxrelation()).add(meetVO);
			}
		}
		Set<String> keySet = map.keySet();
		for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext();) {
			Double totaldebit = 0.0 ;//借方合计
			Double totalcredit = 0.0 ;//贷方合计
			
			Double totalvoucherdebit = 0.0 ;//分录借方合计
			Double totalvouchercredit = 0.0 ;//分录贷方合计
			
			String pk_dxrelation = (String) iterator.next();
			Collection<AggMeetRltHeadVO> collection = map.get(pk_dxrelation);
			for (Iterator<AggMeetRltHeadVO> iterator2 = collection.iterator(); iterator2.hasNext();) {
				AggMeetRltHeadVO aggMeetRltHeadVO = (AggMeetRltHeadVO) iterator2.next();
				MeetResultBodyVO[] bodyVos = (MeetResultBodyVO[]) aggMeetRltHeadVO.getChildrenVO();

				if (bodyVos == null) {
					continue;
				}

				MeetResultHeadVO headVO = (MeetResultHeadVO) aggMeetRltHeadVO.getParentVO();
				for (int j = 0; j < bodyVos.length; j++) {
					if (bodyVos.length >= 1 && j == 0) {//第一行设置本对方单位
						headVO.setPk_measure(bodyVos[j].getPk_measure());
						headVO.setNote(null);
						headVO.setNote2(null);
						headVO.setNote3(null);
						headVO.setNote4(null);
						headVO.setNote5(null);
						headVO.setNote6(null);
						// 借方
						if ((bodyVos[j].getDirection().intValue() == 0)) {
							headVO.setDebitamount(bodyVos[j].getMeet_amount());
							headVO.setVoucherdebitamount(bodyVos[j].getAdjust_amount());
						} else {
							headVO.setCreditamount(bodyVos[j].getMeet_amount());
							headVO.setVouchercreditamount(bodyVos[j].getAdjust_amount());
						}

						headVO.setNoteshow(bodyVos[j].getMeetnote());
						cirlst.add(headVO);
					} else {//其他行
						MeetResultHeadVO vo = new MeetResultHeadVO();
						vo.setPk_totalinfo(headVO.getPk_totalinfo());

						vo.setPk_dxrelation(pk_dxrelation);
						vo.setPk_hbscheme(headVO.getPk_hbscheme());
						vo.setAlone_id(headVO.getAlone_id());
						vo.setPk_keygroup(headVO.getPk_keygroup());

						// vo.setIsquantity(headVO.getIsquantity());
						vo.setIsmeetable(headVO.getIsmeetable());
						// vo.setAmounttype(headVO.getAmounttype());

						vo.setPk_measure(bodyVos[j].getPk_measure());
						// 借方
						if ((bodyVos[j].getDirection().intValue() == 0)) {
							vo.setDebitamount(bodyVos[j].getMeet_amount());
							vo.setVoucherdebitamount(bodyVos[j].getAdjust_amount());
						} else {
							vo.setCreditamount(bodyVos[j].getMeet_amount());
							vo.setVouchercreditamount(bodyVos[j].getAdjust_amount());
						}
						vo.setNoteshow(bodyVos[j].getMeetnote());
						vo.setDataorigin(headVO.getDataorigin());
						cirlst.add(vo);
					}
					
					if(bodyVos[j].getMeet_amount() != null){
						//借方
						if((bodyVos[j].getDirection().intValue()==0)){
							totaldebit = totaldebit + bodyVos[j].getMeet_amount().toDouble();
						}else{
							totalcredit = totalcredit + bodyVos[j].getMeet_amount().toDouble();
						}
					}
					if(bodyVos[j].getAdjust_amount()!= null){
						//借方
						if((bodyVos[j].getDirection().intValue()==0)){
							totalvoucherdebit = totalvoucherdebit + bodyVos[j].getAdjust_amount().toDouble();
						}else{
							totalvouchercredit = totalvouchercredit + bodyVos[j].getAdjust_amount().toDouble();
						}
					}
				}
			}
			Collection<MeetResultHeadVO> tmpcollection = totalMap.get(pk_dxrelation);
			MeetResultHeadVO[] array = tmpcollection.toArray(new MeetResultHeadVO[0]);
			
			//合并项目小计
			for (int j = 0; j < array.length; j++) {
				MeetResultHeadVO meetResultHeadVO = array[j];
				if (j != 0) {
					meetResultHeadVO.setIstotal(null);
				}
			}
			cirlst.addAll(Arrays.asList(array));
			
			//借贷合计
			if(array != null){
				MeetResultHeadVO newvo = (MeetResultHeadVO)array[0].clone();
				newvo.setIstotal(MeetResultHeadVO.DEBITANDCREDIT_TOTAL);
				newvo.setVouchercreditamount(new UFDouble(totalvouchercredit));
				newvo.setVoucherdebitamount(new UFDouble(totalvoucherdebit));
				newvo.setPk_measure(null);
				newvo.setDebitamount(new UFDouble(totaldebit));
				newvo.setCreditamount(new UFDouble(totalcredit));
				cirlst.add(newvo);
			}
			
		}
		CircularlyAccessibleValueObject[] vos = cirlst.toArray(new CircularlyAccessibleValueObject[cirlst.size()]);
		return vos;
	}
	
	
	//
	private static HashMap<String,Collection<MeetResultHeadVO>>  getTotal(Object[] allDatas) {
		//本对方单位PK+模板PK
		HashMap<String,ArrayList<AggMeetRltHeadVO>>   groupMap = new HashMap<String,ArrayList<AggMeetRltHeadVO>>();
		//合计结果
		HashMap<String,Collection<MeetResultHeadVO>>   totalMap = new HashMap<String,Collection<MeetResultHeadVO>>();
		for (int i = 0; i < allDatas.length; i++) {
			AggMeetRltHeadVO meetVO = (AggMeetRltHeadVO) (allDatas[i]);
			MeetResultHeadVO headVO=(MeetResultHeadVO) meetVO.getParentVO();
			String pk_dxrelation = headVO.getPk_dxrelation();
		/*	String pk_selforg = headVO.getPk_selforg();
			String pk_countorg = headVO.getPk_countorg();*/
			String key =/* pk_selforg+pk_countorg+*/pk_dxrelation;
			if(groupMap.containsKey(key)){
				groupMap.get(key).add(meetVO);
			}else{
				ArrayList<AggMeetRltHeadVO>  tmpArrayList = new ArrayList<AggMeetRltHeadVO>();
				tmpArrayList.add(meetVO);
				groupMap.put(key, tmpArrayList);
			}
		}
		Set<String> keySet = groupMap.keySet();
		for (String key : keySet) {
			//合计
//			MeetResultHeadVO tmptotalVO = new MeetResultHeadVO();
			ArrayList<AggMeetRltHeadVO> arrayList = groupMap.get(key);
			
			//合并项目对应的合计结果:使用有序的LinkedHashMap
			Map<String,MeetResultHeadVO> hbprojuectMap = new LinkedHashMap<String,MeetResultHeadVO>();
			for (Iterator<AggMeetRltHeadVO> iterator = arrayList.iterator(); iterator.hasNext();) {
				AggMeetRltHeadVO aggMeetRltHeadVO = (AggMeetRltHeadVO) iterator.next();
				MeetResultHeadVO headVO=(MeetResultHeadVO) aggMeetRltHeadVO.getParentVO();
				MeetResultBodyVO[] bodyVos = (MeetResultBodyVO[]) aggMeetRltHeadVO.getChildrenVO();
				for(int j = 0 ; j < bodyVos.length; j++){
					String pk_measure = bodyVos[j].getPk_measure();
					String projectkeykey = pk_measure+headVO.getPk_dxrelation();
					if(hbprojuectMap.containsKey(projectkeykey)){
						MeetResultHeadVO meetResultHeadVO = hbprojuectMap.get(projectkeykey);
						//借方
						if((bodyVos[j].getDirection().intValue()==0)){
							UFDouble debitamount = meetResultHeadVO.getDebitamount();
							meetResultHeadVO.setDebitamount(debitamount ==null ?bodyVos[j].getMeet_amount():debitamount.add(bodyVos[j].getMeet_amount()));
							UFDouble voucherdebitamount = meetResultHeadVO.getVoucherdebitamount();
							if(voucherdebitamount!=null)
								meetResultHeadVO.setVoucherdebitamount(voucherdebitamount.add(bodyVos[j].getAdjust_amount()==null ?UFDouble.ZERO_DBL:bodyVos[j].getAdjust_amount()));
						}else{
							UFDouble creditamount = meetResultHeadVO.getCreditamount();
							meetResultHeadVO.setCreditamount(creditamount ==null ?bodyVos[j].getMeet_amount() : creditamount.add(bodyVos[j].getMeet_amount()));
							UFDouble vouchercreditamount = meetResultHeadVO.getVouchercreditamount();
							if(vouchercreditamount!=null)
								meetResultHeadVO.setVouchercreditamount(vouchercreditamount.add(bodyVos[j].getAdjust_amount()==null ?UFDouble.ZERO_DBL:bodyVos[j].getAdjust_amount()));
						}
					}else{
						MeetResultHeadVO tmptotalVO = new MeetResultHeadVO();
						//借方
						if((bodyVos[j].getDirection().intValue()==0)){
							tmptotalVO.setDebitamount(bodyVos[j].getMeet_amount());
							tmptotalVO.setVoucherdebitamount(bodyVos[j].getAdjust_amount());
						}else{
							tmptotalVO.setCreditamount(bodyVos[j].getMeet_amount());
							tmptotalVO.setVouchercreditamount(bodyVos[j].getAdjust_amount());
						}
						tmptotalVO.setPk_dxrelation(key);
						tmptotalVO.setPk_measure(pk_measure);
						tmptotalVO.setIstotal(MeetResultHeadVO.PROJECT_TOTAL);
//						tmptotalVO.setNote(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("common","UC000-0001146")/*@res "合计"*/);
//						tmptotalVO.setPk_totalinfo(headVO.getPk_totalinfo());

						tmptotalVO.setPk_hbscheme(headVO.getPk_hbscheme());
						tmptotalVO.setAlone_id(headVO.getAlone_id());
						tmptotalVO.setPk_keygroup(headVO.getPk_keygroup());
						hbprojuectMap.put(projectkeykey, tmptotalVO);
					}
				}
//				Collection<MeetResultHeadVO> values = hbprojuectMap.values();
			}
//			ArrayList<MeetResultHeadVO> tmpArray  =new ArrayList<MeetResultHeadVO>();
			/*AggMeetRltHeadVO aggMeetRltHeadVO2 = arrayList.get(0);
			MeetResultHeadVO headVO2=(MeetResultHeadVO) aggMeetRltHeadVO2.getParentVO();
			MeetResultBodyVO[] childrenVO = (MeetResultBodyVO[]) aggMeetRltHeadVO2.getChildrenVO();
			for (int i = 0; i < childrenVO.length; i++) {
				MeetResultBodyVO meetResultBodyVO = childrenVO[i];
				tmpArray.add(hbprojuectMap.get(meetResultBodyVO.getPk_measure()+headVO2.getPk_dxrelation()));
			}*/
			totalMap.put(key, hbprojuectMap.values());
		}
		return totalMap;
	}

	/**
	 * 设置表头特定行的值
	 */
	@Override
	public void setHeaderRowData(BillListPanel listPanel, Object rowData, int row) {
		 if (rowData == null) {
		      Logger.debug("MeetResultSetter.setHeaderRowData. 设置第" + row
		          + "行的值为null. 忽略该操作");
		    }
		    else {
		      BillModel headModel = listPanel.getBillListData().getHeadBillModel();
		      if (headModel != null) {
		        headModel
		            .setBodyRowVO(((CircularlyAccessibleValueObject) rowData), row);
		        // 处理参照信息
		        if (listPanel.getBillListData().isMeataDataTemplate()) {
		          BillItem[] items = headModel.getBodyItems();
		          if (items != null && items.length > 0) {
		            for (BillItem item : items) {
		              headModel.loadLoadRelationItemValue(row, item.getKey());
		            }
		          }
		        }
		        // 处理装载公式
		        headModel.execLoadFormulaByRow(row);
		      }
		    }

	}

	/**
	 * 设置表体值
	 */
	@Override
	public void setBodyData(BillListPanel listPanel, Object selectedData) {

	}

}