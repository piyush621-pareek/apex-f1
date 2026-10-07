package com.apexf1.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Matches the FSP "before race" form: fan name + pole pick + winner pick.
 */
public class PredictionRequest {

    @NotBlank
    private String fanName;

    @NotNull
    private Long predictedPoleDriverId;

    @NotNull
    private Long predictedWinnerDriverId;

    public String getFanName() {
        return fanName;
    }

    public void setFanName(String fanName) {
        this.fanName = fanName;
    }

    public Long getPredictedPoleDriverId() {
        return predictedPoleDriverId;
    }

    public void setPredictedPoleDriverId(Long predictedPoleDriverId) {
        this.predictedPoleDriverId = predictedPoleDriverId;
    }

    public Long getPredictedWinnerDriverId() {
        return predictedWinnerDriverId;
    }

    public void setPredictedWinnerDriverId(Long predictedWinnerDriverId) {
        this.predictedWinnerDriverId = predictedWinnerDriverId;
    }
}
