package io.icw.thread;

import io.icw.thread.process.PowProcess;

public class PowProcessTask implements Runnable {
    private PowProcess powProcess;
    private int chainId;

    public PowProcessTask(int chainId, PowProcess powProcess) {
        this.chainId = chainId;
        this.powProcess = powProcess;
    }

    @Override
    public void run() {
        try {
        	powProcess.process(chainId);
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
}
