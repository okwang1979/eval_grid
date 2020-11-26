package nc.vo.ct.saledaily.entity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import nc.vo.pub.lang.UFDouble;

public class PaymentPlan {
	        //�������ϵͳ�мƻ�id
			@NotNull
			@Size(max = 50)
			private String planId;
			//�Ǳ�������ƻ�ʱ�ƻ��������
			@Size(max = 50)
			private Integer sortNum;
			//������мƻ�������������Ϣ
			@NotNull
			@Size(max = 100)
			private String performItem;
			//������мƻ�������
			@Size(max = 100)
			private String payDate;
			//�Ǳ���ƻ���������
			@Size(max = 50)
			private String reminderDay;
			//������мƻ����
			@Size(max = 50)
			private UFDouble payAmount;
			
			public PaymentPlan() {
				
			}
			public String getPlanId() {
				return planId;
			}
			public void setPlanId(String planId) {
				this.planId = planId;
			}
			public Integer getSortNum() {
				return sortNum;
			}
			public void setSortNum(Integer sortNum) {
				this.sortNum = sortNum;
			}
			public String getPerformItem() {
				return performItem;
			}
			public void setPerformItem(String performItem) {
				this.performItem = performItem;
			}
			public String getPayDate() {
				return payDate;
			}
			public void setPayDate(String payDate) {
				this.payDate = payDate;
			}
			public String getReminderDay() {
				return reminderDay;
			}
			public void setReminderDay(String reminderDay) {
				this.reminderDay = reminderDay;
			}
			public UFDouble getPayAmount() {
				return payAmount;
			}
			public void setPayAmount(UFDouble payAmount) {
				this.payAmount = payAmount;
			}
			
}
