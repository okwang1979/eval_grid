!function(e,a){"object"==typeof exports&&"object"==typeof module?module.exports=a(require("nc-lightapp-front")):"function"==typeof define&&define.amd?define(["nc-lightapp-front"],a):"object"==typeof exports?exports["ct/ct/price/common/index"]=a(require("nc-lightapp-front")):e["ct/ct/price/common/index"]=a(e["nc-lightapp-front"])}(window,function(e){return function(e){var a={};function t(r){if(a[r])return a[r].exports;var n=a[r]={i:r,l:!1,exports:{}};return e[r].call(n.exports,n,n.exports,t),n.l=!0,n.exports}return t.m=e,t.c=a,t.d=function(e,a,r){t.o(e,a)||Object.defineProperty(e,a,{enumerable:!0,get:r})},t.r=function(e){"undefined"!=typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(e,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(e,"__esModule",{value:!0})},t.t=function(e,a){if(1&a&&(e=t(e)),8&a)return e;if(4&a&&"object"==typeof e&&e&&e.__esModule)return e;var r=Object.create(null);if(t.r(r),Object.defineProperty(r,"default",{enumerable:!0,value:e}),2&a&&"string"!=typeof e)for(var n in e)t.d(r,n,function(a){return e[a]}.bind(null,n));return r},t.n=function(e){var a=e&&e.__esModule?function(){return e.default}:function(){return e};return t.d(a,"a",a),a},t.o=function(e,a){return Object.prototype.hasOwnProperty.call(e,a)},t.p="../../../../",t(t.s=57)}({0:function(a,t){a.exports=e},168:function(e,a,t){"use strict";Object.defineProperty(a,"__esModule",{value:!0}),a.fillCurrentBill=function(e,a,t){var d=this;t||(t=l(r.PriceCache.PriceCacheKey),(0,o.changeUrlParam)(e,{id:t}));if(!t)return void("function"==typeof a&&a&&a());var c={pks:[t],pageId:r.PAGECODE.card};(0,n.ajax)({url:r.ACTION_URL.cardSetUp,data:c,success:function(t){if(t.success){if(t.data){var n=t.data;if(n.head&&e.form.setAllFormValue(function(e,a,t){a in e?Object.defineProperty(e,a,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[a]=t;return e}({},r.AREA.card_head,n.head[r.AREA.card_head])),n.body&&n.body[r.AREA.card_body]){var o=n.body[r.AREA.card_body];e.cardTable.setTableData(r.AREA.card_body,o,null,!0,!0)}var l=n.head[r.AREA.card_head].rows[0].values.pk_ct_price.value,c=n.head[r.AREA.card_head].rows[0].values.pk_pricetemplet.value;i.handleDynamicColumn.call(d,e,l,c,r.AREA.card_body)}else e.button.setButtonVisible(r.BUTTON_CARD_EDIT,!1),e.button.setButtonVisible(r.BUTTON_CARD.Add,!0),e.button.setButtonVisible(r.BUTTON_CARD.Delete,!1);"function"==typeof a&&a&&a()}}})};var r=t(8),n=t(0),i=t(59),o=t(7);var l=n.cardCache.getCurrentLastId},2:function(e,a,t){"use strict";Object.defineProperty(a,"__esModule",{value:!0}),a.getLangByResId=a.initLang=void 0;var r=t(0);a.initLang=function(e,a,t,n){e.lang=null,e.inlt=null,(0,r.getMultiLang)({moduleId:a,domainName:t,callback:function(a,t,r){t&&(e.lang=a,e.inlt=r),n&&n()},needInlt:!0})},a.getLangByResId=function(e,a,t){return function(e,a){if(!e)throw(0,r.toast)({color:"danger",content:"请检查代码中this是否能够取到！当前为undifined,位置："+a}),new Error("请检查代码中this是否能够取到！当前为undifined,位置："+a)}(e,a),t?e.inlt?e.inlt.get(a,t)||a:"":e.lang?e.lang[a]||a:""}},26:function(e,a,t){"use strict";Object.defineProperty(a,"__esModule",{value:!0}),a.clearTransferCache=a.rewriteTransferSrcBids=a.updateCacheDataForList=a.deleteCacheDataForList=a.getNextId=a.getCurrentLastId=a.getDefData=a.setDefData=a.hasListCache=a.addCacheData=a.getCacheDataByPk=a.deleteCacheData=a.updateCacheData=a.changeUrlParam=void 0;var r=t(0);a.changeUrlParam=function(e,a){e.setUrlParam(a)},a.updateCacheData=function(e,a,t,n,i,o){r.cardCache.addCache;var l=r.cardCache.updateCache;r.cardCache.getCacheById,l(a,t,n,i,o)},a.deleteCacheData=function(e,a,t,n){var i=r.cardCache.deleteCacheById;r.cardCache.getNextId,i(a,t,n)},a.getCacheDataByPk=function(e,a,t){return(0,r.cardCache.getCacheById)(t,a)},a.addCacheData=function(e,a,t,n,i,o){(0,r.cardCache.addCache)(t,n,i,o)},a.hasListCache=function(e,a){return e.table.hasCacheData(a)},a.setDefData=function(e,a,t){(0,r.cardCache.setDefData)(a,e,t)},a.getDefData=function(e,a){return(0,r.cardCache.getDefData)(a,e)},a.getCurrentLastId=function(e){return(0,r.cardCache.getCurrentLastId)(e)},a.getNextId=function(e,a,t){return(0,r.cardCache.getNextId)(a,t)},a.deleteCacheDataForList=function(e,a,t){t instanceof Array?t.forEach(function(t){e.table.deleteCacheId(a,t)}):e.table.deleteCacheId(a,t)},a.updateCacheDataForList=function(e,a,t,r,n){var i=r.sucessVOs;if(null!=i&&0!=i.length){var o=[];if(null==n){var l={};e.table.getCheckedRows(a).forEach(function(e){var a=e.data.values[t].value;l[a]=e.index}),i[a].rows.forEach(function(e,a){var r=e.values[t].value,n={index:l[r],data:{values:e.values}};o.push(n)})}else{var d={index:n,data:{values:i[a].rows[0].values}};o.push(d)}e.table.updateDataByIndexs(a,o)}},a.rewriteTransferSrcBids=function(e,a,t){if(t){var r=[];t.forEach(function(e){r.push(e.values[a].value)}),e.transferTable.setSavedTransferTableDataPk(r)}},a.clearTransferCache=function(e,a){e.transferTable.deleteCache(a)}},3:function(e,a,t){"use strict";Object.defineProperty(a,"__esModule",{value:!0}),a.showQuitTransferWarningDialog=a.showBackWarningDialog=a.showBatchOprReturnMessage=a.showQueryResultInfoForNoPage=a.showSaveInfo=a.showRefreshInfo=a.showNoQueryResultInfo=a.showHasQueryResultInfo=a.showChangeOrgDialog=a.showDeleteDialog=a.showSingleDeleteDialog=a.showCancelDialog=a.showBatchOperateInfo=a.showBatchOprMessage=a.showWarningDialog=a.showErrorDialog=a.showInfoDialog=a.showSuccessDialog=a.showInfoInfo=a.showErrorInfo=a.showWarningInfo=a.showSuccessInfo=void 0;var r=function(){function e(e,a){for(var t=0;t<a.length;t++){var r=a[t];r.enumerable=r.enumerable||!1,r.configurable=!0,"value"in r&&(r.writable=!0),Object.defineProperty(e,r.key,r)}}return function(a,t,r){return t&&e(a.prototype,t),r&&e(a,r),a}}(),n=t(0);var i=new(function(){function e(){!function(e,a){if(!(e instanceof a))throw new TypeError("Cannot call a class as a function")}(this,e),this.lang=null,this.inlt=null,console.log("LangContainer初始化"),(0,n.getMultiLang)({moduleId:"4001pubmessage",domainName:"scmpub",callback:this.init.bind(this),needInlt:!0})}return r(e,[{key:"init",value:function(e,a,t){a&&(this.lang=e,this.inlt=t)}},{key:"getLangByResId",value:function(e,a){return a?this.inlt.get(e,a)||e:this.lang&&this.lang[e]||e}}]),e}());function o(e,a,t){d(e,a,t)}function l(e,a,t){d(e,a,t,"warning")}function d(e,a,t,r,i,o,l,d,c){(0,n.toast)({duration:t,color:r,title:e,content:a,groupOperation:i,TextArr:o,groupOperationMsg:l,onExpand:d,onClose:c})}function c(e,a){s(e,a,arguments.length>2&&void 0!==arguments[2]?arguments[2]:{},"warning")}function s(e,a){var t=arguments.length>2&&void 0!==arguments[2]?arguments[2]:{},r=arguments[3];(0,n.promptBox)({color:r,title:e,content:a,noFooter:t.noFooter,noCancelBtn:t.noCancelBtn,beSureBtnName:t.beSureBtnName,cancelBtnName:t.cancelBtnName,beSureBtnClick:t.beSureBtnClick,cancelBtnClick:t.cancelBtnClick,closeBtnClick:t.closeBtnClick,closeByClickBackDrop:t.closeByClickBackDrop})}function u(){l(null,i.getLangByResId("4001PUBMESSAGE-000016"))}a.showSuccessInfo=o,a.showWarningInfo=l,a.showErrorInfo=function(e,a){d(e,a,arguments.length>2&&void 0!==arguments[2]?arguments[2]:"infinity","danger")},a.showInfoInfo=function(e,a,t){d(e,a,t,"info")},a.showSuccessDialog=function(e,a){s(e,a,arguments.length>2&&void 0!==arguments[2]?arguments[2]:{})},a.showInfoDialog=function(e,a){s(e,a,arguments.length>2&&void 0!==arguments[2]?arguments[2]:{},"info")},a.showErrorDialog=function(e,a){s(e,a,arguments.length>2&&void 0!==arguments[2]?arguments[2]:{},"danger")},a.showWarningDialog=c,a.showBatchOprMessage=function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:i.getLangByResId("4001PUBMESSAGE-000003"),a=arguments[1],t=arguments.length>2&&void 0!==arguments[2]?arguments[2]:{},r=arguments.length>3&&void 0!==arguments[3]?arguments[3]:"",l=a.failedNum,d=a.sucessNum;if(0==l)o(i.getLangByResId("4001PUBMESSAGE-000018",{0:r}),i.getLangByResId("4001PUBMESSAGE-000022",{0:a.sucessNum}));else if(0==d){e=i.getLangByResId("4001PUBMESSAGE-000019",{0:r});var c=i.getLangByResId("4001PUBMESSAGE-000020",{0:a.failedNum,1:a.failedNum});(0,n.toast)({duration:"infinity",color:"danger",title:e,content:c,groupOperation:!0,TextArr:[i.getLangByResId("4001PUBMESSAGE-000000"),i.getLangByResId("4001PUBMESSAGE-000001"),i.getLangByResId("4001PUBMESSAGE-000002")],groupOperationMsg:a.errorMessages,onExpand:t.onExpand,onClose:t.onClose})}else{e=i.getLangByResId("4001PUBMESSAGE-000019",{0:r});var s=i.getLangByResId("4001PUBMESSAGE-000021",{0:Number(a.sucessNum)+Number(a.failedNum),1:a.sucessNum,2:a.failedNum});(0,n.toast)({duration:"infinity",color:"danger",title:e,content:s,groupOperation:!0,TextArr:[i.getLangByResId("4001PUBMESSAGE-000000"),i.getLangByResId("4001PUBMESSAGE-000001"),i.getLangByResId("4001PUBMESSAGE-000002")],groupOperationMsg:a.errorMessages,onExpand:t.onExpand,onClose:t.onClose})}},a.showBatchOperateInfo=function(e,a,t){var r=arguments.length>3&&void 0!==arguments[3]?arguments[3]:{};d(e,a,"infinity","danger",!0,[i.getLangByResId("4001PUBMESSAGE-000000"),i.getLangByResId("4001PUBMESSAGE-000001"),i.getLangByResId("4001PUBMESSAGE-000002")],t,r.onExpand,r.onClose)},a.showCancelDialog=function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:{};c(i.getLangByResId("4001PUBMESSAGE-000007"),i.getLangByResId("4001PUBMESSAGE-000008"),e)},a.showSingleDeleteDialog=function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:{};c(i.getLangByResId("4001PUBMESSAGE-000009"),i.getLangByResId("4001PUBMESSAGE-000010"),e)},a.showDeleteDialog=function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:{};c(i.getLangByResId("4001PUBMESSAGE-000009"),i.getLangByResId("4001PUBMESSAGE-000011"),e)},a.showChangeOrgDialog=function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:{};c(i.getLangByResId("4001PUBMESSAGE-000012"),i.getLangByResId("4001PUBMESSAGE-000013"),e)},a.showHasQueryResultInfo=function(e){o(null,e?i.getLangByResId("4001PUBMESSAGE-000015",{1:e}):i.getLangByResId("4001PUBMESSAGE-000014"))},a.showNoQueryResultInfo=u,a.showRefreshInfo=function(){o(i.getLangByResId("4001PUBMESSAGE-000017"))},a.showSaveInfo=function(){o(i.getLangByResId("4001PUBMESSAGE-000023"))},a.showQueryResultInfoForNoPage=function(e){e?o(null,i.getLangByResId("4001PUBMESSAGE-000015",{1:e})):u()},a.showBatchOprReturnMessage=function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:i.getLangByResId("4001PUBMESSAGE-000003"),a=arguments[1],t=arguments.length>2&&void 0!==arguments[2]?arguments[2]:{},r=arguments.length>3&&void 0!==arguments[3]?arguments[3]:"",l="",d=a.failedNum,c=a.sucessNum;if(0==d)o(i.getLangByResId("4001PUBMESSAGE-000018",{0:r}),i.getLangByResId("4001PUBMESSAGE-000022",{0:a.sucessNum})),l=i.getLangByResId("4001PUBMESSAGE-000022",{0:a.sucessNum});else if(0==c){e=i.getLangByResId("4001PUBMESSAGE-000019",{0:r});var s=i.getLangByResId("4001PUBMESSAGE-000020",{0:a.failedNum,1:a.failedNum});(0,n.toast)({duration:"infinity",color:"danger",title:e,content:s,groupOperation:!0,TextArr:[i.getLangByResId("4001PUBMESSAGE-000000"),i.getLangByResId("4001PUBMESSAGE-000001"),i.getLangByResId("4001PUBMESSAGE-000002")],groupOperationMsg:a.errorMessages,onExpand:t.onExpand,onClose:t.onClose,customBtn:t.customBtn}),a.errorMessages.forEach(function(e){l+=e})}else{e=i.getLangByResId("4001PUBMESSAGE-000019",{0:r});var u=i.getLangByResId("4001PUBMESSAGE-000021",{0:Number(a.sucessNum)+Number(a.failedNum),1:a.sucessNum,2:a.failedNum});(0,n.toast)({duration:"infinity",color:"danger",title:e,content:u,groupOperation:!0,TextArr:[i.getLangByResId("4001PUBMESSAGE-000000"),i.getLangByResId("4001PUBMESSAGE-000001"),i.getLangByResId("4001PUBMESSAGE-000002")],groupOperationMsg:a.errorMessages,onExpand:t.onExpand,onClose:t.onClose,customBtn:t.customBtn}),a.errorMessages.forEach(function(e){l+=e})}return l},a.showBackWarningDialog=function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:{};s(i.getLangByResId("4001PUBMESSAGE-000024"),i.getLangByResId("4001PUBMESSAGE-000025"),e,"warning")},a.showQuitTransferWarningDialog=function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:{};s(i.getLangByResId("4001PUBMESSAGE-000026"),i.getLangByResId("4001PUBMESSAGE-000027"),e,"warning")}},43:function(e,a,t){"use strict";Object.defineProperty(a,"__esModule",{value:!0}),a.processExtBillCardHeadEditResult=a.createExtBillHeadAfterEventData=a.processExtBillCardBodyEditResult=a.processGridEditResult=a.processBillCardBodyEditResultNotAddRow=a.processBillCardBodyEditResult=a.processBillCardHeadEditResult=a.createExtBodyAfterEventData=a.createHeadAfterEventData=a.createBodyAfterEventData=a.createGridAfterEventData=void 0;var r=t(48);function n(e,a,t){return a in e?Object.defineProperty(e,a,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[a]=t,e}a.createGridAfterEventData=function(e,a,t,i,o,l,d,c){var s=e.meta.getMeta(),u=e.editTable.getAllRows(t,!1),f=n({templetid:s.pageid,pageid:a},t,{areaType:"table",areacode:t,rows:[u[d]]});return f[t]=(0,r.simplifyData)(f[t]),{attrcode:o,changedrows:l,grid:f,index:0,userobject:c}},a.createBodyAfterEventData=function(e,a,t,n,i,o,l,d,c){var s=e.createBodyAfterEventData(a,t,n,i,o,l);s.index=0;var u=s.card.body[n].rows,f=[u[d]];s.card.body[n].rows=f,s.card.head[t]=(0,r.simplifyData)(s.card.head[t]),s.card.body[n]=(0,r.simplifyData)(s.card.body[n]),c=c||{};var g=[];return u.map(function(e){"3"!=e.status&&g.push(e.values.crowno?e.values.crowno.value:null)}),c.scm_originindex=d+"",c.scm_allrownos=g,s.userobject=c,s},a.createHeadAfterEventData=function(e,a,t,n,i,o,l,d,c){var s=!(arguments.length>9&&void 0!==arguments[9])||arguments[9],u=e.createHeadAfterEventData(a,t,n,i,o,l);return u.card.head[t]=(0,r.simplifyData)(u.card.head[t]),u.card.body[n]=c?(0,r.simplifyDataByFields)(u.card.body[n],!0,c,s):(0,r.simplifyData)(u.card.body[n]),d=d||{},u.userobject=d,u},a.createExtBodyAfterEventData=function(e,a,t,n,i,o,l,d,c){var s=e.createBodyAfterEventData(a,t,n,i,o,l);s.index=0;var u=s.card.bodys[i].rows,f=[u[d]];s.card.bodys[i].rows=f,s.card.head[t]=(0,r.simplifyData)(s.card.head[t]),s.card.bodys[i]=(0,r.simplifyData)(s.card.bodys[i]),c=c||{};var g=[];return u.map(function(e){"3"!=e.status&&g.push(e.values.crowno?e.values.crowno.value:null)}),c.scm_originindex=d+"",c.scm_allrownos=g,s.userObject=c,s},a.processBillCardHeadEditResult=function(e,a,t,r){r&&r.billCard&&(r.billCard.head&&e.form.setAllFormValue(n({},a,r.billCard.head[a])),r.billCard.body&&e.cardTable.updateDiffDataByRowId(t,r.billCard.body[t]))},a.processBillCardBodyEditResult=function(e,a,t,r){if(t&&t.billCard&&t.billCard.body&&t.billCard.body[a]&&t.billCard.body[a].rows.length>0){for(var n=t.billCard.body[a].rows,i=[],o=[],l=0;l<n.length;l++){var d={index:r+l,data:n[l]};0==l?o.push(d):i.push(d)}o.length>0&&e.cardTable.updateDataByIndexs(a,o),i.length>0&&e.cardTable.insertDataByIndexs(a,i,!0)}},a.processBillCardBodyEditResultNotAddRow=function(e,a,t,r){if(t&&t.billCard&&t.billCard.body&&t.billCard.body[a]&&t.billCard.body[a].rows.length>0){for(var n=t.billCard.body[a].rows,i=[],o=[],l=0;l<n.length;l++){var d={index:r+l,data:n[l]};o.push(d)}o.length>0&&e.cardTable.updateDataByIndexs(a,o),i.length>0&&e.cardTable.insertDataByIndexs(a,i,!0)}},a.processGridEditResult=function(e,a,t,r){if(t&&t.grid&&t.grid[a]){for(var n=t.grid[a].rows,i=[],o=[],l=0;l<n.length;l++){var d={index:r+l,data:n[l]};0==l?o.push(d):i.push(d)}o.length>0&&e.editTable.updateDataByIndexs(a,o),i.length>0&&e.editTable.insertDataByIndexs(a,i,!0)}},a.processExtBillCardBodyEditResult=function(e,a,t){if(t.extbillcard&&t.extbillcard.bodys&&t.extbillcard.bodys[a]){var r=[],n=[],i=t.userObject&&t.userObject.scm_originindex?parseInt(t.userObject.scm_originindex):1;t.extbillcard.bodys[a].rows.forEach(function(e,a){e.rowid?r.push(e):n.push({index:a+i,data:e})}),e.cardTable.updateDataByRowId(a,{rows:r},!1,!1),e.cardTable.insertDataByIndexs(a,n,!0)}},a.createExtBillHeadAfterEventData=function(e,a,t,n,i,o,l,d){var c=e.createHeadAfterEventData(a,t,n,i,o,l);return c.card.head[t]=(0,r.simplifyData)(c.card.head[t],!1),n instanceof Array&&n.forEach(function(e){c.card.bodys[e]=(0,r.simplifyData)(c.card.bodys[e],!1)}),d=d||{},c.userobject=d,c},a.processExtBillCardHeadEditResult=function(e,a,t,r){r&&r.extbillcard&&(r.extbillcard.head&&e.form.setAllFormValue(n({},a,r.extbillcard.head[a])),r.extbillcard.bodys&&(t instanceof Array?t.forEach(function(a){r.extbillcard.bodys[a]&&e.cardTable.updateDiffDataByRowId(a,r.extbillcard.bodys[a])}):e.cardTable.updateDiffDataByRowId(t,r.extbillcard.bodys[t])))}},44:function(e,a,t){"use strict";Object.defineProperty(a,"__esModule",{value:!0}),a.RownoUtils=a.rowCopyPasteUtils=void 0;var r=t(60),n=t(46);a.rowCopyPasteUtils=r.rowCopyPasteUtils,a.RownoUtils=n.RownoUtils},45:function(e,a,t){"use strict";Object.defineProperty(a,"__esModule",{value:!0}),a.togglePageStatus=void 0;var r=t(47),n=t(8);a.togglePageStatus=function(e,a,t){this.UIStatus=a,r.buttonController.call(this,e,a);var i=e.cardTable,o=e.form.setFormStatus,l=i.setStatus;o(n.AREA.card_head,a),l(n.AREA.card_body,a),t&&"function"==typeof t&&t.call(this,e)}},46:function(e,a,t){"use strict";Object.defineProperty(a,"__esModule",{value:!0}),a.RownoUtils=void 0;var r=t(0),n=0,i=8,o="crowno";function l(e,a,t,o,l){var d=o-l.length-1,c=(e.cardTable.getValByKeyAndIndex(a,d,t)||{}).value;c=c||n;var u=(e.cardTable.getValByKeyAndIndex(a,o,t)||{}).value,f=l.length,g=[];l.forEach(function(e){g.push(e)});for(var p=i;;){var y={};y[c]=!0,y[u]=!0;for(var h=0;h<f;h++){var v=g[h];if(new Number(c)-new Number(v)>=0||new Number(u)-new Number(v)<=0)break;y[v]=!0}if(Object.getOwnPropertyNames(y).length!==f+2)break;for(var A=0;A<f;A++)l[A]=g[A];if(p<=0)break;--p;for(var b=0;b<f;b++){var B=s((0,r.formatAcuracy)(g[b],p));g[b]="0.00000000"==B?"0.00000001":B}}return l}function d(e,a,t,r){for(var i=e.cardTable.getNumberOfRows(a),o=function(e,a,t,r){if(1===r)return n;for(var i=n,o=null,l=0;l<r;l++)(o=e.cardTable.getValByKeyAndIndex(a,l,t).value)&&Number(i)<Number(o)&&(i=Number(o));return i}(e,a,t,i),l=new Array(r.length),d=0;d<r.length;d++)l[d]=10*(d+1)+Number(o);return l}function c(e,a,t,o,l){var d=function(e,a,t,r){if(1==e.cardTable.getNumberOfRows(a))return n;for(var i=r-1;i>=0;i--){var o=(e.cardTable.getValByKeyAndIndex(a,i,t)||{}).value;if(""!=o&&null!=o)return o}return n}(e,a,t,o),c=e.cardTable.getValByKeyAndIndex(a,o,t).value,u=new Array(l.length);if(d===c)for(var f=0;f<l.length;f++)u[f]=d;else if(null==c||""==c)for(var g=0;g<l.length;g++)u[g]=10*(g+1)+Number(d);else for(var p=(c-d)/(l.length+1),y=d,h=0;h<l.length;h++){y=Number(y)+Number(p);var v=s((0,r.formatAcuracy)(y,i));u[h]="0.00000000"==v?"0.00000001":v}return u}function s(e){return e?e.toString().replace(/\,/gi,""):e}var u={setRowMaterilNo:function(e,a,t,r){null==t&&(t=o);for(var n=e.cardTable.getNumberOfRows(a),i=!0;n>0&&i;){for(var s=[],u=-1,f=0;f<n;f++){f==n-1&&(i=!1);var g=(e.cardTable.getValByKeyAndIndex(a,f,t)||{}).value,p=(e.cardTable.getValByKeyAndIndex(a,f,r)||{}).value;if(g||p){if(0==s.length)continue;u=f;break}s.push(f),u=-1}var y=null;y=-1!=u?l(e,a,t,u,y=c(e,a,t,u,s)):d(e,a,t,s);for(var h=0;h<s.length;h++){var v=s[h];e.cardTable.setValByKeyAndIndex(a,v,t,{value:y[h].toString(),display:y[h].toString()})}}},setRowNo:function(e,a,t){null==t&&(t=o);for(var r=e.cardTable.getNumberOfRows(a),n=!0;r>0&&n;){for(var i=[],s=-1,u=0;u<r;u++){if(u==r-1&&(n=!1),(e.cardTable.getValByKeyAndIndex(a,u,t)||{}).value){if(0==i.length)continue;s=u;break}i.push(u),s=-1}var f=null;f=-1!=s?l(e,a,t,s,f=c(e,a,t,s,i)):d(e,a,t,i);for(var g=0;g<i.length;g++){var p=i[g];e.cardTable.setValByKeyAndIndex(a,p,t,{value:f[g].toString(),display:f[g].toString()})}}},resetRowNo:function(e,a,t){null==t&&(t=o);for(var r=e.cardTable.getNumberOfRows(a),n=0;n<r;n++){var i=10*n+10;e.cardTable.setValByKeyAndIndex(a,n,t,{value:i.toString(),display:i.toString()})}}};a.RownoUtils=u},47:function(e,a,t){"use strict";Object.defineProperty(a,"__esModule",{value:!0}),a.exitCopyStatus=a.intoCopyStatus=a.resetBodyButtons=a.buttonController=void 0;var r=t(8);function n(e){var a=e.button.setButtonVisible,t=this.bvalidateflag;a([r.BUTTON_CARD.Validate],!t),a([r.BUTTON_CARD.UnValidate],t),a(r.BUTTON_CARD.Delete,!t);var n=1==this.iversion;a([r.BUTTON_CARD.History],!n)}function i(e){e.button.setButtonDisabled([r.BUTTON_CARD.AddLine,r.BUTTON_CARD.DeleteLine,r.BUTTON_CARD.CopyLine,r.BUTTON_CARD.ReorderLineNumber],!0),e.form.getFormItemsValue(r.AREA.card_head,r.FIELDS.pk_org).value?e.button.setButtonDisabled([r.BUTTON_CARD.AddLine,r.BUTTON_CARD.ReorderLineNumber],!1):e.button.setButtonDisabled([r.BUTTON_CARD.AddLine,r.BUTTON_CARD.ReorderLineNumber],!0)}a.buttonController=function(e,a){var t=e.button.setButtonVisible;if(a==r.UISTATUS.browse){e.BillHeadInfo.setBillHeadInfoVisible({showBackBtn:!0});var o=e.form.getFormItemsValue(r.AREA.card_head,r.FIELDS.pk_ct_price).value;if(!(o=o||e.getUrlParam(r.FIELDS.pk_ct_price)))return t(r.BUTTON_CARD_BROWSE,!1),t(r.BUTTON_CARD_EDIT,!1),void t(r.BUTTON_CARD.Add,!0);t(r.BUTTON_CARD_EDIT,!1),t(r.BUTTON_CARD_BROWSE,!0),t([r.BUTTON_CARD.Validate,r.BUTTON_CARD.UnValidate,r.BUTTON_CARD.History],!1),e.cardPagination.setCardPaginationVisible("cardPaginationBtn",!0),n.call(this,e)}else a==r.UISTATUS.edit&&(t(r.BUTTON_CARD_BROWSE,!1),t(r.BUTTON_CARD_EDIT,!0),t([r.BUTTON_CARD.Validate,r.BUTTON_CARD.UnValidate,r.BUTTON_CARD.History,r.BUTTON_CARD.PasteThere,r.BUTTON_CARD.PasteToLast,r.BUTTON_CARD.CancelPaste],!1),e.cardPagination.setCardPaginationVisible("cardPaginationBtn",!1),e.BillHeadInfo.setBillHeadInfoVisible({showBackBtn:!1}),i.call(this,e))},a.resetBodyButtons=i,a.intoCopyStatus=function(e){var a=e.button.setButtonVisible;a([r.BUTTON_CARD.AddLine,r.BUTTON_CARD.DeleteLine,r.BUTTON_CARD.CopyLine,r.BUTTON_CARD.ReorderLineNumber,r.BUTTON_CARD.InsertLineInner,r.BUTTON_CARD.DeleteLineInner],!1),a([r.BUTTON_CARD.PasteThere,r.BUTTON_CARD.PasteToLast,r.BUTTON_CARD.CancelPaste],!0)},a.exitCopyStatus=function(e){var a=e.button.setButtonVisible;a([r.BUTTON_CARD.PasteThere,r.BUTTON_CARD.PasteToLast,r.BUTTON_CARD.CancelPaste],!1),a([r.BUTTON_CARD.AddLine,r.BUTTON_CARD.DeleteLine,r.BUTTON_CARD.CopyLine,r.BUTTON_CARD.ReorderLineNumber,r.BUTTON_CARD.InsertLineInner,r.BUTTON_CARD.DeleteLineInner],!0),e.button.setButtonDisabled([r.BUTTON_CARD.CopyLine,r.BUTTON_CARD.DeleteLine],!0)}},48:function(e,a,t){"use strict";Object.defineProperty(a,"__esModule",{value:!0});var r=Object.assign||function(e){for(var a=1;a<arguments.length;a++){var t=arguments[a];for(var r in t)Object.prototype.hasOwnProperty.call(t,r)&&(e[r]=t[r])}return e};function n(e){for(var a=arguments.length,t=Array(a>1?a-1:0),r=1;r<a;r++)t[r-1]=arguments[r];return!(null!=e&&""!==e&&!t.find(function(a){return a==e}))}a.simplifyData=function(e){var a=!(arguments.length>1&&void 0!==arguments[1])||arguments[1];if(e&&Array.isArray(e.rows)&&e.rows.length){var t=r({},e,{rows:[]});return e.rows.forEach(function(e){if(e.values){var i={};for(var o in e.values)e.values[o]&&(n(e.values[o].value)?a||-1==e.values[o].scale||(i[o]={scale:e.values[o].scale}):(i[o]={value:e.values[o].value},a||-1==e.values[o].scale||(i[o].scale=e.values[o].scale)));t.rows.push(r({},e,{values:i}))}}),t}return e},a.simplifyDataByFields=function(e){var a=!(arguments.length>1&&void 0!==arguments[1])||arguments[1],t=arguments[2],i=!(arguments.length>3&&void 0!==arguments[3])||arguments[3];if(e&&Array.isArray(e.rows)&&e.rows.length){var o=r({},e,{rows:[]});return e.rows.forEach(function(e){if(e.values){var l={};for(var d in e.values)t?i?t.includes(d)&&(l[d]={value:e.values[d].value}):t.includes(d)||(l[d]={value:e.values[d].value}):n(e.values[d].value)?a||-1==e.values[d].scale||(l[d]={scale:e.values[d].scale}):(l[d]={value:e.values[d].value},a||-1==e.values[d].scale||(l[d].scale=e.values[d].scale));o.rows.push(r({},e,{values:l}))}}),o}return e}},57:function(e,a,t){"use strict";Object.defineProperty(a,"__esModule",{value:!0}),a.fillCurrentBill=void 0,a.initValidateButtons=function(e){var a,t=e.button.setButtonDisabled,n=(l(a={},r.BUTTON.Validate,!0),l(a,r.BUTTON.UnValidate,!0),a);t(n)},a.refreshcache=function(e,a,t,n,o){a?(0,i.addCacheData)(e,"pk_ct_price",n,t,r.AREA.card_head,o):(0,i.updateCacheData)(e,"pk_ct_price",n,t,r.AREA.card_head,o)},a.setDefaultValues=function(e){var a={pk_org:this.pk_org,pageId:r.PAGECODE.card};(0,n.ajax)({url:r.ACTION_URL.groupQuery,data:a,success:function(a){if(a.success){var t=a.data;e.form.setAllFormValue(l({},r.AREA.card_head,t.head[r.AREA.card_head]));var n=t.body[r.AREA.card_body];e.cardTable.setTableData(r.AREA.card_body,n)}}})};var r=t(8),n=t(0),i=t(7),o=t(168);function l(e,a,t){return a in e?Object.defineProperty(e,a,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[a]=t,e}a.fillCurrentBill=o.fillCurrentBill},59:function(e,a,t){"use strict";Object.defineProperty(a,"__esModule",{value:!0}),a.handleDynamicColumn=function(e,a,t,r){o.call(this,e,a,t,r)},a.doAddDynamicColumn=o,a.resetPriceItems=l;var r=t(8),n=t(0),i=t(89);function o(e,a,t,o){var d=this,c={pk_ct_price:a,pk_pricetemplet:t};(0,n.ajax)({url:r.ACTION_URL.dynamicColumn,data:c,success:function(a){if(a.success&&(i.hiddenPriceItems.call(d,e),a.data)){var r=a.data;l(e,r[t],o)}}})}function l(e,a,t){var r=e.meta.getMeta(),n=function(e){return e.forEach(function(e){e.itemtype="input",e.disabled=!1,e.visible=!0}),e}(a),i=r[t].items;i.findIndex(function(e,a){return"npriceitem1"==e.attrcode});!function(e,a){var t=new Map,r=a.map(function(e){return t.set(e.attrCode,e.label),e.attrCode});e.forEach(function(e){r.includes(e.attrcode)&&(e.disabled=!1,e.visible=!0,e.label=t.get(e.attrcode))})}(i,n),e.meta.setMeta(r)}},60:function(e,a,t){"use strict";Object.defineProperty(a,"__esModule",{value:!0}),a.rowCopyPasteUtils=void 0;var r=t(0),n=!0,i=!1;function o(e,a,t,i,o,c){!function(e,a,t,n,i){var o=(0,r.deepClone)(t);if(o)if(o instanceof Array){for(var d=o.length,c=[],s=0;s<d;s++){var u=o[s].data;l(u,i),c.push(u)}e.cardTable.insertRowsAfterIndex(a,c,n)}else l(o,i),e.cardTable.insertRowsAfterIndex(a,o,n)}(e,a,this.state.copyRowDatas,t-1,c),this.setState({copyRowDatas:null}),d(e,i,o,n),e.cardTable.selectAllRows(a,!1),e.cardTable.setAllCheckboxAble(a,!0)}function l(e,a){a&&a instanceof Array&&(e instanceof Array?e.forEach(function(e){a.forEach(function(a){e.values[a]={value:null,display:null,scale:-1}})}):a.forEach(function(a){e.values[a]={value:null,display:null,scale:-1}}))}function d(e,a,t,r){a&&e.button.setButtonVisible(a,r),t&&e.button.setButtonVisible(t,!r)}var c={copyRow:function(e,a,t,r,n){return this.setState({copyRowDatas:t}),d(e,r,n,i),e.cardTable.setAllCheckboxAble(a,!1),t},copyRows:function(e,a,t,r){var n=e.cardTable.getCheckedRows(a);if(n&&n.length>0)return this.setState({copyRowDatas:n}),d(e,t,r,i),e.cardTable.setAllCheckboxAble(a,!1),n},pasteRowsToIndex:o,pasteRowsToTail:function(e,a,t,r,n){var i=e.cardTable.getNumberOfRows(a);o.call(this,e,a,i,t,r,n)},cancel:function(e,a,t,r){this.setState({copyRowDatas:null}),d(e,t,r,n),e.cardTable.selectAllRows(a,!1),e.cardTable.setAllCheckboxAble(a,!0)}};a.rowCopyPasteUtils=c},7:function(e,a,t){"use strict";Object.defineProperty(a,"__esModule",{value:!0}),a.clearTransferCache=a.rewriteTransferSrcBids=a.updateCacheDataForList=a.deleteCacheDataForList=a.getNextId=a.getCurrentLastId=a.getDefData=a.setDefData=a.hasListCache=a.addCacheData=a.getCacheDataByPk=a.deleteCacheData=a.updateCacheData=a.changeUrlParam=void 0;var r=t(26);a.changeUrlParam=r.changeUrlParam,a.updateCacheData=r.updateCacheData,a.deleteCacheData=r.deleteCacheData,a.getCacheDataByPk=r.getCacheDataByPk,a.addCacheData=r.addCacheData,a.hasListCache=r.hasListCache,a.setDefData=r.setDefData,a.getDefData=r.getDefData,a.getCurrentLastId=r.getCurrentLastId,a.getNextId=r.getNextId,a.deleteCacheDataForList=r.deleteCacheDataForList,a.updateCacheDataForList=r.updateCacheDataForList,a.rewriteTransferSrcBids=r.rewriteTransferSrcBids,a.clearTransferCache=r.clearTransferCache},8:function(e,a,t){"use strict";Object.defineProperty(a,"__esModule",{value:!0});var r={list_group1:"list_group1",Add_list:"Add_list",Delete_list:"Delete_list",Validate:"Validate",UnValidate:"UnValidate",list_print:"list_print",Refresh:"Refresh",Validate_inner:"Validate_inner",UnValidate_inner:"UnValidate_inner",Edit_inner:"Edit_inner",Delete_inner:"Delete_inner",History_inner:"History_inner",Copy_inner:"Copy_inner"},n={Add:"Add",card_group:"card_group",Edit:"Edit",Copy:"Copy",Validate:"Validate",UnValidate:"UnValidate",Print:"Print",Refresh:"Refresh",History:"History",Save:"Save",Cancel:"Cancel",AddLine:"AddLine",DeleteLine:"DeleteLine",CopyLine:"CopyLine",Delete:"Delete",ReorderLineNumber:"ReorderLineNumber",DeleteLineInner:"DeleteLineInner",InsertLineInner:"InsertLineInner",PasteToLast:"PasteToLast",PasteThere:"PasteThere",CancelPaste:"CancelPaste"},i=[n.Save,n.Cancel,n.DeleteLineInner,n.InsertLineInner,n.AddLine,n.DeleteLine,n.CopyLine,n.ReorderLineNumber,n.PasteToLast,n.PasteThere,n.CancelPaste],o=[n.Add,n.card_group,n.Delete,n.Edit,n.Copy,n.Validate,n.UnValidate,n.History,n.Print,n.Refresh],l=[r.list_group1,r.Add_list,r.Delete_list,r.Validate,r.UnValidate,r.list_print,r.Refresh];a.APPCODE=400400602,a.PAGECODE={list:"400400602_list",card:"400400602_card",history:"400400602_history"},a.AREA={searchArea:"searchArea",list_head:"list_head",list_inner:"list_inner",card_head:"card_head",card_body:"card_body",card_body_inner:"card_body_inner",history_head:"history_head",history_body:"history_body"},a.BUTTON=r,a.ACTION_URL={query:"/nccloud/ct/price/query.do",save:"/nccloud/ct/price/save.do",delete:"/nccloud/ct/price/delete.do",groupQuery:"/nccloud/ct/price/groupQuery.do",update:"/nccloud/ct/price/update.do",validate:"/nccloud/ct/price/validate.do",unvalidate:"/nccloud/ct/price/unvalidate.do",pageQuery:"/nccloud/ct/price/pageQuery.do",dynamicColumn:"/nccloud/ct/price/dynamicColumn.do",cardSetUp:"/nccloud/ct/price/cardSetUp.do",queryByPu:"/nccloud/ct/price/queryByPu.do",queryHistory:"/nccloud/ct/price/queryHistory.do",queryHistoryBody:"/nccloud/ct/price/queryHistoryBody.do",print:"/nccloud/ct/price/print.do",merge:"/nccloud/platform/templet/querypage.do",cardHeadAfter:"/nccloud/ct/price/cardHeadAfter.do",cardBodyAfter:"/nccloud/ct/price/cardBodyAfter.do",cardBefore:"/nccloud/ct/price/cardBefore.do",cardHeadBefore:"/nccloud/ct/price/cardHeadBefore.do"},a.UISTATUS={browse:"browse",edit:"edit"},a.BUTTON_BROWSE_LIST=l,a.BUTTON_CARD=n,a.BUTTON_CARD_EDIT=i,a.BUTTON_CARD_BROWSE=o,a.PriceCache={PriceCacheKey:"scm.ct.price.datasource",Searchval:"PriceList_serachVal"},a.FIELDS={pk_group:"pk_group",iversion:"iversion",pk_org:"pk_org",pk_puorg:"pk_puorg",bvalidateflag:"bvalidateflag",blatest:"blatest",cvendorid:"cvendorid",pk_marbasclass:"pk_marbasclass",pk_material:"pk_material","pk_material.materialspec":"pk_material.materialspec",pk_ct_price:"pk_ct_price",pk_ct_price_b:"pk_ct_price_b",ts:"ts",pk_oid:"pk_oid",pk_pricetemplet:"pk_pricetemplet",corigcurrencyid:"corigcurrencyid",orgregion:"orgregion",castunitid:"castunitid",crowno:"crowno",vcode:"vcode",totalprice:"totalprice",baseprice:"baseprice"},a.NPRICEITEMS=["npriceitem1","npriceitem2","npriceitem3","npriceitem4","npriceitem5","npriceitem6","npriceitem7","npriceitem8","npriceitem9","npriceitem10","npriceitem11","npriceitem12","npriceitem13","npriceitem14","npriceitem15","npriceitem16","npriceitem17","npriceitem18","npriceitem19","npriceitem20","npriceitem21","npriceitem22","npriceitem23","npriceitem24","npriceitem25","npriceitem26","npriceitem27","npriceitem28","npriceitem29","npriceitem30"],a.VOSTATUS={DELETE:"3"}},89:function(e,a,t){"use strict";Object.defineProperty(a,"__esModule",{value:!0}),a.cvendorid_after=function(e,a,t,o,l){var d=(0,i.createHeadAfterEventData)(e,n.PAGECODE.card,n.AREA.card_head,n.AREA.card_body,a,t,o,{},[n.FIELDS.pk_org,n.FIELDS.cvendorid]);(0,r.ajax)({url:n.ACTION_URL.cardHeadAfter,data:d,success:function(a){a.success&&(console.log(a.data),(0,i.processBillCardHeadEditResult)(e,n.AREA.card_head,n.AREA.card_body,a.data),f(e))}})},a.material_after=function(e,a,t,o,l){var d=(0,i.createHeadAfterEventData)(e,n.PAGECODE.card,n.AREA.card_head,n.AREA.card_body,a,t,o,{},[n.FIELDS.pk_org,n.FIELDS.pk_marbasclass,n.FIELDS.pk_material]);(0,r.ajax)({url:n.ACTION_URL.cardHeadAfter,data:d,success:function(a){if(a.success){var t=a.data.billCard.head[n.AREA.card_head].rows[0].values.pk_marbasclass;t&&t.value||e.form.setFormItemsValue(n.AREA.card_head,{"pk_marbasclass.name":{value:null,display:null}}),(0,i.processBillCardHeadEditResult)(e,n.AREA.card_head,n.AREA.card_body,a.data)}}})},a.marbasclass_after=function(e,a,t,o,l){var d=(0,i.createHeadAfterEventData)(e,n.PAGECODE.card,n.AREA.card_head,n.AREA.card_body,a,t,o,{},[n.FIELDS.pk_org,n.FIELDS.pk_marbasclass,n.FIELDS.pk_material]);(0,r.ajax)({url:n.ACTION_URL.cardHeadAfter,data:d,success:function(a){var t;e.form.setFormItemsValue(n.AREA.card_head,(u(t={},n.FIELDS.pk_material,{display:null,value:null}),u(t,"pk_material.name",{display:null,value:null}),u(t,"pk_material.materialspec",{display:null,value:null}),u(t,"pk_material.materialtype",{display:null,value:null}),t));var r=e.form.getFormItemsValue(n.AREA.card_head,n.FIELDS.pk_marbasclass);r&&r.value&&(e.form.setFormItemsValue(n.AREA.card_head,u({},n.FIELDS.castunitid,{display:null,value:null})),e.form.setFormItemsDisabled(n.AREA.card_head,u({},n.FIELDS.castunitid,!0))),(0,i.processBillCardHeadEditResult)(e,n.AREA.card_head,n.AREA.card_body,a.data)}})},a.pricetemplet_after=function(e,a,t,r,i){if(i.value&&i.value!=r.value)(0,l.showWarningDialog)((0,s.getLangByResId)(this,"4004PRICE-000000"),(0,s.getLangByResId)(this,"4004PRICE-000001"),{beSureBtnClick:function(){var a=r.value,t=e.form.getFormItemsValue(n.AREA.card_head,n.FIELDS.pk_ct_price).value;g(e,t,a)},cancelBtnClick:function(){e.form.setFormItemsValue(n.AREA.card_head,u({},n.FIELDS.pk_pricetemplet,{display:i.display,value:i.value}))}});else if(i.value!=r.value){var o=r.value,d=e.form.getFormItemsValue(n.AREA.card_head,n.FIELDS.pk_ct_price).value;g(e,d,o)}},a.corigcurrencyid_after=function(e,a,t,o,l){var d=(0,i.createHeadAfterEventData)(e,n.PAGECODE.card,n.AREA.card_head,n.AREA.card_body,a,t,o,{},[n.FIELDS.pk_group,n.FIELDS.corigcurrencyid].concat(n.NPRICEITEMS));(0,r.ajax)({url:n.ACTION_URL.cardHeadAfter,data:d,success:function(a){a.success&&(0,i.processBillCardHeadEditResult)(e,n.AREA.card_head,n.AREA.card_body,a.data)}})},a.pk_org_after=function(e,a,t,r,i){var o=this;i.value?i.value!=r.value&&(0,l.showWarningDialog)((0,s.getLangByResId)(this,"4004PRICE-000000"),(0,s.getLangByResId)(this,"4004PRICE-000002"),{beSureBtnClick:function(){c.add.call(o,e,r.value),d.resetBodyButtons.call(o,e)},cancelBtnClick:function(){e.form.setFormItemsValue(n.AREA.card_head,u({},n.FIELDS.pk_org,{display:i.display,value:i.value}))}}):(c.add.call(this,e,r.value),d.resetBodyButtons.call(this,e))},a.clearPriceItems=f,a.hiddenPriceItems=function(e){e.cardTable.setColVisibleByKey(n.AREA.card_body,{hideKeys:n.NPRICEITEMS})};var r=t(0),n=t(8),i=t(43),o=t(59),l=t(3),d=t(47),c=t(90),s=t(2);function u(e,a,t){return a in e?Object.defineProperty(e,a,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[a]=t,e}function f(e){for(var a=e.cardTable.getNumberOfRows(n.AREA.card_body),t=function(a){n.NPRICEITEMS.forEach(function(t){e.cardTable.setValByKeyAndIndex(n.AREA.card_body,a,t,{value:null,display:null,scale:2})})},r=0;r<a;r++)t(r)}function g(e,a,t){f(e),(0,o.doAddDynamicColumn)(e,a,t,n.AREA.card_body)}},90:function(e,a,t){"use strict";Object.defineProperty(a,"__esModule",{value:!0}),a.add=void 0;var r=t(8),n=t(45),i=t(0),o=(t(47),t(44));function l(e,a){var t={pk_org:a,pageId:r.PAGECODE.card};(0,i.ajax)({url:r.ACTION_URL.groupQuery,data:t,async:!1,success:function(a){if(a.success){var t=a.data;if(e.form.setAllFormValue((i={},o=r.AREA.card_head,l=t.head[r.AREA.card_head],o in i?Object.defineProperty(i,o,{value:l,enumerable:!0,configurable:!0,writable:!0}):i[o]=l,i)),e.form.getFormItemsValue(r.AREA.card_head,r.FIELDS.pk_org).value){var n=t.body[r.AREA.card_body];e.cardTable.setTableData(r.AREA.card_body,n,null,!0,!0),e.cardTable.setColVisibleByKey(r.AREA.card_body,{hideKeys:r.NPRICEITEMS})}}var i,o,l}})}a.add=function(e){var a=arguments.length>1&&void 0!==arguments[1]?arguments[1]:this.mainOrg,t=e.form,i=e.cardTable,d=(t.setFormStatus,t.EmptyAllFormValue),c=(i.setStatus,i.setTableData);d(r.AREA.card_head),c(r.AREA.card_body,{rows:[]}),l.call(this,e,a),o.RownoUtils.setRowNo(e,r.AREA.card_body),this.isAdd=!0,this.save_url=r.ACTION_URL.save,n.togglePageStatus.call(this,e,r.UISTATUS.edit)}}})});
//# sourceMappingURL=index.3923348e.js.map