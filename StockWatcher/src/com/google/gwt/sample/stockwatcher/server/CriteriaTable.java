package com.google.gwt.sample.stockwatcher.server;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.users.User;

@PersistenceCapable (identityType = IdentityType.APPLICATION)
public class CriteriaTable {
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
	double priceSalesLevel1;
	@Persistent
	int priceSalesLevel1Points;
	@Persistent
	double priceSalesLevel2;
	@Persistent
	int priceSalesLevel2Points;
	@Persistent
	double priceSalesLevel3;
	@Persistent
	int priceSalesLevel3Points;
	@Persistent
	double priceSalesLevel4;
	@Persistent
	int priceSalesLevel4Points;
	
	@Persistent
	double priceBookLevel1;
	@Persistent
	int priceBookLevel1Points;
	@Persistent
	double priceBookLevel2;
	@Persistent
	int priceBookLevel2Points;
	@Persistent
	double priceBookLevel3;
	@Persistent
	int priceBookLevel3Points;
	@Persistent
	double priceBookLevel4;
	@Persistent
	int priceBookLevel4Points;
	
	@Persistent
	double ProfitMarginLevel1;
	@Persistent
	int ProfitMarginLevel1Points;
	@Persistent
	double ProfitMarginLevel2;
	@Persistent
	int ProfitMarginLevel2Points;
	@Persistent
	double ProfitMarginLevel3;
	@Persistent
	int ProfitMarginLevel3Points;
	@Persistent
	double ProfitMarginLevel4;
	@Persistent
	int ProfitMarginLevel4Points;	
	
	@Persistent
	double ROELevel1;
	@Persistent
	int ROELevel1Points;
	@Persistent
	double ROELevel2;
	@Persistent
	int ROELevel2Points;
	@Persistent
	double ROELevel3;
	@Persistent
	int ROELevel3Points;
	@Persistent
	double ROELevel4;
	@Persistent
	int ROELevel4Points;
	
	@Persistent
	double ROALevel1;
	@Persistent
	int ROALevel1Points;
	@Persistent
	double ROALevel2;
	@Persistent
	int ROALevel2Points;
	@Persistent
	double ROALevel3;
	@Persistent
	int ROALevel3Points;
	@Persistent
	double ROALevel4;
	@Persistent
	int ROALevel4Points;
	
	@Persistent
	double DebtEquityLevel1;
	@Persistent
	int DebtEquityLevel1Points;
	@Persistent
	double DebtEquityLevel2;
	@Persistent
	int DebtEquityLevel2Points;
	@Persistent
	double DebtEquityLevel3;
	@Persistent
	int DebtEquityLevel3Points;
	@Persistent
	double DebtEquityLevel4;
	@Persistent
	int DebtEquityLevel4Points;
	
	@Persistent
	double CurrentLevel1;
	@Persistent
	int CurrentLevel1Points;
	@Persistent
	double CurrentLevel2;
	@Persistent
	int CurrentLevel2Points;
	@Persistent
	double CurrentLevel3;
	@Persistent
	int CurrentLevel3Points;
	@Persistent
	double CurrentLevel4;
	@Persistent
	int CurrentLevel4Points;
	
	@Persistent
	double OneYearEPSLevel1;
	@Persistent
	int OneYearEPSLevel1Points;
	@Persistent
	double OneYearEPSLevel2;
	@Persistent
	int OneYearEPSLevel2Points;
	@Persistent
	double OneYearEPSLevel3;
	@Persistent
	int OneYearEPSLevel3Points;
	@Persistent
	double OneYearEPSLevel4;
	@Persistent
	int OneYearEPSLevel4Points;
	
	@Persistent
	double QuarterlyEPSLevel1;
	@Persistent
	int QuarterlyEPSLevel1Points;
	@Persistent
	double QuarterlyEPSLevel2;
	@Persistent
	int QuarterlyEPSLevel2Points;
	@Persistent
	double QuarterlyEPSLevel3;
	@Persistent
	int QuarterlyEPSLevel3Points;
	@Persistent
	double QuarterlyEPSLevel4;
	@Persistent
	int QuarterlyEPSLevel4Points;
	
