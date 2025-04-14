package com.poly.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class RevenueStatsResponse {
    private List<String> labels;
    private List<Dataset> datasets;

    @Getter
    @Setter
    @Builder
    public static class Dataset {
        private String label;
        private List<BigDecimal> data;
    }
}
