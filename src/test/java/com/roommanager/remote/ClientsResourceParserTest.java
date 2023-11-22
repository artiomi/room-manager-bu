package com.roommanager.remote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

class ClientsResourceParserTest {

  private static final String JSON_VALID = """
      [23, 209.12]""";
  private static final String JSON_INVALID = """
      [23, 209, acb]""";
  private final ObjectMapper objectMapper = new ObjectMapper();
  private ClientsResourceParser clientsResourceParser;

  private void initParser(String json) {
    Resource countriesResource = new ByteArrayResource(json.getBytes());
    clientsResourceParser = new ClientsResourceParser(objectMapper, countriesResource);
  }

  @Nested
  class GetRecordsTest {

    @Test
    @DisplayName("resource with numbers successfully parsed")
    void parseSuccessfully() {
      initParser(JSON_VALID);
      List<Double> records = clientsResourceParser.getRecords();
      assertThat(records).isNotEmpty();
      assertThat(records).containsAll(List.of(23D, 209.12));
    }

    @Test
    @DisplayName("exception is thrown for invalid resource")
    void parseFailed() {
      initParser(JSON_INVALID);
      assertThatThrownBy(() -> clientsResourceParser.getRecords()).isInstanceOf(RuntimeException.class);
    }
  }
}