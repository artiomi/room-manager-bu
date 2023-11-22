package com.roommanager.domain.calculator;

import com.roommanager.domain.model.RoomsAvailabilityQuery;
import com.roommanager.domain.model.RoomsAvailabilityResult;
import java.util.List;

public interface AvailabilityCalculator {
 List<RoomsAvailabilityResult> execute(RoomsAvailabilityQuery request);
}
