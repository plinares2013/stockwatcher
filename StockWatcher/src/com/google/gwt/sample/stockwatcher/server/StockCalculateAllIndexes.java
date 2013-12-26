package com.google.gwt.sample.stockwatcher.server;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.datanucleus.query.JDOCursorHelper;

public class StockCalculateAllIndexes extends HttpServlet {
	
	private static final Logger LOG = Logger.getLogger(StockServiceImpl.class.getName());
	int rangeIndex = UtilityClass.getRangeIndex();   // Number of indexes calculated by 1 task
//	int rangeIndex = 3;
	int bestNStocks = UtilityClass.getTopNStocks();
	
	TaskHandle handle = null;
	Queue defaultQueue = QueueFactory.getDefaultQueue();
	
	WeightTable weightTable = new WeightTable();
	CriteriaTable criteriaTable = new CriteriaTable();
	PersistenceManager pm = PMF.get().getPersistenceManager();
	
	public void doGet (HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		boolean weightTableInitialized = false , criteriaTableInitialized = false;
		boolean allIndexesUpdated = false;
		
		//String sWorth = "";
		double worth = 0;

		//Retrieve the criteria table
		Query q = pm.newQuery(CriteriaTable.class, "user==u");
		q.declareParameters("com.google.appengine.api.users.User u");
		q.setOrdering("createDate");
		List<CriteriaTable> criteriaList = (List<CriteriaTable>) q.execute(UtilityClass.getUser());
		
		for (CriteriaTable ct : criteriaList) {
			criteriaTable = ct;
			break;
		}
		
		//Retrieve weightTable
		q = pm.newQuery(WeightTable.class, "user==u");
		q.declareParameters("com.google.appengine.api.users.User u");
		q.setOrdering("createDate");
		List<WeightTable> weightList = (List<WeightTable>) q.execute(UtilityClass.getUser());
		
		for (WeightTable wt : weightList) {
			weightTable = wt;
			break;
		}
		
		// Read stock from the data store and calculate worth (index) then persist
		// Split task in several chunks because of the AppEngine 10 minute limitation

		//Get first part of K stocks stored in datastore (K initially set to 600)
		q = pm.newQuery(Stock.class, "user==u");
		q.declareParameters("com.google.appengine.api.users.User u");
		q.setOrdering("createDate");
		q.setRange(0,rangeIndex);
		List<Stock> storedStocks = (List<Stock>) q.execute(UtilityClass.getUser());
		
		//Get cursor after current results in storedStocks
		Cursor cursor = JDOCursorHelper.getCursor(storedStocks);
		String cursorString = cursor.toWebSafeString();
		
		//Check if cursor points to the bottom of the list
		Map<String, Object> extensionMap = new HashMap<String, Object>();
		extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
		q.setExtensions(extensionMap);
		q.setRange(0,1);
		List<Stock> furtherStock = (List<Stock>) q.execute(UtilityClass.getUser());
		if (furtherStock.isEmpty()) {
			allIndexesUpdated = true;
		} else {
			allIndexesUpdated = false;
		}
		
		// If not at the bottom of the list, launch a successor task to continue the work
		if (allIndexesUpdated == false) {
			String taskName = UtilityClass.createTaskName(cursorString + "Index");
			//handle = defaultQueue.add(TaskOptions.Builder.withUrl("/stockwatcher/stockCalculateAllIndexes").param("cursor", cursorString).taskName(taskName).countdownMillis(300000));		
			handle = defaultQueue.add(TaskOptions.Builder.withUrl("/stockwatcher/stockCalculateAllIndexes").param("cursor", cursorString).countdownMillis(300000));
		}
			
			//Calculate the worth index for the stocks as weighted sum of chosen criteria
		try {
			for (Stock stock : storedStocks) {
	
				worth = calculateStockIndex(stock);	// Calculation occurs at this point	
				//sWorth = Double.toString(worth);
				stock.setIndex (worth);
				pm.makePersistent(stock);
				
			}
		} finally {
			if (allIndexesUpdated == true) {
				//pm.close();  Do not close pm as it is  a class variable 
			}

		}
		
		//When all indexes have been calculated, launch the process of finding the Top N indexes after a 10 minute delay 
		//TODO: the N parameter should be part of the configuration modifiable by the admin; N should be read from the data store. Here set to 10.
		if (allIndexesUpdated == true) {
		Queue defaultQueue = QueueFactory.getDefaultQueue();
				TaskHandle handle = defaultQueue.add(TaskOptions.Builder.withUrl("/stockwatcher/getTopNIndexStocks").countdownMillis(600000)
														.param("N", String.valueOf(bestNStocks)));
		}

	}

