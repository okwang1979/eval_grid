package nc.vo.ct.saledaily.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

import javax.validation.constraints.NotNull;

import nc.vo.pub.lang.UFDouble;

public class PaymentPlanAndFeedbackInfo implements Serializable{
		//合同唯一标识
		@NotNull
		@Size(max = 50)
		private String contractUniqueId;
		@NotNull
		@Size(max = 50)
		private String sourceInfo;
		
		private List<PaymentPlan> paymentPlanList;
		
		private List<PaymentFeedback> paymentFeedbackList;
		
		
		public PaymentPlanAndFeedbackInfo() {
			
		}


		public String getContractUniqueId() {
			return contractUniqueId;
		}


		public void setContractUniqueId(String contractUniqueId) {
			this.contractUniqueId = contractUniqueId;
		}


		public String getSourceInfo() {
			return sourceInfo;
		}


		public void setSourceInfo(String sourceInfo) {
			this.sourceInfo = sourceInfo;
		}


		public List<PaymentPlan> getPaymentPlanList() {
			return paymentPlanList;
		}


		public void setPaymentPlanList(List<PaymentPlan> paymentPlanList) {
			this.paymentPlanList = paymentPlanList;
		}


		public List<PaymentFeedback> getPaymentFeedbackList() {
			return paymentFeedbackList;
		}


		public void setPaymentFeedbackList(List<PaymentFeedback> paymentFeedbackList) {
			this.paymentFeedbackList = paymentFeedbackList;
		}

        		
	}

