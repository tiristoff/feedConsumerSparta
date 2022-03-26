package com.sparta.interview.controller;

import com.sparta.interview.service.ProviderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@AllArgsConstructor
public class MainController {

  private ProviderService providerService;

  @PostMapping("/load/{provider}")
  public int load(@PathVariable("provider") String provider, @RequestBody byte[] content)
      throws IOException, ClassNotFoundException {
    return providerService.loadProvider(provider, content);
  }

  @GetMapping("/data/{provider}/total")
  public int total(@PathVariable("provider") String provider) {
    return providerService.totalRecords(provider);
  }
}
