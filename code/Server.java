import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

public class Server {
	
	ArrayList<ArrayList<Integer>> candidates = new ArrayList<ArrayList<Integer>>();
	boolean queryFlag = true;
	long minutil = 0;
	int queryCount = 0;
	long patternCount = 0;
	long candidateCount = 0;
	HashMap<Integer, Integer> isNotExtend = new HashMap<Integer, Integer>();
	HashMap<Integer, HashMap<Integer, Integer>> CoUM = new HashMap<Integer, HashMap<Integer, Integer>>();
	ArrayList<Integer> extendRange = new ArrayList<Integer>();
	
	HashMap<Integer, Long> TUM = new HashMap<Integer, Long>();
	HashMap<Integer, Long> TEUM = new HashMap<Integer, Long>();	
	
	// HashMap<Integer, Long> MUM = new HashMap<Integer, Long>();
	
	
	BufferedWriter writer = null;  
	
	// load items
	public void loadItem(String itemInput, String output) throws IOException {
		writer = new BufferedWriter(new FileWriter(output));
		BufferedReader myInput = null;
		String thisLine;
		try {
			myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(itemInput))));
			while ((thisLine = myInput.readLine()) != null) {
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}
				String item = thisLine; 
				candidates.add(new ArrayList<>(Arrays.asList(Integer.valueOf(item))));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
		candidateCount += candidates.size();
	}
	
	void printCandidates() {
		for(ArrayList<Integer> x : candidates) {
			System.out.print(x + " ");
		}
		System.out.println();
	}
	
	void printExtendRange() {
		for(Integer x : extendRange) {
			System.out.print(x + " ");
		}
		System.out.println();
	}

	
	void setMinutil(long minutil) {
		this.minutil = minutil;
	}
	
	void setQueryCount(int queryCount) {
		this.queryCount = queryCount;
	}

	public void deal(int i, BigInteger uSum, BigInteger ruSum) throws IOException {
		if(uSum.longValue() >= minutil) {
			writeOut(candidates.get(i), uSum.longValue());
		}else if (uSum.longValue() + ruSum.longValue() < minutil) {
			isNotExtend.put(i, 1);
		}
	}
	
	// remove unpromising 1-items
	public boolean dealWithDel(int i, BigInteger uSum, BigInteger ruSum, BigInteger twuSum) throws IOException {
		// System.out.println(candidates.get(i) + " " + uSum + " " + ruSum + " " + twuSum);
		if(twuSum.longValue() < minutil) {
			candidates.remove(i);
			return true;
		}
		if(uSum.longValue() >= minutil) {
			writeOut(candidates.get(i), uSum.longValue());
		}else if (uSum.longValue() + ruSum.longValue() < minutil) {
			isNotExtend.put(i, 1);
		}
		return false;
	}
	
	// remove unpromising 1-items and record them
	public boolean dealWithDelTEU(int i, BigInteger uSum, BigInteger ruSum, BigInteger twuSum, int round) throws IOException {
		// System.out.println(candidates.get(i) + " " + uSum + " " + ruSum + " " + twuSum);
		if(round == 1) {
			TEUM.put(candidates.get(i).get(0), uSum.longValue() + ruSum.longValue());
		}
		ArrayList<Integer> removeIndex = new ArrayList<Integer>();
		if(twuSum.longValue() < minutil) {
			if(isNotExtend.get(i) != null) {
				isNotExtend.remove(i);
			}
			Set<Integer> keys = isNotExtend.keySet();
	        for (Integer t: keys) {
	        	if(t > i) {
	        		removeIndex.add(t);
	        	}
	        }
	        for(Integer t : removeIndex) {
        		isNotExtend.remove(t);
        		isNotExtend.put(t-1, 1);
	        }
			candidates.remove(i);
			return true;
		}
		if(uSum.longValue() >= minutil) {
			writeOut(candidates.get(i), uSum.longValue());
		}else if (uSum.longValue() + ruSum.longValue() < minutil) {
			isNotExtend.put(i, 1);
		}
		if(isNotExtend.get(i) == null) {
			TUM.put(i, uSum.longValue());
		}
		return false;
	}
	
	
	public boolean dealWithDelTEUOR(int i, BigInteger totalSum, BigInteger bmaxU, int round) throws IOException {
		long twuSum = totalSum.mod(bmaxU).longValue();
		totalSum = totalSum.divide(bmaxU);
		long ruSum = totalSum.mod(bmaxU).longValue();
		totalSum = totalSum.divide(bmaxU);
		long uSum = totalSum.mod(bmaxU).longValue();
		// System.out.println(candidates.get(i) + " " + uSum + " " + ruSum + " " + twuSum);
		if(round == 1) {
			TEUM.put(candidates.get(i).get(0), uSum + ruSum);
		}
		ArrayList<Integer> removeIndex = new ArrayList<Integer>();
		if(twuSum < minutil) {
			if(isNotExtend.get(i) != null) {
				isNotExtend.remove(i);
			}
			Set<Integer> keys = isNotExtend.keySet();
	        for (Integer t: keys) {
	        	if(t > i) {
	        		removeIndex.add(t);
	        	}
	        }
	        for(Integer t : removeIndex) {
        		isNotExtend.remove(t);
        		isNotExtend.put(t-1, 1);
	        }
			candidates.remove(i);
			return true;
		}
		if(uSum >= minutil) {
			writeOut(candidates.get(i), uSum);
		}else if (uSum + ruSum < minutil) {
			isNotExtend.put(i, 1);
		}
		if(round == 1 || isNotExtend.get(i) == null) {
			TUM.put(i, uSum);
		}
		return false;
	}
	
	// remove unpromising 1-items using one response
	public boolean dealWithDelOR(int i, BigInteger totalSum, BigInteger bmaxU) throws IOException {
		long twuSum = totalSum.mod(bmaxU).longValue();
		totalSum = totalSum.divide(bmaxU);
		long ruSum = totalSum.mod(bmaxU).longValue();
		totalSum = totalSum.divide(bmaxU);
		long uSum = totalSum.mod(bmaxU).longValue();
		// System.out.println(candidates.get(i) + " " + uSum + " " + ruSum + " " + twuSum);
		if(twuSum < minutil) {
			candidates.remove(i);
			return true;
		}
		if(uSum >= minutil) {
			writeOut(candidates.get(i), uSum);
		}else if (uSum + ruSum < minutil) {
			isNotExtend.put(i, 1);
		}
		return false;
	}
	
	
	public ArrayList<Integer> dealWithDelORList(int start, int round, BigInteger totalSum, long maxU, int queryTime) throws IOException {
		ArrayList<Integer> isDelList = new ArrayList<Integer>();
		
		ArrayList<Long> uSumList = new ArrayList<Long>();
		ArrayList<Long> ruSumList = new ArrayList<Long>();
		ArrayList<Long> twuSumList = new ArrayList<Long>();
		
		while(totalSum.compareTo(BigInteger.ZERO) > 0) {
			twuSumList.add(totalSum.mod(BigInteger.valueOf(maxU)).longValue());
			totalSum = totalSum.divide(BigInteger.valueOf(maxU));
			ruSumList.add(totalSum.mod(BigInteger.valueOf(maxU)).longValue());
			totalSum = totalSum.divide(BigInteger.valueOf(maxU));
			uSumList.add(totalSum.mod(BigInteger.valueOf(maxU)).longValue());
			totalSum = totalSum.divide(BigInteger.valueOf(maxU));
		}
		
		while(twuSumList.size() < queryTime) {
			twuSumList.add((long) 0);
			ruSumList.add((long) 0);
			uSumList.add((long) 0);
		}
		
		for(int i = queryTime-1; i >= 0; i--) {
			long twuSum = twuSumList.get(i);
			long ruSum = ruSumList.get(i);
			long uSum = uSumList.get(i);
			
			// System.out.println(candidates.get(start) + " " + uSum + " " + ruSum + " " + twuSum);
			
			if(twuSum < minutil) {
				if(round == 1) {
					isDelList.add(candidates.get(start).get(0));
				}else {
					isDelList.add(0);
				}
				candidates.remove(start);
				start--;
			}else if(uSum >= minutil) {
				writeOut(candidates.get(start), uSum);
			}else if (uSum + ruSum < minutil) {
				isNotExtend.put(start, 1);
			}
			start++;
		}
		return isDelList;
	}
	
	public ArrayList<Integer> dealWithDelORListOnlyTWU(int start, int round, BigInteger totalSum, long maxU, int queryTime) throws IOException {
		ArrayList<Integer> isDelList = new ArrayList<Integer>();
		
		ArrayList<Long> uSumList = new ArrayList<Long>();
		ArrayList<Long> twuSumList = new ArrayList<Long>();
		
		while(totalSum.compareTo(BigInteger.ZERO) > 0) {
			twuSumList.add(totalSum.mod(BigInteger.valueOf(maxU)).longValue());
			totalSum = totalSum.divide(BigInteger.valueOf(maxU));
			uSumList.add(totalSum.mod(BigInteger.valueOf(maxU)).longValue());
			totalSum = totalSum.divide(BigInteger.valueOf(maxU));
		}
		
		while(twuSumList.size() < queryTime) {
			twuSumList.add((long) 0);
			uSumList.add((long) 0);
		}
		
		for(int i = queryTime-1; i >= 0; i--) {
			long twuSum = twuSumList.get(i);
			long uSum = uSumList.get(i);
			
			// System.out.println(candidates.get(start) + " " + uSum + " " + ruSum + " " + twuSum);
			
			if(twuSum < minutil) {
				if(round == 1) {
					isDelList.add(candidates.get(start).get(0));
				}else {
					isDelList.add(0);
				}
				candidates.remove(start);
				start--;
			}else if(uSum >= minutil) {
				writeOut(candidates.get(start), uSum);
			}
			start++;
		}
		return isDelList;
	}
	
	// using extend range
	public boolean dealWithDelER(int i, BigInteger uSum, BigInteger ruSum, BigInteger twuSum) throws IOException {
		if(twuSum.longValue() < minutil) {
			candidates.remove(i);
			for(int j = extendRange.size() - 1; j >= 0; j--) {
				int t = extendRange.get(j);
				if(t >= i) {
					extendRange.set(j, t-1);
				}else {
					break;
				}
			}
			return true;
		}
		if(uSum.longValue() >= minutil) {
			writeOut(candidates.get(i), uSum.longValue());
		}else if (uSum.longValue() + ruSum.longValue() < minutil) {
			isNotExtend.put(i, 1);
		}
		return false;
	}
	
	public boolean dealWithDelORER(int i, BigInteger totalSum, BigInteger bmaxU) throws IOException {
		long twuSum = totalSum.mod(bmaxU).longValue();
		totalSum = totalSum.divide(bmaxU);
		long ruSum = totalSum.mod(bmaxU).longValue();
		totalSum = totalSum.divide(bmaxU);
		long uSum = totalSum.mod(bmaxU).longValue();
		if(twuSum < minutil) {
			candidates.remove(i);
			for(int j = extendRange.size() - 1; j >= 0; j--) {
				int t = extendRange.get(j);
				if(t >= i) {
					extendRange.set(j, t-1);
				}else {
					break;
				}
			}
			return true;
		}
		if(uSum >= minutil) {
			writeOut(candidates.get(i), uSum);
		}else if (uSum + ruSum < minutil) {
			isNotExtend.put(i, 1);
		}
		return false;
	}
	
	public ArrayList<Integer> dealWithDelORERList(int start, int end, int round, BigInteger totalSum, long maxU, int queryTime) throws IOException {
		ArrayList<Integer> isDelList = new ArrayList<Integer>();
		
		ArrayList<Long> uSumList = new ArrayList<Long>();
		ArrayList<Long> ruSumList = new ArrayList<Long>();
		ArrayList<Long> twuSumList = new ArrayList<Long>();
		
		while(totalSum.compareTo(BigInteger.ZERO) > 0) {
			twuSumList.add(totalSum.mod(BigInteger.valueOf(maxU)).longValue());
			totalSum = totalSum.divide(BigInteger.valueOf(maxU));
			ruSumList.add(totalSum.mod(BigInteger.valueOf(maxU)).longValue());
			totalSum = totalSum.divide(BigInteger.valueOf(maxU));
			uSumList.add(totalSum.mod(BigInteger.valueOf(maxU)).longValue());
			totalSum = totalSum.divide(BigInteger.valueOf(maxU));
		}
		
		while(twuSumList.size() < queryTime) {
			twuSumList.add((long) 0);
			ruSumList.add((long) 0);
			uSumList.add((long) 0);
		}
		
		for(int i = twuSumList.size()-1; i >= 0; i--) {
			long twuSum = twuSumList.get(i);
			long ruSum = ruSumList.get(i);
			long uSum = uSumList.get(i);
			
			// System.out.println(candidates.get(start) + " " + uSum + " " + ruSum + " " + twuSum);
			
			if(twuSum < minutil) {
				if(round == 1) {
					isDelList.add(candidates.get(start).get(0));
				}else {
					isDelList.add(0);
				}
				candidates.remove(start);
				for(int j = extendRange.size() - 1; j >= 0; j--) {
					int t = extendRange.get(j);
					if(t >= start) {
						extendRange.set(j, t-1);
					}else {
						break;
					}
				}
			}else if(uSum >= minutil) {
				writeOut(candidates.get(start), uSum);
			}else if (uSum + ruSum < minutil) {
				isNotExtend.put(start, 1);
			}
			start++;
		}
		return isDelList;
	}
	
	
	// using CoUM
	public boolean dealWithDelRT(ArrayList<Integer>  candidate, int i, BigInteger uSum, BigInteger ruSum, BigInteger twuSum) throws IOException {
		if(twuSum.longValue() < minutil) {
			candidates.remove(i);
			HashMap<Integer, Integer> hm = CoUM.get(candidate.get(0));
			if(hm == null) {
				hm = new HashMap<Integer, Integer>();
				hm.put(candidate.get(1), 1);
				CoUM.put(candidate.get(0), hm);
			}else {
				hm.put(candidate.get(1), 1);
				CoUM.put(candidate.get(0), hm);
			}
			return true;
		}
		if(uSum.longValue() >= minutil) {
			writeOut(candidates.get(i), uSum.longValue());
		}else if (uSum.longValue() + ruSum.longValue() < minutil) {
			isNotExtend.put(i, 1);
		}
		return false;
	}
	
	// generate candidates
	public void generateCandidate() {
		if(candidates.size() == 0) {
			queryFlag = false;
			return;
		}
		ArrayList<ArrayList<Integer>> newCandidates = new ArrayList<ArrayList<Integer>>();
		loopi: for(int i = 0; i < candidates.size()-1; i++) {
			if(isNotExtend.get(i) != null) {
				continue;
			}
			ArrayList<Integer> itemI = candidates.get(i);
			for(int j = i+1; j < candidates.size(); j++) {
				ArrayList<Integer> itemJ = candidates.get(j);
				boolean isSharePresix = judgePrefix(itemI, itemJ);
				if(!isSharePresix) {
					continue loopi;
				}
				ArrayList<Integer> itemK = new ArrayList<Integer>();
				itemK.addAll(itemI);
				itemK.add(itemJ.get(itemJ.size()-1));
				newCandidates.add(itemK);
			}
		}
		isNotExtend.clear();
		candidates.clear();
		candidates.addAll(newCandidates);
		candidateCount += candidates.size();
		newCandidates = null;
	}
	
	// using TEU
	public void generateCandidateTEU() {
		if(candidates.size() == 0) {
			queryFlag = false;
			return;
		}
		ArrayList<ArrayList<Integer>> newCandidates = new ArrayList<ArrayList<Integer>>();
		HashMap<Integer, Integer> newNotExtend = new HashMap<Integer, Integer>();

		loopi: for(int i = 0; i < candidates.size()-1; i++) {
			if(isNotExtend.get(i) != null) {
				continue;
			}

			ArrayList<Integer> itemI = candidates.get(i);
			for(int j = i+1; j < candidates.size(); j++) {
				ArrayList<Integer> itemJ = candidates.get(j);
				boolean isSharePresix = judgePrefix(itemI, itemJ);
				if(!isSharePresix) {
					continue loopi;
				}
				ArrayList<Integer> itemK = new ArrayList<Integer>();
				Integer lastItem = itemJ.get(itemJ.size()-1);
//				// 2, 23
//				if(candidates.get(i).size() == 2 && candidates.get(i).get(0).equals(2)
//						&& candidates.get(i).get(1).equals(23)
//						) {
//					System.out.println(itemI + " " + lastItem + " " + (TUM.get(i) + TEUM.get(lastItem)));
//				}	
				// System.out.println(TUM.get(i) + " " + i);
				
				if(TUM.get(i) + TEUM.get(lastItem) < minutil) {
					newNotExtend.put(newCandidates.size(), 1);
				}
				itemK.addAll(itemI);
				itemK.add(lastItem);
				newCandidates.add(itemK);
			}
		}
		isNotExtend.clear();
		isNotExtend.putAll(newNotExtend);
		TUM.clear();
		candidates.clear();
		candidates.addAll(newCandidates);
		candidateCount += candidates.size();
		newCandidates = null;
	}
	
	// build the MUM
