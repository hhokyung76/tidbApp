package com.example.tidbapp;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Callable;

public class WorkerCallable implements Callable<WorkerResult> {


    private final JdbcTemplate jdbcTemplate;

    private List<KbAudience> childList;

    private WorkerResult resultObj;

    public WorkerCallable(JdbcTemplate jdbcTemplate, List<KbAudience> childList, WorkerResult workerObj) {
        this.jdbcTemplate = jdbcTemplate;
        this.childList = childList;
        this.resultObj = workerObj;
    }

    @Override
    public WorkerResult call() throws Exception {
        //Thread.sleep(1000);
        Instant start = Instant.now();
        resultObj.setStartTime(ScStringUtils.getCurrentTimeOfLog());
        batchInsert(childList);

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        //return the thread name executing this callable task
        resultObj.setEndTime(ScStringUtils.getCurrentTimeOfLog());
        resultObj.setWorkingDuration(duration.getNano());
        resultObj.setThreadName(Thread.currentThread().getName());
        return resultObj;
    }

    public int[] batchInsert(List<KbAudience> audiences) {
        String sql = "insert into kb_audience_ads_group values(?, ?, ?, ?, ?, ?);";
        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                KbAudience audience = audiences.get(i);
                ps.setString(1, audience.getId());
                ps.setInt(2, audience.getAdsGroupNo());
                ps.setString(3, audience.getAdvertisingId());
                ps.setString(4, audience.getIdType());
                ps.setString(5, audience.getCtaList());
                ps.setLong(6, audience.getTtl());
            }

            @Override
            public int getBatchSize() {
                return audiences.size();
            }
        });
    }
}