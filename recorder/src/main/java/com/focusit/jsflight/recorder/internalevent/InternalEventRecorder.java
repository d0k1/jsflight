package com.focusit.jsflight.recorder.internalevent;

import java.io.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOG = LoggerFactory.getLogger(InternalEventRecorder.class);
    private final int maxElementsBeforeFlush;
    private final boolean storeInGzip;
    private final InternalEventRecorderBuilder.FileStrategy newFileStrategy;
    private ArrayBlockingQueue<InternalEventRecord> records;
    private AtomicLong lastId = new AtomicLong(-1);
    private AtomicLong timestampNs = new AtomicLong(0);
    private AtomicBoolean recording = new AtomicBoolean(false);
    private StorageThread storageThread;
    private AtomicBoolean shuttingDown = new AtomicBoolean(false);
    private AtomicBoolean openNewFile = new AtomicBoolean(false);
    private WallClock wallClock = new WallClock();

    InternalEventRecorder(int maxElementsBeforeFlush, int maxQueueSize, String storagePrefix, boolean storeInGzip,
            InternalEventRecorderBuilder.FileStrategy newFileStrategy)
    {
        this.maxElementsBeforeFlush = maxElementsBeforeFlush;
        this.records = new ArrayBlockingQueue<>(maxQueueSize);
        this.storeInGzip = storeInGzip;
        this.storageThread = new StorageThread(storagePrefix);
        this.newFileStrategy = newFileStrategy;
        wallClock.start();
        storageThread.start();
    }

    public boolean hasPendingStores()
    {
        return !records.isEmpty();
    }

    public long getWallTime()
    {
        return timestampNs.get();
    }

    public void openFileForWriting()
    {
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
        record.timestampEpoch = System.currentTimeMillis();
        records.put(record);
    }

    public void shutdown() throws InterruptedException
    {
        stopRecording();
        storageThread.flush();
        shuttingDown.set(true);
        storageThread.join(2000);
        wallClock.join(2000);
    }

    public void recordToNewFile()
    {
        storageThread.openNewFile();
    }

    public void startRecording()
    {
        recording.set(true);
    }

    public void stopRecording()
    {
        recording.set(false);
    }

    public void setWallClockInterval(long interval)
    {
        this.wallClock.setNewInterval(interval);
    }

    /**
     * Internal event representation
     */
    public static class InternalEventRecord
    {
        public long id;
        public long timestampNs;
        public long timestampEpoch;
        public char tag[] = new char[64];
        public Object data;
    }

    class StorageThread extends Thread
    {
        private Kryo kryo;
        private FastOutput output;

        public StorageThread(String storagePrefix)
        {
            super(storagePrefix);
            setPriority(NORM_PRIORITY);
            kryo = new Kryo();
        }

        public void flush()
        {
            output.flush();
        }

        public void openFileForWriting()
        {
            try
            {
                File destinationFile = newFileStrategy.getNewFile();
                LOG.info("{} storing events to: {}", newFileStrategy.toString(), destinationFile.getAbsolutePath());
                FileOutputStream fos = new FileOutputStream(destinationFile);
                OutputStream out = storeInGzip ? new GZIPOutputStream(fos, true) : fos;
                output = new FastOutput(out);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        public void openNewFile()
        {
            openNewFile.set(true);
        }

        private void reOpenFile()
        {
            try
            {
                output.close();
                openFileForWriting();
            }
            finally
            {
                openNewFile.set(false);
            }
        }

        @Override
        public void run()
        {
            boolean needFlush = false;
            int flushedObjects = 0;
            while (isAlive() && !isInterrupted() && !shuttingDown.get())
            {
                try
                {
                    if (recording.get())
                    {
                        InternalEventRecord record;
                        if (flushedObjects != maxElementsBeforeFlush && (record = records.poll()) != null)
                        {
                            if (shouldOpenNewFile() || newFileStrategy.isRecordToNewFile(record))
                            {
                                reOpenFile();
                            }
                            kryo.writeObject(output, record);
                            needFlush = true;
                            flushedObjects++;
                            yield();
                        }
                        else
                        {
                            if (needFlush)
                            {
                                output.flush();
                                flushedObjects = 0;
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

        private boolean shouldOpenNewFile()
        {
            return openNewFile.get();
        }
    }

    class WallClock extends Thread
    {
        //defaults to 10 milliseconds
        private long interval = 10;

        public WallClock()
        {
            super("internal-event-clock");
            setPriority(MAX_PRIORITY);
        }

        public void setNewInterval(long interval)
        {
            this.interval = interval;
        }

        @Override
        public void run()
        {
            while (isAlive() && !isInterrupted() && !shuttingDown.get())
            {
                timestampNs.set(System.nanoTime());
                try
                {
                    if (interval <= 0)
                    {
                        Thread.yield();
                    }
                    else
                    {
                        Thread.sleep(interval);
                    }
                }
                catch (InterruptedException e)
                {
                    break;
                }
            }
        }
    }
}
