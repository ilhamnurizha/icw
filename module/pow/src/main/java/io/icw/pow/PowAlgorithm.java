package io.icw.pow;

public class PowAlgorithm {
	public static boolean stop = false;
	public static String hash = null;
	
	public static void pow(long diff, BlockPow block) {
		stop = false;
		hash = null;
		
		String prefix0 = getPrefix0(diff);
		String hash = block.generationHashCodeBySha256();
		while (!stop) {
			assert prefix0 != null;
			if (hash.startsWith(prefix0)) {
//				System.out.println("pow:" + hash);
				stop = true;
				PowAlgorithm.hash = hash;
				break;
			} else {
				block.setNonce(block.getNonce() + 1);
				block.setTimestamp(System.currentTimeMillis());
				hash = block.generationHashCodeBySha256();
			}
		}
	}

	private static String getPrefix0(long diff) {

		if (diff <= 0) {

			return null;
		}
		return String.format("%0" + diff + "d", 0);
	}

	public static void main(String[] args) {
		BlockPow firstBlock = new BlockPow();
		firstBlock.generateFirstBlock(0);
		System.out.println(firstBlock.toString());
		BlockPow secondBlock = firstBlock.generateNextBlock(1, firstBlock);
		System.out.println(secondBlock.toString());
	}
}