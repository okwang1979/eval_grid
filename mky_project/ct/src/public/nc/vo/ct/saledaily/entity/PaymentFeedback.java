package nc.vo.ct.saledaily.entity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class PaymentFeedback {
			//�������ϵͳ�мƻ�id
			@NotNull
			@Size(max = 50)
			private String planId;
	        //����ID
	        private String feedBackId;
	        //�������
	        private Integer sortNum;
	        //���1���ǣ�0����Ĭ��Ϊ�ǣ�1��
			@NotNull
			@Size(max = 50)
			private Integer isNormal;
			//�쳣ԭ���Ƿ�����Ϊ��0��ʱ����
			@Size(max = 200)
			private String abnormalReason;
			//���ʵ���յ������ȥ���ʱ��
			@NotNull
			private String realPayDate;
			//���ʵ���յ������ȥ��Ľ��
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
