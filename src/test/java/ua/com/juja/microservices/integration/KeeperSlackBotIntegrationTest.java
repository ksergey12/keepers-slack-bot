package ua.com.juja.microservices.integration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import ua.com.juja.microservices.keepers.slackbot.KeeperSlackBotApplication;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * @author Nikolay Horushko
 * @author Dmitriy Lyashenko
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {KeeperSlackBotApplication.class})
@AutoConfigureMockMvc
public class KeeperSlackBotIntegrationTest {

    @Inject
    private RestTemplate restTemplate;

    @Inject
    private MockMvc mvc;
    private MockRestServiceServer mockServer;

    @Before
    public void setup() {
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    public void fakeTest(){
        //delete it
        assertEquals(1,1);
    }
}