	@Persistent
	double ManagementOwnershipLevel1;
	@Persistent
	int ManagementOwnershipLevel1Points;
	@Persistent
	double ManagementOwnershipLevel2;
	@Persistent
	int ManagementOwnershipLevel2Points;
	@Persistent
	double ManagementOwnershipLevel3;
	@Persistent
	int ManagementOwnershipLevel3Points;
	@Persistent
	double ManagementOwnershipLevel4;
	@Persistent
	int ManagementOwnershipLevel4Points;
	
	@Persistent
	double QuickRatioLevel1;
	@Persistent
	int QuickRatioLevel1Points;
	@Persistent
	double QuickRatioLevel2;
	@Persistent
	int QuickRatioLevel2Points;
	@Persistent
	double QuickRatioLevel3;
	@Persistent
	int QuickRatioLevel3Points;
	@Persistent
	double QuickRatioLevel4;
	@Persistent
	int QuickRatioLevel4Points;
	
	public  CriteriaTable () {
	this.createDate = new Date();	
	}
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public boolean isInitialized() {
		return initialized;
	}
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
	public double getPriceSalesLevel1() {
		return priceSalesLevel1;
	}
	public void setPriceSalesLevel1(double priceSalesLevel1) {
		this.priceSalesLevel1 = priceSalesLevel1;
	}
	public int getPriceSalesLevel1Points() {
		return priceSalesLevel1Points;
	}
	public void setPriceSalesLevel1Points(int priceSalesLevel1Points) {
		this.priceSalesLevel1Points = priceSalesLevel1Points;
	}
	public double getPriceSalesLevel2() {
		return priceSalesLevel2;
	}
	public void setPriceSalesLevel2(double priceSalesLevel2) {
		this.priceSalesLevel2 = priceSalesLevel2;
	}
	public int getPriceSalesLevel2Points() {
		return priceSalesLevel2Points;
	}
	public void setPriceSalesLevel2Points(int priceSalesLevel2Points) {
		this.priceSalesLevel2Points = priceSalesLevel2Points;
	}
	public double getPriceSalesLevel3() {
		return priceSalesLevel3;
	}
	public void setPriceSalesLevel3(double priceSalesLevel3) {
		this.priceSalesLevel3 = priceSalesLevel3;
	}
	public int getPriceSalesLevel3Points() {
		return priceSalesLevel3Points;
	}
	public void setPriceSalesLevel3Points(int priceSalesLevel3Points) {
		this.priceSalesLevel3Points = priceSalesLevel3Points;
	}
	public double getPriceSalesLevel4() {
		return priceSalesLevel4;
	}
	public void setPriceSalesLevel4(double priceSalesLevel4) {
		this.priceSalesLevel4 = priceSalesLevel4;
	}
	public int getPriceSalesLevel4Points() {
		return priceSalesLevel4Points;
	}
	public void setPriceSalesLevel4Points(int priceSalesLevel4Points) {
		this.priceSalesLevel4Points = priceSalesLevel4Points;
	}
	public double getPriceBookLevel1() {
		return priceBookLevel1;
	}
	public void setPriceBookLevel1(double priceBookLevel1) {
		this.priceBookLevel1 = priceBookLevel1;
	}
	public int getPriceBookLevel1Points() {
		return priceBookLevel1Points;
	}
	public void setPriceBookLevel1Points(int priceBookLevel1Points) {
		this.priceBookLevel1Points = priceBookLevel1Points;
	}
	public double getPriceBookLevel2() {
		return priceBookLevel2;
	}
	public void setPriceBookLevel2(double priceBookLevel2) {
		this.priceBookLevel2 = priceBookLevel2;
	}
	public int getPriceBookLevel2Points() {
		return priceBookLevel2Points;
	}
	public void setPriceBookLevel2Points(int priceBookLevel2Points) {
		this.priceBookLevel2Points = priceBookLevel2Points;
	}
	public double getPriceBookLevel3() {
		return priceBookLevel3;
	}
	public void setPriceBookLevel3(double priceBookLevel3) {
		this.priceBookLevel3 = priceBookLevel3;
	}
	public int getPriceBookLevel3Points() {
		return priceBookLevel3Points;
	}
	public void setPriceBookLevel3Points(int priceBookLevel3Points) {
		this.priceBookLevel3Points = priceBookLevel3Points;
	}
	public double getPriceBookLevel4() {
		return priceBookLevel4;
	}
	public void setPriceBookLevel4(double priceBookLevel4) {
		this.priceBookLevel4 = priceBookLevel4;
	}
	public int getPriceBookLevel4Points() {
		return priceBookLevel4Points;
	}
	public void setPriceBookLevel4Points(int priceBookLevel4Points) {
		this.priceBookLevel4Points = priceBookLevel4Points;
	}

