package com.sparta.interview.controller;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class MainController {

  @PostMapping("/load/{provider}")
  public int load(@PathVariable("provider") String provider, @RequestBody byte[] content) throws IOException {
  }

  @GetMapping("/data/{provider}/total")
  public int total(@PathVariable("provider") String provider) {
  }

}