	@Override
	public void doPost (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		boolean allIndexesUpdated = false;
		String sWorth = null;
		double worth = 0;
		
		//Retrieve the cursor from last query in datastore
		String cursorString = req.getParameter("cursor");
		Cursor cursor = Cursor.fromWebSafeString(cursorString);
		
		//Read the next batch of stocks
		Query q = pm.newQuery(Stock.class, "user==u");
		q.declareParameters("com.google.appengine.api.users.User u");
		q.setOrdering("createDate");
		q.setRange(0,rangeIndex);
		Map<String, Object> extensionMap = new HashMap<String, Object>();
		extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
		q.setExtensions(extensionMap);
		List<Stock> storedStocks = (List<Stock>) q.execute(UtilityClass.getUser());
		
		//Get cursor after current results in storedStocks
		cursor = JDOCursorHelper.getCursor(storedStocks);
		cursorString = cursor.toWebSafeString();
		
		//Check if cursor points to the bottom of the list
		extensionMap = new HashMap<String, Object>();
		extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
		q.setExtensions(extensionMap);
		q.setRange(0,1);
		List<Stock> furtherStock = (List<Stock>) q.execute(UtilityClass.getUser());
		if (furtherStock.isEmpty()) {
			allIndexesUpdated = true;
		} else {
			allIndexesUpdated = false;
		}
		
		// If not at the bottom of the list, launch a successor task to continue the work
		if (allIndexesUpdated == false) {
			String taskName = UtilityClass.createTaskName(cursorString + "Index");
			//handle = defaultQueue.add(TaskOptions.Builder.withUrl("/stockwatcher/stockCalculateAllIndexes").param("cursor", cursorString).taskName(taskName).countdownMillis(300000));		
			handle = defaultQueue.add(TaskOptions.Builder.withUrl("/stockwatcher/stockCalculateAllIndexes").param("cursor", cursorString).countdownMillis(300000));
		}
		
		//Calculate the worth index for the stock as weighted sum of chosen criteria
	    try {
		    for (Stock stock : storedStocks) {

			    worth = calculateStockIndex(stock);
					
			    //sWorth = Double.toString(worth);
			
			    stock.setIndex (worth);
			    pm.makePersistent(stock);
			
		    }
	    } finally {
	    	if (allIndexesUpdated == true) {
			    //pm.close();	 Do not close pm as it is a class variable
	    	}

	    }
	    
		//Launch the process of finding the Top N indexes after a 10 minute delay if all indexes have been calculated
		//TODO: the N parameter should be part of the configuration modifiable by the admin; N should be read from the data store. Here set to 10.
		if (allIndexesUpdated == true) {
		Queue defaultQueue = QueueFactory.getDefaultQueue();
				TaskHandle handle = defaultQueue.add(TaskOptions.Builder.withUrl("/stockwatcher/getTopNIndexStocks").countdownMillis(600000)
														.param("N", "10"));
		}
		
		return;
	}

  /* 
	private String createTaskName(String cursorString) {
		//Initalize a random number if it has been reinitialized by AppEngine somehow
		if (UtilityClass.getTaskRandom() == null) {
			Double taskRandom = Math.random() * 1000000;
			String sTaskRandom = Double.toString(taskRandom);
			sTaskRandom = sTaskRandom.replaceAll("\\.","-");
			UtilityClass.setTaskRandom(sTaskRandom);
		}
		String sTaskRandom = UtilityClass.getTaskRandom();
		String taskName = cursorString + "_" + sTaskRandom + "Index9";
		return (taskName);
	}
	*/

