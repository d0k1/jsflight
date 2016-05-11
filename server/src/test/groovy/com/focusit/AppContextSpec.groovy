//package com.focusit
//
//import org.apache.commons.httpclient.HttpStatus
//import org.junit.runner.RunWith
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.boot.SpringApplication
//import org.springframework.boot.test.IntegrationTest
//import org.springframework.boot.test.SpringApplicationContextLoader
//import org.springframework.boot.test.WebIntegrationTest
//import org.springframework.context.ConfigurableApplicationContext
//import org.springframework.context.annotation.PropertySource
//import org.springframework.http.ResponseEntity
//import org.springframework.test.context.ContextConfiguration
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
//import org.springframework.test.context.web.WebAppConfiguration
//import org.springframework.web.client.RestTemplate
//import spock.lang.AutoCleanup
//import spock.lang.Shared
//import spock.lang.Specification
//
//import java.util.concurrent.Callable
//import java.util.concurrent.Executors
//import java.util.concurrent.Future
//import java.util.concurrent.TimeUnit
//
///**
// * Created by doki on 11.05.16.
// */
//
//@ContextConfiguration(loader = SpringApplicationContextLoader.class, classes = [ServerApplication.class] )
//@WebIntegrationTest
//@PropertySource("classpath:app.integration.test.properties")
//class AppContextSpec extends Specification {
//    @Value('${local.server.port}')
//    int port
//
//    def "startsContext successfully"(){
//        when:
//        ResponseEntity entity = new RestTemplate().getForEntity("http://localhost:${port}/player/list", String.class);
//        then:
//        entity.statusCode == org.springframework.http.HttpStatus.OK
//    }
//}
