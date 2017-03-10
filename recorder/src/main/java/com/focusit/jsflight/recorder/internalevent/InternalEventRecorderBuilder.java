package com.focusit.jsflight.recorder.internalevent;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dkolmogortsev on 03.03.17.
 * Builder for {@link InternalEventRecorder}
 */
public class InternalEventRecorderBuilder
{
    private static final Logger LOG = LoggerFactory.getLogger(InternalEventRecorderBuilder.class);

    private final String outputPath;
    //Configuration defaults
    private int maxElementsBeforeFlush = -1;
    private int maxQueueSize = 4096;
    private boolean storeInGzip = false;
    private String threadName = "internal-event-storage";
    private FileStrategy strategy;

    private InternalEventRecorderBuilder(String outputPath)
    {
        this.outputPath = outputPath;
        this.strategy = new SimpleIncrementFileStrategy(outputPath);
    }

    public static InternalEventRecorderBuilder builderFor(String outputPath)
    {
        return new InternalEventRecorderBuilder(outputPath);
    }

    public InternalEventRecorderBuilder storageThreadName(String threadName)
    {
        this.threadName = threadName;
        return this;
    }

    public InternalEventRecorderBuilder maxQueueSize(int maxQueueSize)
    {
        this.maxQueueSize = maxQueueSize;
        return this;
    }

    public InternalEventRecorderBuilder storeInGzip()
    {
        this.storeInGzip = true;
        return this;
    }

    public InternalEventRecorderBuilder maxElementsBeforeFlush(int maxElementsBeforeFlush)
    {
        this.maxElementsBeforeFlush = maxElementsBeforeFlush;
        return this;
    }

    public InternalEventRecorderBuilder rolloverStrategy(long rolloverInterval)
    {
        this.strategy = new RollOverFileStrategy(outputPath, rolloverInterval);
        return this;
    }

    public InternalEventRecorder build()
    {
        return new InternalEventRecorder(maxElementsBeforeFlush, maxQueueSize, threadName, storeInGzip, strategy);
    }

    /**
     * Created by dkolmogortsev on 06.03.17.
     * Strategy for acquiring new output file
     */
    interface FileStrategy
    {
        File getNewFile();

        String getOutputPath();

        default boolean isRecordToNewFile(InternalEventRecorder.InternalEventRecord record)
        {
            return false;
        }
    }

    /**
     * Created by dkolmogortsev on 06.03.17.
     * Simple strategy that adds counter value to file name
     * On each request for new file counter is incremented;
     */
    private class SimpleIncrementFileStrategy implements FileStrategy
    {
        private final String basePath;
        private int counter = 0;

        private SimpleIncrementFileStrategy(String path)
        {
            basePath = path;
        }

        @Override
        public File getNewFile()
        {
            return new File(basePath + counter++);
        }

        @Override
        public String getOutputPath()
        {
            return basePath;
        }
    }

    /**
     * Created by dkolmogortsev on 06.03.17.
     * File strategy that rolls over previous file(prev. file is deleted)
     * on each request for new file current date in format "yyyy-MM-dd_HH-mm-ss" is added to base file
     */
    private class RollOverFileStrategy implements FileStrategy
    {
        private final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

        private final long rolloverInterval;
        private final String outputPath;
        private long rolloverDate;
        private File previous;

        private RollOverFileStrategy(String outputPath, long rolloverInterval)
        {
            this.rolloverInterval = rolloverInterval;
            this.outputPath = outputPath;
            updateRolloverDate();
        }

        @Override
        public File getNewFile()
        {
            updateRolloverDate();
            if (previous != null)
            {
                previous.delete();
            }
            return (previous = new File(outputPath + "_" + FORMAT.format(new Date())));
        }

        @Override
        public String getOutputPath()
        {
            return outputPath;
        }

        @Override
        public boolean isRecordToNewFile(InternalEventRecorder.InternalEventRecord record)
        {
            return record.timestampNs > rolloverDate;
        }

        private void updateRolloverDate()
        {
            this.rolloverDate = System.currentTimeMillis() + rolloverInterval;
        }
    }

}
