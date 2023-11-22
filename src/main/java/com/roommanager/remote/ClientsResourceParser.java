package com.roommanager.remote;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClientsResourceParser {

  private final ObjectMapper objectMapper;
  private final Resource clientsResource;

  public ClientsResourceParser(ObjectMapper objectMapper,
      @Value("${app.clients-resource}") Resource clientsResource) {
    this.objectMapper = objectMapper;
    this.clientsResource = clientsResource;
  }

  @PostConstruct
  public List<Double> getRecords() {
    log.info("Start loading clients from file:{}", clientsResource.getFilename());
    List<Double> result;
    try {
      result = objectMapper.readValue(
          clientsResource.getContentAsByteArray(), new TypeReference<>() {
          });

      log.info("Clients load complete. Loaded {} entries.", result.size());
    } catch (IOException e) {
      throw new RuntimeException(String.format("File [%s] parsing failed", clientsResource.getFilename()), e);
    }
    return result;
  }
}
