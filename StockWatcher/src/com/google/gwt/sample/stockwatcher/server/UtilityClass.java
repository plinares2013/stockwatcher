package com.google.gwt.sample.stockwatcher.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.sample.stockwatcher.client.NotLoggedInException;

public class UtilityClass {

	private static final Logger LOG = Logger.getLogger(StockServiceImpl.class.getName());
	
	private static String taskRandom = null;
	
	//TODO - Delays and numbers to be configurable by the user through GUI
	private static long delayRefreshStockTask = 120000; // Delay of 1000 = 1s
	private static long delayCalculateAllIndextask = 600000; // Delay of 1000 = 1s
	
	private static int rangeStock = 50; // Number of stocks refreshed by 1 StockRefreshTask
	private static int rangeIndex = 600; // Number of indexes handled by 1 StockCalculateAllIndexes
	private static int topNStocks = 10; // Number of stocks to select for BestStocks
	
	
	public static User getUser() {
		UserService userSvc = UserServiceFactory.getUserService();
		User user = userSvc.getCurrentUser();
		//LOG.log(Level.WARNING, "User email is " + user.getEmail() );
		return user ;
	}
	
	public static void checkLoggedIn() throws NotLoggedInException {
		if (getUser() == null) {
			throw new NotLoggedInException ("Not logged in.") ;
		}
	}
	public static void setTaskRandom(String random) {
		taskRandom = random;
	}
	
	public static String getTaskRandom() {
		return (taskRandom);
	}
	
	public static String createTaskName(String cursorString) {
		//Initalize a random number if it has been reinitialized by AppEngine somehow
		if (getTaskRandom() == null) {
			Double taskRandom = Math.random() * 1000000;
			String sTaskRandom = Double.toString(taskRandom);
			sTaskRandom = sTaskRandom.replaceAll("\\.","-");
			setTaskRandom(sTaskRandom);
		}
		String sTaskRandom = getTaskRandom();
		String taskName = cursorString + "_" + sTaskRandom;
		return (taskName);
	}

	public static long getDelayCalculateAllIndextask() {
		return delayCalculateAllIndextask;
	}

	public static void setDelayCalculateAllIndextask(long delayCalculateAllIndextask) {
		UtilityClass.delayCalculateAllIndextask = delayCalculateAllIndextask;
	}

	public static int getRangeStock() {
		return rangeStock;
	}

	public static void setRangeStock(int rangeStock) {
		UtilityClass.rangeStock = rangeStock;
	}

	public static int getRangeIndex() {
		return rangeIndex;
	}

	public static void setRangeIndex(int rangeIndex) {
		UtilityClass.rangeIndex = rangeIndex;
	}

	public static int getTopNStocks() {
		return topNStocks;
	}

	public static void setTopNStocks(int topNStocks) {
		UtilityClass.topNStocks = topNStocks;
	}

	public static long getDelayRefreshStockTask() {
		return delayRefreshStockTask;
	}

	public static void setDelayRefreshStockTask(long delayRefreshStockTask) {
		UtilityClass.delayRefreshStockTask = delayRefreshStockTask;
	}
	
}
