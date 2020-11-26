package nc.vo.ct.saledaily.entity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class PaymentFeedback {
			//必填，集成系统中计划id
			@NotNull
			@Size(max = 50)
			private String planId;
	        //反馈ID
	        private String feedBackId;
	        //反馈序号
	        private Integer sortNum;
	        //必填，1：是；0：否，默认为是（1）
			@NotNull
			@Size(max = 50)
			private Integer isNormal;
			//异常原因，是否正常为否（0）时必填
			@Size(max = 200)
			private String abnormalReason;
			//必填，实际收到款、付出去款的时间
			@NotNull
			private String realPayDate;
			//必填，实际收到款、付出去款的金额
			@NotNull
			private String realPayAmount;
			public PaymentFeedback() {
				
			}
			public String getPlanId() {
				return planId;
			}
			public void setPlanId(String planId) {
				this.planId = planId;
			}
			public String getFeedBackId() {
				return feedBackId;
			}
			public void setFeedBackId(String feedBackId) {
				this.feedBackId = feedBackId;
			}
			public Integer getSortNum() {
				return sortNum;
			}
			public void setSortNum(Integer sortNum) {
				this.sortNum = sortNum;
			}
			public Integer getIsNormal() {
				return isNormal;
			}
			public void setIsNormal(Integer isNormal) {
				this.isNormal = isNormal;
			}
			public String getAbnormalReason() {
				return abnormalReason;
			}
			public void setAbnormalReason(String abnormalReason) {
				this.abnormalReason = abnormalReason;
			}
			public String getRealPayDate() {
				return realPayDate;
			}
			public void setRealPayDate(String realPayDate) {
				this.realPayDate = realPayDate;
			}
			public String getRealPayAmount() {
				return realPayAmount;
			}
			public void setRealPayAmount(String realPayAmount) {
				this.realPayAmount = realPayAmount;
			}
			
}
