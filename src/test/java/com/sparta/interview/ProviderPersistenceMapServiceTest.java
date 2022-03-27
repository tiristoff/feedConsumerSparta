package com.sparta.interview;

import com.sparta.interview.domain.Provider;
import com.sparta.interview.persistence.service.ProviderPersistenceMapServiceImpl;
import com.sparta.interview.utils.DataProvider;
import com.sparta.interview.utils.Providers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class ProviderPersistenceMapServiceTest {

  @InjectMocks private ProviderPersistenceMapServiceImpl providerPersistenceMapService;

  @Test
  public void testGivenAProvider_WhenProviderIsStored_ThenIsReturned() {
    Provider provider = DataProvider.getProvider(null);
    providerPersistenceMapService.storeData(provider);

    assertEquals(provider, providerPersistenceMapService.getProviderByName(provider.getName()));
    assertEquals(
        provider.getRecords().size(),
        providerPersistenceMapService.getProviderByName(provider.getName()).getRecords().size());
  }

  @Test
  public void testGivenAnExistingProvider_WhenProviderIsStored_ThenProviderHasAllRecords() {
    Provider existingProvider = DataProvider.getProvider(Providers.BP.toString());
    providerPersistenceMapService.storeData(existingProvider);
    Provider sameProvider = DataProvider.getProvider(Providers.BP.toString());
    providerPersistenceMapService.storeData(sameProvider);
    assertEquals(
        existingProvider.getRecords().size(),
        providerPersistenceMapService.totalRecordsOfProvider(Providers.BP.toString()));
  }
}
