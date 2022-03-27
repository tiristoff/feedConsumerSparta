package com.sparta.interview;

import com.sparta.interview.persistence.service.ProviderPersistenceMapServiceImpl;
import com.sparta.interview.service.ProviderServiceImpl;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.CRC32;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class ProviderServiceTest {

  @InjectMocks private ProviderServiceImpl providerService;
  @Mock private ProviderPersistenceMapServiceImpl providerPersistenceMapService;
  private final static String[] cities = {"Leon", "Barcelona", "Albacete", "Menorca", "Zaragoza"};


  private static byte[] getContent() throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final BufferedOutputStream mainBos = new BufferedOutputStream(baos);
    final DataOutputStream dataOutputStream = new DataOutputStream(mainBos);

    final Random rnd = new Random();
    final long records = rnd.nextInt(26) + 5;

    dataOutputStream.writeLong(records);

    for (int i = 0; i < records; i++) {
      dataOutputStream.writeLong(i);
      dataOutputStream.writeLong(new Date().getTime());
      final String city = cities[rnd.nextInt(5)];
      dataOutputStream.writeInt(city.getBytes().length);
      dataOutputStream.write(city.getBytes());

      final ByteArrayOutputStream sensorArrayStream = new ByteArrayOutputStream();
      final BufferedOutputStream bos = new BufferedOutputStream(sensorArrayStream);
      final DataOutputStream sensorOutputStream = new DataOutputStream(bos);

      final Map<String, Integer> sensors = new HashMap<>();
      final long numberOfSensors = rnd.nextInt(11);
      for (int j = 0; j < numberOfSensors; j++) {
        sensors.put(UUID.randomUUID().toString(), rnd.nextInt(401) + 50);
      }
      sensorOutputStream.writeInt(sensors.size());

      for (Map.Entry<String, Integer> entrySet : sensors.entrySet()) {
        sensorOutputStream.writeInt(entrySet.getKey().getBytes().length);
        sensorOutputStream.write(entrySet.getKey().getBytes());
        sensorOutputStream.writeInt(entrySet.getValue());
      }
      sensorOutputStream.flush();
      final byte[] sensorCollection = sensorArrayStream.toByteArray();
      dataOutputStream.writeInt(sensorCollection.length);
      dataOutputStream.write(sensorCollection);
      final CRC32 crc32 = new CRC32();
      crc32.update(sensorCollection);
      dataOutputStream.writeLong(crc32.getValue());
    }
    dataOutputStream.flush();

    return baos.toByteArray();
  }

  private static Stream<Arguments> parameters() throws IOException {
    return Stream.of(
        Arguments.of("bp", getContent()),
        Arguments.of("repsol", getContent()),
        Arguments.of("cepsa", getContent()));
  }

  @Test
  public void test_givenNoContent_WhenDataForProviderIsRetrieved_NoDataIsRetrived()
      throws IOException {
    assertEquals(0, providerService.loadProvider("bp", null));
  }

  @ParameterizedTest
  @MethodSource("parameters")
  public void test_givenContent_WhenDataIsLoaded_ThenReturnSameRecords(
      String provider, byte[] content) throws IOException {
    DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(content));
    long records = dataInputStream.readLong();
    dataInputStream.reset();
    assertEquals(records, providerService.loadProvider(provider, content));
  }

  @ParameterizedTest
  @MethodSource("parameters")
  public void test_givenContent_WhenDataIsLoaded_ThenTotalRequestReturns(
      String provider, byte[] content) throws IOException {
    DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(content));
    long records = dataInputStream.readLong();
    BDDMockito.given(providerPersistenceMapService.totalRecordsOfProvider(anyString()))
        .willReturn((int) records);
    dataInputStream.reset();
    providerService.loadProvider(provider, content);
    providerService.totalRecords(provider);

    BDDMockito.then(providerPersistenceMapService).should(BDDMockito.times(1)).totalRecordsOfProvider(provider);
  }
}
