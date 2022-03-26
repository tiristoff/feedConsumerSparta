package com.sparta.interview.service;

import com.sparta.interview.domain.Provider;
import com.sparta.interview.domain.Record;
import com.sparta.interview.domain.Sensor;
import com.sparta.interview.persistence.service.ProviderPersistenceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.CRC32;

@Service("providerService")
@AllArgsConstructor
@Slf4j
public class ProviderServiceImpl implements ProviderService {

  private ProviderPersistenceService providerPersistenceMapService;

  @Override
  public int loadProvider(String provider, byte[] content) throws IOException {
    Provider providerDomain = readData(provider, content);
    providerPersistenceMapService.storeData(providerDomain);
    return providerDomain.getRecords().size();
  }

  @Override
  public int totalRecords(String provider) {
    return providerPersistenceMapService.totalRecordsOfProvider(provider);
  }

  private Provider readData(String provider, byte[] content) throws IOException {

    DataInputStream mainReader = getInputStream(content);
    Provider providerDomain = Provider.builder().records(new LinkedList<>()).name(provider).build();

    long numberOfRecords = mainReader.readLong();
    log.info("Number of records " + numberOfRecords);
    for (int i = 0; i < numberOfRecords; i++) {
      Record record = new Record();
      record.setTimestamp(mainReader.readLong());
      record.setCity(readString(mainReader, mainReader.readInt()));
      record.setSensorList(readSensors(mainReader));
      providerDomain.getRecords().add(record);
    }

    return providerDomain;
  }

  private List<Sensor> readSensors(DataInputStream dis) throws IOException {

    int lengthOfSensor = dis.readInt();
    byte[] sensorsData = dis.readNBytes(lengthOfSensor);
    DataInputStream sensorsReader = getInputStream(sensorsData);

    checkCrc32(dis, sensorsData);
    List<Sensor> sensors = new LinkedList<>();
    int numberOfSensors = sensorsReader.readInt();
    for (int i = 0; i < numberOfSensors; i++) {
      sensors.add(
          Sensor.builder()
              .id(readString(sensorsReader, sensorsReader.readInt()))
              .measure(sensorsReader.readInt())
              .build());
    }

    return sensors;
  }

  private void checkCrc32(DataInputStream dis, byte[] sensorsData) throws IOException {
    CRC32 crc32 = new CRC32();
    crc32.update(sensorsData);
    if (crc32.getValue() != dis.readLong()) {
      throw new RuntimeException("Data is wrong");
    }
  }

  private DataInputStream getInputStream(byte[] content) {
    return new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(content)));
  }

  private String readString(DataInputStream dis, int lengthOfString) throws IOException {
    String finalString = "";
    for (int i = 0; i < lengthOfString; i++) {
      finalString = finalString.concat(String.valueOf((char) dis.read()));
    }
    return finalString;
  }
}
