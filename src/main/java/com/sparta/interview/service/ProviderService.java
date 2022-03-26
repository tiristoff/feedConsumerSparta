package com.sparta.interview.service;

import java.io.IOException;

public interface ProviderService {

    public int loadProvider(String provider,byte[] content) throws IOException, ClassNotFoundException;

    public int totalRecords(String provider);
}
