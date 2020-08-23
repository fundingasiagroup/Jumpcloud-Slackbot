package com.fundingsocieties.jumpcloudbot.dtos;

import lombok.Data;

import java.util.List;

@Data
public class SearchFilterDTO {
        String searchTerm;
        List<String> fields;
}
