package nccloud.web.arap.arappub.action;

import nc.ui.querytemplate.querytree.IQueryScheme;
import nc.vo.pub.billtype.BilltypeVO;
import nccloud.framework.core.exception.ExceptionUtils;
import nccloud.framework.core.json.IJson;
import nccloud.framework.service.ServiceLocator;
import nccloud.framework.web.action.itf.ICommonAction;
import nccloud.framework.web.container.IRequest;
import nccloud.framework.web.container.SessionContext;
import nccloud.framework.web.json.JsonFactory;
import nccloud.framework.web.ui.pattern.billcard.SpecilAggBill;
import nccloud.framework.web.ui.pattern.billgrid.BillGrid;
import nccloud.framework.web.ui.pattern.billgrid.BillGridOperator;
import nccloud.pubitf.arap.arappub.IArapPfDataCacheService;
import nccloud.pubitf.platform.query.INCCloudQueryService;
import nccloud.pubitf.platform.template.IAppTemplateAssignment;
import nccloud.web.arap.arappub.Info.SearchInfo;
import nccloud.web.riart.billref.src.INccSrcBillReferQuery;

/**
 * 转单界面查询
 * 
 * @author wangshyh
 * 
 */
public class CtTransferQuery implements ICommonAction {

	@Override
	public Object doAction(IRequest request) {
		BillGrid[] grids = null;
		try {
			// 获取前台json
			String read = request.read();
			IJson json = JsonFactory.create();
			SearchInfo info = json.fromJson(read, SearchInfo.class);
			// 转换成queryscheme
			INCCloudQueryService queryutil = ServiceLocator.find(INCCloudQueryService.class);
			IQueryScheme scheme = queryutil.convertCondition(info.getQueryInfo());
			INccSrcBillReferQuery service = getService(info);
			// 查询 参考nc.ui.arap.billref.F1QueryServiceImpl.queryByQueryScheme(IQueryScheme)
			SpecilAggBill[] vos = (SpecilAggBill[]) service.querySrcBill(scheme, info.getSrc_billtype(), info.getDest_tradetype(), null);
			if (vos != null && vos.length > 0) {
				// 转换成dto
				BillGridOperator operator = new BillGridOperator(getPriorTemplateID(info), info.getPageId());
				operator.setTransFlag(false);
				grids = new BillGrid[vos.length];
				grids = operator.toBillGrids(vos);
			}
		} catch (Exception e) {
			ExceptionUtils.wrapException(e);
		}
		return grids;
	}

	private String getPriorTemplateID(SearchInfo info) {
		SessionContext context = SessionContext.getInstance();
		String templetid = null;
		if (info.getAppCode() != null && !info.getAppCode().equals(context.getAppcode())) {// 传递的应用编码与当前应用编码不一致时，以传递的应用编码为准
			String pagecode = info.getPageId();
			// 获取templetid
			IAppTemplateAssignment service = ServiceLocator.find(IAppTemplateAssignment.class);
			try {
				templetid = service.getPriorTemplateID(context.getClientInfo().getUserid(), context.getClientInfo().getPk_group(), info.getAppCode(), pagecode);
			} catch (Exception e) {
				ExceptionUtils.wrapException(e);
			}
			return templetid;
		}

		return templetid;
	}

	private INccSrcBillReferQuery getService(SearchInfo info) throws Exception {
		BilltypeVO vo = ServiceLocator.find(IArapPfDataCacheService.class).getBillType(info.getSrc_billtype());
		String implclass  = "nccloud.web.arap.ctbill.impl.CtbillTransferQueryImpl";
		// 实例化查询服务类
		Class<?> cla = Class.forName(implclass);
		INccSrcBillReferQuery service = (INccSrcBillReferQuery) cla.newInstance();
		return service;
	}

}
