package com.fundingsocieties.jumpcloudbot.dtos;

import lombok.Data;

@Data
public class ResetMFADTO {
    boolean exclusion;
    String exclusionUntil;
}
