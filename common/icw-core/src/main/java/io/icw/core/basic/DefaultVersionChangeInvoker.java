package io.icw.core.basic;

import io.icw.core.log.Log;

public class DefaultVersionChangeInvoker implements VersionChangeInvoker{
    @Override
    public void process(int chainId) {
        Log.info("DefaultVersionChangeInvoker trigger. chainId-" + chainId);
    }
}
