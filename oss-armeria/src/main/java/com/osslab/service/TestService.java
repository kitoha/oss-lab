package com.osslab.service;

import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.ServiceRequestContext;
import org.springframework.stereotype.Service;

@Service
public class TestService implements HttpService {

    public String getMessage() {
        return "Hello from TestService!";
    }

    public String getGreeting(String name) {
        return "Hello, " + name + "!";
    }

  @Override
  public HttpResponse serve(ServiceRequestContext ctx, HttpRequest req) throws Exception {
    return null;
  }
}
