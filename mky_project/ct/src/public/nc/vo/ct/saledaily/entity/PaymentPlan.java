package nc.vo.ct.saledaily.entity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import nc.vo.pub.lang.UFDouble;

public class PaymentPlan {
	        //必填，集成系统中计划id
			@NotNull
			@Size(max = 50)
			private String planId;
			//非必填，多条计划时计划的排序号
			@Size(max = 50)
			private Integer sortNum;
			//必填，履行计划的事项名称信息
			@NotNull
			@Size(max = 100)
			private String performItem;
			//必填，履行计划的日期
			@Size(max = 100)
			private String payDate;
			//非必填，计划提醒天数
			@Size(max = 50)
			private String reminderDay;
			//必填，履行计划金额
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
