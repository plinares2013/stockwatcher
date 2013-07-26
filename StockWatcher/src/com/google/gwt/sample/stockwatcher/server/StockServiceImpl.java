package com.google.gwt.sample.stockwatcher.server;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;


import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.sample.stockwatcher.client.NotLoggedInException;
import com.google.gwt.sample.stockwatcher.client.StockService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class StockServiceImpl extends RemoteServiceServlet implements StockService {

	private static final Logger LOG = Logger.getLogger(StockServiceImpl.class.getName());
	//private static PersistenceManagerFactory PMF = JDOHelper.getPersistenceManagerFactory("transactions-optional");
	
	public void addStock (String symbol) throws NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistent(new Stock (getUser(), symbol));
		} finally {
			pm.close();
		}
	}
		
	public void removeStock (String symbol) throws NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = PMF.get().getPersistenceManager();	
		
		try {
			long deleteCount =0;
			Query q = pm.newQuery(Stock.class, "user==u");
			q.declareParameters("com.google.appengine.api.users.User u");
			q.setOrdering("createDate");
			List<Stock> stocks = (List<Stock>) q.execute(getUser());
			
			for (Stock stock : stocks) {
				if (stock.getSymbol().equals(symbol)) {
					deleteCount++;
					pm.deletePersistent(stock);
				}
			}
			
			if (deleteCount != 1) {
				LOG.log(Level.WARNING, "removeStock deleted " + deleteCount + " Stocks" );
			}
		} finally {
			pm.close();
		}
	}
	
	public String[] getStocks() throws NotLoggedInException {
		List<String> symbolList = new ArrayList<String>();
		checkLoggedIn();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
		Query q = pm.newQuery(Stock.class, "user==u");
		//Query q = pm.newQuery(Stock.class);
		//Query q = pm.newQuery("SELECT FROM com.google.gwt.sample.stockwatcher.server.Stock WHERE symbol== tickerParam" +
		//						"parameters String tickerParam");
		q.declareParameters("com.google.appengine.api.users.User u");
		q.setOrdering("createDate");
		List<Stock> stocks = (List<Stock>) q.execute(getUser());
		//List<Stock> stocks = (List<Stock>) q.execute("XOM");
		//List<Stock> stocks = (List<Stock>) q.execute();
		
		for (Stock stock : stocks) {
			symbolList.add(stock.getSymbol());
		}
		} finally {
			pm.close();
		}
		
		return (String[]) symbolList.toArray(new String[0]);
	}
		
	private User getUser() {
		UserService userSvc = UserServiceFactory.getUserService();
		User user = userSvc.getCurrentUser();
		LOG.log(Level.WARNING, "User email is " + user.getEmail() );
		return user ;
	}
	
	private void checkLoggedIn() throws NotLoggedInException {
		if (getUser() == null) {
			throw new NotLoggedInException ("Not logged in.") ;
		}
	
	}
}
