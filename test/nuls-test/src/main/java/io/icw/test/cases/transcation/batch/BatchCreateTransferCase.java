package io.icw.test.cases.transcation.batch;

import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.transaction.facade.TransferReq;
import io.icw.test.cases.Constants;
import io.icw.test.cases.TestFailException;
import io.icw.test.cases.transcation.BaseTranscationCase;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.log.Log;
import io.icw.core.thread.ThreadUtils;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;

import static io.icw.test.cases.Constants.REMARK;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-24 13:52
 * @Description: 功能描述
 */
@Component
public class BatchCreateTransferCase extends BaseTranscationCase<Boolean, Long> {

    int THEADH_COUNT = 2;

    public static final BigInteger TRANSFER_AMOUNT = BigInteger.valueOf(10000000L);

    @Autowired
    BatchCreateAccountCase batchCreateAccountCase;

    @Override
    public String title() {
        return "批量创建交易";
    }

    @Override
    public Boolean doTest(Long count, int depth) throws TestFailException {
        ThreadUtils.createAndRunThread("batch-start", () -> {
            AtomicInteger doneTotal = new AtomicInteger(0);
            AtomicInteger successTotal = new AtomicInteger(0);
            Long start = System.currentTimeMillis();
            Log.info("开始创建交易");
            for (int s = 0; s < THEADH_COUNT; s++) {
                ThreadUtils.createAndRunThread("batch-transfer", () -> {
                    int i = doneTotal.getAndIncrement();
                    while (i < count) {
                        int index = i % batchCreateAccountCase.getFormList().size();
                        String formAddress = batchCreateAccountCase.getFormList().get(index);
                        String toAddress = batchCreateAccountCase.getToList().get(index);
                        TransferReq.TransferReqBuilder builder =
                                new TransferReq.TransferReqBuilder(config.getChainId(), config.getAssetsId())
                                        .addForm(formAddress, Constants.PASSWORD, TRANSFER_AMOUNT)
                                        .addTo(toAddress, TRANSFER_AMOUNT);
                        builder.setRemark(REMARK);
                        Result<String> result = transferService.transfer(builder.build(new TransferReq()));
                        try {
                            checkResultStatus(result);
                            successTotal.getAndIncrement();
                        } catch (TestFailException e) {
                            Log.error("创建交易失败:{}", e.getMessage());
                        }
                        i = doneTotal.getAndIncrement();
                    }
                });
            }
            Log.info("创建{}笔交易,成功{}笔，消耗时间:{}", count, successTotal, System.currentTimeMillis() - start);
        });
        return true;
    }

}
