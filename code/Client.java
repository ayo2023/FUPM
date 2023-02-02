import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Client {
	
	String name;
	
	ArrayList<ArrayList<Item>> transactions = new ArrayList<ArrayList<Item>>();
	ArrayList<Integer> TWUList = new ArrayList<Integer>();
	// a hashmap for storing ciphertext
	ConcurrentHashMap<BigInteger, BigInteger> enMap = new ConcurrentHashMap<BigInteger, BigInteger>();
	HashMap<Integer, Integer> visitMap = new HashMap<Integer, Integer>();
	int totalItem = 0;
		

	public Client(String name) {
		this.name = name;
	}

	public Vector<BigInteger> query(ArrayList<Integer> candidate) {
		Vector<BigInteger> res = new Vector<BigInteger>();
		long uSum = 0;
		long ruSum = 0;
		for(int i = 0; i < transactions.size(); i++) {
			int k = 0;
			long u = 0;
			long ru = 0;
			for(int j = 0; j < transactions.get(i).size(); j++) {
				Item item = transactions.get(i).get(j);
				if(k < candidate.size() && item.name > candidate.get(k)) {
					break;
				}else if(k < candidate.size() && item.name.equals(candidate.get(k))) {
					u += item.utility;
					k++;
				}else if (k >= candidate.size()){
					ru += item.utility;
				}
			}
			if(k == candidate.size()) {
				uSum += u;
				ruSum += ru;
			}
		}
		res.add(BigInteger.valueOf(uSum));
		res.add(BigInteger.valueOf(ruSum));
		return res;
	}

	
	public Vector<BigInteger> queryWithTWU(ArrayList<Integer> candidate, boolean isRemoveItem) {
		if(isRemoveItem) {
			for(int i = 0; i < candidate.size(); i++) {
				visitMap.put(candidate.get(i), 1);
			}
		}
		Vector<BigInteger> res = new Vector<BigInteger>();
		long uSum = 0;
		long ruSum = 0;
		long twuSum = 0;
		for(int i = 0; i < transactions.size(); i++) {
			int k = 0;
			long u = 0;
			long ru = 0;
			for(int j = 0; j < transactions.get(i).size(); j++) {
				Item item = transactions.get(i).get(j);
				if(k < candidate.size() && item.name > candidate.get(k)) {
					break;
				}else if(k < candidate.size() && item.name.equals(candidate.get(k))) {
					u += item.utility;
					ru = item.remainUtility;
					k++;
				}else if (k >= candidate.size()){
					break;
					// ru += item.utility;
				}
			}
			if(k == candidate.size()) {
				uSum += u;
				ruSum += ru;
				twuSum += TWUList.get(i);
			}
		}
		res.add(BigInteger.valueOf(uSum));
		res.add(BigInteger.valueOf(ruSum));
		res.add(BigInteger.valueOf(twuSum));
		return res;
	}
	
	
	public Vector<BigInteger> queryOnlyTWU(ArrayList<Integer> candidate, boolean isRemoveItem) {
		if(isRemoveItem) {
			for(int i = 0; i < candidate.size(); i++) {
				visitMap.put(candidate.get(i), 1);
			}
		}
		Vector<BigInteger> res = new Vector<BigInteger>();
		long uSum = 0;
		long twuSum = 0;
		for(int i = 0; i < transactions.size(); i++) {
			int k = 0;
			long u = 0;
			for(int j = 0; j < transactions.get(i).size(); j++) {
				Item item = transactions.get(i).get(j);
				if(k < candidate.size() && item.name > candidate.get(k)) {
					break;
				}else if(k < candidate.size() && item.name.equals(candidate.get(k))) {
					u += item.utility;
					k++;
				}else if (k >= candidate.size()){
					break;
					// ru += item.utility;
				}
			}
			if(k == candidate.size()) {
				uSum += u;
				twuSum += TWUList.get(i);
			}
		}
		res.add(BigInteger.valueOf(uSum));
		res.add(BigInteger.valueOf(twuSum));
		return res;
	}
	
	
	public Vector<BigInteger> queryWithTWUList(List<ArrayList<Integer>> queryCandidates, boolean isRemoveItem) {
		if(isRemoveItem) {
			for(int i = 0; i < queryCandidates.size(); i++) {
				for(int j = 0; j < queryCandidates.get(i).size(); j++) {
					visitMap.put(queryCandidates.get(i).get(j), 1);
				}
			}
		}
		int queryCount = queryCandidates.size();
		if(queryCount == 0) {
			return new Vector<BigInteger>();
		}
		int candidateSize = queryCandidates.get(0).size();
		ArrayList<Integer> lastItemList = new ArrayList<Integer>();
		for(int i = 0; i < queryCount; i++) {
			lastItemList.add(queryCandidates.get(i).get(queryCandidates.get(i).size()-1));
		}
				
		ArrayList<Integer> lastCandidate = queryCandidates.get(queryCandidates.size()-1);
		Integer largeItem = lastCandidate.get(lastCandidate.size()-1);
		Vector<BigInteger> res = new Vector<BigInteger>();
		ArrayList<Long> uSumList = new ArrayList<Long>();
		ArrayList<Long> ruSumList = new ArrayList<Long>();
		ArrayList<Long> twuSumList = new ArrayList<Long>();
		
		for(int i = 0; i < queryCount; i++) {
			uSumList.add((long) 0);
			ruSumList.add((long) 0);
			twuSumList.add((long) 0);
		}
		
		

		for(int i = 0; i < transactions.size(); i++) {
			int k = 0;
			long u = 0;
			long ru = 0;
			for(int j = 0; j < transactions.get(i).size(); j++) {
				Item item = transactions.get(i).get(j);
				if(k < candidateSize && item.name > largeItem) {
					break;
				}else if(k < candidateSize-1 && item.name.equals(lastCandidate.get(k))) {
					u += item.utility;
					k++;
				}else if (k == candidateSize-1){
					for(int p = 0; p < queryCount; p++) {
						Integer e = lastItemList.get(p);
						if(e.equals(item.name)) {
							uSumList.set(p, uSumList.get(p) + (u + item.utility));
							ruSumList.set(p, ruSumList.get(p) + item.remainUtility);
							twuSumList.set(p, twuSumList.get(p) + TWUList.get(i));
							break;
						}
					}
				}
			}
		}
		
		for(int i = 0; i < queryCount; i++) {
			res.add(BigInteger.valueOf(uSumList.get(i)));
			res.add(BigInteger.valueOf(ruSumList.get(i)));
			res.add(BigInteger.valueOf(twuSumList.get(i)));
		}

		return res;
	}
	
	
	
	void removeItems(Integer removeItem, boolean isRemoveItem) {
		if(isRemoveItem) {
			if(visitMap.get(removeItem) != null) {
				visitMap.remove(removeItem);
			}
			totalItem -= 1;
		}		
		for(int i = 0; i < transactions.size(); i++) {
			ArrayList<Item> transaction = transactions.get(i);
			boolean removeFlag = false;
			int removeUtility = 0;
			for(int j = transaction.size()-1; j >= 0; j--) {
				Item item = transaction.get(j);
				if(item.name < removeItem && !removeFlag) {
					break;
				}else if(item.name < removeItem) {
					item.remainUtility -= removeUtility;
				}else if(item.name.equals(removeItem)) {
					removeUtility = item.utility;
					TWUList.set(i, TWUList.get(i) - removeUtility);				
					transaction.remove(j);
				}
			}
		}
	}
	
	void removeJudge() {
		if(totalItem > visitMap.size()) {
			System.out.println("===");
			Collection<Integer> c = visitMap.keySet();
			Object[] obj = c.toArray();
			Integer minItem = Integer.MAX_VALUE;
			Integer maxItem = Integer.MIN_VALUE;
			for(int i = 0; i < obj.length; i++) {
				if(minItem > (int)obj[i]) {
					minItem = (int)obj[i]; 
				}
				if(maxItem < (int)obj[i]) {
					maxItem = (int)obj[i]; 
				}
			}
			for(int i = 0; i < transactions.size(); i++) {
				ArrayList<Item> transaction = transactions.get(i);
				boolean removeFlag = false;
				int removeUtility = 0;
				for(int j = transaction.size()-1; j >= 0; j--) {
					Item item = transaction.get(j);
					if(item.name < minItem && !removeFlag) {
						break;
					}else if(item.name > maxItem) {
						continue;
					}else {
						if(visitMap.get(item.name) != null) {
							item.remainUtility -= removeUtility;
							continue;
						}
						removeUtility += item.utility;
						TWUList.set(i, TWUList.get(i) - item.utility);				
						transaction.remove(j);
					}
				}
			}
		}
		totalItem = visitMap.size();
		visitMap.clear();
	}
	
		
	@Override
	public String toString() {
		return "Client [name=" + name + "]";
	}
	
	public void printTransactions() {
		for(ArrayList<Item> t : transactions) {
			System.out.println(t);
		}
	}

	public BigInteger queryTU() {
		long tu = 0;
		for(int e: TWUList) {
			tu += e;
		}
		return BigInteger.valueOf(tu);
	}
	

}
