package com.google.gwt.sample.stockwatcher.server;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StockCalculateTopNAverages extends HttpServlet {

	private static final Logger logger = Logger.getLogger(StockCalculateTopNAverages.class.getName());
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		logger.log(Level.INFO,"Calculate Averages");
		
		try{
			
			//Determine the time period associated to the Best list
			Query q = pm.newQuery(BestStock.class, "user==u");
			q.declareParameters("com.google.appengine.api.users.User u");
			q.setOrdering("createDate descending");
			q.setResult("max(lastUpdated), min(lastUpdated)");
			Object[] dateArray = (Object[]) q.execute(UtilityClass.getUser());
			Date endDate = (Date) dateArray[0];
			Date startDate = (Date) dateArray[1];
			
			//Retrieve all BestStocks that have been selected ranked by symbol
			q = pm.newQuery(BestStock.class);
			//q.declareParameters("com.google.appengine.api.users.User u");
			q.setOrdering("symbol ascending");
			q.addExtension("datanucleus.appengine.datastoreReadConsistency", "STRONG");
			//TODO: Add selection on date
			//List<BestStock> bestStocks = (List<BestStock>) q.execute(UtilityClass.getUser());
			List<BestStock> bestStocks = (List<BestStock>) q.execute();
			
		/*	Troubleshooting consistency of data store.
			int k=1;
			for (BestStock best : bestStocks) {
				logger.log(Level.WARNING,"Stock entity is " + best.getSymbol() + "at line " + k);
				k++;
			}
			
			q.setResult("count (this)");
			q.addExtension("datanucleus.appengine.datastoreReadConsistency", "STRONG");
			//Long count = (Long) q.execute(UtilityClass.getUser());
			Long count = (Long) q.execute();
		
			logger.log(Level.WARNING,"number of records in BestStocks = " + count);
			
			String[] names = {"AAON","ABAX","ABMD","AVHI","FCCY","FCTY","FLWS","SHLM","SRCE","JOBS"};
			
			for (String symbl : names) {
			pm.newQuery(BestStock.class, "symbol==symb");
			q.declareParameters("String symb");
			q.setFilter("symbol == symb");
			q.setResult("count(symb)");
			q.addExtension("datanucleus.appengine.datastoreReadConsistency", "STRONG");
			Long countSymbol = (Long) q.execute(symbl);
			logger.log(Level.WARNING,"number of entities for symbol " + symbl + " is :" + countSymbol);
			}
		*/

			//Retrieve list of StockAverage entities
			q = pm.newQuery(StockAverage.class);
			List<StockAverage> stockAverages = (List<StockAverage>) q.execute();
		
			String symbol = "", symbolLast = "", symbolToPersist = "";
			long sumRank = 0;
			double  sumIndex = 0, averageIndex = 0;
			long averageRank = 0;
			int i = 0;  // i is used to count the # of appearances of a stock in the Best list
			int appearances = 0;
			StockAverage stockAverage = new StockAverage();
			boolean foundAverageStock = false;
			BestStock bestLast = new BestStock();
			//For each symbol calculate average ranking and # occurrences among the BestStocks
			for (BestStock best : bestStocks) {
				symbol = best.getSymbol();
				if (symbol.equals(symbolLast)) {
					sumRank += best.getRank();
					sumIndex +=best.getIndex();
					i++;
				} else if (symbolLast.equals("")) { //initialize the variables very first time of the loop
					symbolLast = symbol;
					sumRank = best.getRank();
					sumIndex = best.getIndex();
					bestLast = best;
					i=1;
				} else {    // Loop iterated on a different stock
					//Calculate average for the previous Stock
					averageRank = sumRank/i;
					averageIndex = sumIndex/i;
					appearances = i;
					//Identify if symbol already in data store
					for (StockAverage average : stockAverages) {
						if (average.getSymbol() == null) {
							continue;
						}
						if ((average.getSymbol()).equals(symbolLast)) {
							foundAverageStock = true;
							stockAverage = average;
							break;
						} 
					}
					// If symbol already in data store, update the entity
					if (foundAverageStock == true) {
						// Update "average" object
						stockAverage.setSymbol(bestLast.getSymbol());
						stockAverage.setAverageRank(averageRank);
						stockAverage.setNumberAppearances(appearances);
						stockAverage.setStartDate(new Date(startDate.getTime()));
						stockAverage.setEndDate(new Date(endDate.getTime()));
						stockAverage.setAverageIndex(averageIndex);
						foundAverageStock = false;
					} else {   // This is a new Best Stock, create a new element
						stockAverage = new StockAverage();
						stockAverage.setSymbol(bestLast.getSymbol());
						stockAverage.setAverageRank(averageRank);
						stockAverage.setNumberAppearances(appearances);
						stockAverage.setStartDate(new Date(startDate.getTime()));
						stockAverage.setEndDate(new Date(endDate.getTime()));
						stockAverage.setAverageIndex(averageIndex);
					}
					
					pm.makePersistent(stockAverage);
					
					// Handle the Best stock that entered the loop now
					//Reset all counters for current BestStock
					symbolLast = symbol;
					bestLast = best;
					i = 1;
					sumIndex = best.getIndex();
					sumRank = best.getRank();
				}
			}

			// For last Best Stock in the bestStocks, handle the  persistence outside the loop
			//Calculate average for the last Best Stock
			averageRank = (long) sumRank/i;
			averageIndex = sumIndex/i;
			appearances = i;
			//Identify if symbol already in data store
			for (StockAverage average : stockAverages) {
				if (average.getSymbol().equals(symbol)) {
					foundAverageStock = true;
					stockAverage = average;
					break;
				} 
			}
			// If symbol already in data store, update the entity
			if (foundAverageStock == true) {
				// Update the existing "average" object
				stockAverage.setSymbol(bestLast.getSymbol());
				stockAverage.setAverageRank(averageRank);
				stockAverage.setNumberAppearances(appearances);
				stockAverage.setStartDate(new Date(startDate.getTime()));
				stockAverage.setEndDate(new Date(endDate.getTime()));
				stockAverage.setAverageIndex(averageIndex);
				foundAverageStock = false;
			} else {   // This is a new Best Stock, create a new element
				stockAverage = new StockAverage();
				stockAverage.setSymbol(bestLast.getSymbol());
				stockAverage.setAverageRank(averageRank);
				stockAverage.setNumberAppearances(appearances);
				stockAverage.setStartDate(new Date(startDate.getTime()));
				stockAverage.setEndDate(new Date(endDate.getTime()));
				stockAverage.setAverageIndex(averageIndex);
			}
			
			pm.makePersistent(stockAverage);
			
		} finally {
			pm.close();
		}

	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		doGet(req, resp);
	}

}
