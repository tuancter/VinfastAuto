package com.group2.VinfastAuto.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticResponse {
    private String key; // có thể là năm, vị trí, nhóm tuổi
    private Integer count;
}
