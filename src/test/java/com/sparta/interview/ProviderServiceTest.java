package com.sparta.interview;

import com.sparta.interview.domain.Provider;
import com.sparta.interview.domain.Sensor;
import com.sparta.interview.persistence.service.ProviderPersistenceMapServiceImpl;
import com.sparta.interview.service.ProviderServiceImpl;
import com.sparta.interview.utils.DataProvider;
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
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.CRC32;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class ProviderServiceTest {

  @InjectMocks private ProviderServiceImpl providerService;
  @Mock private ProviderPersistenceMapServiceImpl providerPersistenceMapService;

  private static byte[] getContent() throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final BufferedOutputStream mainBos = new BufferedOutputStream(baos);
    final DataOutputStream dataOutputStream = new DataOutputStream(mainBos);

    final Provider provider = DataProvider.getProvider(null);

    dataOutputStream.writeLong(provider.getRecords().size());

    for (int i = 0; i < provider.getRecords().size(); i++) {
      dataOutputStream.writeLong(i);
      dataOutputStream.writeLong(new Date().getTime());
      writeSting(dataOutputStream, provider.getRecords().get(i).getCity().getBytes());

      final byte[] sensorCollection = writeSensorCollection(provider.getRecords().get(i).getSensorList());
      dataOutputStream.writeInt(sensorCollection.length);
      dataOutputStream.write(sensorCollection);
      writeCrc32(dataOutputStream, sensorCollection);
    }
    dataOutputStream.flush();

    return baos.toByteArray();
  }

  private static byte[] writeSensorCollection(List<Sensor> sensors) throws IOException {

    final ByteArrayOutputStream sensorArrayStream = new ByteArrayOutputStream();
    final BufferedOutputStream bos = new BufferedOutputStream(sensorArrayStream);
    final DataOutputStream sensorOutputStream = new DataOutputStream(bos);

    sensorOutputStream.writeInt(sensors.size());

    for (Sensor sensor : sensors) {
      writeSting(sensorOutputStream,sensor.getId().getBytes());
      sensorOutputStream.writeInt(sensor.getMeasure());
    }
    sensorOutputStream.flush();
    return sensorArrayStream.toByteArray();
  }

  private static void writeCrc32(DataOutputStream dataOutputStream, byte[] sensorCollection)
      throws IOException {
    final CRC32 crc32 = new CRC32();
    crc32.update(sensorCollection);
    dataOutputStream.writeLong(crc32.getValue());
  }

  private static void writeSting(DataOutputStream dos, byte[] string) throws IOException {
    dos.writeInt(string.length);
    dos.write(string);
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

    BDDMockito.then(providerPersistenceMapService)
        .should(BDDMockito.times(1))
        .totalRecordsOfProvider(provider);
  }
}
