package io.icw.pow;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.basic.NulsOutputStreamBuffer;
import io.icw.base.data.BaseNulsData;
import io.icw.core.exception.NulsException;
import io.icw.core.parse.SerializeUtils;

public class BlockPow extends BaseNulsData {
	private static final long serialVersionUID = 8812304394897443102L;

	private String preHash;

	private String hashCode;

	private String address;
	
	private long diff;

	private long height;

	private long index;

	private long nonce;

	private long timestamp;

	private long powTimestamp;

	public String getPreHash() {
		return preHash;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public long getHeight() {
		return height;
	}

	public long getPowTimestamp() {
		return powTimestamp;
	}

	public void setPowTimestamp(long powTimestamp) {
		this.powTimestamp = powTimestamp;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setNonce(long nonce) {
		this.nonce = nonce;
	}

	public long getNonce() {
		return nonce;
	}

	public void setPreHash(String preHash) {
		this.preHash = preHash;
	}

	public String getHashCode() {
		return hashCode;
	}

	public void setHashCode(String hashCode) {
		this.hashCode = hashCode;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getDiff() {
		return diff;
	}

	public void setDiff(long diff) {
		this.diff = diff;
	}

	public void setHeight(long height) {
		this.height = height;
	}

	public long getIndex() {
		return index;
	}

	public void setIndex(long index) {
		this.index = index;
	}

	@Override
	public String toString() {
		return "BlockPow [preHash=" + preHash + ", hashCode=" + hashCode + ", timestamp=" + timestamp + ", diff=" + diff
				+ ", height=" + height + ", index=" + index + ", nonce=" + nonce + ", address=" + address
				+ ", powTimestamp=" + powTimestamp + "]";
	}

	public BlockPow generateFirstBlock(long height) {

		this.preHash = "0";
		this.timestamp = System.currentTimeMillis();
		this.diff = 4;
		this.height = height;
		this.index = 1;
		this.nonce = 0;
		// 用sha256算一个hash
		this.hashCode = this.generationHashCodeBySha256();
		return this;
	}

	public String generationHashCodeBySha256() {
		String hashData = "" + this.index + this.nonce + this.diff + this.timestamp;
		return Encryption.getSha256(hashData);
	}

	public BlockPow generateNextBlock(long height, BlockPow oldBlock) {
		BlockPow newBlock = new BlockPow();
		newBlock.setTimestamp(oldBlock.getTimestamp());
		// 规定前导0为4
		newBlock.setDiff(4);
		newBlock.setHeight(height);
		newBlock.setIndex(oldBlock.getIndex() + 1);
		newBlock.setPreHash(oldBlock.getHashCode());
		// 由矿工调整
		newBlock.setNonce(0);
//		newBlock.setHashCode(PowAlgorithm.pow(newBlock.getDiff(), newBlock));
		return newBlock;
	}
	
	public String generateHash(long height, BlockPow oldBlock) {
		BlockPow newBlock = new BlockPow();
		newBlock.setTimestamp(oldBlock.getTimestamp());
		// 规定前导0为4
		newBlock.setDiff(4);
		newBlock.setHeight(height);
		newBlock.setIndex(oldBlock.getIndex() + 1);
		newBlock.setPreHash(oldBlock.getHashCode());
		// 由矿工调整
		newBlock.setNonce(0);
		PowAlgorithm.pow(newBlock.getDiff(), newBlock);
		return PowAlgorithm.hash;
	}

	@Override
	protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
		stream.writeBytesWithLength(preHash.getBytes(StandardCharsets.UTF_8));
		stream.writeBytesWithLength(hashCode.getBytes(StandardCharsets.UTF_8));
		stream.writeBytesWithLength(address.getBytes(StandardCharsets.UTF_8));
		stream.writeUint48(diff);
		stream.writeUint48(height);
		stream.writeUint48(index);
		stream.writeUint48(nonce);
		stream.writeUint48(timestamp);
		stream.writeUint48(powTimestamp);
	}

	@Override
	public void parse(NulsByteBuffer byteBuffer) throws NulsException {
		this.preHash = new String(byteBuffer.readByLengthByte(), StandardCharsets.UTF_8);
		this.hashCode = new String(byteBuffer.readByLengthByte(), StandardCharsets.UTF_8);
		this.address = new String(byteBuffer.readByLengthByte(), StandardCharsets.UTF_8);
		this.diff = byteBuffer.readUint48();
		this.height = byteBuffer.readUint48();
		this.index = byteBuffer.readUint48();
		this.nonce = byteBuffer.readUint48();
		this.timestamp = byteBuffer.readUint48();
		this.powTimestamp = byteBuffer.readUint48();
	}

	@Override
	public int size() {
		int s = 0;
		s += SerializeUtils.sizeOfBytes(preHash.getBytes(StandardCharsets.UTF_8));
		s += SerializeUtils.sizeOfBytes(hashCode.getBytes(StandardCharsets.UTF_8));
		s += SerializeUtils.sizeOfBytes(address.getBytes(StandardCharsets.UTF_8));
		s += SerializeUtils.sizeOfUint48();
		s += SerializeUtils.sizeOfUint48();
		s += SerializeUtils.sizeOfUint48();
		s += SerializeUtils.sizeOfUint48();
		s += SerializeUtils.sizeOfUint48();
		s += SerializeUtils.sizeOfUint48();
		return s;
	}
}