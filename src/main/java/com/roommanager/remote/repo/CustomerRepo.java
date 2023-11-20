package com.roommanager.remote.repo;

import com.roommanager.domain.Customer;
import java.util.List;

public interface CustomerRepo {
List<Customer> findAll();
}
