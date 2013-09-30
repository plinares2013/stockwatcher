package com.google.gwt.sample.stockwatcher.server;

import java.util.Date;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.IdGeneratorStrategy;

import com.google.appengine.api.users.User;

@PersistenceCapable (identityType = IdentityType.APPLICATION)
public class Stock {
	
	@PrimaryKey 
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	@Persistent
	private User user;
	@Persistent
	private String symbol;
	@Persistent
	private Date createDate;
	@Persistent
	private double price;
	@Persistent
	private double change;
	@Persistent
	private double percentChange;
	@Persistent
	private double priceSales;
	@Persistent
	private double priceBook;
	@Persistent
	private double oneYearEPS;
	@Persistent
	private double EPSEstimateNextYear;
	@Persistent
	private double PriceEstimateEPSCurrentYear;
	@Persistent
	private double PriceEstimateEPSNextYear;
	@Persistent
	private double PERatio;
	@Persistent
	private double PEGRatio;
	
	@Persistent
	private double ProfitMargin;
	@Persistent
	private double ROA;
	@Persistent
	private double ROE;
	@Persistent
	private double DebtEquity;
	@Persistent
	private double Current;
	@Persistent
	private double ManagementOwnership;
	@Persistent
	private double EPSEstimateNextQuarter;
	@Persistent
	private double quarterlyEPS;
	
	
	public Stock() {
		this.createDate = new Date();
	}
	
	public Stock (User user, String symbol) {
		this();
		this.user = user;
		this.symbol = symbol;
	}
	
	public Stock (User user, String symbol, double price, double change, double percentChange,
					double priceSales, double priceBook, double oneYearEPS, double EPSEstimateNextYear,
					double PriceEstimateEPSCurrentYear, double PriceEstimateEPSNextYear, double PERatio, 
					double PEGRatio, double quarterlyEPS) {
		this();
		this.user = user;
		this.symbol = symbol;
		this.price = price;
		this.change = change;
		this.percentChange = percentChange;
		this.priceSales = priceSales;
		this.priceBook = priceBook;
		this.oneYearEPS = oneYearEPS;
		this.EPSEstimateNextYear = EPSEstimateNextYear;
		this.PriceEstimateEPSCurrentYear = PriceEstimateEPSCurrentYear;
		this.PriceEstimateEPSNextYear = PriceEstimateEPSNextYear;
		this.PERatio = PERatio;
		this.PEGRatio = PEGRatio;
		this.quarterlyEPS = quarterlyEPS;
	}
	
	public Long getId() {
		return this.id ;
	}

	public String getSymbol() {
		return this.symbol ;
	}
	
	public User getUser() {
		return this.user ;
	}
	
	public Date getCreateDate () {
		return this.createDate;
	}
	
	public void setSymbol (String symbol) {
		this.symbol = symbol ;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public void setPrice (double price) {
		this.price = price;
	}
	
	public double getPrice() {
		return this.price;
	}
	
	public void setChange (double change) {
		this.change = change;
	}
	
	public double getChange () {
		return (change);
	}
	
	public void setPercentChange (double percentChange) {
		this.percentChange = percentChange;
	}
	
	public double getPercentChange () {
		return this.percentChange;
	}
	
	public void setPriceSales (double priceSales) {
		this.priceSales = priceSales;
	}
	
	public double getPriceSales () {
		return this.priceSales;
	}
	
	public void setPriceBook (double priceBook) {
		this.priceBook = priceBook;
	}
	
	public double getPriceBook () {
		return this.priceBook;
	}
	
	public void setEPSEstimateNextYear (double EPSEstimateNextYear) {
		this.EPSEstimateNextYear = EPSEstimateNextYear;
	}
	
	public double getEPSEstimateNextYear () {
		return this.EPSEstimateNextYear;
	}
	
	public void setPriceEstimateEPSCurrentYear (double PriceEstimateEPSCurrentYear) {
		this.PriceEstimateEPSCurrentYear = PriceEstimateEPSCurrentYear;
	}
	
	public double getPriceEstimateEPSCurrentYear () {
		return this.PriceEstimateEPSCurrentYear;
	}
	
	public void setPriceEstimateEPSNextYear (double PriceEstimateEPSNextYear) {
		this.PriceEstimateEPSNextYear = PriceEstimateEPSNextYear;
	}
	
	public double getPriceEstimateEPSNextYear () {
		return this.PriceEstimateEPSNextYear;
	}
	
	public void setPERatio (double PERatio) {
		this.PERatio = PERatio;
	}
	
	public double getPERatio () {
		return this.PERatio;
	}
	
	public void setPEGRatio (double PEGRatio) {
		this.PEGRatio = PEGRatio;
	}
	
	public double getPEGRatio () {
		return this.PEGRatio;
	}

	public double getProfitMargin() {
		return ProfitMargin;
	}

	public void setProfitMargin(double profitMargin) {
		ProfitMargin = profitMargin;
	}

	public double getEPSEstimateNextQuarter() {
		return EPSEstimateNextQuarter;
	}

	public void setEPSEstimateNextQuarter(double ePSEstimateNextQuarter) {
		EPSEstimateNextQuarter = ePSEstimateNextQuarter;
	}

	public double getROA() {
		return ROA;
	}

	public void setROA(double rOA) {
		ROA = rOA;
	}

	public double getROE() {
		return ROE;
	}

	public void setROE(double rOE) {
		ROE = rOE;
	}

	public double getDebtEquity() {
		return DebtEquity;
	}

	public void setDebtEquity(double debtEquity) {
		DebtEquity = debtEquity;
	}

	public double getCurrent() {
		return Current;
	}

	public void setCurrent(double current) {
		Current = current;
	}

	public double getManagementOwnership() {
		return ManagementOwnership;
	}

	public void setManagementOwnership(double managementOwnership) {
		ManagementOwnership = managementOwnership;
	}

	public double getOneYearEPS() {
		return oneYearEPS;
	}

	public void setOneYearEPS(double oneYearEPS) {
		this.oneYearEPS = oneYearEPS;
	}

	public double getQuarterlyEPS() {
		return quarterlyEPS;
	}

	public void setQuarterlyEPS(double quarterlyEPS) {
		this.quarterlyEPS = quarterlyEPS;
	}

}