	public double getProfitMarginLevel1() {
		return ProfitMarginLevel1;
	}

	public void setProfitMarginLevel1(double profitMarginLevel1) {
		ProfitMarginLevel1 = profitMarginLevel1;
	}

	public int getProfitMarginLevel1Points() {
		return ProfitMarginLevel1Points;
	}

	public void setProfitMarginLevel1Points(int profitMarginLevel1Points) {
		ProfitMarginLevel1Points = profitMarginLevel1Points;
	}

	public double getProfitMarginLevel2() {
		return ProfitMarginLevel2;
	}

	public void setProfitMarginLevel2(double profitMarginLevel2) {
		ProfitMarginLevel2 = profitMarginLevel2;
	}

	public int getProfitMarginLevel2Points() {
		return ProfitMarginLevel2Points;
	}

	public void setProfitMarginLevel2Points(int profitMarginLevel2Points) {
		ProfitMarginLevel2Points = profitMarginLevel2Points;
	}

	public double getProfitMarginLevel3() {
		return ProfitMarginLevel3;
	}

	public void setProfitMarginLevel3(double profitMarginLevel3) {
		ProfitMarginLevel3 = profitMarginLevel3;
	}

	public int getProfitMarginLevel3Points() {
		return ProfitMarginLevel3Points;
	}

	public void setProfitMarginLevel3Points(int profitMarginLevel3Points) {
		ProfitMarginLevel3Points = profitMarginLevel3Points;
	}

	public double getProfitMarginLevel4() {
		return ProfitMarginLevel4;
	}

	public void setProfitMarginLevel4(double profitMarginLevel4) {
		ProfitMarginLevel4 = profitMarginLevel4;
	}

	public int getProfitMarginLevel4Points() {
		return ProfitMarginLevel4Points;
	}

	public void setProfitMarginLevel4Points(int profitMarginLevel4Points) {
		ProfitMarginLevel4Points = profitMarginLevel4Points;
	}

	public double getROELevel1() {
		return ROELevel1;
	}

	public void setROELevel1(double rOELevel1) {
		ROELevel1 = rOELevel1;
	}

	public int getROELevel1Points() {
		return ROELevel1Points;
	}

	public void setROELevel1Points(int rOELevel1Points) {
		ROELevel1Points = rOELevel1Points;
	}

	public double getROELevel2() {
		return ROELevel2;
	}

	public void setROELevel2(double rOELevel2) {
		ROELevel2 = rOELevel2;
	}

	public int getROELevel2Points() {
		return ROELevel2Points;
	}

	public void setROELevel2Points(int rOELevel2Points) {
		ROELevel2Points = rOELevel2Points;
	}

	public double getROELevel3() {
		return ROELevel3;
	}

	public void setROELevel3(double rOELevel3) {
		ROELevel3 = rOELevel3;
	}

	public int getROELevel3Points() {
		return ROELevel3Points;
	}

	public void setROELevel3Points(int rOELevel3Points) {
		ROELevel3Points = rOELevel3Points;
	}

	public double getROELevel4() {
		return ROELevel4;
	}

	public void setROELevel4(double rOELevel4) {
		ROELevel4 = rOELevel4;
	}

	public int getROELevel4Points() {
		return ROELevel4Points;
	}

	public void setROELevel4Points(int rOELevel4Points) {
		ROELevel4Points = rOELevel4Points;
	}

	public double getROALevel1() {
		return ROALevel1;
	}

	public void setROALevel1(double rOALevel1) {
		ROALevel1 = rOALevel1;
	}

	public int getROALevel1Points() {
		return ROALevel1Points;
	}

	public void setROALevel1Points(int rOALevel1Points) {
		ROALevel1Points = rOALevel1Points;
	}

	public double getROALevel2() {
		return ROALevel2;
	}

	public void setROALevel2(double rOALevel2) {
		ROALevel2 = rOALevel2;
	}

	public int getROALevel2Points() {
		return ROALevel2Points;
	}

