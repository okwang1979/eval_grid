package nc.bs.arap.busireg;

import java.util.List;
import nc.bs.businessevent.IBusinessEvent;
import nc.bs.framework.common.NCLocator;
import nc.itf.arap.tally.ITallySourceData;
import nc.pubitf.arap.tally.ITallyService;
import nc.vo.arap.tally.BusiTypeEnum;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nccloud.commons.lang.ArrayUtils;

public class ApproveF0Listener extends AbstractTallyListener {
	public void doAction(IBusinessEvent event) throws BusinessException {
		AggregatedValueObject[] obills = getBills(event);
		if (ArrayUtils.isEmpty(obills)) {
			return;
		}
		String eventType = event.getEventType();
		List<ITallySourceData> tallySourceData = construct(obills);
		if ((tallySourceData == null) || (tallySourceData.size() == 0)) {
			return;
		}
//		ITallyService tallySrv = (ITallyService) NCLocator.getInstance().lookup(ITallyService.class);
		if ("1002".equals(eventType)) {
		
		}
	}

}
