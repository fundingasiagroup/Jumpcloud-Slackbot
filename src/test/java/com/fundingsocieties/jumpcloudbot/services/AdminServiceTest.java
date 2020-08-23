package com.fundingsocieties.jumpcloudbot.services;

import com.fundingsocieties.jumpcloudbot.entity.Admin;
import com.fundingsocieties.jumpcloudbot.repositories.AdminsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class AdminServiceTest {

    @MockBean
    private AdminsRepository adminsRepository;

    @Autowired
    AdminService adminService;


    @Test
    void isAdminUser_shouldReturnTrueWhenUserExists(){
        when(adminsRepository.findActiveAdminBySlackUserId("xyz")).thenReturn(new Admin());
        assertTrue(adminService.isAdminUser("xyz"));
    }

    @Test
    void isAdminUser_shouldReturnFalseWhenUserDoesntExists(){
        when(adminsRepository.findActiveAdminBySlackUserId("xyz")).thenReturn(null);
        assertFalse(adminService.isAdminUser("xyz"));
    }
}