	public void setROALevel2Points(int rOALevel2Points) {
		ROALevel2Points = rOALevel2Points;
	}

	public double getROALevel3() {
		return ROALevel3;
	}

	public void setROALevel3(double rOALevel3) {
		ROALevel3 = rOALevel3;
	}

	public int getROALevel3Points() {
		return ROALevel3Points;
	}

	public void setROALevel3Points(int rOALevel3Points) {
		ROALevel3Points = rOALevel3Points;
	}

	public double getROALevel4() {
		return ROALevel4;
	}

	public void setROALevel4(double rOALevel4) {
		ROALevel4 = rOALevel4;
	}

	public int getROALevel4Points() {
		return ROALevel4Points;
	}

	public void setROALevel4Points(int rOALevel4Points) {
		ROALevel4Points = rOALevel4Points;
	}

	public double getDebtEquityLevel1() {
		return DebtEquityLevel1;
	}

	public void setDebtEquityLevel1(double debtEquityLevel1) {
		DebtEquityLevel1 = debtEquityLevel1;
	}

	public int getDebtEquityLevel1Points() {
		return DebtEquityLevel1Points;
	}

	public void setDebtEquityLevel1Points(int debtEquityLevel1Points) {
		DebtEquityLevel1Points = debtEquityLevel1Points;
	}

	public double getDebtEquityLevel2() {
		return DebtEquityLevel2;
	}

	public void setDebtEquityLevel2(double debtEquityLevel2) {
		DebtEquityLevel2 = debtEquityLevel2;
	}

	public int getDebtEquityLevel2Points() {
		return DebtEquityLevel2Points;
	}

	public void setDebtEquityLevel2Points(int debtEquityLevel2Points) {
		DebtEquityLevel2Points = debtEquityLevel2Points;
	}

	public double getDebtEquityLevel3() {
		return DebtEquityLevel3;
	}

	public void setDebtEquityLevel3(double debtEquityLevel3) {
		DebtEquityLevel3 = debtEquityLevel3;
	}

	public int getDebtEquityLevel3Points() {
		return DebtEquityLevel3Points;
	}

	public void setDebtEquityLevel3Points(int debtEquityLevel3Points) {
		DebtEquityLevel3Points = debtEquityLevel3Points;
	}

	public double getDebtEquityLevel4() {
		return DebtEquityLevel4;
	}

	public void setDebtEquityLevel4(double debtEquityLevel4) {
		DebtEquityLevel4 = debtEquityLevel4;
	}

	public int getDebtEquityLevel4Points() {
		return DebtEquityLevel4Points;
	}

	public void setDebtEquityLevel4Points(int debtEquityLevel4Points) {
		DebtEquityLevel4Points = debtEquityLevel4Points;
	}

	public double getCurrentLevel1() {
		return CurrentLevel1;
	}

	public void setCurrentLevel1(double currentLevel1) {
		CurrentLevel1 = currentLevel1;
	}

	public int getCurrentLevel1Points() {
		return CurrentLevel1Points;
	}

	public void setCurrentLevel1Points(int currentLevel1Points) {
		CurrentLevel1Points = currentLevel1Points;
	}

	public double getCurrentLevel2() {
		return CurrentLevel2;
	}

	public void setCurrentLevel2(double currentLevel2) {
		CurrentLevel2 = currentLevel2;
	}

	public int getCurrentLevel2Points() {
		return CurrentLevel2Points;
	}

	public void setCurrentLevel2Points(int currentLevel2Points) {
		CurrentLevel2Points = currentLevel2Points;
	}

	public double getCurrentLevel3() {
		return CurrentLevel3;
	}

	public void setCurrentLevel3(double currentLevel3) {
		CurrentLevel3 = currentLevel3;
	}

	public int getCurrentLevel3Points() {
		return CurrentLevel3Points;
	}

	public void setCurrentLevel3Points(int currentLevel3Points) {
		CurrentLevel3Points = currentLevel3Points;
	}

	public double getCurrentLevel4() {
		return CurrentLevel4;
	}

	public void setCurrentLevel4(double currentLevel4) {
		CurrentLevel4 = currentLevel4;
	}

	public int getCurrentLevel4Points() {
		return CurrentLevel4Points;
	}

