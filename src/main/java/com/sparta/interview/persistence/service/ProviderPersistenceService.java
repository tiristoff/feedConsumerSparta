package com.sparta.interview.persistence.service;

import com.sparta.interview.domain.Provider;

public interface ProviderPersistenceService {

  public void storeData(Provider provider);

  public Provider getProviderByName(String provider);

  public int totalRecordsOfProvider(String provider);
}
