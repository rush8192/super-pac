package org.buyingyourfuture.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Contributor {
	
	private static final boolean USE_JACCARD_SIM = true;
	private static final double JACC_THRESHOLD = 0.725;
	private static final int N_GRAM = 3;
	
	private static final int KEY_LEN = 6;
	
	public String name;
	public String city;
	public String state;
	public String zip;
	
	private Map<String, Integer> contributions;
	
	public Contributor(String name, String city, String state, String zip) {
		name = replaceTitles(name);
		this.name = name;
		this.city = city;
		this.state = state;
		this.zip = zip;
		contributions = new HashMap<String, Integer>();
	}
	
	public int totalContributions() {
		int total = 0;
		for (int contribution : contributions.values()) {
			total += contribution;
		}
		return total;
	}
	
	private String replaceTitles(String name) {
		name = name.replace(" MR.", "");
		name = name.replace(" MR", "");
		name = name.replace(" MRS.", "");
		name = name.replace(" MRS", "");
		name = name.replace(" MS.", "");
		name = name.replace(" MS", "");
		return name;
	}
	
	public String getKey() {
		return name.length() > KEY_LEN ? name.substring(0, KEY_LEN) : name;
	}
	
	public void addContribution(String id, int amount) {
		if (contributions.containsKey(id)) {
			contributions.put(id, contributions.get(id) + amount);
		} else {
			contributions.put(id, amount);
		}
	}
	
	public void mergeWith(Contributor other) {
		// dont change name; need key to remain the same, which is based on name
		this.city = this.city != null ? this.city : other.city;
		this.state = this.state != null ? this.state : other.state;
		this.zip = this.zip != null ? this.zip : other.zip;
		for (String donee : other.getContributions().keySet()) {
			if (!contributions.containsKey(donee)) {
				contributions.put(donee, other.getContributions().get(donee));
			} else {
				int totalContribution = contributions.get(donee) + other.getContributions().get(donee);
				contributions.put(donee, totalContribution);
			}
		}
	}
	
	public Map<String, Integer> getContributions() {
		return contributions;
	}
	
	public boolean samePerson(Contributor other) {
		if (USE_JACCARD_SIM) {
			Set<String> ngramsThis = getNgrams();
			Set<String> ngramsOther = other.getNgrams();
			Set<String> intersectionSet = new HashSet<String>(ngramsThis);
			intersectionSet.retainAll(ngramsOther);
			Set<String> unionSet = new HashSet<String>(ngramsThis);
			unionSet.addAll(ngramsOther);
			double intersectionSize = intersectionSet.size();
			return (intersectionSize / unionSet.size()) >= JACC_THRESHOLD;
		}
		return false;
	}
	
	public String toJSON(int amount) {
		StringBuilder jsonStr = new StringBuilder();
		jsonStr.append("{\n");
		jsonStr.append("\t").append("\"name\":\"").append(name).append("\",\n");
		jsonStr.append("\t").append("\"city\":\"").append(city).append("\",\n");
		jsonStr.append("\t").append("\"state\":\"").append(state).append("\",\n");
		jsonStr.append("\t").append("\"zip\":\"").append(zip).append("\",\n");
		jsonStr.append("\t").append("\"amount\":").append(amount).append("\n");
		jsonStr.append("}");
		//System.out.println(jsonStr.toString());
		return jsonStr.toString();
	}
	
	protected Set<String> getNgrams() {
		Set<String> ngrams = new HashSet<String>();
		for (int i = 0; i + N_GRAM < name.length(); i++) {
			ngrams.add(name.substring(i, i + N_GRAM));
		}
		ngrams.add(city);
		ngrams.add(state);
		if (zip.length() < 5) {
			ngrams.add(zip);
		} else {
			ngrams.add(zip.substring(0, 5));
		}
		return ngrams;
	}
	
	@Override
	public String toString() {
		return "CONTRIBUTOR -- name: " + name + " city: " + city + " state " + state + " zip " + zip;
	}
}