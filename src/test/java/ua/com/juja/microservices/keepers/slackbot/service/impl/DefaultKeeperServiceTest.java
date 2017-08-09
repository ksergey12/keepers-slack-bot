package ua.com.juja.microservices.keepers.slackbot.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import ua.com.juja.microservices.keepers.slackbot.dao.KeeperRepository;
import ua.com.juja.microservices.keepers.slackbot.model.request.KeeperRequest;
import ua.com.juja.microservices.keepers.slackbot.service.KeeperService;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Nikolay Horushko
 * @author Dmitriy Lyashenko
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DefaultKeeperServiceTest {

    @MockBean
    private KeeperRepository keeperRepository;

    @Inject
    private KeeperService keeperService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldSaveKeeperAndReturnNewKeeperId() {

        //given
        String[] expectedKeeperId = {"100"};
        KeeperRequest keeperRequest = new KeeperRequest("qwer", "67ui", "teems");
        given(keeperRepository.addKeeper(keeperRequest)).willReturn(expectedKeeperId);

        //when
        String[] result = keeperService.sendKeeperAddRequest(keeperRequest);

        //then
        assertThat(result, equalTo(expectedKeeperId));
        verify(keeperRepository).addKeeper(keeperRequest);
    }

    @Test
    public void getKeeperDirections() {
        //Given
        KeeperRequest keeperRequest = new KeeperRequest("fromUser", "0000-1111", "direction1");
        List<String> expectedList = new ArrayList<>(Collections.singletonList("direction1"));
        //When
        when(keeperRepository.getKeeperDirections(keeperRequest)).thenReturn(expectedList);
        List<String> actualList = keeperService.getKeeperDirections(keeperRequest);
        //Then
        assertEquals(expectedList, actualList);
    }

    @Test
    public void getKeeperDirectionsWithEmptyResult() {
        //Given
        KeeperRequest keeperRequest = new KeeperRequest("fromUser", "0000-1111", "direction1");
        //When
        when(keeperRepository.getKeeperDirections(keeperRequest)).thenReturn(new ArrayList<>());
        List<String> actualList = keeperService.getKeeperDirections(keeperRequest);
        //Then
        assertTrue(actualList.isEmpty());
    }

    @Test
    public void shouldDismissKeeperAndReturnDismissedKeeperId() {

        //given
        String[] expectedKeeperId = {"100"};
        KeeperRequest keeperRequest = new KeeperRequest("qwer", "67ui", "teems");
        given(keeperRepository.dismissKeeper(keeperRequest)).willReturn(expectedKeeperId);

        //when
        String[] result = keeperService.sendKeeperDismissRequest(keeperRequest);

        //then
        assertThat(result, equalTo(expectedKeeperId));
        verify(keeperRepository).dismissKeeper(keeperRequest);
    }
}