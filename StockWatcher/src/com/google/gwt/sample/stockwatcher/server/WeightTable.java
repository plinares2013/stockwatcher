package com.google.gwt.sample.stockwatcher.server;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.users.User;

@PersistenceCapable (identityType = IdentityType.APPLICATION)
public class WeightTable {
	
	@PrimaryKey 
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	@Persistent
	private User user;
	@Persistent
	private Date createDate;
	@Persistent
	private boolean initialized;
	@Persistent
	private double PriceSalesWeight;
	@Persistent
	private double PriceBookWeight;
	@Persistent
	private double ProfitMarginWeight;
	@Persistent
	private double ROEWeight;
	@Persistent
	private double ROAWeight;
	@Persistent
	private double DebtEquityWeight;
	@Persistent
	private double QuickWeight;
	@Persistent
	private double CurrentWeight;
	@Persistent
	private double OneYearEPSWeight;
	@Persistent
	private double ThreeYearEPSWeight;
	@Persistent
	private double FiveYearEPSWeight;
	@Persistent
	private double FreeCashFlowPerShareWeight;
	@Persistent
	private double ManagementOwnershipWeight;
	
	@Persistent
	private double QuarterlyEPSWeight;
	
	@Persistent
	private double PEGRatioWeight;
	@Persistent
	private double EPSQuarterlyGrowthYoYWeight;
	@Persistent
	private double PERatioWeight;

	
	
	public WeightTable() {
		this.createDate = new Date();
	}
	
	public WeightTable (User user, boolean initialized, double priceSalesWeight, double priceBookWeight, double profitMarginWeight,
			double ROEWeight, double ROAWeight, double debtOverEquityWeight, double quickRatioWeight,
			double currentRatioWeight, double EPSEstimateNextQuarterWeight, double oneYearEPSWeight, double threeYearEPSWeight,
			double fiveYearEPSWeight, double freeCashFlowPerShareWeight, double managementOwnershipWeight, 
			double quarterlyEPSWeight) {
		
		this();
		this.user = user;
		this.initialized = initialized;
		this.PriceSalesWeight = priceSalesWeight;
		this.PriceBookWeight = priceBookWeight;
		this.ProfitMarginWeight = profitMarginWeight;
		this.ROEWeight = ROEWeight;
		this.ROAWeight = ROAWeight;
		this.DebtEquityWeight = debtOverEquityWeight;
		this.QuickWeight = quickRatioWeight;
		this.CurrentWeight = currentRatioWeight;
		this.OneYearEPSWeight = oneYearEPSWeight;
		this.ThreeYearEPSWeight = threeYearEPSWeight;
		this.FiveYearEPSWeight = fiveYearEPSWeight;
		this.FreeCashFlowPerShareWeight = freeCashFlowPerShareWeight;
		this.ManagementOwnershipWeight = managementOwnershipWeight;
		this.QuarterlyEPSWeight = quarterlyEPSWeight;
		
	}
	
	public User getUser() {
		return this.user;
	}
	
	public void setUser(User user ) {
		this.user = user;
	}
	
	public boolean isInitialized () {
		return this.initialized;
	}
	
	public void setInitialized (boolean initialized) {
		this.initialized = initialized;
	}

	public double getPriceSalesWeight() {
		return PriceSalesWeight;
	}

	public void setPriceSalesWeight(double priceSalesWeight) {
		PriceSalesWeight = priceSalesWeight;
	}

	public double getPriceBookWeight() {
		return PriceBookWeight;
	}

	public void setPriceBookWeight(double priceBookWeight) {
		PriceBookWeight = priceBookWeight;
	}

	public double getProfitMarginWeight() {
		return ProfitMarginWeight;
	}

	public void setProfitMarginWeight(double profitMarginWeight) {
		ProfitMarginWeight = profitMarginWeight;
	}

	public double getROEWeight() {
		return ROEWeight;
	}

	public void setROEWeight(double rOEWeight) {
		ROEWeight = rOEWeight;
	}

	public double getROAWeight() {
		return ROAWeight;
	}

	public void setROAWeight(double rOAWeight) {
		ROAWeight = rOAWeight;
	}

	public double getOneYearEPSWeight() {
		return OneYearEPSWeight;
	}

	public void setOneYearEPSWeight(double oneYearEPSWeight) {
		OneYearEPSWeight = oneYearEPSWeight;
	}

	public double getThreeYearEPSWeight() {
		return ThreeYearEPSWeight;
	}

	public void setThreeYearEPSWeight(double threeYearEPSWeight) {
		ThreeYearEPSWeight = threeYearEPSWeight;
	}

	public double getFiveYearEPSWeight() {
		return FiveYearEPSWeight;
	}

	public void setFiveYearEPSWeight(double fiveYearEPSWeight) {
		FiveYearEPSWeight = fiveYearEPSWeight;
	}

	public double getFreeCashFlowPerShareWeight() {
		return FreeCashFlowPerShareWeight;
	}

	public void setFreeCashFlowPerShareWeight(double freeCashFlowPerShareWeight) {
		FreeCashFlowPerShareWeight = freeCashFlowPerShareWeight;
	}

	public double getManagementOwnershipWeight() {
		return ManagementOwnershipWeight;
	}

	public void setManagementOwnershipWeight(double managementOwnershipWeight) {
		ManagementOwnershipWeight = managementOwnershipWeight;
	}

	public double getDebtEquityWeight() {
		return DebtEquityWeight;
	}

	public void setDebtEquityWeight(double debtEquityWeight) {
		DebtEquityWeight = debtEquityWeight;
	}

	public double getQuickWeight() {
		return QuickWeight;
	}

	public void setQuickWeight(double quickWeight) {
		QuickWeight = quickWeight;
	}

	public double getCurrentWeight() {
		return CurrentWeight;
	}

	public void setCurrentWeight(double currentWeight) {
		CurrentWeight = currentWeight;
	}

	public double getQuarterlyEPSWeight() {
		return QuarterlyEPSWeight;
	}

	public void setQuarterlyEPSWeight(double quarterlyEPSWeight) {
		QuarterlyEPSWeight = quarterlyEPSWeight;
	}

	public double getPEGRatioWeight() {
		return PEGRatioWeight;
	}

	public void setPEGRatioWeight(double pEGRatioWeight) {
		PEGRatioWeight = pEGRatioWeight;
	}

	public double getEPSQuarterlyGrowthYoYWeight() {
		return EPSQuarterlyGrowthYoYWeight;
	}

	public void setEPSQuarterlyGrowthYoYWeight(double ePSQuarterlyGrowthYoYWeight) {
		EPSQuarterlyGrowthYoYWeight = ePSQuarterlyGrowthYoYWeight;
	}

	public double getPERatioWeight() {
		return PERatioWeight;
	}

	public void setPERatioWeight(double pERatioWeight) {
		PERatioWeight = pERatioWeight;
	}
	
	
}
