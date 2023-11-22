package com.roommanager.domain.calculator;

import com.roommanager.domain.model.RoomsAvailabilityDto;
import com.roommanager.remote.api.RoomsAvailabilityRequest;
import java.util.List;

public interface AvailabilityCalculator {
 List<RoomsAvailabilityDto> execute(RoomsAvailabilityRequest request);
}
