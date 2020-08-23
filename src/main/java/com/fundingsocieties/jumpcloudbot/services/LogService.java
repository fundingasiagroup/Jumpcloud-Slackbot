package com.fundingsocieties.jumpcloudbot.services;

import com.fundingsocieties.jumpcloudbot.entity.Log;
import com.fundingsocieties.jumpcloudbot.repositories.LogsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogService {

    @Autowired
    private LogsRepository logsRepository;

    public void log(String slackUserId, String command, String params){
        Log log = new Log();
        log.setCommand(command);
        log.setParams(params);
        log.setCreatedBy(slackUserId);
        logsRepository.saveAndFlush(log);
    }
}
