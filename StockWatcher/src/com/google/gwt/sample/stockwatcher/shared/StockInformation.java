package com.google.gwt.sample.stockwatcher.shared;

import java.io.Serializable;

public class StockInformation implements Serializable {

		  private String symbol;
		  private double price;
		  private double change;
		  private double percentchange;
		  private double index;

		  public StockInformation() {
		  }

		  public StockInformation(String symbol, double price, double change, double percentchange) {
		    this.symbol = symbol;
		    this.price = price;
		    this.change = change;
		    this.percentchange = percentchange;
		  }

		  public String getSymbol() {
		    return this.symbol;
		  }

		  public double getPrice() {
		    return this.price;
		  }

		  public double getChange() {
		    return this.change;
		  }

		  public double getChangePercent() {
		    return this.percentchange;
		  }

		  public void setSymbol(String symbol) {
		    this.symbol = symbol;
		  }

		  public void setPrice(double price) {
		    this.price = price;
		  }

		  public void setChange(double change) {
		    this.change = change;
		  }
		  
		  public void setPercentChange(double percentchange) {
			  this.percentchange = percentchange;
		  }

		public double getIndex() {
			return index;
		}

		public void setIndex(double index) {
			this.index = index;
		}

}
