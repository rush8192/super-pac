package org.buyingyourfuture.data;

import java.util.HashMap;
import java.util.Map;

public class Committee {
	public final String id;
	public final String name;
	public final String city;
	public final String state;
	public final String zip;
	public final String type;
	public final Integer affiliation;
	public final String association;
	
	private Map<Contributor, Integer> contributors;
	
	public int numContributors() {
		return contributors.size();
	}
	
	public Committee(String id, String name, String city, String state, String zip, String type, int affiliation, String association) {
		this.id = id;
		this.name = name;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.type = type;
		this.affiliation = affiliation;
		this.association = association;
		this.contributors = new HashMap<Contributor, Integer>();
	}
	
	public boolean noContributions() {
		return contributors.size() == 0;
	}
	
	public int totalContributions() {
		int total = 0;
		for (int contrib : contributors.values()) {
			total += contrib;
		}
		return total;
	}
	
	public void addContributor(Contributor c, int amount) {
		contributors.put(c, amount);
	}
	
	public boolean isSuperPAC() {
		return "O".equals(type);
	}
	
	public String toString() {
		return name;
	}
	
	public int getSmallerDonerTotal(int min) {
		int total = 0;
		for (Contributor c : contributors.keySet()) {
			if (contributors.get(c) < min) {
				total += contributors.get(c);
			}
		}
		return total;
	}
	
	public String toJson(int minDonation) {
		StringBuilder jsonString = new StringBuilder();
		jsonString.append("{\n");
		jsonString.append("\t\"id\":\"" + id +"\",\n");
		jsonString.append("\t\"name\":\"" + name +"\",\n");
		jsonString.append("\t\"city\":\"" + city +"\",\n");
		jsonString.append("\t\"state\":\"" + state +"\",\n");
		jsonString.append("\t\"zip\":\"" + zip +"\",\n");
		jsonString.append("\t\"type\":\"" + type +"\",\n");
		jsonString.append("\t\"affiliation\":" + affiliation + ",\n");
		jsonString.append("\t\"association\":\"" + association +"\",\n");
		jsonString.append("\t\"funds\":" + this.totalContributions() + ",\n");
		jsonString.append("\t\"contributors\": [\n");
		
		Map<Contributor, Integer> contributorsPlusSmall = new HashMap<Contributor, Integer>(contributors);
		Contributor smallDoner = new Contributor("Smaller Doners (< $" + minDonation + " each)", "Small Town", "US", "00000");
		int smallDonerTotal = getSmallerDonerTotal(minDonation);
		contributorsPlusSmall.put(smallDoner, smallDonerTotal);
		
		StringBuilder contribString = new StringBuilder();
		ContributionData.outputMinAmountContributors(contributorsPlusSmall.keySet(), Integer.MAX_VALUE, minDonation, contributorsPlusSmall, contribString, false);
		jsonString.append(contribString);
		jsonString.append("\t]\n");
		jsonString.append("}");
		System.out.println(jsonString.toString());
		return jsonString.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof Committee))
			return false;
		return ((Committee) o).id.equals(this.id);
	}
	
	@Override
	public int hashCode() {
		return this.id.hashCode();
	}
}