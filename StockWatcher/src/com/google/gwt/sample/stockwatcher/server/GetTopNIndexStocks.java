package com.google.gwt.sample.stockwatcher.server;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;



public class GetTopNIndexStocks extends HttpServlet {
	
	private static final Logger logger = Logger.getLogger(GetTopNIndexStocks.class.getName());
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		return;
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {	

		logger.log(Level.INFO,"GetTopMIndexStocks");
		
		//TODO - Have priceThreshold configurable by the user
		double priceThreshold = 10;
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
	    try {
		//Retrieve the number N of top Stocks to rank
		String sNum = req.getParameter("N");
		int N = Integer.valueOf(sNum);
		
		//Get top N stocks stored in datastore
		Query q = pm.newQuery(Stock.class, "user==u");
		//Query q = pm.newQuery(Stock.class, "price > threshold && index > 0.0");
		q.declareParameters("com.google.appengine.api.users.User u");
		//q.declareParameters("double threshold");
		//q.setOrdering("price descending");
		q.setOrdering("index descending");
		//q.setFilter("price > threshold");
		q.setRange(0, N);
		List<Stock> storedStocks = (List<Stock>) q.execute(UtilityClass.getUser());
		// Consider only stocks with price > $10
		//List<Stock> storedStocks = (List<Stock>) q.execute(priceThreshold);
		
		//Initialize the Stock array for the top N Stocks
		Stock[] topNIndexStocks = new Stock[N];
		for (int i = 0; i < N ; i++) {
			topNIndexStocks[i] = new Stock();
		}
		
		//Store top N stocks ranked by Index in a Stock array
		int j = 0;
		for (Stock stock : storedStocks) {
			topNIndexStocks[j++] = stock;
			if (j == N) {
				break;
			}
		}
		// Persist top N Stock ranked by Index in data store
		int k=1;
		for (Stock stock : storedStocks) {
			BestStock best = new BestStock();
			best.setCreateDate(stock.getCreateDate());
			best.setChange(stock.getChange());
			best.setCurrent(stock.getCurrent());
			best.setDebtEquity(stock.getDebtEquity());
			best.setEPSEstimateNextQuarter(stock.getEPSEstimateNextQuarter());
			best.setEPSEstimateNextYear(stock.getEPSEstimateNextYear());
			best.setIndex(stock.getIndex());
			best.setLastUpdated(stock.getLastUpdated());
			best.setManagementOwnership(stock.getManagementOwnership());
			best.setOneYearEPS(stock.getOneYearEPS());
			best.setPEGRatio(stock.getPEGRatio());
			best.setPERatio(stock.getPERatio());
			best.setPercentChange(stock.getPercentChange());
			best.setPrice(stock.getPrice());
			best.setPriceBook(stock.getPriceBook());
			best.setPriceEstimateEPSCurrentYear(stock.getPriceEstimateEPSCurrentYear());
			best.setPriceEstimateEPSNextYear(stock.getPriceEstimateEPSNextYear());
			best.setPriceSales(stock.getPriceSales());
			best.setProfitMargin(stock.getProfitMargin());
			best.setQuarterlyEPS(stock.getQuarterlyEPS());
			best.setQuickRatio(stock.getQuickRatio());
			best.setROA(stock.getROA());
			best.setROE(stock.getROE());
			best.setSymbol(stock.getSymbol());
			best.setUser(stock.getUser());
			//Ranked by decreasing order
			best.setRank(k++);
			
			pm.makePersistent(best);
			
			//Troubleshooting output
		/* 
			logger.log(Level.WARNING,"Stock " + best.getSymbol() + " is ranked # " + best.getRank() + 
					" with index of " + best.getIndex());
		 */
		}
		
	    } finally {
	    	pm.close();
	    }
	    
		//Launch StockAverage task in 2 minutes
		Queue defaultQueue = QueueFactory.getDefaultQueue();
		TaskHandle handle = defaultQueue.add(TaskOptions.Builder.withUrl("/stockwatcher/calculateBestStocksAverages").countdownMillis(600000));
	return;
	}
}
