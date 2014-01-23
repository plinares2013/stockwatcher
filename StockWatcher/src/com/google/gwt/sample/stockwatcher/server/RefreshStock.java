package com.google.gwt.sample.stockwatcher.server;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
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

import com.google.appengine.api.backends.BackendServiceFactory;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.datanucleus.query.JDOCursorHelper;
import com.google.apphosting.utils.remoteapi.RemoteApiPb.Request;
import com.google.gwt.sample.stockwatcher.shared.StockInformation;

//Task in the backend that retrieves Stock information from the Web and updates the datastore
//Called once a day as per war/cron.xml
public class RefreshStock extends HttpServlet {
	
	// int range = 4;
	int rangeStock = UtilityClass.getRangeStock(); // Number of stocks handled by task
	String stockOrder = UtilityClass.getStockOrder(); // Ascending or descending
	
	long delayRefreshStock = UtilityClass.getDelayRefreshStockTask();
	
	// TODO - Have range configurable by the administrator.
	Query q = null;
	private static final Logger logger = Logger.getLogger(StockServiceImpl.class.getName());
	PersistenceManager pm = PMF.get().getPersistenceManager();

	TaskHandle handle = null;
	Queue defaultQueue = QueueFactory.getDefaultQueue();
	StockFinancialData stockData = null;

	public void doGet (HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		boolean allStocksUpdated = false;
		
		// Determine if request is coming from initialization of backend by AppEngine
		// If so, just return and wait for cron to call the task
		String name = req.getHeader("X-AppEngine-TaskName");
		String queue = req.getHeader("X-AppEngine-QueueName");
		String query = req.getQueryString();
		//Enumeration<String> parameters = req.getParameterNames();
		//for ( String param = parameters.nextElement() ; parameters.hasMoreElements() ; )  {
		//	logger.log(Level.INFO,"parameter is " + param);
		//}

			if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) { // Only in production mode
				try{
					logger.log(Level.INFO,"Queue name is " + queue);
					if (!(queue.contains("cron") || queue.contains("default"))) {
						logger.log(Level.INFO,"Received request from queue different from cron or default");			
						return;						
					}
				} catch (Exception e) {
					logger.log(Level.WARNING,"RefreshStockCaught Exception upon queue testing");
					return;
				}
			}
				
			


		
		//Initalize a random number that will be used to create task names later
		// Avoid tombstoned taskname to occur in AppEngine
		
		Double taskRandom = Math.random() * 1000000;
		String sTaskRandom = Double.toString(taskRandom);
		sTaskRandom = sTaskRandom.replaceAll("\\.","-");
		UtilityClass.setTaskRandom(sTaskRandom);
		
		// For each stock of the datastore,  update financial information
		// Need to do this work through several tasks because of AppEngine 10 minute limitation
		
		// Start with the first N stocks (N set by the int "range" then continue with every N stocks from the POST request
		
