package org.buyingyourfuture.data;

import java.sql.*;
import java.util.*;

public class DBHandle {
	
	private Connection conn;
	private static final String DB_NAME = "campaignDB";
	private static final int MAX_CONTRIBS = Integer.MAX_VALUE;  // no limit for now
	
	private static final boolean verboseMerge = false;
	
	public DBHandle(){
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private ResultSet executeQueryStr(String statementString) throws SQLException {
		Statement stmt = conn.createStatement();
		String stmtStr = statementString;
		return stmt.executeQuery(stmtStr);
	}
	
	private void executeUpdateStr(String updateString) throws SQLException {
		Statement stmt = conn.createStatement();
		String stmtStr = updateString;
		stmt.executeUpdate(stmtStr);
	}
	
	public Map<String, Committee> getAllSuperPACs(boolean createPacTable) {
		try {
			Map<String, Committee> superPACs = new HashMap<String, Committee>();
			String queryStr = "SELECT CMTE_ID, CMTE_NM, CMTE_CITY, CMTE_ST, CMTE_ZIP, CMTE_TP, AFFILIATION, ASSOCIATION FROM PacList " + 
					"WHERE CMTE_TP = 'O' OR CMTE_TP = 'U' OR CMTE_TP = 'V' OR CMTE_TP = 'W'";
			ResultSet results = executeQueryStr(queryStr);
			while (results.next()) {
				Committee c = new Committee(results.getString("CMTE_ID"),
											results.getString("CMTE_NM"),
											results.getString("CMTE_CITY"),
											results.getString("CMTE_ST"),
											results.getString("CMTE_ZIP"),
											results.getString("CMTE_TP"),
											results.getInt("AFFILIATION"),
											results.getString("ASSOCIATION"));
				superPACs.put(c.id, c);
			}
			if (createPacTable) {
				String dropTableQuery = "DROP TABLE if exists SuperPACs";
				executeUpdateStr(dropTableQuery);
				String pacTableQuery = "CREATE TABLE SuperPACs as SELECT CMTE_ID FROM PacList WHERE CMTE_TP = 'O' " + 
						"OR CMTE_TP = 'U' OR CMTE_TP = 'V' OR CMTE_TP = 'W'";
				executeUpdateStr(pacTableQuery);
			}
			return superPACs;
		} catch (SQLException e) {
			System.err.println("Error while retrieving super pac data " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	public Set<Contributor> getAllContributors() {
		String allPACContributionsStr = "SELECT CMTE_ID,NAME,CITY,STATE,ZIP_CODE,TRANSACTION_AMT FROM IndCont WHERE CMTE_ID IN SuperPACs";
		try {
			ResultSet results = executeQueryStr(allPACContributionsStr);
			Map<String, List<Contributor>> contributorsByKey = new HashMap<String, List<Contributor>>();
			while (results.next()) {
				Contributor c = new Contributor(results.getString("NAME"),
												results.getString("CITY"),
												results.getString("STATE"),
												results.getString("ZIP_CODE"));
				int amount = results.getInt("TRANSACTION_AMT");
				String donee = results.getString("CMTE_ID");
				c.addContribution(donee, amount);
				checkAndMergeDuplicates(c, contributorsByKey);
			}
			Set<Contributor> contributors = new HashSet<Contributor>();
			for (List<Contributor> contribsForKey : contributorsByKey.values()) {
				for (Contributor c : contribsForKey) {
					contributors.add(c);
				}
			}
			return contributors;
		} catch (SQLException e) {
			System.err.println("Error while retrieving contributor data " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	private void checkAndMergeDuplicates(Contributor c, Map<String, List<Contributor>> contributorsByKey) {
		String contribKey = c.getKey();
		if (contributorsByKey.containsKey(contribKey)) {
			boolean foundDupe = false;
			if (verboseMerge)
				System.out.println("checking " + contributorsByKey.get(contribKey).size() + " entries for key: " + contribKey);
			for (Contributor cOther : contributorsByKey.get(contribKey)) {
				if (cOther.samePerson(c)) {
					if (verboseMerge)
						System.out.println("Merging: " + cOther + " with " + c);
					cOther.mergeWith(c);
					foundDupe = true;
					break;
				} else {
					if (verboseMerge)
						System.out.println("Not the same: " + cOther + " and " + c);
				}
			}
			if (!foundDupe) {
				contributorsByKey.get(contribKey).add(c);
				if (verboseMerge)
					System.out.println("key: " + contribKey + "now has " + contributorsByKey.get(contribKey).size() + " entries");
			}
		} else {
			List<Contributor> contribs = new ArrayList<Contributor>();
			contribs.add(c);
			contributorsByKey.put(contribKey, contribs);
		}
	}
}