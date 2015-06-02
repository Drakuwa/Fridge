package com.app.afridge.interfaces;

import com.google.common.eventbus.Subscribe;

import com.cloudant.sync.notifications.ReplicationCompleted;
import com.cloudant.sync.notifications.ReplicationErrored;
import com.cloudant.sync.replication.ErrorInfo;

import java.util.concurrent.CountDownLatch;

/**
 * A {@code ReplicationListener} that sets a latch when it's told the
 * replication has finished.
 */
public class ReplicationListener {

    private final CountDownLatch latch;

    public ErrorInfo error = null;

    public ReplicationListener(CountDownLatch latch) {
        this.latch = latch;
    }

    @Subscribe
    public void complete(ReplicationCompleted event) {
        latch.countDown();
    }

    @Subscribe
    public void error(ReplicationErrored event) {
        this.error = event.errorInfo;
        latch.countDown();
    }
}