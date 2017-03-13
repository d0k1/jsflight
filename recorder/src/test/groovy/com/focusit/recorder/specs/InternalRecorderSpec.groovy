import com.focusit.jsflight.recorder.internalevent.InternalEventRecorderBuilder
import org.awaitility.Duration
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.Callable

import static org.awaitility.Awaitility.await

class InternalRecorderSpec extends Specification {

    @Shared
    File file = new File("/tmp/record/test")

    def setup() {
        file.parentFile.mkdirs()
    }

    def "Records with timestamp after rollover date should be stored in a new file"() {
        File file = new File("/tmp/record/test")
        file.parentFile.mkdirs()
        given:
        def recorder = InternalEventRecorderBuilder.builderFor(file.getAbsolutePath()).rolloverStrategy(1000).build();
        recorder.openFileForWriting()
        recorder.startRecording()
        await('Single file should be created').until({
            def files = file.getParentFile().listFiles()
            return files?.size() == 1 && files?.find { f -> f.name.startsWith('test') }
        } as Callable<Boolean>)
        def fileName = file.getParentFile().listFiles().first().getName();
        when:
        await().pollDelay(Duration.TWO_SECONDS).until({
            recorder.push("test", new Object())
        } as Runnable);

        then:
        await().until({
            def newFileName = file.getParentFile().listFiles().first();
            return !newFileName.equals(fileName);
        } as Callable<Boolean>)
    }

    def cleanup() {
        file.parentFile.listFiles().each { it -> it.delete() }
        file.parentFile.delete()
    }
}