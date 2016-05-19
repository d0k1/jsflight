package com.focusit.specs.integration

import com.focusit.ServerApplication
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

/**
 * Created by doki on 11.05.16.
 */

@EnableAutoConfiguration(exclude = [EmbeddedMongoAutoConfiguration.class])
@ContextConfiguration(loader = SpringApplicationContextLoader.class, classes = [ServerApplication.class])
@WebIntegrationTest
@TestPropertySource("classpath:app.integration.test.properties")
class AppContextSpec extends Specification {
    @Value('${local.server.port}')
    int port

    def "startsContext successfully"() {
        when:
        ResponseEntity entity = new RestTemplate().getForEntity("http://localhost:${port}/player/list", String.class);
        then:
        entity.statusCode == org.springframework.http.HttpStatus.OK
    }

    def "can import record"() {

    }
}
