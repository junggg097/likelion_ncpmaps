package com.example.ncpmaps.service;

import com.example.ncpmaps.dto.NaviRouteDto;
import com.example.ncpmaps.dto.NaviWithPointsDto;
import com.example.ncpmaps.dto.PointDto;
import com.example.ncpmaps.dto.direction.DirectionNcpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaviService {
    private final NcpMapApiService mapApiService;
    private final NcpGeolocationService geolocationService;

    public NaviRouteDto twoPointRoute(NaviWithPointsDto dto) {
        Map<String, Object> params = new HashMap<>();
        params.put("start", dto.getStart().toQueryValue());
        params.put("goal", dto.getGoal().toQueryValue());
        DirectionNcpResponse response = mapApiService.direction5(params);
        List<PointDto> path = new ArrayList<>();
        response.getRoute()
                // 실시간 추적 (api 사용가이드에 있음)
                .get("traoptimal")
                .get(0)
                .getPath()
                .forEach(point
                        -> path.add(new PointDto(point.get(1), point.get(0))));
        return new NaviRouteDto(path);
    }
}
