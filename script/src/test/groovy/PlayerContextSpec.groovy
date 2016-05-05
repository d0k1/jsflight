/**
 * Created by doki on 06.05.16.
 */

import com.focusit.script.player.PlayerContext
import spock.lang.Specification

class PlayerContextSpec extends Specification {
    public static final String TEST_KEY = "testKey";
    public static final String TEST_VALUE = "testValue";

    PlayerContext ctx = new PlayerContext();

    def "can put an element to the context"() {
        when:
        ctx.put(TEST_KEY, TEST_VALUE);
        then:
        ctx.get(TEST_KEY)!=null;
        ctx.get(TEST_KEY).equals(TEST_VALUE);
    }

    def "context is empty after clean"(){
        when:
        ctx.put(TEST_KEY, TEST_VALUE);
        then:
        ctx.get(TEST_KEY)!=null;

        when:
        ctx.reset();
        then:
        ctx.get(TEST_KEY)==null;
    }
}
