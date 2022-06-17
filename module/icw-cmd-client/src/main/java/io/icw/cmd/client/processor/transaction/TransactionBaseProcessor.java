package io.icw.cmd.client.processor.transaction;

import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.transaction.TransferService;
import io.icw.cmd.client.processor.CommandGroup;
import io.icw.cmd.client.processor.CommandProcessor;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-12 17:10
 * @Description: 功能描述
 */
public abstract class TransactionBaseProcessor implements CommandProcessor {

    TransferService transferService = ServiceManager.get(TransferService.class);

    @Override
    public CommandGroup getGroup(){
        return CommandGroup.Transaction;
    }

}
