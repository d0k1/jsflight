package com.focusit.jsflight.recorder.internalevent;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ArrayBlockingQueue;
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
    private ArrayBlockingQueue<InternalEventRecord> records = new ArrayBlockingQueue<>(4096);
    private AtomicLong lastId = new AtomicLong(-1);
    private AtomicLong timestampNs = new AtomicLong(0);

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
        public long id;
        public long timestampNs;
        public String tag;
        public long bytes;
        public byte[] data;
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
            while (isAlive() && !isInterrupted())
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

    class StorageThread extends Thread
    {
        private long position = 0;
        private int size = 8192 * 1024 * 1024;

        private MappedByteBuffer buffer;
        private int fileNo = 0;

        public StorageThread()
        {
            super("internal-event-storage");
            setPriority(MAX_PRIORITY);

            buffer = getBuffer(this.size);
        }

        private MappedByteBuffer getBuffer(int size)
        {
            try
            {
                return new RandomAccessFile("internal_" + (fileNo++) + ".dat", "rw").getChannel()
                        .map(FileChannel.MapMode.READ_WRITE, 0, size);
            }
            catch (IOException e)
            {
                System.err.println("Error creating memory mapped file for internal events recorder");
                return null;
            }
            finally
            {
                this.size = size;
                this.position = 0;
            }
        }

        @Override
        public void run()
        {
            while (isAlive() && !isInterrupted())
            {
                InternalEventRecord record = records.peek();
            }
        }
    }
}
