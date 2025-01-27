package com.sequenceiq.common.api.telemetry.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sequenceiq.common.api.telemetry.doc.TelemetryModelDescription;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "TelemetryResponse")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelemetryResponse implements Serializable {

    @ApiModelProperty(TelemetryModelDescription.TELEMETRY_LOGGING)
    private LoggingResponse logging;

    @ApiModelProperty(TelemetryModelDescription.TELEMETRY_WORKLOAD_ANALYTICS)
    private WorkloadAnalyticsResponse workloadAnalytics;

    public LoggingResponse getLogging() {
        return logging;
    }

    public void setLogging(LoggingResponse logging) {
        this.logging = logging;
    }

    public WorkloadAnalyticsResponse getWorkloadAnalytics() {
        return workloadAnalytics;
    }

    public void setWorkloadAnalytics(WorkloadAnalyticsResponse workloadAnalytics) {
        this.workloadAnalytics = workloadAnalytics;
    }
}
