package io.icw.protocol;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import io.icw.base.basic.NulsByteBuffer;
import io.icw.core.exception.NulsException;
import io.icw.core.model.ByteUtils;
import io.icw.core.rockdb.service.RocksDBService;
import io.icw.protocol.model.po.StatisticsInfo;

public class Test {

	public static void main(String[] args) throws Exception {
//    	String dbPath = "E:\\statistics_info_1\\rocksdb";
		String dbPath = "E:\\cached_info_1\\rocksdb";
		
		RocksDB db = RocksDB.open(dbPath);
		
//		StatisticsInfo po = new StatisticsInfo();
//		for (int i = 0;i < 1147388; i++) {
//	    	System.out.println(i + " :: " + db.get(ByteUtils.longToBytes(i)));
//	    	try {
//		    	po.parse(new NulsByteBuffer(db.get(ByteUtils.longToBytes(i))));
//		    	System.out.println( po );
//	    	} catch (Exception e) {
//	    		
//	    	}
//    	}
		
    	System.out.println( ByteUtils.bytesToInt(db.get("currentProtocolVersionCount".getBytes())) );
    	
    	if ( (short) (100 + 1) < 0) {
    		System.out.println("???");
    	}
	}

}
