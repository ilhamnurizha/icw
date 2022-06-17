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

package io.icw.cmd.client.processor.system;

import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.protocol.ProtocolProvider;
import io.icw.base.api.provider.protocol.facade.GetVersionReq;
import io.icw.base.api.provider.protocol.facade.VersionInfo;
import io.icw.cmd.client.CommandBuilder;
import io.icw.cmd.client.CommandResult;
import io.icw.cmd.client.config.Config;
import io.icw.cmd.client.processor.CommandGroup;
import io.icw.cmd.client.processor.CommandProcessor;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.parse.MapUtils;

import java.util.Map;

/**
 * @author: Charlie
 */
@Component

public class VersionProcessor implements CommandProcessor {

    ProtocolProvider transferService = ServiceManager.get(ProtocolProvider.class);

    @Autowired
    Config config;

    @Override
    public String getCommand() {
        return "version";
    }

    @Override
    public CommandGroup getGroup() {
        return CommandGroup.System;
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine("version -- print node version info");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "version";
    }

    @Override
    public boolean argsValidate(String[] args) {
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        Result<VersionInfo> res = transferService.getVersion(new GetVersionReq());
        if(config.getClientVersion() != null){
            Map<String,Object> m = MapUtils.beanToLinkedMap(res.getData());
            m.put("clientVersion",config.getClientVersion());
            return CommandResult.getSuccess(new Result(m));
        }else{
            return CommandResult.getSuccess(res);
        }
    }

}
