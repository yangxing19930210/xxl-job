package com.xxl.job.admin.core.thread;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xxl.job.admin.core.util.WXTokenUtil;

/**
 * job wx token helper
 *
 * @author xuxueli 2019-11-22
 */
public class JobWXTokenHelper {
    private static Logger logger = LoggerFactory.getLogger(JobWXTokenHelper.class);
    private static JobWXTokenHelper instance = new JobWXTokenHelper();

    private Thread logrThread;
    private volatile boolean toStop = false;

    public static JobWXTokenHelper getInstance() {
        return instance;
    }

    public void start() {
        logrThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (!toStop) {
                    try {
                        TimeUnit.MINUTES.sleep(60 * 20);
                    } catch (Exception e) {
                        if (!toStop) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    WXTokenUtil.clientToken();;
                }
                logger.info(">>>>>>>>>>> xxl-job, job wx token thread stop");
            }
        });
        logrThread.setDaemon(true);
        logrThread.setName("xxl-job, admin JobWXTokenHelper");
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
