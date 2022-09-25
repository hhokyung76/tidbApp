package com.example.tidbapp;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
public class BatchInsertTidb {

    public static void main(String[] args){

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://192.168.30.128:4000/tidb1");
        config.setUsername("root");
        config.setPassword("tidbrootpass");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        HikariDataSource ds = new HikariDataSource(config);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);

        List<KbAudience> audiences = new ArrayList<>();

        String startTime = ScStringUtils.getCurrentTimeOfLog();
        for (int ii = 0; ii < 5000000; ii++) {
            KbAudience audience = new KbAudience();
            audience.setId(ScStringUtils.getObjectID("90"));
            audience.setAdsGroupNo(1004);
            audience.setAdvertisingId(UUID.randomUUID().toString());
            audience.setIdType("ADID");
            audience.setCtaList("[433]]");

            audience.setTtl(Instant.now().getEpochSecond());
            audiences.add(audience);
        }

        //Get ExecutorService from Executors utility class, thread pool size is 10
        ExecutorService executor = Executors.newFixedThreadPool(10);
        //create a list to hold the Future object associated with Callable
        List<Future<WorkerResult>> list = new ArrayList<Future<WorkerResult>>();
        //Create MyCallable instance
        List<List<KbAudience>> subList = Lists.partition(audiences, 1000);

        for(int i=0; i< subList.size(); i++){
            //submit Callable tasks to be executed by thread pool
            List<KbAudience> childList = subList.get(i);
            WorkerResult workerResult = new WorkerResult();
            workerResult.setWorkerId("audienct"+i);
            workerResult.setSubIndex(i);
            Callable<WorkerResult> callable = new WorkerCallable(jdbcTemplate, childList, workerResult);
            Future<WorkerResult> future = executor.submit(callable);
            //add Future to the list, we can get return value using Future
            list.add(future);
        }
        for(Future<WorkerResult> fut : list){
            try {
                //print the return value of Future, notice the output delay in console
                // because Future.get() waits for task to get completed
                System.out.println(new Date()+ "::"+fut.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        String endTime = ScStringUtils.getCurrentTimeOfLog();
        log.info("subList: "+subList.size());
        log.info("startTime: "+startTime);
        log.info("endTime: "+endTime);
        //shut down the executor service now
        executor.shutdown();
    }
}
