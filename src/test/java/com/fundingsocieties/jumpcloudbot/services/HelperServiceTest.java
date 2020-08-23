package com.fundingsocieties.jumpcloudbot.services;

import io.swagger.clientv1.model.Systemuserreturn;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class HelperServiceTest {

    @MockBean
    SlackService slackService;

    @MockBean
    JumpCloudV1Service jumpCloudV1Service;

    @Autowired
    HelperService helperService;


    @Test
    void findSystemUserByEmailWrapper_returnNullWhenUserNotFound(){
        when(jumpCloudV1Service.findSystemUserByEmail("abc@example.com")).thenReturn(null);
        assertNull(helperService.findSystemUserByEmailWrapper("abc@example.com"));
    }

    @Test
    void findSystemUserByEmailWrapper_returnUserWhenFound(){
        Systemuserreturn systemuserreturn = new Systemuserreturn();
        systemuserreturn.setEmail("abc@example.com");
        when(jumpCloudV1Service.findSystemUserByEmail("abc@example.com")).thenReturn(systemuserreturn);
        assertTrue(helperService.findSystemUserByEmailWrapper("abc@example.com") instanceof Systemuserreturn);
        assertEquals(helperService.findSystemUserByEmailWrapper("abc@example.com").getEmail(), systemuserreturn.getEmail());
    }

    @Test
    void findEmail_returnEmailWhenPassed(){
        String testInput = "<mailto:abc@example.com|abc@example.com>";
        String testEmail = "abc@example.com";
        assertEquals(helperService.findEmail(testInput), testEmail);
    }

    @Test
    void findEmail_callSlackWhenUsernamePassed(){
        String testInput = "<@USXXXX|user.name>";
        String testEmail = "abc@example.com";
        when(slackService.findEmailBySlackId(testInput)).thenReturn("abc@example.com");
        assertEquals(helperService.findEmail(testInput), testEmail);
    }


}
