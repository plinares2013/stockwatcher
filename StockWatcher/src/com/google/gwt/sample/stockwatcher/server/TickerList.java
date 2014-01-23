package com.google.gwt.sample.stockwatcher.server;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;

// TODO: need to restrict access to admin only in web.xml

public class TickerList extends HttpServlet {
		
	private static Logger logger= Logger.getLogger ("Building Ticker list");
	
	public void doGet (HttpServletRequest req, HttpServletResponse resp) throws IOException {
		// Get the list of symbols and persist in datastore
		logger.log(Level.INFO, "First task queued: to build the ticker list");
		
		//Read the list from the local file system (files in resources directory)
		BufferedReader reader = null;
		
		//Handle all stocks from NASDAQ
		reader = new BufferedReader (new FileReader ("resources/companylist_NASDAQ_Overall_List.csv"));
			
		int result = updateStocksInDataStore(reader);
		
		//Launch the next initialization step for NYSE stocks
		String taskName = UtilityClass.createTaskName("Tickerlist");
		Queue defaultQueue = QueueFactory.getDefaultQueue();
		//TaskHandle handle = defaultQueue.add(TaskOptions.Builder.withUrl("/stockwatcher/tickerlist").taskName(taskName).method(TaskOptions.Method.valueOf("POST")));
		TaskHandle handle = defaultQueue.add(TaskOptions.Builder.withUrl("/stockwatcher/tickerlist").method(TaskOptions.Method.valueOf("POST")));
		
		return;
		}

	

	@Override
	public void doPost (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		// Handle the initialization of NYSE stocks in a separate task
		
		// Get the list of symbols and persist in datastore
		//logger.log(Level.WARNING, "First task queued: to build the ticker list");
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		//Read the list from the local file system (files in resources directory)
		BufferedReader reader = null;
			
		//Handle all stocks from NYSE
		reader = new BufferedReader (new FileReader ("resources/companylist_NYSE_Overall_List.csv"));
			
		int result = updateStocksInDataStore(reader);
		
		//Initialize AMEX stocks
		reader = new BufferedReader (new FileReader ("resources/companylist_AMEX_Overall_List.csv"));
		
		result = updateStocksInDataStore(reader);
			
		return;
	}


	private int updateStocksInDataStore(BufferedReader reader) {
	
		PersistenceManager pm = PMF.get().getPersistenceManager();
	
		//Read the list from the local file system (files in resources directory)
		String sLine = "", headerline ="";
		String symbol = "";
	
	try {
		headerline = reader.readLine();  //Skip headerline in Excel file
		
		while ((sLine = reader.readLine()) != null) {
			
			//Extract first part of the line (the ticker)
			String[] split = sLine.split(",");
			symbol = split[0];
			symbol = symbol.trim();  //Get rid of leading or trailing spaces
			
			//Skip all symbols with ^ or /
			if ( (symbol.contains("^")) || (symbol.contains("/"))) {
				continue;
			}
			
			//Eliminate any " in the symbol
			symbol = symbol.replaceAll("\"","");
		
			//Check if symbol already in the datastore

			Query q = pm.newQuery(Stock.class, "symbol==sym");
			q.declareParameters("String sym");
			List<Stock> storedStocks = (List<Stock>) q.execute(symbol);
			
			//If not in data store
			if (storedStocks.isEmpty()) {
				Stock stock = new Stock();
				stock.setSymbol(symbol);
				//Persist in the datastore with JDO
				pm.makePersistent(stock);
			} else {
				for (Stock stock : storedStocks) {
					if (stock.getSymbol().contains("\"")) {  // Eliminate all symbols with a "
						pm.deletePersistent(stock);
					} else {
						pm.makePersistent(stock); //Of interest when new fields are added to the Stock object, to create these fields in the datastore	
					}
				}

			}
		}
	} catch (IOException e ) {
		e.printStackTrace();
	} finally {
		try {
			if (reader != null) {
				reader.close();
			} 
			if (pm != null) {
				pm.close();
			}
		}catch (IOException e ) {
			e.printStackTrace();
		}
	}
	
	return 0;
	}
	
	}
