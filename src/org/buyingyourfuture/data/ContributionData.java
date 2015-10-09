package org.buyingyourfuture.data;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ContributionData {
	
	public static final int NUM_PACS = 35;
	public static final int MIN_CONTRIBUTION = 24000;
	
	public static Map<String, Committee> getPACs(DBHandle db) {
		Map<String, Committee> superPACs = db.getAllSuperPACs(true);
		System.out.println("Found " + superPACs.size() + " PACSs");
		return superPACs;
	}
	
	public static Set<Contributor> getContributors(DBHandle db) {
		Set<Contributor> contributors = db.getAllContributors();
		System.out.println("Found " + contributors.size() + " contributors");
		return contributors;
	}
	
	public static void linkContributorsAndCommittees(Set<Contributor> contributors, Map<String, Committee> superPACs) {
		for (Contributor c : contributors) {
			for (String donee : c.getContributions().keySet()) {
				superPACs.get(donee).addContributor(c, c.getContributions().get(donee));
			}
		}
	}
	
	public static void exportJSONCommittees(DBHandle db, String outputFile) {
		Map<String, Committee> superPACs = getPACs(db);
		Set<Contributor> contributors = getContributors(db);
		linkContributorsAndCommittees(contributors, superPACs);
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
			
			out.write("{");
			out.write("\"pacs\":[\n");
			StringBuilder pacString = new StringBuilder();
			outputTopNPACs(superPACs, NUM_PACS, MIN_CONTRIBUTION, pacString, false);
			out.write(pacString.toString());
			out.write("]\n");
			out.write("}");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//outputTopNContributors(contributors, 1000);
	}
	
	public static void main(String[] argv) {
		String outputFileName = argv.length > 0 ? argv[0] : "pacData.json";
		DBHandle db = new DBHandle();
		exportJSONCommittees(db, outputFileName);
	}
	
	public static void outputMinAmountContributors(Set<Contributor> contributors, int n, int minAmount, Map<Contributor, Integer> amounts,
			StringBuilder output, boolean print) {
		TreeMap<Integer, List<Contributor>> sortedContribs = new TreeMap<Integer, List<Contributor>>(Collections.reverseOrder());
		for (Contributor c : contributors) {
			int amt = amounts == null ? c.totalContributions() : amounts.get(c);
			if (sortedContribs.containsKey(amt)) {
				sortedContribs.get(amt).add(c);
			} else {
				List<Contributor> contribList = new ArrayList<Contributor>();
				contribList.add(c);
				sortedContribs.put(amt, contribList);
			}
		}
		int onContrib = 0;
		outerloop:
		for (int contributionAmt : sortedContribs.keySet()) {
			for (Contributor c : sortedContribs.get(contributionAmt)) {
				if (contributionAmt < minAmount)
					break outerloop;
				if (print)
					System.out.println(c.toJSON(contributionAmt));
				if (output != null)
					output.append(c.toJSON(contributionAmt)).append(",\n");
				onContrib++;
				if (onContrib == n)
					return;
			}
		}
		// remove trailing comma
		if (onContrib > 0 && output != null)
			output.deleteCharAt(output.length() - 2);
	}
	
	public static void outputTopNPACs(Map<String, Committee> superPACs, int n, int minDonation, StringBuilder output, boolean print) {
		TreeMap<Integer, List<Committee>> sortedPACs = new TreeMap<Integer, List<Committee>>(Collections.reverseOrder());
		for (Committee c : superPACs.values()) {
			if (sortedPACs.containsKey(c.totalContributions())) {
				sortedPACs.get(c.totalContributions()).add(c);
			} else {
				List<Committee> comList = new ArrayList<Committee>();
				comList.add(c);
				sortedPACs.put(c.totalContributions(), comList);
				
			}
		}
		int onPac = 0;
		outerloop:
		for (int contributionAmt : sortedPACs.keySet()) {
			for (Committee c : sortedPACs.get(contributionAmt)) {
				if (output != null)
					output.append(c.toJson(minDonation)).append(",\n");
				if (print)
					System.out.println("Pac : " + c + " with $" + c.totalContributions() + " from " + c.numContributors());
				onPac++;
				if (onPac == n)
					break outerloop;
			}
		}
		if (onPac > 0 && output != null) {
			// delete trailing comma
			output.deleteCharAt(output.length() - 2);
		}
	}
}