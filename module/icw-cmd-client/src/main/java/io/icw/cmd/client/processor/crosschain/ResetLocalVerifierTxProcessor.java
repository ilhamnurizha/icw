package io.icw.cmd.client.processor.crosschain;

import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.crosschain.facade.CreateResetLocalVerifierTxReq;
import io.icw.cmd.client.CommandBuilder;
import io.icw.cmd.client.CommandResult;
import io.icw.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-06 17:34
 * @Description: 功能描述
 */
@Component
public class ResetLocalVerifierTxProcessor extends CrossChainBaseProcessor {

    @Override
    public String getCommand() {
        return "resetlocalverifier";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address>  address - require")
                .newLine("\t[password] \t\tpassword");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "resetlocalverifier <address> [password]--create cross chain tx";
    }


    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args, 1,2);
        checkAddress(config.getChainId(), args[1]);
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        Integer chainId = config.getChainId();
        String address = args[1];
        String password = null;
        if (args.length == 3) {
            password = args[2];
        }else {
            password = getPwd();
        }
        Result<String> result = crossChainProvider.resetLocalVerifier(new CreateResetLocalVerifierTxReq(address,password));
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result.getData());
    }
}
