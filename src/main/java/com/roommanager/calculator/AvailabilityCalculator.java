package com.roommanager.calculator;

import com.roommanager.model.RoomsAvailabilityDto;
import com.roommanager.remote.api.RoomsAvailabilityRequest;
import java.util.List;

public interface AvailabilityCalculator {
 List<RoomsAvailabilityDto> execute(RoomsAvailabilityRequest request);
}