	private double calculateStockIndex(Stock stock) {
		
		double priceSalesPoints = 0, priceBookPoints = 0, profitMarginPoints = 0, ROEPoints = 0;
		double ROAPoints = 0, debtEquityPoints = 0, currentPoints = 0, oneYearEPSPoints = 0, quarterlyEPSPoints = 0;
		double managementOwnershipPoints = 0, quickRatioPoints = 0;
		double worth = 0;
		
		if (stock.getPriceSales() == 0) {
			priceSalesPoints = 0;
		} else if (stock.getPriceSales() < criteriaTable.getPriceSalesLevel1()) {
			priceSalesPoints = criteriaTable.getPriceSalesLevel1Points();
		} else if ( stock.getPriceSales() < criteriaTable.getPriceSalesLevel2()) {
			priceSalesPoints = criteriaTable.getPriceSalesLevel2Points();
		} else if ( stock.getPriceSales() < criteriaTable.getPriceSalesLevel3()) {
			priceSalesPoints = criteriaTable.getPriceSalesLevel3Points();
		} else if ( stock.getPriceSales() >= criteriaTable.getPriceSalesLevel3()) {
			priceSalesPoints = criteriaTable.getPriceSalesLevel4Points();
		} else {
			LOG.log(Level.WARNING, "PriceSales out of range for " + stock.getSymbol() + "\n");
		}
		
		//Points for PriceBook
		if (stock.getPriceBook() == 0) {
			priceBookPoints = 0;		
		}else if (stock.getPriceBook() < criteriaTable.getPriceBookLevel1()) {
			priceBookPoints = criteriaTable.getPriceBookLevel1Points();
		} else if ( stock.getPriceBook() < criteriaTable.getPriceBookLevel2()) {
			priceBookPoints = criteriaTable.getPriceBookLevel2Points();
		} else if ( stock.getPriceBook() < criteriaTable.getPriceBookLevel3()) {
			priceBookPoints = criteriaTable.getPriceBookLevel3Points();
		} else if ( stock.getPriceBook() >= criteriaTable.getPriceBookLevel3()) {
			priceBookPoints = criteriaTable.getPriceBookLevel4Points();
		} else {
			LOG.log(Level.WARNING, "PriceBook out of range for " + stock.getSymbol() + "\n");
		}
		
		//Points for Profit Margin
		if (stock.getProfitMargin() == 0) {
			profitMarginPoints = 0;
		} else if (stock.getProfitMargin() < criteriaTable.getProfitMarginLevel1()) {
			profitMarginPoints = criteriaTable.getProfitMarginLevel1Points();
		} else if ( stock.getProfitMargin() < criteriaTable.getProfitMarginLevel2()) {
			profitMarginPoints = criteriaTable.getProfitMarginLevel2Points();
		} else if ( stock.getProfitMargin() < criteriaTable.getProfitMarginLevel3()) {
			profitMarginPoints = criteriaTable.getProfitMarginLevel3Points();
		} else if ( stock.getProfitMargin() >= criteriaTable.getProfitMarginLevel3()) {
			profitMarginPoints = criteriaTable.getProfitMarginLevel4Points();
		} else {
			LOG.log(Level.WARNING, "ProfitMargin out of range for " + stock.getSymbol() + "\n");
		}
		
		//Points for ROE
		if (stock.getROE() == 0) {
			ROEPoints = 0;
		} else if (stock.getROE() < criteriaTable.getROELevel1()) {
			ROEPoints = criteriaTable.getROELevel1Points();
		} else if ( stock.getROE() < criteriaTable.getROELevel2()) {
			ROEPoints = criteriaTable.getROELevel2Points();
		} else if ( stock.getROE() < criteriaTable.getROELevel3()) {
			ROEPoints = criteriaTable.getROELevel3Points();
		} else if ( stock.getROE() >= criteriaTable.getROELevel3()) {
			ROEPoints = criteriaTable.getROELevel4Points();
		} else {
			LOG.log(Level.WARNING, "ROE out of range for " + stock.getSymbol() + "\n");
		}
		
		//Points for ROA
		if (stock.getROA() == 0) {
			ROAPoints = 0;
		} else if (stock.getROA() < criteriaTable.getROALevel1()) {
			ROAPoints = criteriaTable.getROALevel1Points();
		} else if ( stock.getROA() < criteriaTable.getROALevel2()) {
			ROAPoints = criteriaTable.getROALevel2Points();
		} else if ( stock.getROA() < criteriaTable.getROALevel3()) {
			ROAPoints = criteriaTable.getROALevel3Points();
		} else if ( stock.getROA() >= criteriaTable.getROALevel3()) {
			ROAPoints = criteriaTable.getROALevel4Points();
		} else {
			LOG.log(Level.WARNING, "ROA out of range for " + stock.getSymbol() + "\n");
		}
		
		//Points for DebtEquity
		if (stock.getDebtEquity() == 0 ) {
			debtEquityPoints =0;
		} else if (stock.getDebtEquity() < criteriaTable.getDebtEquityLevel1()) {
			debtEquityPoints = criteriaTable.getDebtEquityLevel1Points();
		} else if ( stock.getDebtEquity() < criteriaTable.getDebtEquityLevel2()) {
			debtEquityPoints = criteriaTable.getDebtEquityLevel2Points();
		} else if ( stock.getDebtEquity() < criteriaTable.getDebtEquityLevel3()) {
			debtEquityPoints = criteriaTable.getDebtEquityLevel3Points();
		} else if ( stock.getDebtEquity() >= criteriaTable.getDebtEquityLevel3()) {
			debtEquityPoints = criteriaTable.getDebtEquityLevel4Points();
		} else {
			LOG.log(Level.WARNING, "DebtEquity out of range for " + stock.getSymbol() + "\n");
		}
		
		//Points for Current Ratio
		if (stock.getCurrent() == 0) {
			currentPoints = 0;
		} else if (stock.getCurrent() < criteriaTable.getCurrentLevel1()) {
			currentPoints = criteriaTable.getCurrentLevel1Points();
		} else if ( stock.getCurrent() < criteriaTable.getCurrentLevel2()) {
			currentPoints = criteriaTable.getCurrentLevel2Points();
		} else if ( stock.getCurrent() < criteriaTable.getCurrentLevel3()) {
			currentPoints = criteriaTable.getCurrentLevel3Points();
		} else if ( stock.getCurrent() >= criteriaTable.getCurrentLevel3()) {
			currentPoints = criteriaTable.getCurrentLevel4Points();
		} else {
			LOG.log(Level.WARNING, "Current out of range for " + stock.getSymbol() + "\n");
		}
		
		// Points for QuickRatio
		if (stock.getQuickRatio() == 0) {
			quickRatioPoints = 0;
		} else if (stock.getQuickRatio() < criteriaTable.getQuickRatioLevel1()) {
			quickRatioPoints = criteriaTable.getQuickRatioLevel1Points();
		} else if ( stock.getQuickRatio() < criteriaTable.getQuickRatioLevel2()) {
			quickRatioPoints = criteriaTable.getQuickRatioLevel2Points();
		} else if ( stock.getQuickRatio() < criteriaTable.getQuickRatioLevel3()) {
			quickRatioPoints = criteriaTable.getQuickRatioLevel3Points();
		} else if ( stock.getQuickRatio() >= criteriaTable.getQuickRatioLevel3()) {
			quickRatioPoints = criteriaTable.getQuickRatioLevel4Points();
		} else {
			LOG.log(Level.WARNING, "QuickRatio out of range for " + stock.getSymbol() + "\n");
		}
		
		// Points for OneYearEPS
		if (stock.getOneYearEPS() == 0) {
			oneYearEPSPoints = 0;
		} else if (stock.getOneYearEPS() < criteriaTable.getOneYearEPSLevel1()) {
			oneYearEPSPoints = criteriaTable.getOneYearEPSLevel1Points();
		} else if ( stock.getOneYearEPS() < criteriaTable.getOneYearEPSLevel2()) {
			oneYearEPSPoints = criteriaTable.getOneYearEPSLevel2Points();
		} else if ( stock.getOneYearEPS() < criteriaTable.getOneYearEPSLevel3()) {
			oneYearEPSPoints = criteriaTable.getOneYearEPSLevel3Points();
		} else if ( stock.getOneYearEPS() >= criteriaTable.getOneYearEPSLevel3()) {
			oneYearEPSPoints = criteriaTable.getOneYearEPSLevel4Points();
		} else {
			LOG.log(Level.WARNING, "OneYearEPS out of range for " + stock.getSymbol() + "\n");
		}
		
		//Points for quarterly EPS
		if (stock.getQuarterlyEPS() == 0) {
			quarterlyEPSPoints = 0;
		} else if (stock.getQuarterlyEPS() < criteriaTable.getQuarterlyEPSLevel1()) {
			quarterlyEPSPoints = criteriaTable.getQuarterlyEPSLevel1Points();
		} else if ( stock.getQuarterlyEPS() < criteriaTable.getQuarterlyEPSLevel2()) {
			quarterlyEPSPoints = criteriaTable.getQuarterlyEPSLevel2Points();
		} else if ( stock.getQuarterlyEPS() < criteriaTable.getQuarterlyEPSLevel3()) {
			quarterlyEPSPoints = criteriaTable.getQuarterlyEPSLevel3Points();
		} else if ( stock.getQuarterlyEPS() >= criteriaTable.getQuarterlyEPSLevel3()) {
			quarterlyEPSPoints = criteriaTable.getQuarterlyEPSLevel4Points();
		} else {
			LOG.log(Level.WARNING, "QuarterlyEPS out of range for " + stock.getSymbol() + "\n");
		}
		
		//Points for Management Ownership
		if (stock.getManagementOwnership() == 0) {
			managementOwnershipPoints = 0;
		} else if (stock.getManagementOwnership() < criteriaTable.getManagementOwnershipLevel1()) {
			managementOwnershipPoints = criteriaTable.getManagementOwnershipLevel1Points();
		} else if ( stock.getManagementOwnership() < criteriaTable.getManagementOwnershipLevel2()) {
			managementOwnershipPoints = criteriaTable.getManagementOwnershipLevel2Points();
		} else if ( stock.getManagementOwnership() < criteriaTable.getManagementOwnershipLevel3()) {
			managementOwnershipPoints = criteriaTable.getManagementOwnershipLevel3Points();
		} else if ( stock.getManagementOwnership() >= criteriaTable.getManagementOwnershipLevel3()) {
			managementOwnershipPoints = criteriaTable.getManagementOwnershipLevel4Points();
		} else {
			LOG.log(Level.WARNING, "ManagementOwnership out of range for " + stock.getSymbol() + "\n");
		}
		
		//Calculate the index for the stock
		
		worth = priceSalesPoints * weightTable.getPriceSalesWeight() +
				priceBookPoints * weightTable.getPriceBookWeight() +
				profitMarginPoints * weightTable.getProfitMarginWeight() +
				ROEPoints * weightTable.getROEWeight() +
				ROAPoints * weightTable.getROAWeight() +
				debtEquityPoints * weightTable.getDebtEquityWeight() +
				currentPoints * weightTable.getCurrentWeight() +
				quickRatioPoints * weightTable.getQuickWeight() +
				oneYearEPSPoints * weightTable.getOneYearEPSWeight() +
				quarterlyEPSPoints * weightTable.getQuarterlyEPSWeight() +
				managementOwnershipPoints * weightTable.getManagementOwnershipWeight();
		
		return (worth);
	}
}
