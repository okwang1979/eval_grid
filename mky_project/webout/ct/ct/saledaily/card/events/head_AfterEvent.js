/*
 * @Author: wangshrc 
 * @PageInfo: 销售合同维护卡片  
 * @Date: 2018-05-04 16:20:17 
 * @Last Modified by: liangzhyf
 * @Last Modified time: 2019-09-10 18:30:51
 */
import { ajax, toast } from 'nc-lightapp-front';
import { SALEDAILY_CONST, SALEDAILY_URL, CARDTABLEAREAIDS, FIELDS } from '../../const';
import { showChangeOrgDialog, showWarningInfo } from '../../../../../scmpub/scmpub/pub/tool/messageUtil';
import { cardBodyAreas, clearCardData } from '../../utils/cardPageUtil';
import {
	createExtBillHeadAfterEventData,
	processExtBillCardHeadEditResult
} from '../../../../../scmpub/scmpub/pub/tool/afterEditUtil';
import { getLangByResId } from '../../../../../scmpub/scmpub/pub/tool/multiLangUtil';
import buttonController from '../viewController/buttonController';
import addrow_BtnClick from '../btnClicks/addrow_BtnClick';
import { transtypeUtils } from '../../../../../scmpub/scmpub/pub/tool';
import { getDefData } from '../../../../../scmpub/scmpub/pub/cache/cacheDataManager';

export default function(props, moduleId, key, value, oldVal, refInfo) {
	if (key == 'pk_org_v') {
		// 主组织切换事件
		if (!value.value) {
			showChangeOrgDialog({
				beSureBtnClick: clearData.bind(this, props),
				cancelBtnClick: () => {
					props.form.setFormItemsValue(moduleId, {
						pk_org_v: { value: oldVal.value, display: oldVal.display }
					});
				}
			});
			buttonController.call(this, props);
			return;
		} else if (value.value) {
			let data = createExtBillHeadAfterEventData(
				props,
				SALEDAILY_CONST.CARDPAGEID,
				SALEDAILY_CONST.FORMID,
				cardBodyAreas,
				moduleId,
				key,
				value
			);
			data.card.bodys = []; // 切换主组织时，表格数据会被清空

			props.resMetaAfterPkorgEdit();

			if (oldVal && oldVal.value) {
				// 主组织切换前，有数据
				showChangeOrgDialog({
					beSureBtnClick: doAction.bind(this, props, data, key),
					cancelBtnClick: () => {
						props.form.setFormItemsValue(moduleId, {
							pk_org_v: { value: oldVal.value, display: oldVal.display }
						});
					}
				});
				return;
			} else {
				doAction.call(this, props, data, key);
			}
		}
	} else if (
		key == 'pk_customer' ||
		key == 'personnelid' ||
		key == 'subscribedate' ||
		key == 'invallidate' ||
		key == 'valdate' ||
		key == 'ctrantypeid' ||
		key == 'nglobalexchgrate' ||
		key == 'nexchangerate' ||
		key == 'ngroupexchgrate' ||
		key == 'corigcurrencyid'
	) {
		let reqData = createExtBillHeadAfterEventData(
			props,
			SALEDAILY_CONST.CARDPAGEID,
			SALEDAILY_CONST.FORMID,
			cardBodyAreas,
			moduleId,
			key,
			value
		);
		doAction.call(this, props, reqData, key);
	} else if (key == 'pk_payterm') {
		//需要假删除付款协议行，保存时要用
		let allrows = props.cardTable.getAllRows(SALEDAILY_CONST.CARDTABLE_PAYTERM);
		// let rownumbers = props.cardTable.getNumberOfRows(SALEDAILY_CONST.CARDTABLE_PAYTERM);
		let rowid = [];
		let rowindex = [];
		let length = allrows.length;
		for (let i = 0; i < length; i++) {
			if (allrows[i].status != '3') {
				rowindex.push(i);
				rowid.push(allrows[i].rowid);
			}
		}
		props.cardTable.delRowsByIndex(SALEDAILY_CONST.CARDTABLE_PAYTERM, rowindex);
		// rowid.map((id) => {
		// 	props.cardTable.delRowByRowId(SALEDAILY_CONST.CARDTABLE_PAYTERM, id);
		// });
		if (value.value) {
			let reqData = createExtBillHeadAfterEventData(
				props,
				SALEDAILY_CONST.CARDPAGEID,
				SALEDAILY_CONST.FORMID,
				cardBodyAreas,
				moduleId,
				key,
				value
			);
			doAction.call(this, props, reqData, key);
		} else {
			buttonController.call(this, props);
		}
	} else if (key === 'vdef33') {
		if(value.value){
			let meta = props.meta.getMeta();
			meta[moduleId].items.map((item) => {
				if (item.attrcode == 'vdef34') {
						item.queryCondition = () => {
										return { vdef33Val: value.value}; 
						};
				}
			});
			props.meta.setMeta(meta);
		}
		
	}
}

