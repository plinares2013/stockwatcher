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
	private double EPSEstimateCurrentYear;
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
	
	
	
	
	
	
	public Stock() {
		this.createDate = new Date();
	}
	
	public Stock (User user, String symbol) {
		this();
		this.user = user;
		this.symbol = symbol;
	}
	
	public Stock (User user, String symbol, double price, double change, double percentChange,
					double priceSales, double priceBook, double EPSEstimateCurrentYear, double EPSEstimateNextYear,
					double PriceEstimateEPSCurrentYear, double PriceEstimateEPSNextYear, double PERatio, double PEGRatio) {
		this();
		this.user = user;
		this.symbol = symbol;
		this.price = price;
		this.change = change;
		this.percentChange = percentChange;
		this.priceSales = priceSales;
		this.priceBook = priceBook;
		this.EPSEstimateCurrentYear = EPSEstimateCurrentYear;
		this.EPSEstimateNextYear = EPSEstimateNextYear;
		this.PriceEstimateEPSCurrentYear = PriceEstimateEPSCurrentYear;
		this.PriceEstimateEPSNextYear = PriceEstimateEPSNextYear;
		this.PERatio = PERatio;
		this.PEGRatio = PEGRatio;
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
	
	public void setEPSEstimateCurrentYear (double EPSEstimateCurrentYear) {
		this.EPSEstimateCurrentYear = EPSEstimateCurrentYear;
	}
	
	public double getEPSEstimateCurrentYear () {
		return this.EPSEstimateCurrentYear;
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

}
