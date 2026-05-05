package com.harsh.rentify.service;

import com.harsh.rentify.dto.response.LookupOptionResponse;
import com.harsh.rentify.repository.AmenityRepository;
import com.harsh.rentify.repository.RoomTypeRepository;
import com.harsh.rentify.repository.RuleRepository;
import com.harsh.rentify.repository.TransportRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReferenceDataService {

    private final RoomTypeRepository roomTypeRepository;
    private final AmenityRepository amenityRepository;
    private final RuleRepository ruleRepository;
    private final TransportRepository transportRepository;

    public ReferenceDataService(
            RoomTypeRepository roomTypeRepository,
            AmenityRepository amenityRepository,
            RuleRepository ruleRepository,
            TransportRepository transportRepository
    ) {
        this.roomTypeRepository = roomTypeRepository;
        this.amenityRepository = amenityRepository;
        this.ruleRepository = ruleRepository;
        this.transportRepository = transportRepository;
    }

    @Transactional(readOnly = true)
    public List<LookupOptionResponse> getRoomTypes() {
        return roomTypeRepository.findAll().stream()
                .map(type -> new LookupOptionResponse(type.getId(), type.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LookupOptionResponse> getAmenities() {
        return amenityRepository.findAll().stream()
                .map(amenity -> new LookupOptionResponse(amenity.getId(), amenity.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LookupOptionResponse> getRules() {
        return ruleRepository.findAll().stream()
                .map(rule -> new LookupOptionResponse(rule.getId(), rule.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LookupOptionResponse> getTransports() {
        return transportRepository.findAll().stream()
                .map(transport -> new LookupOptionResponse(transport.getId(), transport.getName()))
                .toList();
    }
}
