package com.roommanager;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RoomManagerApplicationIT {

  private static final String ENDPOINT_ROOMS_AVAILABILITY = "/rooms/availability";
  private static final String AVAILABLE_PREMIUM_ROOMS = "availablePremiumRooms";
  private static final String AVAILABLE_ECONOMY_ROOMS = "availableEconomyRooms";
  @Autowired
  private MockMvc mvc;

  private static Map<String, Object> getMatchersFromAccessor(ArgumentsAccessor accessor, int roomType, int count,
      int price, int currency) {
    return getMatchersFromValues(accessor.getString(roomType), accessor.getInteger(count), accessor.getDouble(price),
        accessor.getString(currency));
  }

  private static Map<String, Object> getMatchersFromValues(String roomType, int count, double price, String currency) {
    return Map.of(
        "roomType", roomType,
        "customersCount", count,
        "totalPrice", price,
        "currency", currency
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {"availablePremiumRooms", "availableEconomyRooms"})
  @DisplayName("returns 400 for negative query params")
  void negativeQueryParams(String paramName) throws Exception {
    mvc.perform(get(ENDPOINT_ROOMS_AVAILABILITY)
            .param(paramName, "-1"))
        .andDo(print())
        .andExpect(status().is(400))
        .andExpect(jsonPath("$.message").value(Matchers.containsString(paramName)));
  }

  @Test
  @DisplayName("returns empty array for missing query params")
  void emptyArrayWhenNoParamsPassed() throws Exception {
    mvc.perform(get(ENDPOINT_ROOMS_AVAILABILITY))
        .andDo(print())
        .andExpect(status().is(200))
        .andExpect(jsonPath("$", Matchers.empty()));
  }

  @ParameterizedTest
  @CsvSource({
      "availablePremiumRooms, 1, PREMIUM, 1, 374, EUR",
      "availableEconomyRooms, 1, ECONOMY, 1, 99.99, EUR"})
  @DisplayName("if single type of room is available, result contains one availability for given type")
  void returnSingleValueList(ArgumentsAccessor accessor) throws Exception {
    mvc.perform(get(ENDPOINT_ROOMS_AVAILABILITY)
            .param(accessor.getString(0), accessor.getString(1)))
        .andDo(print())
        .andExpect(status().is(200))
        .andExpect(jsonPath("$", Matchers.hasSize(1)))
        .andExpect(jsonPath("$").value(containsInAnyOrder(getMatchersFromAccessor(accessor, 2, 3, 4, 5))));
  }

  @Test
  @DisplayName("premium and economy customers are checkin in corresponding rooms, if customers of each type are more than free rooms")
  void customersCheckinInCorrespondingRoomsIfMoreCustomersThanRooms() throws Exception {
    mvc.perform(get(ENDPOINT_ROOMS_AVAILABILITY)
            .param(AVAILABLE_PREMIUM_ROOMS, "3")
            .param(AVAILABLE_ECONOMY_ROOMS, "3"))
        .andDo(print())
        .andExpect(status().is(200))
        .andExpect(jsonPath("$", Matchers.hasSize(2)))
        .andExpect(jsonPath("$.[?(@.roomType=='PREMIUM')]")
            .value(containsInAnyOrder(getMatchersFromValues("PREMIUM", 3, 738, "EUR"))))
        .andExpect(jsonPath("$.[?(@.roomType=='ECONOMY')]")
            .value(containsInAnyOrder(getMatchersFromValues("ECONOMY", 3, 167.99, "EUR"))));
  }

  @Test
  @DisplayName("premium and economy customers are checkin in corresponding rooms, if customers of each type are less than free rooms")
  void customersCheckinInCorrespondingIfMoreRoomsThanCustomers() throws Exception {
    mvc.perform(get(ENDPOINT_ROOMS_AVAILABILITY)
            .param(AVAILABLE_PREMIUM_ROOMS, "7")
            .param(AVAILABLE_ECONOMY_ROOMS, "5"))
        .andDo(print())
        .andExpect(status().is(200))
        .andExpect(jsonPath("$", Matchers.hasSize(2)))
        .andExpect(jsonPath("$.[?(@.roomType=='PREMIUM')]")
            .value(containsInAnyOrder(getMatchersFromValues("PREMIUM", 6, 1054, "EUR"))))
        .andExpect(jsonPath("$.[?(@.roomType=='ECONOMY')]")
            .value(containsInAnyOrder(getMatchersFromValues("ECONOMY", 4, 189.99, "EUR"))));
  }

  @Test
  @DisplayName("if after checkin, remain free economy rooms, no premium customer is moved there")
  void noPremiumCustomerCheckinInEconomyRoom() throws Exception {
    mvc.perform(get(ENDPOINT_ROOMS_AVAILABILITY)
            .param(AVAILABLE_PREMIUM_ROOMS, "2")
            .param(AVAILABLE_ECONOMY_ROOMS, "7"))
        .andDo(print())
        .andExpect(status().is(200))
        .andExpect(jsonPath("$", Matchers.hasSize(2)))
        .andExpect(jsonPath("$.[?(@.roomType=='PREMIUM')]")
            .value(containsInAnyOrder(getMatchersFromValues("PREMIUM", 2, 583, "EUR"))))
        .andExpect(jsonPath("$.[?(@.roomType=='ECONOMY')]")
            .value(containsInAnyOrder(getMatchersFromValues("ECONOMY", 4, 189.99, "EUR"))));
  }

  @Test
  @DisplayName("if after checkin, remain free premium rooms, economy customer with higher price is moved there")
  void economyCustomerCheckinInPremiumRoom() throws Exception {
    mvc.perform(get(ENDPOINT_ROOMS_AVAILABILITY)
            .param(AVAILABLE_PREMIUM_ROOMS, "7")
            .param(AVAILABLE_ECONOMY_ROOMS, "1"))
        .andDo(print())
        .andExpect(status().is(200))
        .andExpect(jsonPath("$", Matchers.hasSize(2)))
        .andExpect(jsonPath("$.[?(@.roomType=='PREMIUM')]")
            .value(containsInAnyOrder(getMatchersFromValues("PREMIUM", 7, 1153.99, "EUR"))))
        .andExpect(jsonPath("$.[?(@.roomType=='ECONOMY')]")
            .value(containsInAnyOrder(getMatchersFromValues("ECONOMY", 1, 45, "EUR"))));
  }
}
