package com.roommanager.remote.repository;

import com.roommanager.model.Customer;
import java.math.BigDecimal;
import java.util.List;

public interface CustomerRepo {

  List<Customer> findAll();

  List<Customer> findByPriceOfferGTEOrderByPriceOfferDesc(BigDecimal price, int limit);

  List<Customer> findByPriceOfferLTOrderByPriceOfferDesc(BigDecimal price, int limit);
}