//	void buildMUM() {
//		long utility = 0;
//		for(int i = candidates.size() - 1; i >= 0; i--) {
//			MUM.put(candidates.get(i).get(0), TUM.get(i)+utility);
//			utility = TUM.get(i) + utility;
//		}
//	}
	
	// using extendRange
	public void generateCandidateER() {
		// printExtendRange();
		// printCandidates();
		if(candidates.size() == 0) {
			queryFlag = false;
			return;
		}
		ArrayList<ArrayList<Integer>> newCandidates = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> newExtendRange = new ArrayList<Integer>();
		if(candidates.get(0).size() == 1) {
			for(int i = 0; i < candidates.size()-1; i++) {
				if(isNotExtend.get(i) != null) {
					continue;
				}
				ArrayList<Integer> itemI = candidates.get(i);
				for(int j = i+1; j < candidates.size(); j++) {
					ArrayList<Integer> itemJ = candidates.get(j);
					ArrayList<Integer> itemK = new ArrayList<Integer>();
					itemK.addAll(itemI);
					itemK.add(itemJ.get(itemJ.size()-1));
					newCandidates.add(itemK);
				}
				newExtendRange.add(newCandidates.size()-1);
			}
		}else {
			int rangeIndex = 0;
			int range = extendRange.get(rangeIndex);
			loopi: for(int i = 0; i < candidates.size()-1; i++) {
				while(i > range) {
					rangeIndex++;
					range = extendRange.get(rangeIndex);
				}
				if(isNotExtend.get(i) != null) {
					continue;
				}
				ArrayList<Integer> itemI = candidates.get(i);
				for(int j = i+1; j < candidates.size(); j++) {
					if(j > range) {
						newExtendRange.add(newCandidates.size()-1);
						continue loopi;
					}
					ArrayList<Integer> itemJ = candidates.get(j);
					ArrayList<Integer> itemK = new ArrayList<Integer>();
					itemK.addAll(itemI);
					itemK.add(itemJ.get(itemJ.size()-1));
					newCandidates.add(itemK);
				}
				newExtendRange.add(newCandidates.size()-1);
			}
		}
		isNotExtend.clear();
		candidates.clear();
		candidates.addAll(newCandidates);
		extendRange.clear();
		extendRange.addAll(newExtendRange);
		candidateCount += candidates.size();
		newCandidates = null;
	}
	
	
	public void generateCandidateRT() {
		if(candidates.size() == 0) {
			queryFlag = false;
			return;
		}
		ArrayList<ArrayList<Integer>> newCandidates = new ArrayList<ArrayList<Integer>>();
		loopi: for(int i = 0; i < candidates.size()-1; i++) {
			if(isNotExtend.get(i) != null) {
				continue;
			}
			ArrayList<Integer> itemI = candidates.get(i);
			Integer itemII = itemI.get(itemI.size() - 1);
			for(int j = i+1; j < candidates.size(); j++) {
				ArrayList<Integer> itemJ = candidates.get(j);
				Integer itemJJ = itemJ.get(itemJ.size() - 1);
				if(CoUM.get(itemII) != null && CoUM.get(itemII).get(itemJJ) != null) {
					continue;
				}
				boolean isSharePresix = judgePrefix(itemI, itemJ);
				if(!isSharePresix) {
					continue loopi;
				}
				ArrayList<Integer> itemK = new ArrayList<Integer>();
				itemK.addAll(itemI);
				itemK.add(itemJ.get(itemJ.size()-1));
				newCandidates.add(itemK);
			}
		}
		isNotExtend.clear();
		candidates.clear();
		candidates.addAll(newCandidates);
		candidateCount += candidates.size();
		newCandidates = null;
	}

	private boolean judgePrefix(ArrayList<Integer> itemI, ArrayList<Integer> itemJ) {
		if(itemI.size() == 1) {
			return true;
		}
		for(int i = 0; i < itemI.size()-1; i++) {
			if(!itemI.get(i).equals(itemJ.get(i))) {
				return false;
			}
		}
		return true;
	}

	private void writeOut(ArrayList<Integer> itemList, long utility) throws IOException {
		patternCount++; 
	
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < itemList.size(); i++) {
			buffer.append(itemList.get(i));
			buffer.append(' ');
		}
		buffer.append(" #UTIL: ");
		buffer.append(utility);
		// write to file
		writer.write(buffer.toString());
		writer.newLine();
		
	}

	public void printStats() {
		System.out.println("High-utility itemsets count : " + patternCount); 
		System.out.println("Candidate count : " + candidateCount); 
		
	}
	
//	private static int sum = 0 ;
//	public static  void  main(String [] arg) throws InterruptedException {
//		CountDownLatch countDownLatch = new CountDownLatch(10);
//		for(int i = 1; i <= 10; i++) {
//			new Thread(()->{
//				sum++;
//				countDownLatch.countDown();		
//			}).start();
//		}
//		countDownLatch.await();
//		System.out.println(sum);
//	} 
	
}
