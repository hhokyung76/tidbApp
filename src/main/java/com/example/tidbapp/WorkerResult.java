package com.example.tidbapp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WorkerResult {
    private String workerId;
    private String threadName;
    private int subIndex;
    private int workingSize;
    private int workingSuccessSize;
    private int workingErrorSize;
    private String startTime;
    private String endTime;
    private long workingDuration;
}
