package com.roommanager.remote.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.roommanager.domain.model.Customer;
import com.roommanager.remote.ClientsResourceParser;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InMemoryCustomerRepoTest {

  private static final List<Double> PRICES = List.of(23D, 45D, 155D, 374D, 22D, 99.99D, 100D, 101D, 115D, 209D);
  private InMemoryCustomerRepo repo;
  @Mock
  private ClientsResourceParser clientsResourceParser;

  @BeforeEach
  void beforeEach() {
    repo = new InMemoryCustomerRepo(clientsResourceParser);
  }

  private void initClientsStore() {
    when(clientsResourceParser.getRecords()).thenReturn(PRICES);
    repo.postConstruct();
  }

  @Nested
  class PostConstructTest {

    @Test
    @DisplayName("customers list is null, when postConstruct not called")
    void customersListIsNullWhenPostConstructNotCalled() {
      List<Customer> customers = repo.findAll();
      assertThat(customers).isNull();
      verify(clientsResourceParser, never()).getRecords();
    }

    @Test
    @DisplayName("customers list not null/empty, when postConstruct is called")
    void customersListNotNullAfterPostConstructCall() {
      initClientsStore();
      List<Customer> customers = repo.findAll();
      assertThat(customers).isNotNull();
      assertThat(customers).isNotEmpty();
    }

    @Test
    @DisplayName("customers list is sorted descendent")
    void customersListAfterPostConstructCallIsSorted() {
      initClientsStore();
      List<Customer> customers = repo.findAll();
      assertThat(customers).first().matches(c -> c.priceOffer().compareTo(BigDecimal.valueOf(374)) == 0);
      assertThat(customers).last().matches(c -> c.priceOffer().compareTo(BigDecimal.valueOf(22)) == 0);
    }
  }

  @Nested
  class FindByPriceOfferGTEOrderByPriceOfferDescTest {

    @Test
    @DisplayName("throw NPE for null customers list")
    void throwsNPEForNullCustomersList() {
      assertThatThrownBy(() -> repo.findByPriceOfferGTEOrderByPriceOfferDesc(BigDecimal.ONE, 2))
          .isInstanceOf(NullPointerException.class);
      verify(clientsResourceParser, never()).getRecords();
    }

    @Test
    @DisplayName("empty list returned if there is no price greater or equal to provided one")
    void returnEmptyListOnMissingGreaterPrice() {
      initClientsStore();
      assertThat(repo.findByPriceOfferGTEOrderByPriceOfferDesc(BigDecimal.valueOf(1000), 2))
          .isEmpty();
    }

    @Test
    @DisplayName("list of given size is returned")
    void returnCustomersListOfRequestedSize() {
      initClientsStore();
      int limit = 2;
      var result = repo.findByPriceOfferGTEOrderByPriceOfferDesc(BigDecimal.valueOf(100), limit);
      assertThat(result).size().isEqualTo(limit);
    }

    @Test
    @DisplayName("list of lower size is returned if there are less customers with price greater or equal than filtered")
    void returnCustomersListOfSizeLessThanGivenLimit() {
      initClientsStore();
      int limit = 20;
      var result = repo.findByPriceOfferGTEOrderByPriceOfferDesc(BigDecimal.valueOf(100), limit);
      assertThat(result).size().isLessThan(limit);
      assertThat(result).size().isEqualTo(6);
    }

    @Test
    @DisplayName("list of customers with price greater or equal to provided filter returned")
    void returnCustomersListWithPricesGreaterThanOrEqualToGivenFilter() {
      initClientsStore();
      BigDecimal priceFilter = BigDecimal.valueOf(100);
      var result = repo.findByPriceOfferGTEOrderByPriceOfferDesc(priceFilter, 20);
      assertThat(result).allMatch(c -> c.priceOffer().compareTo(priceFilter) >= 0);
    }
  }

  @Nested
  class FindByPriceOfferLTOrderByPriceOfferDescTest {

    @Test
    @DisplayName("throw NPE for null customers list")
    void throwsNPEForNullCustomersList() {
      assertThatThrownBy(() -> repo.findByPriceOfferLTOrderByPriceOfferDesc(BigDecimal.ONE, 2))
          .isInstanceOf(NullPointerException.class);
      verify(clientsResourceParser, never()).getRecords();
    }

    @Test
    @DisplayName("empty list returned if there is no price smaller than provided one")
    void returnEmptyListOnMissingSmallerPrice() {
      initClientsStore();
      assertThat(repo.findByPriceOfferLTOrderByPriceOfferDesc(BigDecimal.valueOf(10), 2))
          .isEmpty();
    }

    @Test
    @DisplayName("list of given size is returned")
    void returnCustomersListOfRequestedSize() {
      initClientsStore();
      int limit = 2;
      var result = repo.findByPriceOfferLTOrderByPriceOfferDesc(BigDecimal.valueOf(100), limit);
      assertThat(result).size().isEqualTo(limit);
    }

    @Test
    @DisplayName("list of lower size is returned if there are less customers with price lower than filter, if there is customer with filter price")
    void returnCustomersListOfSizeLessThanGivenLimit() {
      initClientsStore();
      int limit = 20;
      var result = repo.findByPriceOfferLTOrderByPriceOfferDesc(BigDecimal.valueOf(100), limit);
      assertThat(result).size().isLessThan(limit);
      assertThat(result).size().isEqualTo(4);
    }

    @Test
    @DisplayName("list of lower size is returned if there are less customers with price lower than filter, if there is no customer with filter price")
    void returnCustomersListOfSizeLessThanGivenLimitForPriceNotPresentInOffers() {
      initClientsStore();
      int limit = 20;
      var result = repo.findByPriceOfferLTOrderByPriceOfferDesc(BigDecimal.valueOf(99.999), limit);
      assertThat(result).size().isLessThan(limit);
      assertThat(result).size().isEqualTo(4);
    }

    @Test
    @DisplayName("list of customers with price less than provided filter returned, if there is customer with filter price")
    void returnCustomersListWithPricesLessThanGivenFilter() {
      initClientsStore();
      BigDecimal priceFilter = BigDecimal.valueOf(100);
      var result = repo.findByPriceOfferLTOrderByPriceOfferDesc(priceFilter, 20);
      assertThat(result).allMatch(c -> c.priceOffer().compareTo(priceFilter) < 0);
    }

    @Test
    @DisplayName("list of customers with price less than provided filter returned, if there is no customer with filter price")
    void returnCustomersListWithPricesLessThanGivenFilterForPriceNotPresentInOffers() {
      initClientsStore();
      BigDecimal priceFilter = BigDecimal.valueOf(99.999);
      var result = repo.findByPriceOfferLTOrderByPriceOfferDesc(priceFilter, 20);
      assertThat(result).allMatch(c -> c.priceOffer().compareTo(priceFilter) < 0);
    }
  }
}