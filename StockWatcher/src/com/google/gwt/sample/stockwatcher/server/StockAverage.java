package com.google.gwt.sample.stockwatcher.server;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable (identityType = IdentityType.APPLICATION)
public class StockAverage {

		@PrimaryKey 
		@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
		private Long id;
		@Persistent
		private String symbol;
		@Persistent
		private Long averageRank;
		@Persistent
		private int numberAppearances;
		@Persistent
		private Date startDate;
		@Persistent
		private Date endDate;
		@Persistent
		private double averageIndex;
		
		public StockAverage() {
			return;
		}
		
		public String getSymbol() {
			return symbol;
		}
		public void setSymbol(String symbol) {
			this.symbol = symbol;
		}
		public Long getAverageRank() {
			return averageRank;
		}
		public void setAverageRank(Long averageRank) {
			this.averageRank = averageRank;
		}
		public int getNumberAppearances() {
			return numberAppearances;
		}
		public void setNumberAppearances(int numberAppearances) {
			this.numberAppearances = numberAppearances;
		}
		public Date getStartDate() {
			return startDate;
		}
		public void setStartDate(Date startDate) {
			this.startDate = startDate;
		}
		public Date getEndDate() {
			return endDate;
		}
		public void setEndDate(Date endDate) {
			this.endDate = endDate;
		}
		public double getAverageIndex() {
			return averageIndex;
		}
		public void setAverageIndex(double averageIndex) {
			this.averageIndex = averageIndex;
		}
}