	public void setCurrentLevel4Points(int currentLevel4Points) {
		CurrentLevel4Points = currentLevel4Points;
	}

	public double getOneYearEPSLevel1() {
		return OneYearEPSLevel1;
	}

	public void setOneYearEPSLevel1(double oneYearEPSLevel1) {
		OneYearEPSLevel1 = oneYearEPSLevel1;
	}

	public int getOneYearEPSLevel1Points() {
		return OneYearEPSLevel1Points;
	}

	public void setOneYearEPSLevel1Points(int oneYearEPSLevel1Points) {
		OneYearEPSLevel1Points = oneYearEPSLevel1Points;
	}

	public double getOneYearEPSLevel2() {
		return OneYearEPSLevel2;
	}

	public void setOneYearEPSLevel2(double oneYearEPSLevel2) {
		OneYearEPSLevel2 = oneYearEPSLevel2;
	}

	public int getOneYearEPSLevel2Points() {
		return OneYearEPSLevel2Points;
	}

	public void setOneYearEPSLevel2Points(int oneYearEPSLevel2Points) {
		OneYearEPSLevel2Points = oneYearEPSLevel2Points;
	}

	public double getOneYearEPSLevel3() {
		return OneYearEPSLevel3;
	}

	public void setOneYearEPSLevel3(double oneYearEPSLevel3) {
		OneYearEPSLevel3 = oneYearEPSLevel3;
	}

	public int getOneYearEPSLevel3Points() {
		return OneYearEPSLevel3Points;
	}

	public void setOneYearEPSLevel3Points(int oneYearEPSLevel3Points) {
		OneYearEPSLevel3Points = oneYearEPSLevel3Points;
	}

	public double getOneYearEPSLevel4() {
		return OneYearEPSLevel4;
	}

	public void setOneYearEPSLevel4(double oneYearEPSLevel4) {
		OneYearEPSLevel4 = oneYearEPSLevel4;
	}

	public int getOneYearEPSLevel4Points() {
		return OneYearEPSLevel4Points;
	}

	public void setOneYearEPSLevel4Points(int oneYearEPSLevel4Points) {
		OneYearEPSLevel4Points = oneYearEPSLevel4Points;
	}

	public double getQuarterlyEPSLevel1() {
		return QuarterlyEPSLevel1;
	}

	public void setQuarterlyEPSLevel1(double quarterlyEPSLevel1) {
		QuarterlyEPSLevel1 = quarterlyEPSLevel1;
	}

	public int getQuarterlyEPSLevel1Points() {
		return QuarterlyEPSLevel1Points;
	}

	public void setQuarterlyEPSLevel1Points(int quarterlyEPSLevel1Points) {
		QuarterlyEPSLevel1Points = quarterlyEPSLevel1Points;
	}

	public double getQuarterlyEPSLevel2() {
		return QuarterlyEPSLevel2;
	}

	public void setQuarterlyEPSLevel2(double quarterlyEPSLevel2) {
		QuarterlyEPSLevel2 = quarterlyEPSLevel2;
	}

	public int getQuarterlyEPSLevel2Points() {
		return QuarterlyEPSLevel2Points;
	}

	public void setQuarterlyEPSLevel2Points(int quarterlyEPSLevel2Points) {
		QuarterlyEPSLevel2Points = quarterlyEPSLevel2Points;
	}

	public double getQuarterlyEPSLevel3() {
		return QuarterlyEPSLevel3;
	}

	public void setQuarterlyEPSLevel3(double quarterlyEPSLevel3) {
		QuarterlyEPSLevel3 = quarterlyEPSLevel3;
	}

	public int getQuarterlyEPSLevel3Points() {
		return QuarterlyEPSLevel3Points;
	}

	public void setQuarterlyEPSLevel3Points(int quarterlyEPSLevel3Points) {
		QuarterlyEPSLevel3Points = quarterlyEPSLevel3Points;
	}

	public double getQuarterlyEPSLevel4() {
		return QuarterlyEPSLevel4;
	}

	public void setQuarterlyEPSLevel4(double quarterlyEPSLevel4) {
		QuarterlyEPSLevel4 = quarterlyEPSLevel4;
	}

	public int getQuarterlyEPSLevel4Points() {
		return QuarterlyEPSLevel4Points;
	}