		//Get N stocks stored in datastore
		Query q = pm.newQuery(Stock.class, "user==u");
		q.declareParameters("com.google.appengine.api.users.User u");
		q.setOrdering("createDate " + stockOrder);
		q.setRange(0,rangeStock);
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
			allStocksUpdated = true;
		} else {
			allStocksUpdated = false;
		}
		
		//Launch backends task
	    //handle = defaultQueue.add(TaskOptions.Builder.withUrl("/stockwatcher/backends/refreshStock2"));
		
		// If not at the bottom of the list, launch a successor task to continue the work
		if (allStocksUpdated == false) {
			String taskName = UtilityClass.createTaskName(cursorString + "Stock");
			//handle = defaultQueue.add(TaskOptions.Builder.withUrl("/stockwatcher/cron/refreshStock").param("cursor", cursorString).taskName(taskName).countdownMillis(delayRefreshStock));
			//handle = defaultQueue.add(TaskOptions.Builder.withUrl("/stockwatcher/cron/refreshStock").param("cursor", cursorString).countdownMillis(delayRefreshStock));
			handle = defaultQueue.add(TaskOptions.Builder.withUrl("/stockwatcher/backends/refreshStock").param("cursor", cursorString).countdownMillis(delayRefreshStock)
					              		.header("Host", BackendServiceFactory.getBackendService().getBackendAddress("backendrefreshstock"))
					              		.method(Method.POST));
		}
		//For each stock among the N handled by that task, get financial information
		//String and StockInformation are arrays (of size 1 as a first step)
		String[] symbols = new String[1];
		StockInformation[] stockAbstract = new StockInformation[1];
		for (Stock stored : storedStocks) {
			//Troubleshooting performance in AppEngine
			//date = new Date();
			//logger.log(Level.INFO,"In refreshStock stock " + stored.getSymbol() + " started at " + dateFormat.format(date));
			
			stockData = new StockFinancialData();
			symbols[0]=stored.getSymbol();
			if ((stockAbstract = stockData.getStockInformation(symbols)) == null) {
				logger.log(Level.WARNING,stored.getSymbol() + " has not been updated");
			}
		
		/*
			try {
				Thread.sleep(1000);   // Introduce a 1 s delay between each stock request to avoid appearing as a DOS attack
			} catch (InterruptedException e) {
				e.printStackTrace();
				logger.log(Level.WARNING,"Timeout interrupted in RefreshStock!");
			}
		*/
			
			//Troubleshooting performance in AppEngine
			//date = new Date();
			//logger.log(Level.INFO,"In refreshStock stock " + stored.getSymbol() + " completed at " + dateFormat.format(date));
		}
		
		//After all stocks have been updated launch the process of calculating the index for all stocks after a 20 minute delay
		
		 if (allStocksUpdated == true) {
		 handle = defaultQueue.add(TaskOptions.Builder.withUrl("/stockwatcher/stockCalculateAllIndexes").countdownMillis(600000).method(TaskOptions.Method.valueOf("GET")));
		 }
	}
	
	// TODO - DoPost is almost a duplicate of doGet. Need to factor the code once operational
	@Override
	public void doPost (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		boolean allStocksUpdated = false;
		
		// Determine if request is coming from initialization of backend by AppEngine
				// If so, just return and wait for cron to call the task
				String name = req.getHeader("X-AppEngine-TaskName");
				String queue = req.getHeader("X-AppEngine-QueueName");
				String query = req.getQueryString();
				//Enumeration<String> parameters = req.getParameterNames();
				//for ( String param = parameters.nextElement() ; parameters.hasMoreElements() ; )  {
				//	logger.log(Level.INFO,"parameter is " + param);
				//}
				
				if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {  // Only in production mode
					try{
						logger.log(Level.INFO,"Queue name is " + queue);
						if (!(queue.contains("cron") || queue.contains("default"))) {
							logger.log(Level.INFO,"Received request from queue different from cron or default");						
								return;
						}
					} catch (Exception e) {
						logger.log(Level.WARNING,"RefreshStockCaught Exception upon queue testing");
						return;
					}

				}
				
		
		//Retrieve the cursor from last query in datastore
		String cursorString = req.getParameter("cursor");
		Cursor cursor = Cursor.fromWebSafeString(cursorString);
		
		//Read the next batch of stocks
		Query q = pm.newQuery(Stock.class, "user==u");
		q.declareParameters("com.google.appengine.api.users.User u");
		q.setOrdering("createDate " + stockOrder);
		q.setRange(0,rangeStock);
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
			allStocksUpdated = true;
		} else {
			allStocksUpdated = false;
		}
		
		// If cursor not at the bottom, launch the next worker with a delay
		if (allStocksUpdated == false) {
			String taskName = UtilityClass.createTaskName(cursorString + "Stock");
			//handle = defaultQueue.add(TaskOptions.Builder.withUrl("/stockwatcher/cron/refreshStock").param("cursor", cursorString).taskName(taskName).countdownMillis(delayRefreshStock));
			//handle = defaultQueue.add(TaskOptions.Builder.withUrl("/stockwatcher/cron/refreshStock").param("cursor", cursorString).countdownMillis(delayRefreshStock));
			handle = defaultQueue.add(TaskOptions.Builder.withUrl("/stockwatcher/backends/refreshStock").param("cursor", cursorString).countdownMillis(delayRefreshStock).
								header("Host", BackendServiceFactory.getBackendService().getBackendAddress("backendrefreshstock"))
								.method(Method.POST));
		}
		//For each stock, get financial information
		//String and StockInformation are arrays (of size 1 as a first step)
		String[] symbols = new String[1];
		StockInformation[] stockAbstract = new StockInformation[1];
		for (Stock stored : storedStocks) {
			stockData = new StockFinancialData();
			symbols[0]=stored.getSymbol();
			if ((stockAbstract = stockData.getStockInformation(symbols)) == null) {
				logger.log(Level.WARNING,stored.getSymbol() + " has not been updated");
			}
			try {
				Thread.sleep(1000);   // Introduce a 1 s delay to avoid appearing as a DOS attack
			} catch (InterruptedException e) {
				logger.log(Level.WARNING,"Timeout interrupted in RefreshStock!");
				//logger.log(Level.SEVERE,e.toString(),e);
			}
 
			
		}
		
		
		//Launch the process of calculating the index for all stocks after a 20 minute delay
		//TODO - Consider the delay as a parameter to be configured at init time from a TaskParameters table
		 if (allStocksUpdated == true) {
		 handle = defaultQueue.add(TaskOptions.Builder.withUrl("/stockwatcher/stockCalculateAllIndexes").countdownMillis(600000).method(TaskOptions.Method.valueOf("GET")));
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
		String taskName = cursorString + "_" + sTaskRandom + "Stock9";
		return (taskName);
	}
	*/
}
