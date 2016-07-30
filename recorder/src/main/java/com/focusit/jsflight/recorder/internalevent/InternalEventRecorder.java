package com.focusit.jsflight.recorder.internalevent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Recorder for special server event that must be recorded to get correct overall recording.
 * For example if you record only user interaction and miss same important server state changes,
 * so it might get very hard to replay that recording
 *
 * Created by doki on 30.07.16.
 */
public class InternalEventRecorder
{
    private AtomicLong walltimeNs;

    public void push(String tag, byte[] data)
    {

    }

    public void push(String tag, long value)
    {

    }

    /**
     * Internal event representation
     */
    static class InternalEventRecord
    {
        public long timestampNs;
        public String tag;
        public byte[] data;
    }
}
