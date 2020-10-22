package com.xxl.job.admin.core.thread;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * job log report helper
 *
 * @author xuxueli 2019-11-22
 */
public class JobMailTimeHelper {
    private static Logger logger = LoggerFactory.getLogger(JobMailTimeHelper.class);
    private static JobMailTimeHelper instance = new JobMailTimeHelper();

    private Thread logrThread;
    private volatile boolean toStop = false;

    public static JobMailTimeHelper getInstance() {
        return instance;
    }

    public void start() {
        logrThread = new Thread(new Runnable() {

            @Override
            public void run() {
                // last clean log time
                long lastCleanLogTime = 0;
                while (!toStop) {
                    try {
                        TimeUnit.MINUTES.sleep(60 * 60 * 5);
                    } catch (Exception e) {
                        if (!toStop) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    JobFailMonitorHelper.getStringIntegerMap().clear();
                }
                logger.info(">>>>>>>>>>> xxl-job, job mail time thread stop");
            }
        });
        logrThread.setDaemon(true);
        logrThread.setName("xxl-job, admin JobMailTimeHelper");
        logrThread.start();
    }

    public void toStop() {
        toStop = true;
        // interrupt and wait
        logrThread.interrupt();
        try {
            logrThread.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
