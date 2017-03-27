import com.focusit.jsflight.recorder.internalevent.InternalEventRecorderBuilder
import org.awaitility.Duration
import spock.lang.Shared
import spock.lang.Specification

import static org.awaitility.Awaitility.await

class InternalRecorderSpec extends Specification {

    @Shared
    File file = new File("/tmp/record/test")

    def setup() {
        file.parentFile.mkdirs()
    }

    def "Records with timestamp after rollover date should be stored in a new file"() {
        given:
        def recorder = InternalEventRecorderBuilder.builderFor(file.getAbsolutePath()).rolloverStrategy(1000).build();
        recorder.openFileForWriting()
        recorder.startRecording()

        await('Single file should be created').until {
            def files = file.getParentFile().listFiles()
            files?.size() == 1 && files?.find { f -> f.name.startsWith('test') }
        }
        def fileName = file.getParentFile().listFiles().first().getName();
        when:
        await().pollDelay(Duration.TWO_SECONDS).until(new Runnable() {
            @Override
            void run() {
                recorder.push("test", new Object())
            }
        })
        then:
        await().until {
            def newFileName = file.getParentFile().listFiles().first().getName();
            return !newFileName.equals(fileName);
        }
    }


    def 'rolled over data file must be lesser then previous'() {
        given:
        def recorder = InternalEventRecorderBuilder.builderFor(file.getAbsolutePath()).rolloverStrategy(1000).build();
        recorder.openFileForWriting()
        recorder.startRecording()

        await('Single file should be created').until {
            def files = file.getParentFile().listFiles()
            files?.size() == 1 && files?.find { f -> f.name.startsWith('test') }
        }
        100.times {
            recorder.push("test", new Object())
        }

        await().until { !recorder.hasPendingStores() }

        def fileSize = file.getParentFile().listFiles().first().length();
        println fileSize
        when:
        await().pollDelay(Duration.TWO_SECONDS).until(new Runnable() {
            @Override
            void run() {
                recorder.push("test", new Object())
            }
        })
        then:
        await().until {
            def newFileSize = file.getParentFile().listFiles().first().length();
            println newFileSize
            return newFileSize < fileSize
        }
    }


    def "data file rotates only once within rollover interval"() {
        given:
        def recorder = InternalEventRecorderBuilder.builderFor(file.getAbsolutePath()).rolloverStrategy(100000).build();
        recorder.openFileForWriting()
        recorder.startRecording()

        await('Single file should be created').until {
            def files = file.getParentFile().listFiles()
            return files?.size() == 1 && files?.find { f -> f.name.startsWith('test') }
        }

        def fileName = file.getParentFile().listFiles().first().getName();
        when:
        await().pollDelay(Duration.TWO_SECONDS).until(new Runnable() {
            @Override
            void run() {
                recorder.push("test", new Object())
            }
        })

        then:
        await("data file must remain the same").pollDelay(Duration.TWO_SECONDS).atMost(Duration.FIVE_SECONDS).until {
            def newFileName = file.getParentFile().listFiles().first().getName();
            newFileName.equals(fileName)
        }
    }

    def cleanup() {
        file.parentFile.listFiles().each { it -> it.delete() }
        file.parentFile.delete()
    }
}