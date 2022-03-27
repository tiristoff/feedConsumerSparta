package com.sparta.interview.persistence.service;

import com.sparta.interview.domain.Provider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service("providerPersistenceMapService")
public class ProviderPersistenceMapServiceImpl implements ProviderPersistenceService {

  private final List<Provider> providers = new LinkedList<>();

  @Override
  public void storeData(Provider provider) {
    final Provider storedProvider = findExistingProvider(provider.getName());
    if (storedProvider == null) {
      providers.add(provider);
    } else {
      storedProvider.getRecords().addAll(provider.getRecords());
    }
  }

  @Override
  public Provider getProviderByName(String provider) {
    return findExistingProvider(provider);
  }

  @Override
  public int totalRecordsOfProvider(String provider) {
    return Optional.ofNullable(getProviderByName(provider).getRecords())
        .orElse(new ArrayList<>())
        .size();
  }

  private Provider findExistingProvider(String provider) {
    return providers.stream().filter(p -> p.getName().equals(provider)).findFirst().orElse(null);
  }
}
