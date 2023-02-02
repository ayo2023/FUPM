import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

// sixth generation method
// encryption
// 1. use the gr-Paillier algorithm
// 2. one response

// generation
// 1. remove unpromising items in all rounds, and also delete unpromising 1-items (better effect)
// 2. using multiple queries
// 3. using multiple responses

// other
// 1. multiple-thread


public class MainTest {

	static BigInteger uSum =  BigInteger.ZERO;
	static BigInteger ruSum = BigInteger.ZERO;
	static BigInteger twuSum = BigInteger.ZERO;
	static BigInteger totalSum = BigInteger.ZERO;
	
	static BigInteger totalUtility = BigInteger.ZERO;
	static long maxU = 0;
	static BigInteger bmaxU = BigInteger.ZERO;
	
	
	static long encryptCount = 0;
	static int queryTime = 0;

	public static synchronized void add(PublicKey publicKeyG, Vector<BigInteger> res, AtomicInteger reduceE) {
		uSum = uSum.multiply(res.get(0));
		ruSum = ruSum.multiply(res.get(1));
		encryptCount += (2 - reduceE.get());
	}
	
	public static synchronized void addTWU(PublicKey publicKeyG, Vector<BigInteger> res, AtomicInteger reduceE) {
		uSum = uSum.multiply(res.get(0));
		ruSum = ruSum.multiply(res.get(1));
		twuSum = twuSum.multiply(res.get(2));
		encryptCount += (3 - reduceE.get());
	}
	
	// one response
	public static synchronized void addTWUOR(BigInteger res) {
		totalSum = totalSum.multiply(res);
		encryptCount += 1;
	}
	
	public static synchronized void addTotalUtility(PublicKey publicKeyG, BigInteger tu) {
		totalUtility = totalUtility.multiply(tu);
		encryptCount += 1;
	}
	
	// one response for a query list
	public static synchronized BigInteger sumResponse(Vector<BigInteger> ans) {
		int queryCount = ans.size();
		BigInteger res = (ans.get(0).multiply(bmaxU).add(ans.get(1))
				.multiply(bmaxU)).add(ans.get(2));
		int j = 3;
		while(j < queryCount) {
			res = res.multiply(bmaxU).add(ans.get(j));
			j++;
		}
		return res;
	}
	
	
	public static void main(String [] arg) throws IOException, InterruptedException{
		// if need
		MemoryLogger.getInstance().reset();
		
	    long startTime = 0;
		long endTime  = 0;
		startTime = System.currentTimeMillis();
		
	    KeyPairBuilder keygen = new KeyPairBuilder();
	    keygen = keygen.bits(1024);
	    KeyPair keyPairGR = keygen.generateKeyPairUsingGR();
		PublicKey publicKeyGR = keyPairGR.getPublicKey();
		endTime = System.currentTimeMillis(); 
		System.out.println("Time of keypair generation: "+ (endTime - startTime) + "ms");
		
		
		startTime = System.currentTimeMillis();
		// settings
		Utils util = new Utils();
		String itemInput = "./data/Item_foodmart4K.txt";
		String dataInput = "./data/foodmart4K.txt";
		String output = ".//output.txt";
		
		Server server = new Server();
		server.setMinutil(50000);
		server.setQueryCount(5);

		server.loadItem(itemInput, output);
		server.printCandidates();
		
		ArrayList<Client> clients = util.assignClient(10, 400, dataInput);
		endTime = System.currentTimeMillis(); 
		System.out.println("Time of setting: "+ (endTime - startTime) + "ms");

		MemoryLogger.getInstance().checkMemory();
		startTime = System.currentTimeMillis();
		int round = 1;
		BigInteger zero = publicKeyGR.encryptUsingGR(BigInteger.ZERO);
		CountDownLatch firstCountDownLatch = new CountDownLatch(clients.size());
		totalUtility = zero;
		for(Client client : clients) {
			new Thread(()->{
				BigInteger tu = publicKeyGR.encryptUsingGR(client.queryTU());
				addTotalUtility(publicKeyGR, tu);
				firstCountDownLatch.countDown();		
			}).start();
		}
		firstCountDownLatch.await();
		maxU = keyPairGR.decryptCRT(totalUtility).longValue();
		long pu = 1;
		while(maxU > pu) {
			pu *= 10;
		}
		maxU = pu;
		bmaxU = BigInteger.valueOf(maxU);
		while(server.queryFlag) {
			queryTime = server.queryCount;
			for(int i = 0; i < server.candidates.size(); i++) {
				if(i + queryTime > server.candidates.size()) {
					queryTime = server.candidates.size() - i;
				}
				List<ArrayList<Integer>> queryCandidates = server.candidates.subList(i, i + queryTime);
				totalSum = zero;
				CountDownLatch countDownLatch = new CountDownLatch(clients.size());
				for(Client client : clients) {
					new Thread(()->{
						Vector<BigInteger> ans = new Vector<BigInteger>();
						for(int j = 0; j < queryTime; j++) {
							ans.addAll(client.queryWithTWU(queryCandidates.get(j), false));	
						}	
						addTWUOR(publicKeyGR.encryptUsingGR(sumResponse(ans)));
						countDownLatch.countDown();		
					}).start();
				}
				countDownLatch.await();		
				ArrayList<Integer> isDelList = server.dealWithDelORList(i, round, keyPairGR.decryptCRT(totalSum), maxU, queryTime);
				i += (queryTime-1);
				i -= isDelList.size();
				if(round == 1) {
					for(Integer e : isDelList) {
						for(Client client : clients) {
							client.removeItems(e, false);
						}
					}
				}
			}
			server.generateCandidate();
			server.printCandidates();
			round++;
		}
		server.writer.close();
		endTime = System.currentTimeMillis(); 
		MemoryLogger.getInstance().checkMemory();
		System.out.println("Time of mining: "+ (endTime - startTime) + "ms");
		System.out.println("Memory: " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println("Count of encryption: "+ encryptCount);
		server.printStats();
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestBase.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}