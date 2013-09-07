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
	
	
	public Stock() {
		this.createDate = new Date();
	}
	
	public Stock (User user, String symbol) {
		this();
		this.user = user;
		this.symbol = symbol;
	}
	
	public Stock (User user, String symbol, double price, double change, double percentChange) {
		this();
		this.user = user;
		this.symbol = symbol;
		this.price = price;
		this.change = change;
		this.percentChange = percentChange;
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
	
	public void setPercentChange (double percentChange) {
		this.percentChange = percentChange;
	}
	
	public double getPercentChange () {
		return this.percentChange;
	}
}
