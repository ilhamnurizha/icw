package io.icw.pow;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.icw.base.RPCUtil;
import io.icw.base.basic.NulsByteBuffer;
import io.icw.core.exception.NulsException;
import io.icw.core.thread.ThreadUtils;
import io.icw.core.thread.commom.NulsThreadFactory;
import io.icw.thread.process.PowProcess;

class PowProcessTask implements Runnable {
    @Override
    public void run() {
        try {
        	System.out.println(Thread.currentThread().getName() + ":" + System.currentTimeMillis());
        	Thread.sleep(10000);
        	System.out.println(Thread.currentThread().getName() + "::::::::::" + System.currentTimeMillis());
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
}

public class PowTest {

	public static void main(String[] args) throws NulsException {
		
		System.out.println(2495304l / 10);
//		String hash = "40393965303964663331353832666564343636316265646531633361356630386637386165643138613936393566623833383730356130643561326138356236644030636230383864636133313538346432633232636639363331613537396663616333366532343032333664323636376166386538646264356464616432383830244943576336486755524544546a7a686b68424d7977526f576a706f4d3162645a5168695101000000000062ff0a00000049b60300000001000000000065faaeea810113faaeea8101";
		
//		hash = "403038386634393336623339323339356638646434616364646562333539613032666430633265323733376565366665326636383262306331356463363030326240303030303030303765636133393738353138313061373263643438343832383836313831646334663832343533343837306562363464316262363930643563302449435763364867555167616f526f4269675650726343354d69357472386646337831674e0700000000007e2f0b0000009db6030000008d7bdc0400009825b8ea8101081db6ea8101";
//		BlockPow blockPow = new BlockPow();
//		blockPow.parse(new NulsByteBuffer(RPCUtil.decode(hash)));
//		System.out.println(blockPow);
//		
//		ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = ThreadUtils.createScheduledThreadPool(2,new NulsThreadFactory("pow1"));
//    	scheduledThreadPoolExecutor.scheduleAtFixedRate(new PowProcessTask(),1000L,5000L,TimeUnit.MILLISECONDS);
	}
}
