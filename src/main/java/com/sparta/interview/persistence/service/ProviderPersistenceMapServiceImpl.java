package com.sparta.interview.persistence.service;

import com.sparta.interview.domain.Provider;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

@Service("providerPersistenceMapService")
public class ProviderPersistenceMapServiceImpl implements ProviderPersistenceService {

  Map<String, Provider> providerMap = new HashMap<>();

  @Override
  public void storeData(Provider provider) {
    Provider storedProvider = providerMap.get(provider.getName());
    if (storedProvider == null) {
      providerMap.put(provider.getName(), provider);
    } else {
      storedProvider.getRecords().addAll(provider.getRecords());
    }
  }

  @Override
  public Provider getProviderByName(String provider) {
    return providerMap.get(provider);
  }

  @Override
  public int totalRecordsOfProvider(String provider) {
    return Optional.ofNullable(getProviderByName(provider).getRecords())
        .orElse(new LinkedList<>())
        .size();
  }
}
