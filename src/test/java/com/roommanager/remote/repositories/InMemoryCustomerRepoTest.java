package com.roommanager.remote.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.roommanager.domain.model.Customer;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InMemoryCustomerRepoTest {

  private final InMemoryCustomerRepo repo = new InMemoryCustomerRepo();

  @Nested
  class PostConstructTest {

    @Test
    @DisplayName("customers list is null, when postConstruct not called")
    void customersListIsNullWhenPostConstructNotCalled() {
      List<Customer> customers = repo.findAll();
      assertThat(customers).isNull();
    }

    @Test
    @DisplayName("customers list not null/empty, when postConstruct is called")
    void customersListNotNullAfterPostConstructCall() {
      repo.postConstruct();
      List<Customer> customers = repo.findAll();
      assertThat(customers).isNotNull();
      assertThat(customers).isNotEmpty();
    }

    @Test
    @DisplayName("customers list is sorted descendent")
    void customersListAfterPostConstructCallIsSorted() {
      repo.postConstruct();
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
    }

    @Test
    @DisplayName("empty list returned if there is no price greater or equal to provided one")
    void returnEmptyListOnMissingGreaterPrice() {
      repo.postConstruct();
      assertThat(repo.findByPriceOfferGTEOrderByPriceOfferDesc(BigDecimal.valueOf(1000), 2))
          .isEmpty();
    }

    @Test
    @DisplayName("list of given size is returned")
    void returnCustomersListOfRequestedSize() {
      repo.postConstruct();
      int limit = 2;
      var result = repo.findByPriceOfferGTEOrderByPriceOfferDesc(BigDecimal.valueOf(100), limit);
      assertThat(result).size().isEqualTo(limit);
    }

    @Test
    @DisplayName("list of lower size is returned if there are less customers with price greater or equal than filtered")
    void returnCustomersListOfSizeLessThanGivenLimit() {
      repo.postConstruct();
      int limit = 20;
      var result = repo.findByPriceOfferGTEOrderByPriceOfferDesc(BigDecimal.valueOf(100), limit);
      assertThat(result).size().isLessThan(limit);
      assertThat(result).size().isEqualTo(6);
    }

    @Test
    @DisplayName("list of customers with price greater or equal to provided filter returned")
    void returnCustomersListWithPricesGreaterThanOrEqualToGivenFilter() {
      repo.postConstruct();
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
    }

    @Test
    @DisplayName("empty list returned if there is no price smaller than provided one")
    void returnEmptyListOnMissingSmallerPrice() {
      repo.postConstruct();
      assertThat(repo.findByPriceOfferLTOrderByPriceOfferDesc(BigDecimal.valueOf(10), 2))
          .isEmpty();
    }

    @Test
    @DisplayName("list of given size is returned")
    void returnCustomersListOfRequestedSize() {
      repo.postConstruct();
      int limit = 2;
      var result = repo.findByPriceOfferLTOrderByPriceOfferDesc(BigDecimal.valueOf(100), limit);
      assertThat(result).size().isEqualTo(limit);
    }

    @Test
    @DisplayName("list of lower size is returned if there are less customers with price lower than filter, if there is customer with filter price")
    void returnCustomersListOfSizeLessThanGivenLimit() {
      repo.postConstruct();
      int limit = 20;
      var result = repo.findByPriceOfferLTOrderByPriceOfferDesc(BigDecimal.valueOf(100), limit);
      assertThat(result).size().isLessThan(limit);
      assertThat(result).size().isEqualTo(4);
    }

    @Test
    @DisplayName("list of lower size is returned if there are less customers with price lower than filter, if there is no customer with filter price")
    void returnCustomersListOfSizeLessThanGivenLimitForPriceNotPresentInOffers() {
      repo.postConstruct();
      int limit = 20;
      var result = repo.findByPriceOfferLTOrderByPriceOfferDesc(BigDecimal.valueOf(99.999), limit);
      assertThat(result).size().isLessThan(limit);
      assertThat(result).size().isEqualTo(4);
    }

    @Test
    @DisplayName("list of customers with price less than provided filter returned, if there is customer with filter price")
    void returnCustomersListWithPricesLessThanGivenFilter() {
      repo.postConstruct();
      BigDecimal priceFilter = BigDecimal.valueOf(100);
      var result = repo.findByPriceOfferLTOrderByPriceOfferDesc(priceFilter, 20);
      assertThat(result).allMatch(c -> c.priceOffer().compareTo(priceFilter) < 0);
    }

    @Test
    @DisplayName("list of customers with price less than provided filter returned, if there is no customer with filter price")
    void returnCustomersListWithPricesLessThanGivenFilterForPriceNotPresentInOffers() {
      repo.postConstruct();
      BigDecimal priceFilter = BigDecimal.valueOf(99.999);
      var result = repo.findByPriceOfferLTOrderByPriceOfferDesc(priceFilter, 20);
      assertThat(result).allMatch(c -> c.priceOffer().compareTo(priceFilter) < 0);
    }
  }
}