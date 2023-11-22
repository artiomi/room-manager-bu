package com.roommanager.remote.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.roommanager.domain.model.Customer;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InMemoryCustomerRepoTest {

  private final InMemoryCustomerRepo repo = new InMemoryCustomerRepo();

  @Nested
  class PostConstructTest {

    @Test
    void customersListIsNullWhenPostConstructNotCalled() {
      List<Customer> customers = repo.findAll();
      assertThat(customers).isNull();
    }

    @Test
    void customersListNotNullAfterPostConstructCall() {
      repo.postConstruct();
      List<Customer> customers = repo.findAll();
      assertThat(customers).isNotNull();
      assertThat(customers).isNotEmpty();
    }

    @Test
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
    void throwsNPEForNullCustomersList() {
      assertThatThrownBy(() -> repo.findByPriceOfferGTEOrderByPriceOfferDesc(BigDecimal.ONE, 2))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    void returnEmptyListOnMissingGreaterPrice() {
      repo.postConstruct();
      assertThat(repo.findByPriceOfferGTEOrderByPriceOfferDesc(BigDecimal.valueOf(1000), 2))
          .isEmpty();
    }

    @Test
    void returnCustomersListOfRequestedSize() {
      repo.postConstruct();
      int limit = 2;
      var result = repo.findByPriceOfferGTEOrderByPriceOfferDesc(BigDecimal.valueOf(100), limit);
      assertThat(result).size().isEqualTo(limit);
    }

    @Test
    void returnCustomersListOfSizeLessThanGivenLimit() {
      repo.postConstruct();
      int limit = 20;
      var result = repo.findByPriceOfferGTEOrderByPriceOfferDesc(BigDecimal.valueOf(100), limit);
      assertThat(result).size().isLessThan(limit);
      assertThat(result).size().isEqualTo(6);
    }

    @Test
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
    void throwsNPEForNullCustomersList() {
      assertThatThrownBy(() -> repo.findByPriceOfferLTOrderByPriceOfferDesc(BigDecimal.ONE, 2))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    void returnEmptyListOnMissingSmallerPrice() {
      repo.postConstruct();
      assertThat(repo.findByPriceOfferLTOrderByPriceOfferDesc(BigDecimal.valueOf(10), 2))
          .isEmpty();
    }

    @Test
    void returnCustomersListOfRequestedSize() {
      repo.postConstruct();
      int limit = 2;
      var result = repo.findByPriceOfferLTOrderByPriceOfferDesc(BigDecimal.valueOf(100), limit);
      assertThat(result).size().isEqualTo(limit);
    }

    @Test
    void returnCustomersListOfSizeLessThanGivenLimit() {
      repo.postConstruct();
      int limit = 20;
      var result = repo.findByPriceOfferLTOrderByPriceOfferDesc(BigDecimal.valueOf(100), limit);
      assertThat(result).size().isLessThan(limit);
      assertThat(result).size().isEqualTo(4);
    }

    @Test
    void returnCustomersListOfSizeLessThanGivenLimitForPriceNotPresentInOffers() {
      repo.postConstruct();
      int limit = 20;
      var result = repo.findByPriceOfferLTOrderByPriceOfferDesc(BigDecimal.valueOf(99.999), limit);
      assertThat(result).size().isLessThan(limit);
      assertThat(result).size().isEqualTo(4);
    }

    @Test
    void returnCustomersListWithPricesLessThanGivenFilter() {
      repo.postConstruct();
      BigDecimal priceFilter = BigDecimal.valueOf(100);
      var result = repo.findByPriceOfferLTOrderByPriceOfferDesc(priceFilter, 20);
      assertThat(result).allMatch(c -> c.priceOffer().compareTo(priceFilter) < 0);
    }

    @Test
    void returnCustomersListWithPricesLessThanGivenFilterForPriceNotPresentInOffers() {
      repo.postConstruct();
      BigDecimal priceFilter = BigDecimal.valueOf(99.999);
      var result = repo.findByPriceOfferLTOrderByPriceOfferDesc(priceFilter, 20);
      assertThat(result).allMatch(c -> c.priceOffer().compareTo(priceFilter) < 0);
    }
  }
}