!function(e,t){"object"==typeof exports&&"object"==typeof module?module.exports=t(require("nc-lightapp-front")):"function"==typeof define&&define.amd?define(["nc-lightapp-front"],t):"object"==typeof exports?exports["ct/ct/saledaily/utils/index"]=t(require("nc-lightapp-front")):e["ct/ct/saledaily/utils/index"]=t(e["nc-lightapp-front"])}(window,function(e){return function(e){var t={};function a(r){if(t[r])return t[r].exports;var o=t[r]={i:r,l:!1,exports:{}};return e[r].call(o.exports,o,o.exports,a),o.l=!0,o.exports}return a.m=e,a.c=t,a.d=function(e,t,r){a.o(e,t)||Object.defineProperty(e,t,{enumerable:!0,get:r})},a.r=function(e){"undefined"!=typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(e,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(e,"__esModule",{value:!0})},a.t=function(e,t){if(1&t&&(e=a(e)),8&t)return e;if(4&t&&"object"==typeof e&&e&&e.__esModule)return e;var r=Object.create(null);if(a.r(r),Object.defineProperty(r,"default",{enumerable:!0,value:e}),2&t&&"string"!=typeof e)for(var o in e)a.d(r,o,function(t){return e[t]}.bind(null,o));return r},a.n=function(e){var t=e&&e.__esModule?function(){return e.default}:function(){return e};return a.d(t,"a",t),t},a.o=function(e,t){return Object.prototype.hasOwnProperty.call(e,t)},a.p="../../../../",a(a.s=202)}({0:function(t,a){t.exports=e},1:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.CARDTABLEAREAIDS=t.FIELDS=t.SALEDAILY_URL=t.BILL_STATUS=t.CARD_HEAD_BUTTONS=t.CARD_BODY_INNER_BUTTONS=t.CARD_BODY_BUTTONS=t.SALEDAILY_CONST=void 0;var r=a(20);t.SALEDAILY_CONST=r.SALEDAILY_CONST,t.CARD_BODY_BUTTONS=r.CARD_BODY_BUTTONS,t.CARD_BODY_INNER_BUTTONS=r.CARD_BODY_INNER_BUTTONS,t.CARD_HEAD_BUTTONS=r.CARD_HEAD_BUTTONS,t.BILL_STATUS=r.BILL_STATUS,t.SALEDAILY_URL=r.SALEDAILY_URL,t.FIELDS=r.FIELDS,t.CARDTABLEAREAIDS=r.CARDTABLEAREAIDS},13:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.default=function(e){if(this.props.getUrlParam("status")!=r.SALEDAILY_CONST.STATUS_BROWSE){var t=this.props.cardTable.getCheckedRows(e),a=(this.props.form.getFormItemsValue(r.SALEDAILY_CONST.FORMID,"pk_payterm")||{}).value,o=void 0;e?e==r.SALEDAILY_CONST.CARDTABLE_TERM?o=["Term_DeleteLine","Term_CopyLine"]:e==r.SALEDAILY_CONST.CARDTABLE_PAYTERM&&a?o=["Payterm_DeleteLine","Payterm_CopyLine"]:e==r.SALEDAILY_CONST.CARDTABLE_MEMORA?o=["Memora_DeleteLine","Memora_CopyLine"]:e==r.SALEDAILY_CONST.CARDTABLE_EXP?o=["Exp_DeleteLine","Exp_CopyLine"]:e==r.SALEDAILY_CONST.CARDTABLEID&&(o=["DeleteLine","CopyLine"]):o=["DeleteLine","CopyLine","Exp_DeleteLine","Exp_CopyLine","Term_DeleteLine","Term_CopyLine","Payterm_DeleteLine","Payterm_CopyLine","Memora_DeleteLine","Memora_CopyLine"],void 0!=t&&t.length>0?this.props.button.setButtonDisabled(o,!1):this.props.button.setButtonDisabled(o,!0);var n=(this.props.form.getFormItemsValue(r.SALEDAILY_CONST.FORMID,"pk_org_v")||{}).value;this.props.button.setButtonDisabled(["AddLine","Term_AddLine","Exp_AddLine","Payterm_AddLine","Memora_AddLine"],!n)}this.props.getUrlParam("status")!=r.SALEDAILY_CONST.STATUS_BROWSE&&((this.props.form.getFormItemsValue(r.SALEDAILY_CONST.FORMID,"pk_payterm")||{}).value&&this.props.getUrlParam("status")!=r.SALEDAILY_CONST.STATUS_MODIFY?this.props.button.setButtonVisible("Payterm_InsertLine",!0):this.props.button.setButtonVisible("Payterm_InsertLine",!1))};var r=a(1)},15:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.clearTransferCache=t.rewriteTransferSrcBids=t.updateCacheDataForList=t.deleteCacheDataForList=t.getNextId=t.getCurrentLastId=t.getDefData=t.setDefData=t.hasListCache=t.addCacheData=t.getCacheDataByPk=t.deleteCacheData=t.updateCacheData=t.changeUrlParam=void 0;var r=a(0);t.changeUrlParam=function(e,t){e.setUrlParam(t)},t.updateCacheData=function(e,t,a,o,n,i){r.cardCache.addCache;var l=r.cardCache.updateCache;r.cardCache.getCacheById,l(t,a,o,n,i)},t.deleteCacheData=function(e,t,a,o){var n=r.cardCache.deleteCacheById;r.cardCache.getNextId,n(t,a,o)},t.getCacheDataByPk=function(e,t,a){return(0,r.cardCache.getCacheById)(a,t)},t.addCacheData=function(e,t,a,o,n,i){(0,r.cardCache.addCache)(a,o,n,i)},t.hasListCache=function(e,t){return e.table.hasCacheData(t)},t.setDefData=function(e,t,a){(0,r.cardCache.setDefData)(t,e,a)},t.getDefData=function(e,t){return(0,r.cardCache.getDefData)(t,e)},t.getCurrentLastId=function(e){return(0,r.cardCache.getCurrentLastId)(e)},t.getNextId=function(e,t,a){return(0,r.cardCache.getNextId)(t,a)},t.deleteCacheDataForList=function(e,t,a){a instanceof Array?a.forEach(function(a){e.table.deleteCacheId(t,a)}):e.table.deleteCacheId(t,a)},t.updateCacheDataForList=function(e,t,a,r,o){var n=r.sucessVOs;if(null!=n&&0!=n.length){var i=[];if(void 0==o){var l={};e.table.getCheckedRows(t).forEach(function(e){var t=e.data.values[a].value;l[t]=e.index}),n[t].rows.forEach(function(e,t){var r=e.values[a].value,o={index:l[r],data:{values:e.values}};i.push(o)})}else{var s={index:o,data:{values:n[t].rows[0].values}};i.push(s)}e.table.updateDataByIndexs(t,i)}},t.rewriteTransferSrcBids=function(e,t,a){if(a){var r=[];a.forEach(function(e){r.push(e.values[t].value)}),e.transferTable.setSavedTransferTableDataPk(r)}},t.clearTransferCache=function(e,t){e.transferTable.deleteCache(t)}},20:function(e,t,a){"use strict";var r;function o(e,t,a){return t in e?Object.defineProperty(e,t,{value:a,enumerable:!0,configurable:!0,writable:!0}):e[t]=a,e}Object.defineProperty(t,"__esModule",{value:!0});var n={FORMID:"head",CARDTABLEID:"saledaily_base",CARDTABLE_TERM:"saledaily_term",CARDTABLE_EXP:"saledaily_exp",CARDTABLE_MEMORA:"saledaily_memora",CARDTABLE_CHANGE:"saledaily_change",CARDTABLE_EXEC:"saledaily_exec",CARDTABLE_PAYTERM:"saledaily_payterm",CARDPAGEID:"400600200_card",LISTPAGEID:"400600200_list",HISPAGECODE:"400600200_history",SEARCHID:"400600200",MODULEID:"4006",LISTTABLEID:"ct_saledaily",LISTHEADBUTTONAREA:"list_head",LISTINNERBUTTONAREA:"list_inner",CARDHEADBUTTONAREA:"card_head",CARDBODYBUTTONAREA:"card_body",CARDBODYTERMBUTTONAREA:"card_body_term",CARDBODYEXPBUTTONAREA:"card_body_exp",CARDBODYMEMORABUTTONAREA:"card_body_memora",CARDBODYPAYTERMBUTTONAREA:"card_body_payterm",CARDBODYINNERBUTTONAREA:"card_body_inner",CARDBODYMEMORAINNERBUTTONAREA:"card_memora_inner",CARDBODYTERMINNERBUTTONAREA:"card_term_inner",CARDBODYPAYTERMINNERBUTTONAREA:"card_payterm_inner",CARDBODYEXPINNERBUTTONAREA:"card_exp_inner",CARD_BODY_EDIT_BUTTONS:["Spread","DeleteLine","CopyLine","ResetRowNo"],CARD_BODY_PASTE_BUTTONS:["PasteLineToTail","CanelCopy"],STATUS_BROWSE:"browse",STATUS_EDIT:"edit",STATUS_MODIFY:"modify",STATUS_ADD:"add",DATA_SOURCE:"ct.ct.saledaily.saledailycache",extendAttribute:"extendAttribute",transTypeCacheKey:"transTypeCacheKey"},i=[n.CARDTABLEID,n.CARDTABLE_TERM,n.CARDTABLE_EXP,n.CARDTABLE_MEMORA,n.CARDTABLE_CHANGE,n.CARDTABLE_EXEC,n.CARDTABLE_PAYTERM],l=(o(r={pk_ct_sale:"pk_ct_sale",fstatusflag:"fstatusflag",version:"version",vchangecode:"vchangecode",pk_org_v:"pk_org_v",controltype:"controltype",pk_org:"pk_org",ctrantypeid:"ctrantypeid",vbillcode:"vbillcode",blatest:"blatest"},"fstatusflag","fstatusflag"),o(r,"vtrantypecode","vtrantypecode"),o(r,"marbasclassbound","marbasclassbound"),o(r,"pk_marbasclass","pk_ct_sale_b.pk_marbasclass"),r);t.SALEDAILY_CONST=n,t.CARD_BODY_BUTTONS={ALL:["AddLine","ResetRowNo","CopyLine","CancelCopy","PasteToTail","DeleteLine","Term_AddLine","Term_CopyLine","Term_DeleteLine","Term_PasteToTail","Term_ResetRowNo","Term_CancelCopy","Exp_AddLine","Exp_CopyLine","Exp_CancelCopy","Exp_DeleteLine","Exp_PasteToTail","Exp_ResetRowNo","Memora_AddLine","Memora_CopyLine","Memora_CancelCopy","Memora_DeleteLine","Memora_PasteToTail","Memora_ResetRowNo","Payterm_AddLine","Payterm_CopyLine","Payterm_CancelCopy","Payterm_DeleteLine","Payterm_PasteToTail","Payterm_ResetRowNo"],BROWSE:[],EDIT:["DeleteLine","CopyLine","ResetRowNo","AddLine"],EDIT_COPYING:["PasteToTail","CancelCopy"],TERM_EDIT:["Term_DeleteLine","Term_CopyLine","Term_AddLine"],TERM_EDIT_COPYING:["Term_PasteToTail","Term_CancelCopy"],MEMORA_EDIT:["Memora_DeleteLine","Memora_CopyLine","Memora_AddLine"],MEMORA_EDIT_COPYING:["Memora_PasteToTail","Memora_CancelCopy"],EXP_EDIT:["Exp_DeleteLine","Exp_CopyLine","Exp_AddLine"],EXP_EDIT_COPYING:["Exp_PasteToTail","Exp_CancelCopy"],PAYTERM_EDIT:["Payterm_DeleteLine","Payterm_CopyLine","Payterm_AddLine"],PAYTERM_EDIT_COPYING:["Payterm_PasteToTail","Payterm_CancelCopy"]},t.CARD_BODY_INNER_BUTTONS={ALL:["CloseOut","OpenOut","CheckBill","PriceChange","Spread","PasteLine"],BROWSE:["Spread"]},t.CARD_HEAD_BUTTONS={ALL:["Copy","Cancel","CancelValidate","End","ModiDelete","Output","Save","SaveCommit","Receive","Excute","File","Print","Validate","Freeze","UnFreeze","CTUnEnd","ReceivePlan","Ref4310","Edit","Delete","Commit","UnCommit","Modify","StructuredFile","ModifyHistory","ApproveInfo","ReceiveStartControl","Add","BillLinkQuery","QueryAboutBusiness","Refresh","QuitTransfer","ImageScan","ImageView"],EDIT:["Save","Cancel","SaveCommit"],EDIT_APPROVING:["Save","Cancel"],EDIT_TRANSFER:["Save","Cancel","SaveCommit","QuitTransfer"],EDIT_RECEIVE:["Save","Cancel"],BROWSE_FREE:["Copy","Output","Excute","File","Print","Ref4310","Edit","Delete","Commit","StructuredFile","QueryAboutBusiness","ApproveInfo","Add","BillLinkQuery","Refresh","ImageScan","ImageView"],BROWSE_FREE_VERSIONED:["Copy","ModiDelete","Output","Excute","File","Print","Ref4310","Edit","Commit","StructuredFile","QueryAboutBusiness","ModifyHistory","Modify","ApproveInfo","Add","BillLinkQuery","Refresh","ImageScan","ImageView"],BROWSE_VALIDATE:["Add","Ref4310","Copy","CancelValidate","Freeze","End","Modify","StructuredFile","File","Receive","BillLinkQuery","QueryAboutBusiness","Print","Output","ApproveInfo","Refresh","ImageScan","ImageView"],BROWSE_APPROVING:["Add","UnCommit","Copy","Ref4310","StructuredFile","File","ModifyHistory","Print","Output","QueryAboutBusiness","ApproveInfo","Refresh","ImageScan","ImageView"],BROWSE_APPROVED:["Add","UnCommit","Copy","Ref4310","Validate","StructuredFile","File","Print","Output","QueryAboutBusiness","ApproveInfo","Refresh","ImageScan","ImageView"],BROWSE_APPROVED_VERSIONED:["Add","UnCommit","Copy","Ref4310","Validate","StructuredFile","File","ModifyHistory","Print","Output","QueryAboutBusiness","ApproveInfo","Refresh","ImageScan","ImageView"],BROWSE_END:["Add","CTUnEnd","Copy","Ref4310","StructuredFile","File","ModifyHistory","Print","Output","QueryAboutBusiness","ReceivePlan","ApproveInfo","Refresh"],BROWSE_FREEZE:["Add","UnFreeze","Copy","Ref4310","StructuredFile","File","Print","Output","QueryAboutBusiness","ReceivePlan","ApproveInfo","Refresh"],BROWSE_FREEZE_VERSIONED:["Add","UnFreeze","Copy","Ref4310","StructuredFile","File","ModifyHistory","Print","Output","QueryAboutBusiness","ReceivePlan","ApproveInfo","Refresh"],BROWSE_NOPASS:["Add","Edit","Copy","Ref4310","StructuredFile","File","Print","Output","QueryAboutBusiness","ApproveInfo","Refresh"],BROWSE_NOPASS_VERSIONED:["Add","Copy","Ref4310","Modify","ModiDelete","StructuredFile","File","ModifyHistory","Print","Output","QueryAboutBusiness","ApproveInfo","Refresh","ImageScan","ImageView"],BROWSE_EMPTY:["Add","Ref4310"],BROWSE_APPROVECENTER_APPROVING:["Edit","QueryAboutBusiness","CreditQuery","FileManage","Print","Output","SplitPrint"]},t.BILL_STATUS={I_FREE:"1",I_AUDITING:"7",I_AUDIT:"2",I_CLOSED:"4",I_NOPASS:"8"},t.SALEDAILY_URL={CARD_URL:"/ct/ct/saledaily/card/index.html",LIST_URL:"/ct/ct/saledaily/list/index.html",LIST_QUERY_URL:"/nccloud/ct/saledaily/listquery.do",CARD_QUERY_URL:"/nccloud/ct/saledaily/cardquery.do",CARD_EDIT_URL:"/nccloud/ct/saledaily/cardedit.do",CARD_DELETE_URL:"/nccloud/ct/saledaily/carddelete.do",CARD_MODIFY_URL:"/nccloud/ct/saledaily/cardmodify.do",CARD_MODIDELETE_URL:"/nccloud/ct/saledaily/cardmodidelete.do",CARD_SAVE_URL:"/nccloud/ct/saledaily/cardsave.do",CARD_MODIFYSAVE_URL:"/nccloud/ct/saledaily/cardmodifysave.do",CARD_COMMIT_URL:"/nccloud/ct/saledaily/cardcommit.do",CARD_VALIDATE_URL:"/nccloud/ct/saledaily/cardvalidate.do",CARD_UNVALIDATE_URL:"/nccloud/ct/saledaily/cardunvalidate.do",CARD_UNCOMMIT_URL:"/nccloud/ct/saledaily/carduncommit.do",CARD_END_URL:"/nccloud/ct/saledaily/cardend.do",CARD_UNEND_URL:"/nccloud/ct/saledaily/cardunend.do",CARD_FREEZE_URL:"/nccloud/ct/saledaily/cardfreeze.do",CARD_UNFREEZE_URL:"/nccloud/ct/saledaily/cardunfreeze.do",CARD_COPY_URL:"/nccloud/ct/saledaily/cardcopy.do",CARD_ADDLINE_URL:"/nccloud/ct/saledaily/bodyaddline.do",LIST_DELETE_URL:"/nccloud/ct/saledaily/listdelete.do",LIST_COMMIT_URL:"/nccloud/ct/saledaily/listcommit.do",LIST_UNCOMMIT_URL:"/nccloud/ct/saledaily/listuncommit.do",LIST_EDIT_URL:"/nccloud/ct/saledaily/listedit.do",LIST_END_URL:"/nccloud/ct/saledaily/listend.do",LIST_UNEND_URL:"/nccloud/ct/saledaily/listunend.do",LIST_FREEZE_URL:"/nccloud/ct/saledaily/listfreeze.do",LIST_UNFREEZE_URL:"/nccloud/ct/saledaily/listunfreeze.do",LIST_VALIDATE_URL:"/nccloud/ct/saledaily/listvalidate.do",LIST_UNVALIDATE_URL:"/nccloud/ct/saledaily/listunvalidate.do",LIST_MODIDELETE_URL:"/nccloud/ct/saledaily/listmodidelete.do",LIST_GATHERMNY_URL:"/nccloud/ct/saledaily/listgathermny.do",LIST_QUERYBYPKS_URL:"/nccloud/ct/saledaily/listquerybypks.do",CARD_HEAD_BEFORE_URL:"/nccloud/ct/saledaily/headbefore.do",CARD_BODY_BEFORE_URL:"/nccloud/ct/saledaily/bodybefore.do",CARD_HEAD_AFTER_URL:"/nccloud/ct/saledaily/headafter.do",CARD_BODY_AFTER_URL:"/nccloud/ct/saledaily/bodyafter.do",PRINT_URL:"/nccloud/ct/saledaily/print.do",printdatapermission:"/nccloud/ct/saledaily/checkPrintDataPermission.do",TRANSFER4310_URL:"/nccloud/ct/saledaily/tranfer4310.do",RECEIVE_URL:"/nccloud/ct/saledaily/receive.do",MODIFYHISTORY_URL:"/nccloud/ct/saledaily/modifyhistory.do",queryTranstype:"/nccloud/ct/saledaily/queryTranstype.do",queryTranstypeBypk:"/nccloud/ct/saledaily/queryTranstypeBypk.do"},t.FIELDS=l,t.CARDTABLEAREAIDS=i},202:function(e,t,a){e.exports=a(203)},203:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.cardPageUtil=void 0;var r=function(e){return e&&e.__esModule?e:{default:e}}(a(39));t.cardPageUtil=r.default},21:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.default=function(e,t){return new Promise(function(a,o){var n=t.key,i=t.areahid,l=t.areabid,s=t.index,A=t.cmaterialid,d=e.form.getFormItemsValue(i,"pk_org");d||a(!1);var _=e.cardTable.getValByKeyAndIndex(l,s,A);if(_&&_.value){var c={key:n,params:{cmaterialvid:_.value,pk_org:d.value,key:n}};(0,r.ajax)({url:"/nccloud/ct/saledaily/vfreebefore.do",data:c,success:function(e){if(e.data){var t=e.data.isedit;t?a(t):e.data.message&&(0,r.toast)({color:"warning",content:e.data.message}),a(!1)}},error:function(e){(0,r.toast)({color:"warning",content:e.message}),a(!1)}})}else a(!1)})};var r=a(0)},24:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.default=function(e){var t=e.getUrlParam("status");void 0==t&&(t=e.form.getFormStatus(r.SALEDAILY_CONST.FORMID)),void 0==t&&(t=r.SALEDAILY_CONST.STATUS_BROWSE),function(e,t){var a=t,o=null;o=t==r.SALEDAILY_CONST.STATUS_BROWSE?r.SALEDAILY_CONST.STATUS_BROWSE:r.SALEDAILY_CONST.STATUS_EDIT;t==r.SALEDAILY_CONST.STATUS_MODIFY&&(a=r.SALEDAILY_CONST.STATUS_EDIT);e.form.setFormStatus(r.SALEDAILY_CONST.FORMID,a),e.cardTable.setStatus(r.SALEDAILY_CONST.CARDTABLEID,o),e.cardTable.setStatus(r.SALEDAILY_CONST.CARDTABLE_CHANGE,o),e.cardTable.setStatus(r.SALEDAILY_CONST.CARDTABLE_EXEC,o),e.cardTable.setStatus(r.SALEDAILY_CONST.CARDTABLE_EXP,o),e.cardTable.setStatus(r.SALEDAILY_CONST.CARDTABLE_MEMORA,o),e.cardTable.setStatus(r.SALEDAILY_CONST.CARDTABLE_PAYTERM,o),e.cardTable.setStatus(r.SALEDAILY_CONST.CARDTABLE_TERM,o)}.call(this,e,t);var a=t===r.SALEDAILY_CONST.STATUS_BROWSE;(function(e,t,a){if(a){e.button.setButtonVisible(r.CARD_HEAD_BUTTONS.ALL,!1);var n=function(e){var t=(e.form.getFormItemsValue(r.SALEDAILY_CONST.FORMID,"fstatusflag")||{}).value,a=(e.form.getFormItemsValue(r.SALEDAILY_CONST.FORMID,"version")||{}).value,o=((e.form.getFormItemsValue(r.SALEDAILY_CONST.FORMID,"ctrantypeid")||{}).value,(e.form.getFormItemsValue(r.SALEDAILY_CONST.FORMID,"pk_payterm")||{}).value);switch(t){case null:case void 0:return r.CARD_HEAD_BUTTONS.BROWSE_EMPTY;case"0":return 1==a?r.CARD_HEAD_BUTTONS.BROWSE_FREE:r.CARD_HEAD_BUTTONS.BROWSE_FREE_VERSIONED;case"1":var n=JSON.parse(JSON.stringify(r.CARD_HEAD_BUTTONS.BROWSE_VALIDATE));return this.state.showPayTerm&&o&&n.push("ReceiveStartControl"),this.state.showPayTerm||n.push("ReceivePlan"),1!=a&&n.push("ModifyHistory"),n;case"2":var i=JSON.parse(JSON.stringify(r.CARD_HEAD_BUTTONS.BROWSE_APPROVING)),l=e.getUrlParam("scene");return!l||"approvesce"!=l&&"zycl"!=l||i.push("Edit"),i;case"3":return 1==a?r.CARD_HEAD_BUTTONS.BROWSE_APPROVED:r.CARD_HEAD_BUTTONS.BROWSE_APPROVED_VERSIONED;case"4":return 1==a?r.CARD_HEAD_BUTTONS.BROWSE_NOPASS:r.CARD_HEAD_BUTTONS.BROWSE_NOPASS_VERSIONED;case"5":return 1==a?r.CARD_HEAD_BUTTONS.BROWSE_FREEZE:r.CARD_HEAD_BUTTONS.BROWSE_FREEZE_VERSIONED;case"6":return r.CARD_HEAD_BUTTONS.BROWSE_END;default:return r.CARD_HEAD_BUTTONS.BROWSE_EMPTY}}.call(this,e),i=e.getUrlParam("scene");i&&"approvesce"==i&&n&&n.indexOf("ApproveInfo")>-1&&(n=n.filter(function(e){return"ApproveInfo"!=e})),e.button.setButtonVisible(n,!0),"ref4310"==this.props.getUrlParam("type")?(e.button.setButtonVisible("QuitTransfer",!0),e.button.setButtonVisible(["Add","Ref4310","Copy"],!1)):(e.button.setButtonVisible("QuitTransfer",!1),e.button.setButtonVisible(["Add","Ref4310","Copy"],!0));var l=(e.form.getFormItemsValue(r.SALEDAILY_CONST.FORMID,"version")||{}).value;parseInt(l)>1?(e.button.setButtonVisible("Edit",!1),e.button.setButtonVisible("ModifyHistory",!0)):e.button.setButtonVisible("ModifyHistory",!1)}else e.button.setButtonVisible(r.CARD_HEAD_BUTTONS.ALL,!1),e.button.setButtonVisible(function(e){var t=e.getUrlParam("type");if("ref4310"==t)return r.CARD_HEAD_BUTTONS.EDIT_TRANSFER;if("receive"==t)return r.CARD_HEAD_BUTTONS.EDIT_RECEIVE;var a=e.getUrlParam("scene");if(a&&("approvesce"==a||"zycl"==a))return r.CARD_HEAD_BUTTONS.EDIT_APPROVING;return r.CARD_HEAD_BUTTONS.EDIT}(e),!0);r.SALEDAILY_CONST.STATUS_EDIT!=e.getUrlParam("status")&&"ref4310"!=e.getUrlParam("type")||e.form.setFormItemsDisabled(r.SALEDAILY_CONST.FORMID,{pk_org_v:!0});if(e.button.setButtonVisible(r.CARD_BODY_BUTTONS.ALL,!1),!a&&"receive"!=this.props.getUrlParam("type")){var s=!!this.state.copyRowDatas;if(s?e.button.setButtonVisible(r.CARD_BODY_BUTTONS.EDIT_COPYING,!0):e.button.setButtonVisible(r.CARD_BODY_BUTTONS.EDIT,!0),(s=!!this.state.termCopyRowDatas)?e.button.setButtonVisible(r.CARD_BODY_BUTTONS.TERM_EDIT_COPYING,!0):e.button.setButtonVisible(r.CARD_BODY_BUTTONS.TERM_EDIT,!0),s=!!this.state.paytermCopyRowDatas)e.button.setButtonVisible(r.CARD_BODY_BUTTONS.PAYTERM_EDIT_COPYING,!0);else{var A=(e.form.getFormItemsValue(r.SALEDAILY_CONST.FORMID,"pk_payterm")||{}).value;A&&this.props.getUrlParam("status")!=r.SALEDAILY_CONST.STATUS_MODIFY?e.button.setButtonVisible(r.CARD_BODY_BUTTONS.PAYTERM_EDIT,!0):e.button.setButtonVisible(r.CARD_BODY_BUTTONS.PAYTERM_EDIT,!1)}(s=!!this.state.expCopyRowDatas)?e.button.setButtonVisible(r.CARD_BODY_BUTTONS.EXP_EDIT_COPYING,!0):e.button.setButtonVisible(r.CARD_BODY_BUTTONS.EXP_EDIT,!0),(s=!!this.state.memoraCopyRowDatas)?e.button.setButtonVisible(r.CARD_BODY_BUTTONS.MEMORA_EDIT_COPYING,!0):e.button.setButtonVisible(r.CARD_BODY_BUTTONS.MEMORA_EDIT,!0)}"receive"!=this.props.getUrlParam("type")&&o.default.call(this)}).call(this,e,t,a),function(e,t){var a=!0,o=e.getUrlParam("scene");t&&"zycl"!=o&&"zycx"!=o&&"approvesce"!=o||(a=!1);"ref4310"==e.getUrlParam("type")&&(a=!0);e.BillHeadInfo.setBillHeadInfoVisible({showBackBtn:a,showBillCode:!0,billCode:(e.form.getFormItemsValue(r.SALEDAILY_CONST.FORMID,"vbillcode")||{}).value})}.call(this,e,a),function(e,t){var a=t,o=e.getUrlParam("scene"),n=(e.form.getFormItemsValue(r.SALEDAILY_CONST.FORMID,"fstatusflag")||{}).value,i=this.props.getUrlParam("type");n?"approvesce"==o||"zycl"==o?(a=!1,e.form.setFormItemsDisabled(r.SALEDAILY_CONST.FORMID,{ctrantypeid:!0})):"ref4310"==i&&(a=!1):a=!1;e.cardPagination.setCardPaginationVisible("cardPaginationBtn",a)}.call(this,e,a)};var r=a(1),o=function(e){return e&&e.__esModule?e:{default:e}}(a(13));a(6)},28:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.rowCopyPasteUtils=void 0;var r=a(0),o=a(1);function n(e,t,a){return t in e?Object.defineProperty(e,t,{value:a,enumerable:!0,configurable:!0,writable:!0}):e[t]=a,e}function i(e,t,a,o,i){var A=s(t);!function(e,t,a,o,n){var i=(0,r.deepClone)(a);if(i)if(i instanceof Array)for(var s=i.length,A=0;A<s;A++){var d=i[A].data;l(d,n),e.cardTable.insertRowsAfterIndex(t,d,o+A)}else l(i,n),e.cardTable.insertRowsAfterIndex(t,i,o)}(e,t,this.state[A],a-1,o),this.setState(n({},A,null),i),e.cardTable.selectAllRows(t,!1),e.cardTable.setAllCheckboxAble(t,!0)}function l(e,t){t&&t instanceof Array&&(e instanceof Array?e.forEach(function(e){t.forEach(function(t){e.values[t]={value:null,display:null,scale:-1}})}):t.forEach(function(t){e.values[t]={value:null,display:null,scale:-1}}))}function s(e){var t=void 0;return e==o.SALEDAILY_CONST.CARDTABLEID?t="copyRowDatas":e==o.SALEDAILY_CONST.CARDTABLE_EXP?t="expCopyRowDatas":e==o.SALEDAILY_CONST.CARDTABLE_MEMORA?t="memoraCopyRowDatas":e==o.SALEDAILY_CONST.CARDTABLE_TERM?t="termCopyRowDatas":e==o.SALEDAILY_CONST.CARDTABLE_PAYTERM&&(t="paytermCopyRowDatas"),t}var A={copyRow:function(e,t,a,r){var o=s(t);return this.setState(n({},o,a),r),e.cardTable.setAllCheckboxAble(t,!1),a},copyRows:function(e,t,a){var r=s(t),o=e.cardTable.getCheckedRows(t);if(o&&o.length>0)return this.setState(n({},r,o),a),e.cardTable.setAllCheckboxAble(t,!1),o},pasteRowsToIndex:i,pasteRowsToTail:function(e,t,a,r){var o=e.cardTable.getNumberOfRows(t);i.call(this,e,t,o,a,r)},cancel:function(e,t,a){var r=s(t);this.setState(n({},r,null),a),e.cardTable.selectAllRows(t,!1),e.cardTable.setAllCheckboxAble(t,!0)},getEditButtonsByTableId:function(e){return e==o.SALEDAILY_CONST.CARDTABLEID&&o.CARD_BODY_BUTTONS.EDIT||e==o.SALEDAILY_CONST.CARDTABLE_EXP&&o.CARD_BODY_BUTTONS.EXP_EDIT||e==o.SALEDAILY_CONST.CARDTABLE_MEMORA&&o.CARD_BODY_BUTTONS.MEMORA_EDIT||e==o.SALEDAILY_CONST.CARDTABLE_TERM&&o.CARD_BODY_BUTTONS.TERM_EDIT||e==o.SALEDAILY_CONST.CARDTABLE_PAYTERM&&o.CARD_BODY_BUTTONS.PAYTERM_EDIT||o.CARD_BODY_BUTTONS.EDIT},getCopyingButtonsByTableId:function(e){return e==o.SALEDAILY_CONST.CARDTABLEID&&o.CARD_BODY_BUTTONS.EDIT_COPYING||e==o.SALEDAILY_CONST.CARDTABLE_EXP&&o.CARD_BODY_BUTTONS.EXP_EDIT_COPYING||e==o.SALEDAILY_CONST.CARDTABLE_MEMORA&&o.CARD_BODY_BUTTONS.MEMORA_EDIT_COPYING||e==o.SALEDAILY_CONST.CARDTABLE_TERM&&o.CARD_BODY_BUTTONS.TERM_EDIT_COPYING||e==o.SALEDAILY_CONST.CARDTABLE_PAYTERM&&o.CARD_BODY_BUTTONS.PAYTERM_EDIT_COPYING||o.CARD_BODY_BUTTONS.EDIT_COPYING},getCopyDataNameByTableId:s};t.rowCopyPasteUtils=A},29:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.getConfig=void 0;var r=a(1);t.getConfig=function(){var e=new Map;return e.set("pk_ct_sale_b",r.SALEDAILY_CONST.CARDTABLEID),e.set("pk_ct_sale_term",r.SALEDAILY_CONST.CARDTABLE_TERM),e.set("pk_ct_sale_exec",r.SALEDAILY_CONST.CARDTABLE_EXEC),e.set("pk_ct_sale_exp",r.SALEDAILY_CONST.CARDTABLE_EXP),e.set("pk_ct_sale_memora",r.SALEDAILY_CONST.CARDTABLE_MEMORA),e.set("pk_ct_sale_change",r.SALEDAILY_CONST.CARDTABLE_CHANGE),e.set("pk_ct_sale_payterm",r.SALEDAILY_CONST.CARDTABLE_PAYTERM),{headAreaId:r.SALEDAILY_CONST.FORMID,bodyIdAndPkMap:e,baseBack:!0}}},30:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.queryZ3TransType=void 0;var r=a(0),o=a(1);t.queryZ3TransType=function(e,t){var a=this,n=this.z3trantypevo;return new Promise(function(i,l){if("{}"!=JSON.stringify(n||{})&&t)i(n);else{var s={pk:e};(0,r.ajax)({url:o.SALEDAILY_URL.queryTranstypeBypk,data:s,success:function(e){e.success&&e.data&&(i(e.data),a.z3trantypevo=e.data)}})}})}},39:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.toggleCardTable=t.addRowForModify=t.setCardCompareData=t.setCardPage=t.clearCardData=t.cardBodyAreas=void 0;var r=a(1),o=a(4),n=function(e){return e&&e.__esModule?e:{default:e}}(a(24)),i=a(9);function l(e,t,a){return t in e?Object.defineProperty(e,t,{value:a,enumerable:!0,configurable:!0,writable:!0}):e[t]=a,e}var s=[r.SALEDAILY_CONST.CARDTABLEID,r.SALEDAILY_CONST.CARDTABLE_CHANGE,r.SALEDAILY_CONST.CARDTABLE_EXEC,r.SALEDAILY_CONST.CARDTABLE_EXP,r.SALEDAILY_CONST.CARDTABLE_MEMORA,r.SALEDAILY_CONST.CARDTABLE_PAYTERM,r.SALEDAILY_CONST.CARDTABLE_TERM];t.cardBodyAreas=s,t.clearCardData=function(e){e.form.EmptyAllFormValue(r.SALEDAILY_CONST.FORMID),s.forEach(function(t){e.cardTable.setTableData(t,{rows:[]})}),e.initMetaByPkorg(r.FIELDS.pk_org_v)},t.setCardPage=function(e,t){var a=!(arguments.length>2&&void 0!==arguments[2])||arguments[2];e.beforeUpdatePage();var i=null;t.head&&t.head.head&&(e.form.setAllFormValue(l({},r.SALEDAILY_CONST.FORMID,t.head.head)),i=t.head.head.rows[0].values[r.FIELDS.pk_ct_sale].value),t.bodys&&s.forEach(function(r){t.bodys[r]?(a?t.bodys[r]=e.cardTable.updateDataByRowId(r,t.bodys[r],!0):e.cardTable.setTableData(r,t.bodys[r],null,!0,!0),e.cardTable.selectAllRows(r,!1)):e.cardTable.setTableData(r,{rows:[]})}),(0,o.updateCacheData)(e,r.FIELDS.pk_ct_sale,i,t,r.SALEDAILY_CONST.FORMID,DATASOURCECACHE.dataSourceCacheKey),n.default.call(this,e),e.updatePage(r.SALEDAILY_CONST.FORMID,s)},t.setCardCompareData=function(e,t,a){e.beforeUpdatePage(),(0,i.updateExtBillDataForCompareByPk)(e,t,a),n.default.call(this,e),(0,o.updateCacheData)(e,r.FIELDS.pk_ct_sale,e.form.getFormItemsValue(r.SALEDAILY_CONST.FORMID,r.FIELDS.pk_ct_sale).value,t,r.SALEDAILY_CONST.FORMID,DATASOURCECACHE.dataSourceCacheKey),e.updatePage(r.SALEDAILY_CONST.FORMID,s)},t.addRowForModify=function(e){var t=e.getUrlParam("status"),a=e.form.getFormItemsValue(r.SALEDAILY_CONST.FORMID,r.FIELDS.fstatusflag).value;if(t==UISTATUS.modify&&a==FSTATUSFLAG.validate){var o=parseInt(e.form.getFormItemsValue(r.SALEDAILY_CONST.FORMID,r.FIELDS.version).value)+1;e.form.setFormItemsValue(r.SALEDAILY_CONST.FORMID,l({},r.FIELDS.version,{value:o}));var n=e.cardTable.getNumberOfRows(r.SALEDAILY_CONST.CARDTABLE_CHANGE);e.cardTable.addRow(r.SALEDAILY_CONST.CARDTABLE_CHANGE,n,l({},r.FIELDS.vchangecode,{value:o}),!0)}},t.toggleCardTable=function(e){e.cardTable.toggleCardTable(r.SALEDAILY_CONST.CARDTABLEID,!0),e.cardTable.toggleCardTable([r.SALEDAILY_CONST.CARDTABLE_CHANGE,r.SALEDAILY_CONST.CARDTABLE_EXEC,r.SALEDAILY_CONST.CARDTABLE_EXP,r.SALEDAILY_CONST.CARDTABLE_MEMORA,r.SALEDAILY_CONST.CARDTABLE_TERM],!1)}},4:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.clearTransferCache=t.rewriteTransferSrcBids=t.updateCacheDataForList=t.deleteCacheDataForList=t.getNextId=t.getCurrentLastId=t.getDefData=t.setDefData=t.hasListCache=t.addCacheData=t.getCacheDataByPk=t.deleteCacheData=t.updateCacheData=t.changeUrlParam=void 0;var r=a(15);t.changeUrlParam=r.changeUrlParam,t.updateCacheData=r.updateCacheData,t.deleteCacheData=r.deleteCacheData,t.getCacheDataByPk=r.getCacheDataByPk,t.addCacheData=r.addCacheData,t.hasListCache=r.hasListCache,t.setDefData=r.setDefData,t.getDefData=r.getDefData,t.getCurrentLastId=r.getCurrentLastId,t.getNextId=r.getNextId,t.deleteCacheDataForList=r.deleteCacheDataForList,t.updateCacheDataForList=r.updateCacheDataForList,t.rewriteTransferSrcBids=r.rewriteTransferSrcBids,t.clearTransferCache=r.clearTransferCache},6:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.queryZ3TransType=t.vfreeBeforeEvent=t.getConfig=t.rowCopyPasteUtils=void 0;var r=a(28),o=a(29),n=function(e){return e&&e.__esModule?e:{default:e}}(a(21)),i=a(30);t.rowCopyPasteUtils=r.rowCopyPasteUtils,t.getConfig=o.getConfig,t.vfreeBeforeEvent=n.default,t.queryZ3TransType=i.queryZ3TransType},9:function(e,t,a){"use strict";function r(e,t,a){return t in e?Object.defineProperty(e,t,{value:a,enumerable:!0,configurable:!0,writable:!0}):e[t]=a,e}Object.defineProperty(t,"__esModule",{value:!0}),t.updateExtBillDataForCompareByPk=function(e,t,a,o){var n=a.headAreaId,i=a.bodyIdAndPkMap,l=a.baseBack;return e.form.setAllFormValue(r({},n,t.head[n])),i.forEach(function(a,r){var n=t.bodys[a];if(n){var i=void 0;if(l&&"saledaily_exec"==a||"validate"==o){var s=e.cardTable.getVisibleRows(a),A=new Map;s.forEach(function(e){var t=e.values[r].value;A.set(t,e.rowid)}),n.rows.forEach(function(e){if("{}"!=JSON.stringify(e.values)||l){var t=e.rowid,a=A.get(t);e.rowid=a}}),i=e.cardTable.updateDiffDataByRowId(a,n,!0,!0,!1)}else i=e.cardTable.updateDataByRowId(a,n,!0);t.bodys[a]=i}}),t},t.updateExtBillDataForCompare=function(e,t,a){var o=a.headAreaId,n=a.bodyIdAndPkMap;return e.form.setAllFormValue(r({},o,t.head[o])),n.forEach(function(a,r){var o=t.bodys[a];if(o){var n=e.cardTable.setTableData(a,o);t.bodys[a]=n}}),t}}})});
//# sourceMappingURL=index.136bfb04.js.map