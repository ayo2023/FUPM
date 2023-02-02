import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class Utils {

	public ArrayList<Client> assignClient(int n, int j, String dataInput) throws IOException {
		ArrayList<Client> clients = new ArrayList<Client>();
//		for(int i = 1; i <= n; i++) {
//			Client c = new Client("client"+i);
//			clients.add(c);
//		}
		BufferedReader myInput = null;
		String thisLine;
		try {
			myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(dataInput))));
			int k = 0;
			int m = 1;
			Client c = new Client("client"+m);
			HashSet<Integer> iSet = new HashSet<Integer>();
			while ((thisLine = myInput.readLine()) != null) {
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}
				String split[] = thisLine.split(":"); 
				String items[] = split[0].split(" "); 
				String utilies[] = split[2].split(" "); 
				int transactionUtility = Integer.parseInt(split[1]);  
				ArrayList<Item> itemset = new ArrayList<Item>();
				for(int i = 0; i < items.length; i++){
					Integer item = Integer.parseInt(items[i]);
					Integer Utility = Integer.parseInt(utilies[i]);
					Item ii = new Item(item, Utility);
					itemset.add(ii);
					iSet.add(item);
				}	
				Collections.sort(itemset);
				for(int i = items.length-2; i >= 0; i--) {
					itemset.get(i).setRemainUtility(itemset.get(i+1).remainUtility + itemset.get(i+1).utility);
				}
				c.transactions.add(itemset);
				c.TWUList.add(transactionUtility);
				k++;
				if(k == j) {
					c.totalItem = iSet.size();
					iSet.clear();
					clients.add(c);
					m++;
					if(m > n) {
						break;
					}
					k=0;
					c = new Client("client" + m);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
		return clients;
	}

}