function clearData(props) {
	clearCardData.call(this, props); // 清空卡片数据
	// 报账平台，默认交易类型值
	let transtypecode = getDefData(SALEDAILY_CONST.transTypeCacheKey, 'transtypecode');
	if (transtypecode) {
		let billtypeid = getDefData(SALEDAILY_CONST.transTypeCacheKey, 'billtypeid');
		let billtypename = getDefData(SALEDAILY_CONST.transTypeCacheKey, 'billtypename');
		props.form.setFormItemsValue(SALEDAILY_CONST.FORMID, {
			[FIELDS.ctrantypeid]: { value: billtypeid, display: billtypename },
			[FIELDS.vtrantypecode]: { value: transtypecode, display: transtypecode }
		});
	}
}

function doAction(props, data, key) {
	// transtypeUtils.setValue.call(this, SALEDAILY_CONST.FORMID, 'ctrantypeid', 'vtrantype');
	ajax({
		url: SALEDAILY_URL.CARD_HEAD_AFTER_URL,
		data: data,
		async: false,
		success: (res) => {
			if (res.data) {
				this.props.beforeUpdatePage();
				if (key == 'pk_payterm') {
					if (res.data.extbillcard.bodys[SALEDAILY_CONST.CARDTABLE_PAYTERM]) {
						res.data.extbillcard.bodys[SALEDAILY_CONST.CARDTABLE_PAYTERM].rows.map((row, index) => {
							row.status = 2;
							row.values[FIELDS.pk_ct_sale].value = props.form.getFormItemsValue(
								SALEDAILY_CONST.FORMID,
								FIELDS.pk_ct_sale
							).value;
							props.cardTable.insertRowsAfterIndex(SALEDAILY_CONST.CARDTABLE_PAYTERM, row);
							// props.cardTable.insertDataByIndexs(SALEDAILY_CONST.CARDTABLE_PAYTERM,row)
						});
					}
					res.data.extbillcard.bodys[SALEDAILY_CONST.CARDTABLE_PAYTERM] = null;
				}
				processExtBillCardHeadEditResult(props, SALEDAILY_CONST.FORMID, cardBodyAreas, res.data);
				//主组织编辑后清空页面表体数据
				if (key == 'pk_org_v') {
					cardBodyAreas.forEach((tableId) => {
						props.cardTable.setTableData(tableId, { rows: [] });
					});
					// 设置默认交易类型(发布交易类型后, 报账平台)
					transtypeUtils.setValue.call(
						this,
						SALEDAILY_CONST.FORMID,
						FIELDS.ctrantypeid,
						FIELDS.vtrantypecode
					);
					// 报账平台，默认交易类型值
					let transtypecode = getDefData(SALEDAILY_CONST.transTypeCacheKey, 'transtypecode');
					if (transtypecode) {
						let billtypeid = getDefData(SALEDAILY_CONST.transTypeCacheKey, 'billtypeid');
						let billtypename = getDefData(SALEDAILY_CONST.transTypeCacheKey, 'billtypename');
						props.form.setFormItemsValue(SALEDAILY_CONST.FORMID, {
							[FIELDS.ctrantypeid]: { value: billtypeid, display: billtypename },
							[FIELDS.vtrantypecode]: { value: transtypecode, display: transtypecode }
						});
					}
					//主组织切换后自动增行
					addrow_BtnClick.call(this, props, SALEDAILY_CONST.CARDTABLEID);
					addRowForModify.call(this, props);
				}
				if (res.data.userObject.errMsg) {
					let oldvalue = data.oldvalue.value;
					this.props.form.setFormItemsValue(SALEDAILY_CONST.FORMID, { [key]: { value: oldvalue } });
					showWarningInfo(
						getLangByResId(this, '4006SALEDAILY-000045'),
						res.data.userObject.errMsg
					); /* 国际化处理： 提示*/
				}
				if (res.data.userObject.showPayTerm !== undefined) {
					this.setState({
						showPayTerm: res.data.userObject.showPayTerm
					});
				}
				buttonController.call(this, props);
				this.props.updatePage(SALEDAILY_CONST.FORMID, CARDTABLEAREAIDS);
			}
		}
	});
}
/**
 * 组织编辑后需要在变更历史表格增一行
 * @param {*} props 
 */
function addRowForModify(props) {
	props.cardTable.addRow(
		SALEDAILY_CONST.CARDTABLE_CHANGE,
		undefined,
		{ vchangecode: { value: 1 }, vmemo: { value: getLangByResId(this, '4006SALEDAILY-000086') /* 国际化处理：原始版本*/ } },
		true
	);
}
