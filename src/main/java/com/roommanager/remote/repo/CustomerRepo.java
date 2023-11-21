package com.roommanager.remote.repo;

import com.roommanager.domain.Customer;
import java.math.BigDecimal;
import java.util.List;

public interface CustomerRepo {

  List<Customer> findAll();

  List<Customer> findByPriceOfferGTEOrderByPriceOfferDesc(BigDecimal price, int limit);

  List<Customer> findByPriceOfferLTOrderByPriceOfferDesc(BigDecimal price, int limit);
}
