package com.roommanager.remote.repositories;

import com.roommanager.domain.model.Customer;
import com.roommanager.remote.ClientsResourceParser;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryCustomerRepo implements CustomerRepo {

  private final Comparator<Customer> customerComparator = Comparator.comparing(Customer::priceOffer).reversed();
  private final ClientsResourceParser clientsResourceParser;
  private List<Customer> customers = null;

  public InMemoryCustomerRepo(ClientsResourceParser clientsResourceParser) {
    this.clientsResourceParser = clientsResourceParser;
  }

  @PostConstruct
  void postConstruct() {
    customers = this.clientsResourceParser.getRecords().stream()
        .map(p -> new Customer(BigDecimal.valueOf(p)))
        .sorted(customerComparator)
        .toList();
  }

  @Override
  public List<Customer> findAll() {
    return customers;
  }

  @Override
  public List<Customer> findByPriceOfferGTEOrderByPriceOfferDesc(BigDecimal price, int limit) {
    return customers.stream()
        .limit(limit)
        .takeWhile(c -> c.priceOffer().compareTo(price) >= 0)
        .toList();
  }

  @Override
  public List<Customer> findByPriceOfferLTOrderByPriceOfferDesc(BigDecimal price, int limit) {
    int position = findCustomerPosition(new Customer(price));
    int index = getStartIndex(position);
    if (index == customers.size()) {
      return List.of();
    }
    return customers.subList(index, customers.size()).stream()
        .filter(c -> c.priceOffer().compareTo(price) < 0)
        .limit(limit)
        .toList();
  }

  private int getStartIndex(int position) {
    return position < 0 ? Math.abs(position) - 1 : position + 1;
  }

  private int findCustomerPosition(Customer customer) {
    return Collections.binarySearch(customers, customer, customerComparator);
  }
}
