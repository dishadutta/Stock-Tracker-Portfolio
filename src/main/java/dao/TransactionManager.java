package main.java.dao;

import main.java.common.CommonDefine;
import main.java.model.Stock;
import main.java.model.Transaction;
import main.java.model.TransactionWrapper;
import main.java.utility.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class TransactionManager<T> extends BaseManager<T> {

	public List<TransactionWrapper> findSummaryTransactions(Integer userId) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
	
		String hql = "SELECT stock.stockCode as symbol, stock.stockName as company, SUM(stock.price*stock.amount) as totalPayment, SUM(stock.amount) as totalAmount "
				+ "FROM Stock stock "
		        + "INNER JOIN UserStock us " 
				+ "ON us.id.userId = :userId " 
		        + "AND stock = us.stock "
		        + "AND (us.stockType = 1 " 
		        + "OR us.stockType = 2) " 
		        + "GROUP BY stock.stockCode";

		@SuppressWarnings("unchecked")
		Query<Object> query = session.createQuery(hql);
		query.setParameter("userId", userId);
		// }
		List<Object> transactions = query.getResultList();
		session.close();
		List<TransactionWrapper> wrapper = new ArrayList<>();
		for (Object t : transactions) {
			Object[] data = (Object[])t;
			Transaction tran = new Transaction();
			Stock stock = new Stock();
			// Extract data
			stock.setStockCode(data[0].toString());
			stock.setStockName(data[1].toString());;
			tran.setPayment(Double.valueOf(data[2].toString()));
			stock.setAmount(Integer.parseInt(data[3].toString()));
			
			wrapper.add(new TransactionWrapper(tran, stock));
		}
		return wrapper;
	}

	public List<TransactionWrapper> findTransactions(Integer userId, int stockType) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		try {
			String hql;
			switch(stockType) {
				case CommonDefine.OWNED_STOCK:
					hql = "SELECT transaction "
							+ "FROM Transaction transaction "
							+ "INNER JOIN Stock stock "
							+ "ON transaction.stockId = stock.id "
							+ "AND transaction.account.userId = :userId "
							+ "INNER JOIN UserStock us "
							+ "ON us.id.userId = :userId "
							+ "AND stock = us.stock "
							+ "AND (us.stockType = 1 "
							+ "OR us.stockType = 2) "
							+ "ORDER BY transaction.transactionDate DESC";
					break;
				case CommonDefine.TRANSACTION_STOCK:
					hql = "SELECT transaction "
							+ "FROM Transaction transaction "
							+ "INNER JOIN Stock stock "
							+ "ON transaction.stockId = stock.id "
							+ "AND transaction.account.userId = :userId "
							+ "INNER JOIN UserStock us "
							+ "ON us.id.userId = :userId "
							+ "AND stock = us.stock "
							+ "AND us.stockType != 2 " // Don't include remaining stock
							+ "ORDER BY transaction.transactionDate DESC";
					break;
				default:
					return null;
			}
			@SuppressWarnings("unchecked")
			Query<Transaction> query = session.createQuery(hql);
			query.setParameter("userId", userId);
			List<Transaction> transactions = (List<Transaction>)query.getResultList();
			session.close();
			List<TransactionWrapper> wrapper = new ArrayList<>();
			for (Transaction t : transactions) {
				wrapper.add(new TransactionWrapper(t, t.getStock()));
			}
			return wrapper;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			session.close();
		}
	}
}