	public void setQuarterlyEPSLevel4Points(int quarterlyEPSLevel4Points) {
		QuarterlyEPSLevel4Points = quarterlyEPSLevel4Points;
	}

	public double getManagementOwnershipLevel1() {
		return ManagementOwnershipLevel1;
	}

	public void setManagementOwnershipLevel1(double managementOwnershipLevel1) {
		ManagementOwnershipLevel1 = managementOwnershipLevel1;
	}

	public int getManagementOwnershipLevel1Points() {
		return ManagementOwnershipLevel1Points;
	}

	public void setManagementOwnershipLevel1Points(
			int managementOwnershipLevel1Points) {
		ManagementOwnershipLevel1Points = managementOwnershipLevel1Points;
	}

	public double getManagementOwnershipLevel2() {
		return ManagementOwnershipLevel2;
	}

	public void setManagementOwnershipLevel2(double managementOwnershipLevel2) {
		ManagementOwnershipLevel2 = managementOwnershipLevel2;
	}

	public int getManagementOwnershipLevel2Points() {
		return ManagementOwnershipLevel2Points;
	}

	public void setManagementOwnershipLevel2Points(
			int managementOwnershipLevel2Points) {
		ManagementOwnershipLevel2Points = managementOwnershipLevel2Points;
	}

	public double getManagementOwnershipLevel3() {
		return ManagementOwnershipLevel3;
	}

	public void setManagementOwnershipLevel3(double managementOwnershipLevel3) {
		ManagementOwnershipLevel3 = managementOwnershipLevel3;
	}

	public int getManagementOwnershipLevel3Points() {
		return ManagementOwnershipLevel3Points;
	}

	public void setManagementOwnershipLevel3Points(
			int managementOwnershipLevel3Points) {
		ManagementOwnershipLevel3Points = managementOwnershipLevel3Points;
	}

	public double getManagementOwnershipLevel4() {
		return ManagementOwnershipLevel4;
	}

	public void setManagementOwnershipLevel4(double managementOwnershipLevel4) {
		ManagementOwnershipLevel4 = managementOwnershipLevel4;
	}

	public int getManagementOwnershipLevel4Points() {
		return ManagementOwnershipLevel4Points;
	}

	public void setManagementOwnershipLevel4Points(
			int managementOwnershipLevel4Points) {
		ManagementOwnershipLevel4Points = managementOwnershipLevel4Points;
	}

	public double getQuickRatioLevel1() {
		return QuickRatioLevel1;
	}

	public void setQuickRatioLevel1(double quickRatioLevel1) {
		QuickRatioLevel1 = quickRatioLevel1;
	}

	public int getQuickRatioLevel1Points() {
		return QuickRatioLevel1Points;
	}

	public void setQuickRatioLevel1Points(int quickRatioLevel1Points) {
		QuickRatioLevel1Points = quickRatioLevel1Points;
	}

	public double getQuickRatioLevel2() {
		return QuickRatioLevel2;
	}

	public void setQuickRatioLevel2(double quickRatioLevel2) {
		QuickRatioLevel2 = quickRatioLevel2;
	}

	public int getQuickRatioLevel2Points() {
		return QuickRatioLevel2Points;
	}

	public void setQuickRatioLevel2Points(int quickRatioLevel2Points) {
		QuickRatioLevel2Points = quickRatioLevel2Points;
	}

	public double getQuickRatioLevel3() {
		return QuickRatioLevel3;
	}

	public void setQuickRatioLevel3(double quickRatioLevel3) {
		QuickRatioLevel3 = quickRatioLevel3;
	}

	public int getQuickRatioLevel3Points() {
		return QuickRatioLevel3Points;
	}

	public void setQuickRatioLevel3Points(int quickRatioLevel3Points) {
		QuickRatioLevel3Points = quickRatioLevel3Points;
	}

	public double getQuickRatioLevel4() {
		return QuickRatioLevel4;
	}

	public void setQuickRatioLevel4(double quickRatioLevel4) {
		QuickRatioLevel4 = quickRatioLevel4;
	}

	public int getQuickRatioLevel4Points() {
		return QuickRatioLevel4Points;
	}

	public void setQuickRatioLevel4Points(int quickRatioLevel4Points) {
		QuickRatioLevel4Points = quickRatioLevel4Points;
	}
	
}
