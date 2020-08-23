package com.fundingsocieties.jumpcloudbot.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Data
@Table(name = "admin")
public class Admin extends Thing implements Serializable {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    @Column(name = "slack_username")
    private String slackUsername;

    @Column(name = "slack_user_id")
    private String slackUserId;
}
