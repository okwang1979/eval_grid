import { high } from 'nc-lightapp-front';
import { conf as unitConf } from '../../../../uapbd/refer/org/BusinessUnitTreeRef/index';

const { Refer } = high;

export default function (props = {}) {
	var conf = {
		multiLang: {
			domainName: 'ct',
			currentLocale: 'zh-CN',
			moduleId: '4004refer',
		},

		refType: 'tree',
		refName: '合同分类及标的',/* 国际化处理： 自定义档案*/
		placeholder: '合同分类及标的',/* 国际化处理： 自定义档案*/
		rootNode: { refname: '合同分类及标的', refpk: 'root' },/* 国际化处理： 自定义档案*/
		refCode: 'ct.refer.contract.type',
		queryTreeUrl: '/nccloud/ct/purdaily/cttype.do',    
		treeConfig: { name: ['refer-000002', 'refer-000003'], code: ['refcode', 'refname'] },/* 国际化处理： 编码,名称*/
		isMultiSelectedEnabled: false,
		unitProps: unitConf,
		onlyLeafCanSelect:true,
		isShowUnit: false
	};

	return <Refer {...conf} {...props} />
}
