!function(e,t){"object"==typeof exports&&"object"==typeof module?module.exports=t(require("nc-lightapp-front"),require("react"),require("react-dom")):"function"==typeof define&&define.amd?define(["nc-lightapp-front","react","react-dom"],t):"object"==typeof exports?exports["ct/ct/saledaily/ref4310/index"]=t(require("nc-lightapp-front"),require("react"),require("react-dom")):e["ct/ct/saledaily/ref4310/index"]=t(e["nc-lightapp-front"],e.React,e.ReactDOM)}(window,function(e,t,a){return function(e){var t={};function a(n){if(t[n])return t[n].exports;var r=t[n]={i:n,l:!1,exports:{}};return e[n].call(r.exports,r,r.exports,a),r.l=!0,r.exports}return a.m=e,a.c=t,a.d=function(e,t,n){a.o(e,t)||Object.defineProperty(e,t,{enumerable:!0,get:n})},a.r=function(e){"undefined"!=typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(e,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(e,"__esModule",{value:!0})},a.t=function(e,t){if(1&t&&(e=a(e)),8&t)return e;if(4&t&&"object"==typeof e&&e&&e.__esModule)return e;var n=Object.create(null);if(a.r(n),Object.defineProperty(n,"default",{enumerable:!0,value:e}),2&t&&"string"!=typeof e)for(var r in e)a.d(n,r,function(t){return e[t]}.bind(null,r));return n},a.n=function(e){var t=e&&e.__esModule?function(){return e.default}:function(){return e};return a.d(t,"a",t),t},a.o=function(e,t){return Object.prototype.hasOwnProperty.call(e,t)},a.p="../../../../",a(a.s=201)}({0:function(t,a){t.exports=e},10:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.REF4310_CONST=void 0;var n=a(42);t.REF4310_CONST=n.REF4310_CONST},11:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.setRefShowDisabledData=t.setPsndocShowLeavePower=t.vbatchcodeHelper=t.getUrlParam=t.crossRuleUtils=t.marAsstUtils=t.transtypeUtils=t.dateFormat=t.deepClone=void 0;var n=d(a(22)),r=d(a(31)),o=d(a(32)),i=d(a(36)),s=a(37),u=d(a(38)),l=a(0),c=a(23);function d(e){return e&&e.__esModule?e:{default:e}}t.deepClone=l.deepClone,t.dateFormat=n.default,t.transtypeUtils=r.default,t.marAsstUtils=o.default,t.crossRuleUtils=i.default,t.getUrlParam=s.getUrlParam,t.vbatchcodeHelper=u.default,t.setPsndocShowLeavePower=c.setPsndocShowLeavePower,t.setRefShowDisabledData=c.setRefShowDisabledData},15:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.clearTransferCache=t.rewriteTransferSrcBids=t.updateCacheDataForList=t.deleteCacheDataForList=t.getNextId=t.getCurrentLastId=t.getDefData=t.setDefData=t.hasListCache=t.addCacheData=t.getCacheDataByPk=t.deleteCacheData=t.updateCacheData=t.changeUrlParam=void 0;var n=a(0);t.changeUrlParam=function(e,t){e.setUrlParam(t)},t.updateCacheData=function(e,t,a,r,o,i){n.cardCache.addCache;var s=n.cardCache.updateCache;n.cardCache.getCacheById,s(t,a,r,o,i)},t.deleteCacheData=function(e,t,a,r){var o=n.cardCache.deleteCacheById;n.cardCache.getNextId,o(t,a,r)},t.getCacheDataByPk=function(e,t,a){return(0,n.cardCache.getCacheById)(a,t)},t.addCacheData=function(e,t,a,r,o,i){(0,n.cardCache.addCache)(a,r,o,i)},t.hasListCache=function(e,t){return e.table.hasCacheData(t)},t.setDefData=function(e,t,a){(0,n.cardCache.setDefData)(t,e,a)},t.getDefData=function(e,t){return(0,n.cardCache.getDefData)(t,e)},t.getCurrentLastId=function(e){return(0,n.cardCache.getCurrentLastId)(e)},t.getNextId=function(e,t,a){return(0,n.cardCache.getNextId)(t,a)},t.deleteCacheDataForList=function(e,t,a){a instanceof Array?a.forEach(function(a){e.table.deleteCacheId(t,a)}):e.table.deleteCacheId(t,a)},t.updateCacheDataForList=function(e,t,a,n,r){var o=n.sucessVOs;if(null!=o&&0!=o.length){var i=[];if(null==r){var s={};e.table.getCheckedRows(t).forEach(function(e){var t=e.data.values[a].value;s[t]=e.index}),o[t].rows.forEach(function(e,t){var n=e.values[a].value,r={index:s[n],data:{values:e.values}};i.push(r)})}else{var u={index:r,data:{values:o[t].rows[0].values}};i.push(u)}e.table.updateDataByIndexs(t,i)}},t.rewriteTransferSrcBids=function(e,t,a){if(a){var n=[];a.forEach(function(e){n.push(e.values[t].value)}),e.transferTable.setSavedTransferTableDataPk(n)}},t.clearTransferCache=function(e,t){e.transferTable.deleteCache(t)}},181:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var n=function(){function e(e,t){for(var a=0;a<t.length;a++){var n=t[a];n.enumerable=n.enumerable||!1,n.configurable=!0,"value"in n&&(n.writable=!0),Object.defineProperty(e,n.key,n)}}return function(t,a,n){return a&&e(t.prototype,a),n&&e(t,n),t}}(),r=a(8),o=p(r),i=(p(a(182)),a(0)),s=a(10),u=a(183),l=a(25),c=a(72),d=a(47),f=a(2);function p(e){return e&&e.__esModule?e:{default:e}}var g=i.base.NCBackBtn,h=i.base.NCToggleViewBtn,y=i.base.NCDiv,b=function(e){function t(e){!function(e,t){if(!(e instanceof t))throw new TypeError("Cannot call a class as a function")}(this,t);var a=function(e,t){if(!e)throw new ReferenceError("this hasn't been initialised - super() hasn't been called");return!t||"object"!=typeof t&&"function"!=typeof t?e:t}(this,(t.__proto__||Object.getPrototypeOf(t)).call(this,e));return a.clickReturn=function(){a.props.pushTo("/list",{})},a.changeViewType=function(){a.props.meta.getMeta()[s.REF4310_CONST.singleTableId]||u.initSingleTemplate.call(a,a.props),a.props.transferTable.changeViewType()},a.state={ntotalnum:0,ntotalmny:0},a}return function(e,t){if("function"!=typeof t&&null!==t)throw new TypeError("Super expression must either be null or a function, not "+typeof t);e.prototype=Object.create(t&&t.prototype,{constructor:{value:e,enumerable:!1,writable:!0,configurable:!0}}),t&&(Object.setPrototypeOf?Object.setPrototypeOf(e,t):e.__proto__=t)}(t,r.Component),n(t,[{key:"componentWillMount",value:function(){(0,f.initLang)(this,["4006saledaily"],"ct",u.initTemplate.bind(this,this.props))}},{key:"render",value:function(){var e=this,t=this.props,a=t.transferTable,n=t.button,r=t.search,i=t.BillHeadInfo.createBillHeadInfo,u=r.NCCreateSearch,p=n.createButtonApp,b=a.createTransferTable,v=a.getSelectedListDisplay(s.REF4310_CONST.formId);return o.default.createElement("div",{id:"transferList",className:"nc-bill-list"},v?"":o.default.createElement("div",null,o.default.createElement(y,{areaCode:y.config.HEADER,className:"nc-bill-header-area"},o.default.createElement(g,{onClick:this.clickReturn}),o.default.createElement("div",{className:"header-title-search-area"},i({title:(0,f.getLangByResId)(this,"4006SALEDAILY-000058"),initShowBackBtn:!1})),o.default.createElement("div",{className:"header-button-area"},p({area:"list_head",buttonLimit:8,onButtonClick:l.buttonClick.bind(this),popContainer:document.querySelector(".header-button-area")}),o.default.createElement(h,{expand:!1,onClick:this.changeViewType}))),o.default.createElement("div",{className:"nc-bill-search-area"},u(s.REF4310_CONST.searchId,{clickSearchBtn:l.serach_btnClick.bind(this),onAfterEvent:c.search_afterEvent.bind(this),renderCompleteEvent:d.renderCompleteEvent.bind(this,s.REF4310_CONST.searchId,"pk_org",c.search_afterEvent)}))),o.default.createElement("div",{className:"nc-bill-transferTable-area"},b({dataSource:s.REF4310_CONST.Ref4310DataSource,headTableId:s.REF4310_CONST.formId,bodyTableId:s.REF4310_CONST.tableId,fullTableId:s.REF4310_CONST.singleTableId,transferBtnText:(0,f.getLangByResId)(this,"4006SALEDAILY-000055"),containerSelector:"#transferList",showMasterIndex:!0,showChildIndex:!1,onChangeViewClick:this.changeViewType,onTransferBtnClick:function(t){e.props.pushTo(s.REF4310_CONST.destPageUrl,{type:"ref4310"})},totalKey:["nassistnum","norigtaxmny"],totalTitle:[(0,f.getLangByResId)(this,"4006SALEDAILY-000056"),(0,f.getLangByResId)(this,"4006SALEDAILY-000057")]})))}}]),t}();b=(0,i.createPage)({})(b),t.default=b},182:function(e,t){e.exports=a},183:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.initSingleTemplate=t.initTemplate=void 0;var n=o(a(184)),r=o(a(193));function o(e){return e&&e.__esModule?e:{default:e}}t.initTemplate=n.default,t.initSingleTemplate=r.default},184:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.default=function(e){var t=this;e.createUIDom({pagecode:r.REF4310_CONST.transPageId,appcode:r.REF4310_CONST.appcode},function(a){if(a){if(a.template){var s=a.template;o.referEvent.call(t,e,s),function(e,t){var a=this;t[r.REF4310_CONST.formId].items.map(function(t,n){i.transferSkipToSrcBillUtil.call(a,e,t,{billtype:"28",billcodefield:r.REF4310_CONST.vbillcode,pkfield:"pk_salequotation"})})}.call(t,e,s),e.meta.setMeta(s,n.btn_Controller.bind(t,e))}if(a.button){var u=a.button;e.button.setButtons(u)}}})};var n=a(25),r=a(10),o=a(72),i=a(192)},185:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.default=function(){if(!1===this.props.search.getAllSearchData(r.REF4310_CONST.searchId))return;var e=this.props.search.getQueryInfo(r.REF4310_CONST.searchId);e.pageInfo=null,(0,s.setDefData)(r.REF4310_CONST.Ref4310DataSource,r.REF4310_CONST.searchId,e),i.default.call(this,this.props,e)};var n,r=a(10),o=(a(0),a(71)),i=(n=o)&&n.__esModule?n:{default:n},s=a(4)},186:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.default=function(e,t){switch(t){case"Refresh":return o.default.call(this,e)}};var n,r=a(187),o=(n=r)&&n.__esModule?n:{default:n}},187:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.default=function(e){var t=(0,i.getDefData)(s.REF4310_CONST.Ref4310DataSource,s.REF4310_CONST.searchId);o.default.call(this,e,t)};a(0);var n,r=a(71),o=(n=r)&&n.__esModule?n:{default:n},i=a(4),s=a(10)},188:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.default=function(e){null==(0,n.getDefData)(r.REF4310_CONST.Ref4310DataSource,r.REF4310_CONST.searchId)?this.props.button.setButtonDisabled(["Refresh"],!0):this.props.button.setButtonDisabled(["Refresh"],!1)};var n=a(4),r=a(10)},189:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.default=function(e,t){t[n.REF4310_CONST.searchId].items.map(function(t){"pk_org"===t.attrcode&&(t.queryCondition=function(){return{TreeRefActionExt:"nccloud.web.scmpub.ref.AppPermissionOrgRefFilter"}}),i.includes(t.attrcode)&&(t.queryCondition=function(){var t=(0,r.getSearchValByField)(e,n.REF4310_CONST.searchId,"pk_org");return{pk_org:t}}),"pk_dept"!==t.attrcode&&"cemployeeid"!==t.attrcode||(t.queryCondition=function(){var t=(0,r.getSearchValByField)(e,n.REF4310_CONST.searchId,"pk_org");return{pk_org:t,busifuncode:"sa"}}),t.isRunWithChildren=!1,(0,o.setPsndocShowLeavePower)(t),(0,o.setRefShowDisabledData)(t)})};a(0);var n=a(10),r=a(190),o=a(11),i=["pk_customer","pk_customer.pk_custclass","pk_customer.sales.pk_custsaleclass","salequotationdetail.pk_material","salequotationdetail.pk_material.pk_marbasclass"]},190:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.getSearchValByField=function(e,t,a){var n=e.search.getSearchValByField(t,a),r=n&&n.value&&n.value.firstvalue?n.value.firstvalue:"";return-1!=r.indexOf(",")?null:r}},191:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.default=function(e,t){if("pk_org"===e){var a=["pk_dept","cemployeeid","pk_customer","pk_customer.pk_custclass","pk_customer.sales.pk_custsaleclass","salequotationdetail.pk_material","salequotationdetail.pk_material.pk_marbasclass"].concat(s("vdef",20)).concat(s("salequotationdetail.vfree",10)).concat(s("salequotationdetail.vbdef",20));i.default.call(this,this.props,t,r.REF4310_CONST.searchId,a)}};a(0);var n,r=a(10),o=a(48),i=(n=o)&&n.__esModule?n:{default:n};function s(e,t){for(var a=[],n=1;n<=t;n++)a.push(e+n);return a}},192:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.transferSkipToSrcBillUtil=function(e,t,a){if(arguments.length>3&&void 0!==arguments[3]&&arguments[3]){var n=a.billtypefield,r=a.billcodefield,o=void 0===r?"vbillcode":r;if(t.attrcode==o)return t.width=150,t.renderStatus="browse",t.render=function(t,a,r){if(a[n]&&a[n].value){var i=a[n].value;return React.createElement("a",{style:{cursor:"pointer"},onClick:function(t){t.stopPropagation(),e.openTo(null,{billtype:i,sence:4,status:"browse",id:a.headKey})}},a[o].display?a[o].display:a[o].value)}},t}else{var i=a.billtype,s=a.billcodefield,u=void 0===s?"vbillcode":s,l=a.pkfield;if(t.attrcode==u)return t.width=150,t.renderStatus="browse",t.render=function(t,a,n){return React.createElement("a",{style:{cursor:"pointer"},onClick:function(t){i&&a[l]&&(t.stopPropagation(),e.openTo(null,{billtype:i,sence:4,status:"browse",id:a[l].value}))}},a[u].display?a[u].display:a[u].value)},t}}},193:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.default=function(e){e.createUIDom({pagecode:n.REF4310_CONST.singleTableId,appcode:n.REF4310_CONST.appcode},function(t){if(t&&t.template){var a=t.template;e.meta.addMeta(a)}})};a(0),a(25);var n=a(10)},2:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.getLangByResId=t.initLang=void 0;var n=a(0);t.initLang=function(e,t,a,r){e.lang=null,e.inlt=null,(0,n.getMultiLang)({moduleId:t,domainName:a,callback:function(t,a,n){a&&(e.lang=t,e.inlt=n),r&&r()},needInlt:!0})},t.getLangByResId=function(e,t,a){return function(e,t){if(!e)throw(0,n.toast)({color:"danger",content:"请检查代码中this是否能够取到！当前为undifined,位置："+t}),new Error("请检查代码中this是否能够取到！当前为undifined,位置："+t)}(e,t),a?e.inlt?e.inlt.get(t,a)||t:"":e.lang?e.lang[t]||t:""}},201:function(e,t,a){e.exports=a(181)},22:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.default=function(){Date.prototype.Format=function(e){var t={"M+":this.getMonth()+1,"d+":this.getDate(),"h+":this.getHours(),"m+":this.getMinutes(),"s+":this.getSeconds(),"q+":Math.floor((this.getMonth()+3)/3),S:this.getMilliseconds()};for(var a in/(y+)/.test(e)&&(e=e.replace(RegExp.$1,(this.getFullYear()+"").substr(4-RegExp.$1.length))),t)new RegExp("("+a+")").test(e)&&(e=e.replace(RegExp.$1,1==RegExp.$1.length?t[a]:("00"+t[a]).substr((""+t[a]).length)));return e}}},23:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.setPsndocShowLeavePower=function(e){var t=!(arguments.length>1&&void 0!==arguments[1])||arguments[1];e.isShowDimission=t},t.setRefShowDisabledData=function(e){var t=!(arguments.length>1&&void 0!==arguments[1])||arguments[1];e.isShowDisabledData=t,e.unitPropsExtend={isShowDisabledData:t,isHasDisabledData:t}}},25:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.btn_Controller=t.buttonClick=t.serach_btnClick=void 0;var n=i(a(185)),r=i(a(186)),o=i(a(188));function i(e){return e&&e.__esModule?e:{default:e}}t.serach_btnClick=n.default,t.buttonClick=r.default,t.btn_Controller=o.default},3:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.showQuitTransferWarningDialog=t.showBackWarningDialog=t.showBatchOprReturnMessage=t.showQueryResultInfoForNoPage=t.showSaveInfo=t.showRefreshInfo=t.showNoQueryResultInfo=t.showHasQueryResultInfo=t.showChangeOrgDialog=t.showDeleteDialog=t.showSingleDeleteDialog=t.showCancelDialog=t.showBatchOperateInfo=t.showBatchOprMessage=t.showWarningDialog=t.showErrorDialog=t.showInfoDialog=t.showSuccessDialog=t.showInfoInfo=t.showErrorInfo=t.showWarningInfo=t.showSuccessInfo=void 0;var n=function(){function e(e,t){for(var a=0;a<t.length;a++){var n=t[a];n.enumerable=n.enumerable||!1,n.configurable=!0,"value"in n&&(n.writable=!0),Object.defineProperty(e,n.key,n)}}return function(t,a,n){return a&&e(t.prototype,a),n&&e(t,n),t}}(),r=a(0);var o=new(function(){function e(){!function(e,t){if(!(e instanceof t))throw new TypeError("Cannot call a class as a function")}(this,e),this.lang=null,this.inlt=null,console.log("LangContainer初始化"),(0,r.getMultiLang)({moduleId:"4001pubmessage",domainName:"scmpub",callback:this.init.bind(this),needInlt:!0})}return n(e,[{key:"init",value:function(e,t,a){t&&(this.lang=e,this.inlt=a)}},{key:"getLangByResId",value:function(e,t){return t?this.inlt.get(e,t)||e:this.lang&&this.lang[e]||e}}]),e}());function i(e,t,a){u(e,t,a)}function s(e,t,a){u(e,t,a,"warning")}function u(e,t,a,n,o,i,s,u,l){(0,r.toast)({duration:a,color:n,title:e,content:t,groupOperation:o,TextArr:i,groupOperationMsg:s,onExpand:u,onClose:l})}function l(e,t){c(e,t,arguments.length>2&&void 0!==arguments[2]?arguments[2]:{},"warning")}function c(e,t){var a=arguments.length>2&&void 0!==arguments[2]?arguments[2]:{},n=arguments[3];(0,r.promptBox)({color:n,title:e,content:t,noFooter:a.noFooter,noCancelBtn:a.noCancelBtn,beSureBtnName:a.beSureBtnName,cancelBtnName:a.cancelBtnName,beSureBtnClick:a.beSureBtnClick,cancelBtnClick:a.cancelBtnClick,closeBtnClick:a.closeBtnClick,closeByClickBackDrop:a.closeByClickBackDrop})}function d(){s(null,o.getLangByResId("4001PUBMESSAGE-000016"))}t.showSuccessInfo=i,t.showWarningInfo=s,t.showErrorInfo=function(e,t){u(e,t,arguments.length>2&&void 0!==arguments[2]?arguments[2]:"infinity","danger")},t.showInfoInfo=function(e,t,a){u(e,t,a,"info")},t.showSuccessDialog=function(e,t){c(e,t,arguments.length>2&&void 0!==arguments[2]?arguments[2]:{})},t.showInfoDialog=function(e,t){c(e,t,arguments.length>2&&void 0!==arguments[2]?arguments[2]:{},"info")},t.showErrorDialog=function(e,t){c(e,t,arguments.length>2&&void 0!==arguments[2]?arguments[2]:{},"danger")},t.showWarningDialog=l,t.showBatchOprMessage=function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:o.getLangByResId("4001PUBMESSAGE-000003"),t=arguments[1],a=arguments.length>2&&void 0!==arguments[2]?arguments[2]:{},n=arguments.length>3&&void 0!==arguments[3]?arguments[3]:"",s=t.failedNum,u=t.sucessNum;if(0==s)i(o.getLangByResId("4001PUBMESSAGE-000018",{0:n}),o.getLangByResId("4001PUBMESSAGE-000022",{0:t.sucessNum}));else if(0==u){e=o.getLangByResId("4001PUBMESSAGE-000019",{0:n});var l=o.getLangByResId("4001PUBMESSAGE-000020",{0:t.failedNum,1:t.failedNum});(0,r.toast)({duration:"infinity",color:"danger",title:e,content:l,groupOperation:!0,TextArr:[o.getLangByResId("4001PUBMESSAGE-000000"),o.getLangByResId("4001PUBMESSAGE-000001"),o.getLangByResId("4001PUBMESSAGE-000002")],groupOperationMsg:t.errorMessages,onExpand:a.onExpand,onClose:a.onClose})}else{e=o.getLangByResId("4001PUBMESSAGE-000019",{0:n});var c=o.getLangByResId("4001PUBMESSAGE-000021",{0:Number(t.sucessNum)+Number(t.failedNum),1:t.sucessNum,2:t.failedNum});(0,r.toast)({duration:"infinity",color:"danger",title:e,content:c,groupOperation:!0,TextArr:[o.getLangByResId("4001PUBMESSAGE-000000"),o.getLangByResId("4001PUBMESSAGE-000001"),o.getLangByResId("4001PUBMESSAGE-000002")],groupOperationMsg:t.errorMessages,onExpand:a.onExpand,onClose:a.onClose})}},t.showBatchOperateInfo=function(e,t,a){var n=arguments.length>3&&void 0!==arguments[3]?arguments[3]:{};u(e,t,"infinity","danger",!0,[o.getLangByResId("4001PUBMESSAGE-000000"),o.getLangByResId("4001PUBMESSAGE-000001"),o.getLangByResId("4001PUBMESSAGE-000002")],a,n.onExpand,n.onClose)},t.showCancelDialog=function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:{};l(o.getLangByResId("4001PUBMESSAGE-000007"),o.getLangByResId("4001PUBMESSAGE-000008"),e)},t.showSingleDeleteDialog=function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:{};l(o.getLangByResId("4001PUBMESSAGE-000009"),o.getLangByResId("4001PUBMESSAGE-000010"),e)},t.showDeleteDialog=function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:{};l(o.getLangByResId("4001PUBMESSAGE-000009"),o.getLangByResId("4001PUBMESSAGE-000011"),e)},t.showChangeOrgDialog=function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:{};l(o.getLangByResId("4001PUBMESSAGE-000012"),o.getLangByResId("4001PUBMESSAGE-000013"),e)},t.showHasQueryResultInfo=function(e){i(null,e?o.getLangByResId("4001PUBMESSAGE-000015",{1:e}):o.getLangByResId("4001PUBMESSAGE-000014"))},t.showNoQueryResultInfo=d,t.showRefreshInfo=function(){i(o.getLangByResId("4001PUBMESSAGE-000017"))},t.showSaveInfo=function(){i(o.getLangByResId("4001PUBMESSAGE-000023"))},t.showQueryResultInfoForNoPage=function(e){e?i(null,o.getLangByResId("4001PUBMESSAGE-000015",{1:e})):d()},t.showBatchOprReturnMessage=function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:o.getLangByResId("4001PUBMESSAGE-000003"),t=arguments[1],a=arguments.length>2&&void 0!==arguments[2]?arguments[2]:{},n=arguments.length>3&&void 0!==arguments[3]?arguments[3]:"",s="",u=t.failedNum,l=t.sucessNum;if(0==u)i(o.getLangByResId("4001PUBMESSAGE-000018",{0:n}),o.getLangByResId("4001PUBMESSAGE-000022",{0:t.sucessNum})),s=o.getLangByResId("4001PUBMESSAGE-000022",{0:t.sucessNum});else if(0==l){e=o.getLangByResId("4001PUBMESSAGE-000019",{0:n});var c=o.getLangByResId("4001PUBMESSAGE-000020",{0:t.failedNum,1:t.failedNum});(0,r.toast)({duration:"infinity",color:"danger",title:e,content:c,groupOperation:!0,TextArr:[o.getLangByResId("4001PUBMESSAGE-000000"),o.getLangByResId("4001PUBMESSAGE-000001"),o.getLangByResId("4001PUBMESSAGE-000002")],groupOperationMsg:t.errorMessages,onExpand:a.onExpand,onClose:a.onClose,customBtn:a.customBtn}),t.errorMessages.forEach(function(e){s+=e})}else{e=o.getLangByResId("4001PUBMESSAGE-000019",{0:n});var d=o.getLangByResId("4001PUBMESSAGE-000021",{0:Number(t.sucessNum)+Number(t.failedNum),1:t.sucessNum,2:t.failedNum});(0,r.toast)({duration:"infinity",color:"danger",title:e,content:d,groupOperation:!0,TextArr:[o.getLangByResId("4001PUBMESSAGE-000000"),o.getLangByResId("4001PUBMESSAGE-000001"),o.getLangByResId("4001PUBMESSAGE-000002")],groupOperationMsg:t.errorMessages,onExpand:a.onExpand,onClose:a.onClose,customBtn:a.customBtn}),t.errorMessages.forEach(function(e){s+=e})}return s},t.showBackWarningDialog=function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:{};c(o.getLangByResId("4001PUBMESSAGE-000024"),o.getLangByResId("4001PUBMESSAGE-000025"),e,"warning")},t.showQuitTransferWarningDialog=function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:{};c(o.getLangByResId("4001PUBMESSAGE-000026"),o.getLangByResId("4001PUBMESSAGE-000027"),e,"warning")}},31:function(e,t,a){"use strict";function n(e,t,a){return t in e?Object.defineProperty(e,t,{value:a,enumerable:!0,configurable:!0,writable:!0}):e[t]=a,e}function r(){return(this.transtypeData||{}).pk_transtype}function o(){return(this.transtypeData||{}).transtype_name}Object.defineProperty(t,"__esModule",{value:!0}),t.default={init:function(e){e&&e.paramMap&&e.paramMap.transtype&&(this.transtypeData={transtype:e.paramMap.transtype,transtype_name:e.paramMap.transtype_name,pk_transtype:e.paramMap.pk_transtype})},getTranstypeID:r,getTranstypeCode:function(){return(this.transtypeData||{}).transtype},getTranstypeName:o,beforeEdit:function(e,t,a){return e!=t&&e!=a||!this.transtypeData||!this.transtypeData.transtype},setValue:function(e,t,a){var r;this.transtypeData&&this.props.form.setFormItemsValue(e,(n(r={},t,{value:this.transtypeData.pk_transtype,display:this.transtypeData.transtype_name}),n(r,a,{value:this.transtypeData.transtype,display:this.transtypeData.transtype}),r))},beforeSearch:function(e,t,a){if(e&&this.transtypeData&&this.transtypeData.pk_transtype){e.querycondition||(e.querycondition={}),e.querycondition.conditions||(e.querycondition.conditions=[]),e.querycondition.logic||(e.querycondition.logic="and");var n={field:t,datatype:"204",oprtype:"=",value:{firstvalue:this.transtypeData.pk_transtype}};e.querycondition.conditions.push(n)}return e},initQuery:function(e,t,a,n){var r=t[a].items.find(function(e){return e.attrcode==n});r&&this.transtypeData&&(r.disabled=!0,r.isfixedcondition=!0,r.visible=!0)},setQueryDefaultValue:function(e,t,a){this.transtypeData&&e.search.setSearchValByField(t,a,{display:o.call(this),value:r.call(this)})}}},32:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var n="function"==typeof Symbol&&"symbol"==typeof Symbol.iterator?function(e){return typeof e}:function(e){return e&&"function"==typeof Symbol&&e.constructor===Symbol&&e!==Symbol.prototype?"symbol":typeof e},r=Object.assign||function(e){for(var t=1;t<arguments.length;t++){var a=arguments[t];for(var n in a)Object.prototype.hasOwnProperty.call(a,n)&&(e[n]=a[n])}return e},o=a(0);a(33);function i(e,t,a){return t in e?Object.defineProperty(e,t,{value:a,enumerable:!0,configurable:!0,writable:!0}):e[t]=a,e}t.default={resetItem:function(e,t,a,o,s,u){var l=e.meta.getMeta()[o].items.find(function(e){return e.attrcode==s});if("refer"==l.itemtype){var c=l.queryCondition;"function"!=typeof c||l.filterCon||(l.filterCon=c),e.cardTable.setQueryCondition(o,i({},s,function(e){"function"==typeof c?c=r({},c(e)):"object"===(void 0===c?"undefined":n(c))&&(c=r({},l.filterCon(e)));var d="TreeRefActionExt";return"grid"!=e.refType&&"gridTree"!=e.refType||(d="GridRefActionExt"),c.appcode=t,c.pagecode=a,c.areacode=o,c.data=JSON.stringify(i({},o,{areacode:o,rows:[u]})),c.defineField=s,c[d]="nccloud.web.scmpub.pub.marasst.MarAsstDefaultRef",c.UsualGridRefActionExt="nccloud.web.scmpub.pub.marasst.MarAsstDefaultRef",c}))}},afterEdit:function(e,t,a,n,r,i,s,u){var l={appcode:t,pagecode:a,areacode:n,controlField:r,controlValue:(s.values[r]||{}).value,materialvid:(s.values[i]||{}).value};(0,o.ajax)({url:"/nccloud/scmpub/pub/marasstAfterEdit.do",data:l,async:!1,mode:"normal",success:function(t){if(console.log(t,"hj"),t.data&&t.data.data)for(var a in t.data.data){var r=t.data.data[a];if(r&&0!=r.length){var o=(s.values[a]||{}).value;r.includes(o)||e.cardTable.setValByKeyAndIndex(n,u,a,{value:null,display:null,scale:-1})}else e.cardTable.setValByKeyAndIndex(n,u,a,{value:null,display:null,scale:-1})}}})},resetGridItem:function(e,t,a,o,s,u){var l=e.meta.getMeta()[o].items.find(function(e){return e.attrcode==s});if("refer"==l.itemtype){var c=l.queryCondition;l.queryCondition=function(e){c=r({},"function"==typeof c?c(e):"object"===(void 0===c?"undefined":n(c))?c:{});var l="TreeRefActionExt";return"grid"!=e.refType&&"gridTree"!=e.refType||(l="GridRefActionExt"),c.appcode=t,c.pagecode=a,c.areacode=o,c.data=JSON.stringify(i({},o,{areacode:o,rows:[u]})),c.defineField=s,c[l]="nccloud.web.scmpub.pub.marasst.MarAsstDefaultRef",c.UsualGridRefActionExt="nccloud.web.scmpub.pub.marasst.MarAsstDefaultRef",c}}},resetViewItem:function(e,t,a,o,s,u){var l=e.meta.getMeta()[o].items.find(function(e){return e.attrcode==s});if("refer"==l.itemtype){var c=l.queryCondition;"function"!=typeof c||l.filterCon||(l.filterCon=c),e.cardTable.setQueryCondition(o,i({},s,function(e){"function"==typeof c?c=r({},c(e)):"object"===(void 0===c?"undefined":n(c))&&(c=r({},l.filterCon(e)));var d="TreeRefActionExt";return"grid"!=e.refType&&"gridTree"!=e.refType||(d="GridRefActionExt"),c.appcode=t,c.pagecode=a,c.areacode=o,c.data=JSON.stringify(i({},o,{areacode:o,rows:[u]})),c.defineField=s,c[d]="nccloud.web.scmpub.pub.marasst.ViewMarAsstDefaultRef",c.UsualGridRefActionExt="nccloud.web.scmpub.pub.marasst.ViewMarAsstDefaultRef",c}))}}}},33:function(e,t,a){"use strict";t.decode=t.parse=a(34),t.encode=t.stringify=a(35)},34:function(e,t,a){"use strict";function n(e,t){return Object.prototype.hasOwnProperty.call(e,t)}e.exports=function(e,t,a,o){t=t||"&",a=a||"=";var i={};if("string"!=typeof e||0===e.length)return i;var s=/\+/g;e=e.split(t);var u=1e3;o&&"number"==typeof o.maxKeys&&(u=o.maxKeys);var l=e.length;u>0&&l>u&&(l=u);for(var c=0;c<l;++c){var d,f,p,g,h=e[c].replace(s,"%20"),y=h.indexOf(a);y>=0?(d=h.substr(0,y),f=h.substr(y+1)):(d=h,f=""),p=decodeURIComponent(d),g=decodeURIComponent(f),n(i,p)?r(i[p])?i[p].push(g):i[p]=[i[p],g]:i[p]=g}return i};var r=Array.isArray||function(e){return"[object Array]"===Object.prototype.toString.call(e)}},35:function(e,t,a){"use strict";var n=function(e){switch(typeof e){case"string":return e;case"boolean":return e?"true":"false";case"number":return isFinite(e)?e:"";default:return""}};e.exports=function(e,t,a,s){return t=t||"&",a=a||"=",null===e&&(e=void 0),"object"==typeof e?o(i(e),function(i){var s=encodeURIComponent(n(i))+a;return r(e[i])?o(e[i],function(e){return s+encodeURIComponent(n(e))}).join(t):s+encodeURIComponent(n(e[i]))}).join(t):s?encodeURIComponent(n(s))+a+encodeURIComponent(n(e)):""};var r=Array.isArray||function(e){return"[object Array]"===Object.prototype.toString.call(e)};function o(e,t){if(e.map)return e.map(t);for(var a=[],n=0;n<e.length;n++)a.push(t(e[n],n));return a}var i=Object.keys||function(e){var t=[];for(var a in e)Object.prototype.hasOwnProperty.call(e,a)&&t.push(a);return t}},36:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var n="function"==typeof Symbol&&"symbol"==typeof Symbol.iterator?function(e){return typeof e}:function(e){return e&&"function"==typeof Symbol&&e.constructor===Symbol&&e!==Symbol.prototype?"symbol":typeof e},r=Object.assign||function(e){for(var t=1;t<arguments.length;t++){var a=arguments[t];for(var n in a)Object.prototype.hasOwnProperty.call(a,n)&&(e[n]=a[n])}return e},o=a(11);function i(e,t,a){return t in e?Object.defineProperty(e,t,{value:a,enumerable:!0,configurable:!0,writable:!0}):e[t]=a,e}function s(e,t){if(e&&e[t]&&e[t].rows&&e[t].rows[0]){var a=(0,o.deepClone)(e),n=a[t].rows[0].values,r={},i=void 0;for(i in n)n[i]&&n[i].value&&(r[i]=n[i]);return a[t].rows=[{values:r}],JSON.stringify(a)}}t.default={beforeEdit:function(e){var t=e.props,a=e.appcode,o=e.pagecode,u=e.headarea,l=e.bodyarea,c=e.key,d=e.isHead,f=e.record,p=e.billtype,g=e.pk_org_field,h=e.transtypeid_field,y=t.meta.getMeta(),b=void 0;if(d){var v=t.cardTable.getCheckedRows(l);!v&&v.length>0&&(f=v[0]),b=y[u].items.find(function(e){return e.attrcode==c})}else b=y[l].items.find(function(e){return e.attrcode==c});var m={};m.pk_org=(t.form.getFormItemsValue(u,g)||{}).value,m.transtype=(t.form.getFormItemsValue(u,h)||{}).value,m.key=c,m.billtype=p,m.currarea=d?u:l,m.appcode=a,m.pagecode=o,m.headarea=u,m.bodyarea=l,m.headdata=s(i({},u,t.form.getAllFormValue(u)),u),m.bodydata=s(i({},l,{rows:[f]}),l),b._queryCondition=b._queryCondition||b.queryCondition;var S=d?b._queryCondition:b.queryCondition;S&&S.crossRuleParams||(d?(b&&"refer"==b.itemtype&&(b.queryCondition=function(e){if(S=r({},"function"==typeof S?S(e):"object"===(void 0===S?"undefined":n(S))?S:{}),!e)return S;var t="TreeRefActionExt";return"grid"!=e.refType&&"gridTree"!=e.refType||(t="GridRefActionExt"),(!S[t]||S[t].indexOf("nccloud.web.scmpub.pub.crossrule.CrossRuleSqlBuilder")<0)&&(S[t]=S[t]?S[t]+",nccloud.web.scmpub.pub.crossrule.CrossRuleSqlBuilder":"nccloud.web.scmpub.pub.crossrule.CrossRuleSqlBuilder"),(!S.UsualGridRefActionExt||S.UsualGridRefActionExt.indexOf("nccloud.web.scmpub.pub.crossrule.CrossRuleSqlBuilder")<0)&&(S.UsualGridRefActionExt=S.UsualGridRefActionExt?S.UsualGridRefActionExt+",nccloud.web.scmpub.pub.crossrule.CrossRuleSqlBuilder":"nccloud.web.scmpub.pub.crossrule.CrossRuleSqlBuilder"),S.crossRuleParams=JSON.stringify(m),S}),t.meta.setMeta(y)):t.cardTable.setQueryCondition(l,i({},c,function(e){if(S=r({},"function"==typeof S?S(e):"object"===(void 0===S?"undefined":n(S))?S:{}),!e)return S;var t="TreeRefActionExt";return"grid"!=e.refType&&"gridTree"!=e.refType||(t="GridRefActionExt"),(!S[t]||S[t].indexOf("nccloud.web.scmpub.pub.crossrule.CrossRuleSqlBuilder")<0)&&(S[t]=S[t]?S[t]+",nccloud.web.scmpub.pub.crossrule.CrossRuleSqlBuilder":"nccloud.web.scmpub.pub.crossrule.CrossRuleSqlBuilder"),(!S.UsualGridRefActionExt||S.UsualGridRefActionExt.indexOf("nccloud.web.scmpub.pub.crossrule.CrossRuleSqlBuilder")<0)&&(S.UsualGridRefActionExt=S.UsualGridRefActionExt?S.UsualGridRefActionExt+",nccloud.web.scmpub.pub.crossrule.CrossRuleSqlBuilder":"nccloud.web.scmpub.pub.crossrule.CrossRuleSqlBuilder"),S.crossRuleParams=JSON.stringify(m),S})))}}},37:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.getUrlParam=function(e){var t=decodeURIComponent(window.parent.location.href).split("?");if(t&&t[1]){var a=t[1].split("&");if(a&&a instanceof Array){var n={};return a.forEach(function(e){var t=e.split("=")[0],a=e.split("=")[1];n[t]=a}),n[e]}}}},38:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var n=a(0);function r(e,t,a){return t in e?Object.defineProperty(e,t,{value:a,enumerable:!0,configurable:!0,writable:!0}):e[t]=a,e}t.default={onBlur:function(e,t,a,o,i,s){var u=this;if(s){var l={onhandDimAppcode:e.appcode,onhandDimPagecode:e.pagecode,onhandDimVOGrid:{head:{areaType:"table",rows:[e.headRows.rows[0]]},pageid:e.pagecode},appcode:t.appcode,pagecode:t.pagecode,bodyarea:t.bodyarea,currGrid:r({},t.bodyarea,{rows:[t.record]}),batchcode:s,canInsert:o};(0,n.ajax)({url:a,data:l,mode:"normal",success:function(e){i&&i.call(u,t.bodyarea,t.index,e.data)}})}}}},4:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.clearTransferCache=t.rewriteTransferSrcBids=t.updateCacheDataForList=t.deleteCacheDataForList=t.getNextId=t.getCurrentLastId=t.getDefData=t.setDefData=t.hasListCache=t.addCacheData=t.getCacheDataByPk=t.deleteCacheData=t.updateCacheData=t.changeUrlParam=void 0;var n=a(15);t.changeUrlParam=n.changeUrlParam,t.updateCacheData=n.updateCacheData,t.deleteCacheData=n.deleteCacheData,t.getCacheDataByPk=n.getCacheDataByPk,t.addCacheData=n.addCacheData,t.hasListCache=n.hasListCache,t.setDefData=n.setDefData,t.getDefData=n.getDefData,t.getCurrentLastId=n.getCurrentLastId,t.getNextId=n.getNextId,t.deleteCacheDataForList=n.deleteCacheDataForList,t.updateCacheDataForList=n.updateCacheDataForList,t.rewriteTransferSrcBids=n.rewriteTransferSrcBids,t.clearTransferCache=n.clearTransferCache},42:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0});t.REF4310_CONST={Ref4310DataSource:"ref4310DataSource",formId:"head",tableId:"quotation_b",transPageId:"400600100_4310toZ3",singleTableId:"quotation_view",appcode:"400600100",searchId:"400600100_search",destPageUrl:"/card",ref4310Card:"/so/so/salequotation/main/index.html#/card",serachUrl:"/nccloud/ct/saledaily/queryforz3return.do",pk_head:"pk_salequotation",pk_body:"pk_salequotation_b",vbillcode:"vbillcode"}},47:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.renderCompleteEvent=void 0;var n=a(11);t.renderCompleteEvent=function(e,t,a,r){n.transtypeUtils.setQueryDefaultValue.call(this,this.props,e,r);var o=this.props.search.getSearchValByField(e,t),i=null;o&&o.value&&o.value.firstvalue&&(i=o.value.firstvalue.split(",").map(function(e){return{refpk:e}})),a&&a.call(this,t,i)}},48:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var n="function"==typeof Symbol&&"symbol"==typeof Symbol.iterator?function(e){return typeof e}:function(e){return e&&"function"==typeof Symbol&&e.constructor===Symbol&&e!==Symbol.prototype?"symbol":typeof e};function r(e,t,a){"object"==(void 0===t?"undefined":n(t))&&t.constructor==Array&&e.map(function(e){t.includes(e.attrcode)&&(e.isShowUnit=a)}),"string"==typeof t&&t.constructor==String&&e.map(function(e){t==e.attrcode&&(e.isShowUnit=a)})}t.default=function(e,t,a,r){var o=e.meta.getMeta(),i=o[a].items,s=0;if("object"==(void 0===r?"undefined":n(r))&&r.constructor==Array)if(t&&(1==t.length||t.refpk))for(var u=0;u<i.length;u++)for(var l=0,c=r.length;l<c;l++){var d=i[u];if(d.attrcode==r[l]){if(d.isShowUnit&&0==d.isShowUnit)continue;if(s++,d.isShowUnit=!1,s==c)break}}else for(var f=0;f<i.length;f++)for(var p=0,g=r.length;p<g;p++){var h=i[f];if(h.attrcode==r[p]){if(h.isShowUnit)continue;if(s++,h.isShowUnit=!0,s==g)break}}else if("string"==typeof r&&r.constructor==String)if(t&&(1==t.length||t.refpk))for(var y=0;y<i.length;y++){var b=i[y];b.attrcode==r&&(b.isShowUnit=!1)}else for(var v=0;v<i.length;v++){var m=i[v];m.attrcode==r&&(m.isShowUnit=!0)}e.meta.setMeta(o)},t.multiCorpInit=function(e,t){if(!e)return;!function(e,t){e.map(function(e){e.isShowUnit=!1}),function(e,t){r(e,t,!0)}(e,t)}(e,t)}},71:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.default=function(e,t){var a=this;(0,r.ajax)({url:n.REF4310_CONST.serachUrl,data:t,success:function(t){var r=t.success,s=t.data;r&&(t.formulamsg&&t.formulamsg instanceof Array&&t.formulamsg.length>0&&e.dealFormulamsg(t.formulamsg),a.props.transferTable.setTransferTableValue(n.REF4310_CONST.formId,n.REF4310_CONST.tableId,s,n.REF4310_CONST.pk_head,n.REF4310_CONST.pk_body),o.btn_Controller.call(a,e),(0,i.showQueryResultInfoForNoPage)(s?s.length:null),a.setState({ntotalnum:0,ntotalmny:0}))}})};var n=a(10),r=a(0),o=a(25),i=a(3)},72:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.search_afterEvent=t.referEvent=void 0;var n=o(a(189)),r=o(a(191));function o(e){return e&&e.__esModule?e:{default:e}}t.referEvent=n.default,t.search_afterEvent=r.default},8:function(e,a){e.exports=t}})});
//# sourceMappingURL=index.5d933346.js.map