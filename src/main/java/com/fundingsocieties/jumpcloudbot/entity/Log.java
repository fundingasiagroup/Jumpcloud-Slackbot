package com.fundingsocieties.jumpcloudbot.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Data
@Table(name = "log")
public class Log extends Thing implements Serializable {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "command")
    private String command;

    @Column
    private String params;
}
