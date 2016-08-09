package com.focusit.jsflight.recorder.internalevent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.FastOutput;

/**
 * Recorder for special server event that must be recorded to get correct overall recording.
 * For example if you record only user interaction and miss same important server state changes,
 * so it might get very hard to replay that recording
 * <p>
 * Created by doki on 30.07.16.
 */
public class InternalEventRecorder
{
    /**
     * Internal event representation
     */
    static class InternalEventRecord
    {
        public long id;
        public long timestampNs;
        public char tag[] = new char[64];
        public long bytes;
        public byte[] data;
    }

    class StorageThread extends Thread
    {
        private Kryo kryo;
        private FastOutput output;
        // 32 mb buffer should be enough for everyone
        private int size = 32 * 1024 * 1024;
        private int fileNo = 0;

        public StorageThread()
        {
            super("internal-event-storage");
            try
            {
                setPriority(NORM_PRIORITY);
                kryo = new Kryo();
                output = new FastOutput(new FileOutputStream("internal.data"), size);
            }
            catch (IOException e)
            {
                // do nothing if kryo initialization failed
            }
        }

        @Override
        public void run()
        {
            while (isAlive() && !isInterrupted() && !shuttingDown.get())
            {
                try
                {
                    if (recording.get())
                    {
                        InternalEventRecord record = records.poll();
                        kryo.writeObject(output, record);
                    }
                    else
                    {
                        sleep(1000);
                    }
                }
                catch (Exception e)
                {
                    // no Exception could break this thread. only Error
                }
            }
        }
    }

    class WallClock extends Thread
    {
        public WallClock()
        {
            super("internal-event-clock");
            setPriority(MAX_PRIORITY);
        }

        @Override
        public void run()
        {
            while (isAlive() && !isInterrupted() && !shuttingDown.get())
            {
                timestampNs.set(System.nanoTime());
                try
                {
                    Thread.sleep(1);
                }
                catch (InterruptedException e)
                {
                    break;
                }
            }
        }
    }

    private static final InternalEventRecorder instance = new InternalEventRecorder();

    public static InternalEventRecorder getInstance()
    {
        return instance;
    }

    private ArrayBlockingQueue<InternalEventRecord> records = new ArrayBlockingQueue<>(4096);
    private AtomicLong lastId = new AtomicLong(-1);

    private AtomicLong timestampNs = new AtomicLong(0);

    private AtomicBoolean recording = new AtomicBoolean(false);

    private StorageThread storageThread = new StorageThread();

    private AtomicBoolean shuttingDown = new AtomicBoolean(false);

    private WallClock wallClock = new WallClock();

    private InternalEventRecorder()
    {
        wallClock.start();
        storageThread.start();
    }

    public void push(String tag, byte[] data) throws UnsupportedEncodingException, InterruptedException
    {
        if (!recording.get())
        {
            return;
        }

        InternalEventRecord record = new InternalEventRecord();
        record.id = lastId.incrementAndGet();
        byte tagBytes[] = tag.getBytes("UTF-8");
        System.arraycopy(tagBytes, 0, record.tag, 0, 64);

        record.bytes = data.length;
        System.arraycopy(data, 0, record.data, 0, data.length);

        records.put(record);
    }

    public void shutdown()
    {
        shuttingDown.set(true);
    }

    public void startRecording()
    {
        recording.set(true);
    }

    public void stopRecording()
    {
        recording.set(false);
    }
}
