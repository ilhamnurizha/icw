/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.icw.cmd.client.processor.transaction;


import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.transaction.facade.GetConfirmedTxByHashReq;
import io.icw.base.api.provider.transaction.facade.TransactionData;
import io.icw.base.data.NulsHash;
import io.icw.cmd.client.CommandBuilder;
import io.icw.cmd.client.CommandResult;
import io.icw.cmd.client.processor.CommandProcessor;
import io.icw.core.core.annotation.Component;

/**
 * @author zhoulijun
 */
@Component
public class GetTxProcessor extends TransactionBaseProcessor implements CommandProcessor {
    @Override
    public String getCommand() {
        return "gettx";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<hash>  transaction hash -required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "gettx <hash> --get the transaction information by txhash";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,1);
        checkArgs(NulsHash.validHash(args[1]),"hash format error");
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String hash = args[1];
        Result<TransactionData> result = transferService.getSimpleTxDataByHash(new GetConfirmedTxByHashReq(hash));
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result);
    }

}