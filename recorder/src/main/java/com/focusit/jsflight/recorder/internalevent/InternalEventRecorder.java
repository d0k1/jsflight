package com.focusit.jsflight.recorder.internalevent;

import java.io.File;
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
    public static class InternalEventRecord
    {
        public long id;
        public long timestampNs;
        public char tag[] = new char[64];
        public Object data;
    }

    class StorageThread extends Thread
    {
        private Kryo kryo;
        private FastOutput output;

        // 4 mb buffer should be enough for everyone
        private int size = 4 * 1024 * 1024;

        public StorageThread()
        {
            super("internal-event-storage");
            setPriority(NORM_PRIORITY);
        }

        public void flush()
        {
            output.flush();
        }

        public void openFileForWriting()
        {
            try
            {
                kryo = new Kryo();
                File dest = new File(eventsFilename);
                System.out.println("Storing internal events to " + dest.getAbsolutePath());
                output = new FastOutput(new FileOutputStream(dest), size);
            }
            catch (IOException e)
            {
                // do nothing if kryo initialization failed
            }

        }

        @Override
        public void run()
        {
            boolean needFlush = false;
            while (isAlive() && !isInterrupted() && !shuttingDown.get())
            {
                try
                {
                    if (recording.get())
                    {
                        InternalEventRecord record = records.poll();
                        if (record != null)
                        {
                            kryo.writeObject(output, record);
                            needFlush = true;
                            yield();
                        }
                        else
                        {
                            if (needFlush)
                            {
                                output.flush();
                                needFlush = false;
                            }
                            sleep(100);
                        }
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
                    Thread.sleep(10);
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

    private String eventsFilename = "internal.data";

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

    public long getWallTime()
    {
        return timestampNs.get();
    }

    public void openFileForWriting(String filename)
    {
        this.eventsFilename = filename;
        storageThread.openFileForWriting();
    }

    public void push(String tag, Object data) throws UnsupportedEncodingException, InterruptedException
    {
        if (!recording.get())
        {
            return;
        }

        InternalEventRecord record = new InternalEventRecord();
        record.id = lastId.incrementAndGet();

        String tagValue = tag.trim();
        if (!tagValue.isEmpty())
        {
            tagValue.getChars(0, tagValue.length() > 64 ? 64 : tagValue.length(), record.tag, 0);
        }
        if (data != null)
        {
            record.data = data;
        }
        record.timestampNs = timestampNs.get();
        records.put(record);
    }

    public void shutdown() throws InterruptedException
    {
        shuttingDown.set(true);
        storageThread.join(2000);
        wallClock.join(2000);
        storageThread.flush();
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
