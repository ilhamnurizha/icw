package io.icw.pow;

import java.math.BigInteger;

public class PowThread extends Thread {
	
	@Override
    public void run() {
        
    }
	
	public static void main(String args[]) {
//		System.out.println((67262d / 200000l));
//		double d = 67262d;
//		if (d / 200000 < 0.2) {
//			System.out.println("cccc");
//		}
		
		System.out.println(BigInteger.valueOf(2).min(BigInteger.valueOf(1)));
		
		for (int i = 0; i < 10; i++) {
			long start = System.currentTimeMillis();
			System.out.println(PowAlgorithm.hash);
			
			long timeMillis = System.currentTimeMillis();
			BlockPow blockPow = new BlockPow();
			blockPow.setTimestamp(timeMillis);
			blockPow.setDiff(7);
			blockPow.setHeight(100);
			blockPow.setIndex(100);
			blockPow.setPreHash("03fa553f6c1211754e621cda63a794a9cfa4a26d986458c4e8723b8cfd30c962");
			blockPow.setNonce(0);
			blockPow.setAddress("EDAOd6HghV8fuoDD3TNnbHWRMr4V5LQbVtrC6");
			blockPow.setPowTimestamp(timeMillis);
			PowAlgorithm.pow(blockPow.getDiff(), blockPow);
			blockPow.setHashCode(PowAlgorithm.hash);
			long end = System.currentTimeMillis();
			
			System.out.println(blockPow);
			System.out.println(end - start);
		}
	}
}
