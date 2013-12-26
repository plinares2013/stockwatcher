package com.google.gwt.sample.stockwatcher.server;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.gwt.sample.stockwatcher.client.NotLoggedInException;
import com.google.gwt.sample.stockwatcher.client.StockIndexService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class StockIndexServiceImpl extends RemoteServiceServlet implements
		StockIndexService {
	
	private static final Logger LOG = Logger.getLogger(StockServiceImpl.class.getName());
	
	boolean weightTableInitialized = false , criteriaTableInitialized = false;
	//TODO: look at closing the Persistence manager to free resources. Try/catch to handle this.
	PersistenceManager pm = PMF.get().getPersistenceManager();
	WeightTable weightTable = new WeightTable();
	CriteriaTable criteriaTable = new CriteriaTable();
	
	// The init() function is replaced by the scheduled task ScheduledInit.java
	//TODO: remove the init function below once the scheduled task is operational
	
	@Override
	public void init() {
		
	}
	
	@Override
	public String calculateWorth(String symbol) throws NotLoggedInException {
		
		//Check logged in and security to be handled. Program is called by client and by cron jobs
		UtilityClass.checkLoggedIn();
		Stock stock = new Stock();

		String sWorth = "";
		double worth = 0;
		double priceSalesPoints = 0, priceBookPoints = 0, profitMarginPoints = 0, ROEPoints = 0;
		double ROAPoints = 0, debtEquityPoints = 0, currentPoints = 0, oneYearEPSPoints = 0, quarterlyEPSPoints = 0;
		double managementOwnershipPoints = 0, quickRatioPoints = 0;
		
		// Read Stock data from data store
		// TODO: need to add protection checking the data is entitled to the user calling
		Query q = pm.newQuery (Stock.class);
		q.setFilter("symbol == ticker");
		q.declareParameters("String ticker");
		List<Stock> stocks = (List<Stock>) q.execute(symbol);
		
		for (Stock stockElem : stocks) {
			if (stockElem.getSymbol().equals(symbol)) {
				stock = stockElem;
				break;
			}
		}

		//Retrieve the worth index for the stock from data store
		
		worth = stock.getIndex();
		sWorth = Double.toString(worth);
		
		return (sWorth);
	}


}
