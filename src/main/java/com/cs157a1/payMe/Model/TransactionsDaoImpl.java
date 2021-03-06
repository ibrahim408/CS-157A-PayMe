package com.cs157a1.payMe.Model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.cs157a1.payMe.Entity.Comment;
import com.cs157a1.payMe.Entity.TransType;
import com.cs157a1.payMe.Entity.Transactions;
import com.cs157a1.payMe.Entity.UserHasTransactions;



@Repository("TransactionsDao")
public class TransactionsDaoImpl implements TransactionsDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public List<Transactions> returnAllInfo() {
		return jdbcTemplate.query("SELECT * FROM Transactions "
				+ "NATURAL JOIN users_has_Transactions", new TransactionsResultSetExtractor());
	}

	@Override
	public Transactions returnTransactionsBytransID(int transID) {
		final String sql = "SELECT * from Transactions NATURAL JOIN users_has_Transactions WHERE transId = ?";
		return jdbcTemplate.queryForObject(sql, new TransactionsRowMapper(), transID);
		
	}
	
	// return all sent requests or sent transfers
	// depends on type
	@Override
	public 	List<Transactions> returnUsersRequest(String type, String username) {
		final String sql = "SELECT * FROM Transactions"
                + " JOIN users_has_Transactions on Transactions.transID = users_has_Transactions.transId" 
			      + " JOIN Users on users_has_Transactions.sender_username = Users.username" 
                + " where Transactions.type= ? and Users.username= ? ORDER BY Transactions.transID asc";
		return jdbcTemplate.query(sql, new TransactionsResultSetExtractor(), type,username);
	}
	
	// returns all received request or received transfers
	// depends on type
	@Override
	public List<Transactions> returnUsersTransfers(String type, String username){
		final String sql = "SELECT * FROM Transactions"
                  + " JOIN users_has_Transactions on Transactions.transID = users_has_Transactions.transId" 
			      + " JOIN Users on users_has_Transactions.receiver_username = Users.username" 
                  + " where Transactions.type= ? and Users.username= ? ORDER BY Transactions.transID asc";
		return jdbcTemplate.query(sql, new TransactionsResultSetExtractor(), type,username);
	}
	
	
	@Override
	public void addTransactionsToDB(Transactions transaction, String sender, String receiver) {
		final String sql = "INSERT INTO Transactions(type,amount) VALUES (?,?)";
		double amount = transaction.getAmount();
		TransType type = transaction.getType();
		String typeStr = (type==TransType.REQUEST) ? "REQUEST" : "TRANSFER";
		jdbcTemplate.update(sql, new Object[] {typeStr,amount});	
		
		final String sqlTransaction = "SELECT * FROM Transactions " 
		+ "order by transid  desc limit 1";
		Transactions t = jdbcTemplate.queryForObject(sqlTransaction, new TransactionsRowMapper2());
		addToUserHasTransactions(t.getTransID(),sender,receiver);
	}
	
	public void addToUserHasTransactions(int id,String sender, String receiver) {
		final String sqlUserHasTransactions = "INSERT INTO users_has_Transactions(receiver_username,transId,sender_username) VALUES (?,?,?)";
		
		jdbcTemplate.update(sqlUserHasTransactions, new Object[] {receiver,id,sender});	
	}

	@Override
	public void deleteTransactions(int transID) {
		final String sql = "DELETE FROM Transactions WHERE transId = ?";
		final String sql2 = "DELETE FROM users_has_transactions WHERE transId = ?";
		final String sql3 = "DELETE FROM Comments WHERE Transactions_transId = ?";
		jdbcTemplate.update(sql3,transID);
		jdbcTemplate.update(sql2,transID);
		jdbcTemplate.update(sql,transID);
	
	}
	
	@Override
	public void deleteUserHasTransactions(int transID, String receiver) {
		final String sql = "DELETE FROM users_has_transactions WHERE transID = ? AND receiver_username = ?";
		jdbcTemplate.update(sql,transID, receiver);
	}
	
	public class TransactionsResultSetExtractor implements ResultSetExtractor<List<Transactions>> {

		   @Override
		   public List<Transactions> extractData(ResultSet rs) throws SQLException {
		         Map<Integer, Transactions> transactionmap = new HashMap<Integer, Transactions>();
		         while (rs.next()) {
		             int id = rs.getInt("transID");
		             Transactions transaction = transactionmap.get(id);
		             if(transaction == null) {
		            	     transaction = new Transactions();
		   		    	     transaction.setTransID(rs.getInt("transID"));
				    	     transaction.setAmount(rs.getDouble("amount"));
				    	     transaction.setType(TransType.valueOf(rs.getString("type")));
				    	     transactionmap.put(rs.getInt("transID"), transaction);
		             }

					 UserHasTransactions userHasTransactions = new UserHasTransactions();
					 userHasTransactions.setReceivedUserName(rs.getString("receiver_username"));
					 userHasTransactions.setSentUserName(rs.getString("sender_username"));
					 userHasTransactions.setType(rs.getString("type"));
					 		 
					 transaction.getUserHasTransactions().add(userHasTransactions);
					  
		         }
		
		    	return new ArrayList<Transactions>(transactionmap.values());
			   
		  }
	}
	
	private static class TransactionsRowMapper implements RowMapper<Transactions>{
		  
		@Override
		public Transactions mapRow(ResultSet rs, int rowNum) throws SQLException {
			Transactions transactions = new Transactions();
	    	    transactions.setTransID(rs.getInt("transID"));
	    	    transactions.setType(TransType.valueOf(rs.getString("type")));	
	    	    UserHasTransactions uht = new UserHasTransactions();
	    	    uht.setSentUserName(rs.getString("sender_username"));
	    	    uht.setReceivedUserName(rs.getString("receiver_username"));
	    	    List<UserHasTransactions> userHasTransactions = new ArrayList<>();
	    	    userHasTransactions.add(uht);
	    	    transactions.setUserHasTransactions(userHasTransactions);
	    	    transactions.setAmount(rs.getDouble("amount"));
			return transactions;
		}
	}

	private static class TransactionsRowMapper2 implements RowMapper<Transactions>{
		  
		@Override
		public Transactions mapRow(ResultSet rs, int rowNum) throws SQLException {
			Transactions transactions = new Transactions();
	    	    transactions.setTransID(rs.getInt("transID"));
	    	    transactions.setType(TransType.valueOf(rs.getString("type")));
	    	    transactions.setAmount(rs.getDouble("amount"));
			return transactions;
		}
	}

}
