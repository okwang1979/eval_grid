/*
 * @Author: wangshrc 
 * @PageInfo: 发货单模板加载  
 * @Date: 2018-05-31 19:33:44 
 * @Last Modified by: cuijun
 * @Last Modified time: 2019-07-09 10:31:14
 */
import { rowCopyPasteUtils } from '../utils';
import { SALEDAILY_CONST } from '../../const';
import { transtypeUtils } from '../../../../../scmpub/scmpub/pub/tool';
import { getLangByResId } from '../../../../../scmpub/scmpub/pub/tool/multiLangUtil';
import { columnSortUtils } from '../../../../../scmpub/scmpub/pub/tool/columnSortUtils';
import { initCard_BtnClick, onCardInnerButtonClicks } from '../btnClicks';
import { headRefersFilter } from '../refers';
import { setDefData } from '../../../../../scmpub/scmpub/pub/cache';

let formId = SALEDAILY_CONST.FORMID;
let tableId = SALEDAILY_CONST.CARDTABLEID;
export default function(props) {
	props.createUIDom(
		{
			pagecode: SALEDAILY_CONST.CARDPAGEID
		},
		(data) => {
			if (data) {
				transtypeUtils.init.call(this, data.context);
				//个性化设置组织放到缓存中
				if (data.context.org_Name && data.context.pk_org_v) {
					let org_v = {
						display: data.context.org_v_Name,
						value: data.context.pk_org_v
					};
					setDefData(SALEDAILY_CONST.DATA_SOURCE, 'pk_org_v', org_v);
				}
				if (data.button) {
					let button = data.button;
					props.button.hideButtonsByAreas([
						SALEDAILY_CONST.CARDHEADBUTTONAREA,
						SALEDAILY_CONST.CARDBODYBUTTONAREA
					]);
					props.button.setOprationBtnsRenderStatus([ SALEDAILY_CONST.CARDBODYINNERBUTTONAREA ], false);
					props.button.setButtons(button);
				}
				if (data.template) {
					let meta = data.template;
					modifierMeta.call(this, props, meta);
					props.meta.setMeta(meta, initCard_BtnClick.bind(this, props));
				}
			}
		}
	);
}

function modifierMeta(props, meta) {
	// 过滤表头字段
	meta[SALEDAILY_CONST.FORMID].items = meta[SALEDAILY_CONST.FORMID].items.map((item, key) => {
		if (item.attrcode == 'vdef5') {
			item.isMultiSelectedEnabled=true;
		}
		return item;
	});
	headRefersFilter(props, meta);
	columnSortUtils.numberSort(meta, tableId, 'crowno');
	let porCol = getOprCol.call(this, props, tableId);
	meta[tableId].items.push(porCol);
	porCol = getOprCol.call(this, props, SALEDAILY_CONST.CARDTABLE_TERM);
	meta[SALEDAILY_CONST.CARDTABLE_TERM].items.push(porCol);
	porCol = getOprCol.call(this, props, SALEDAILY_CONST.CARDTABLE_EXP);
	meta[SALEDAILY_CONST.CARDTABLE_EXP].items.push(porCol);
	porCol = getOprCol.call(this, props, SALEDAILY_CONST.CARDTABLE_PAYTERM);
	meta[SALEDAILY_CONST.CARDTABLE_PAYTERM].items.push(porCol);
	porCol = getOprCol.call(this, props, SALEDAILY_CONST.CARDTABLE_MEMORA);
	meta[SALEDAILY_CONST.CARDTABLE_MEMORA].items.push(porCol);
	return meta;
}
function getOprCol(props, tableId) {
	let _this = this;
	let areaId =
		(tableId == SALEDAILY_CONST.CARDTABLEID && SALEDAILY_CONST.CARDBODYINNERBUTTONAREA) ||
		(tableId == SALEDAILY_CONST.CARDTABLE_EXP && SALEDAILY_CONST.CARDBODYEXPINNERBUTTONAREA) ||
		(tableId == SALEDAILY_CONST.CARDTABLE_MEMORA && SALEDAILY_CONST.CARDBODYMEMORAINNERBUTTONAREA) ||
		(tableId == SALEDAILY_CONST.CARDTABLE_TERM && SALEDAILY_CONST.CARDBODYTERMINNERBUTTONAREA) ||
		(tableId == SALEDAILY_CONST.CARDTABLE_PAYTERM && SALEDAILY_CONST.CARDBODYPAYTERMINNERBUTTONAREA) ||
		SALEDAILY_CONST.CARDBODYINNERBUTTONAREA;
	let browseButtonList = (tableId == SALEDAILY_CONST.CARDTABLEID && [ 'Spread', 'DeleteLine', 'InsertLine' ]) ||
	(tableId == SALEDAILY_CONST.CARDTABLE_EXP && [ 'Exp_DeleteLine', 'Exp_InsertLine' ]) ||
	(tableId == SALEDAILY_CONST.CARDTABLE_MEMORA && [ 'Memora_DeleteLine', 'Memora_InsertLine' ]) ||
	(tableId == SALEDAILY_CONST.CARDTABLE_TERM && [ 'Term_DeleteLine', 'Term_InsertLine' ]) ||
	(tableId == SALEDAILY_CONST.CARDTABLE_PAYTERM && [ 'Payterm_DeleteLine', 'Payterm_InsertLine' ]) || [
		'Spread',
		'DeleteLine',
		'InsertLine'
	];
	let pasteButtonList = (tableId == SALEDAILY_CONST.CARDTABLEID && [ 'PasteLine' ]) ||
	(tableId == SALEDAILY_CONST.CARDTABLE_EXP && [ 'Exp_PasteLine' ]) ||
	(tableId == SALEDAILY_CONST.CARDTABLE_MEMORA && [ 'Memora_PasteLine' ]) ||
	(tableId == SALEDAILY_CONST.CARDTABLE_TERM && [ 'Term_PasteLine' ]) ||
	(tableId == SALEDAILY_CONST.CARDTABLE_PAYTERM && [ 'Payterm_PasteLine' ]) || [ 'PasteLine' ];
	let dataName = rowCopyPasteUtils.getCopyDataNameByTableId(tableId);
	let porCol = {
		label: getLangByResId(this, '4006SALEDAILY-000048') /* 国际化处理： 操作列*/,
		itemtype: 'customer',
		attrcode: 'opr',
		width: '180px',
		visible: true,
		fixed: 'right',
		render(text, record, index) {
			let status = props.getUrlParam('status');
			let type = props.getUrlParam('type');
			if (status !== SALEDAILY_CONST.STATUS_BROWSE && type != 'receive') {
				let buttonList = browseButtonList;
				if (_this.state[dataName]) buttonList = pasteButtonList;
				return (
					<div>
						<span>
							{props.button.createOprationButton(buttonList, {
								area: areaId,
								buttonLimit: 3,
								onButtonClick: (props, key) =>
									onCardInnerButtonClicks.call(
										_this,
										props,
										tableId,
										key,
										text,
										record,
										index,
										status
									)
							})}
						</span>
					</div>
				);
			} else {
				let buttonList = tableId == SALEDAILY_CONST.CARDTABLEID ? [ 'Spread' ] : [];
				return (
					<div>
						{
							<span>
								{props.button.createOprationButton(buttonList, {
									area: areaId,
									buttonLimit: 3,
									onButtonClick: (props, key) =>
										onCardInnerButtonClicks.call(
											_this,
											props,
											tableId,
											key,
											text,
											record,
											index,
											status
										)
								})}
							</span>
						}
					</div>
				);
			}
		}
	};
	return porCol;
}
