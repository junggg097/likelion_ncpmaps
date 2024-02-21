package com.example.ncpmaps.service;

import com.example.ncpmaps.dto.direction.DirectionNcpResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import java.util.Map;

public interface NcpMapApiService {
    // Directions5 서비스
    @GetExchange("/map-direction/v1/driving")
    DirectionNcpResponse direction5(
            @RequestParam
            Map<String, Object> params
    );


}
