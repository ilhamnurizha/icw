package io.icw.test.cases.transcation;

import io.icw.core.core.annotation.Component;
import io.icw.core.log.Log;
import io.icw.core.thread.ThreadUtils;
import io.icw.test.cases.CallRemoteTestCase;
import io.icw.test.cases.TestFailException;
import io.icw.test.cases.transcation.batch.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Author: ljs
 * @Time: 2019-04-25 12:08
 * @Description: 功能描述
 */
@Component
public class BatchFatigueTxsCase extends CallRemoteTestCase<Void,Integer> {

    @Override
    public String title() {
        return "ljs test";
    }
    @Override
    public Void doTest(Integer total, int depth) throws TestFailException {
        List<String> nodes = getRemoteNodes();
                CountDownLatch latch = new CountDownLatch(nodes.size());
                int i = 0;
                for (String n:nodes) {
                    i++;
                    Map param = new HashMap<>();
                    param.put("id",n);
                    ThreadUtils.createAndRunThread("batch-transfer-" + i , () -> {
                        try {
                            String res = doRemoteTest(n, BatchTxsCase.class, param);
                            Log.info("成功发起交易:{}", res);
                            latch.countDown();
                        } catch (TestFailException e) {
                            Log.error(e.getMessage(),e);
                            latch.countDown();
                        }
                    });
                }
                try {
                    latch.await();
                    TimeUnit.SECONDS.sleep(15);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
    }
}
