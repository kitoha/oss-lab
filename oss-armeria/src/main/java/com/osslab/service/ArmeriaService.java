package com.osslab.service;

import com.linecorp.armeria.server.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArmeriaService {

  private final TestService testService;

    public void startService() {
      Server server = Server.builder()
          .http(8080)
          .build();
    }


}
