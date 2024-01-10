package com.nimai.splan.payload;

public class CalculationValue 
{
	private Double minDue;
	private Double totalDue;
	private Double perTxnDue;
	private Double totalPayment;
	private Integer paymentCounter;

	private String email;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Double getMinDue() {
		return minDue;
	}
	public void setMinDue(Double minDue) {
		this.minDue = minDue;
	}
	public Double getTotalDue() {
		return totalDue;
	}
	public void setTotalDue(Double totalDue) {
		this.totalDue = totalDue;
	}
	public Double getPerTxnDue() {
		return perTxnDue;
	}
	public void setPerTxnDue(Double perTxnDue) {
		this.perTxnDue = perTxnDue;
	}
	public Double getTotalPayment() {
		return totalPayment;
	}
	public void setTotalPayment(Double totalPayment) {
		this.totalPayment = totalPayment;
	}
	public Integer getPaymentCounter() {
		return paymentCounter;
	}
	public void setPaymentCounter(Integer paymentCounter) {
		this.paymentCounter = paymentCounter;
	}
	
	
}
