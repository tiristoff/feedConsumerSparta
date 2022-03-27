package com.sparta.interview.service;

import com.sparta.interview.domain.Provider;
import com.sparta.interview.domain.Record;
import com.sparta.interview.domain.Sensor;
import com.sparta.interview.persistence.service.ProviderPersistenceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.CRC32;

@Service("providerService")
@Slf4j
@AllArgsConstructor
public class ProviderServiceImpl implements ProviderService {

  private ProviderPersistenceService providerPersistenceMapService;

  @Override
  public int loadProvider(String provider, byte[] content) throws IOException {
    if (null == content || content.length == 0){
       return 0;
    }
    Provider providerDomain = readData(provider, content);
    providerPersistenceMapService.storeData(providerDomain);
    return providerDomain.getRecords().size();
  }

  @Override
  public int totalRecords(String provider) {
    return providerPersistenceMapService.totalRecordsOfProvider(provider);
  }

  private Provider readData(String provider, byte[] content) throws IOException {

    final DataInputStream mainReader = getInputStream(content);
    final Provider providerDomain = Provider.builder().records(new LinkedList<>()).name(provider).build();

    final long numberOfRecords = mainReader.readLong();
    log.info("Number of records " + numberOfRecords);
    for (int i = 0; i < numberOfRecords; i++) {
      final Record record = new Record();
      log.info("Record number: "+mainReader.readLong());
      record.setTimestamp(mainReader.readLong());
      record.setCity(readString(mainReader, mainReader.readInt()));
      record.setSensorList(readSensors(mainReader));
      providerDomain.getRecords().add(record);
    }

    return providerDomain;
  }

  private List<Sensor> readSensors (DataInputStream dis) throws IOException {

    final int lengthOfSensor = dis.readInt();
    final byte[] sensorsData = dis.readNBytes(lengthOfSensor);
    final DataInputStream sensorsReader = getInputStream(sensorsData);

    testCrc32(dis, sensorsData);
    final List<Sensor> sensors = new LinkedList<>();
    final int numberOfSensors = sensorsReader.readInt();
    for (int i = 0; i < numberOfSensors; i++) {
      sensors.add(
          Sensor.builder()
              .id(readString(sensorsReader, sensorsReader.readInt()))
              .measure(sensorsReader.readInt())
              .build());
    }

    return sensors;
  }

  private void testCrc32(DataInputStream dis, byte[] sensorsData) throws IOException {
    final CRC32 crc32 = new CRC32();
    crc32.update(sensorsData);
    if (crc32.getValue() != dis.readLong()){
      throw new RuntimeException("Data is wrong");
    }
  }

  private DataInputStream getInputStream(byte[] content) {
    return new DataInputStream(new ByteArrayInputStream(content));
  }

  private String readString(DataInputStream dis, int lengthOfString) throws IOException {
    String finalString = "";
    for (int i = 0; i < lengthOfString; i++) {
      finalString = finalString.concat(String.valueOf((char) dis.read()));
    }
    return finalString;
  }
}
