package com.sparta.interview.utils;

import com.sparta.interview.domain.Provider;
import com.sparta.interview.domain.Record;
import com.sparta.interview.domain.Sensor;

import java.util.Date;
import java.util.LinkedList;
import java.util.Random;
import java.util.UUID;

public class DataProvider {

    private static final Random rnd = new Random();
    private static final String[] cities = {"Leon", "Barcelona", "Albacete", "Menorca", "Zaragoza"};
    private static final Providers[] providers = Providers.values();

    public static Provider getProvider(String providerName) {


        Provider provider =
                Provider.builder().name(null == providerName ? providers[rnd.nextInt(5)].toString() : providerName).records(new LinkedList<>()).build();
        int records = rnd.nextInt(121) + 10;

        for (int i = 0; i < records; i++) {
            Record record =
                    Record.builder()
                            .city(cities[rnd.nextInt(5)])
                            .timestamp(new Date().getTime())
                            .sensorList(new LinkedList<>())
                            .build();
            provider.getRecords().add(record);
            int numberOfSensors = rnd.nextInt(21) + 5;
            for (int j = 0; j < numberOfSensors; j++) {
                record
                        .getSensorList()
                        .add(
                                Sensor.builder()
                                        .id(UUID.randomUUID().toString())
                                        .measure(rnd.nextInt(3001))
                                        .build());
            }
        }

        return provider;
    }
}
