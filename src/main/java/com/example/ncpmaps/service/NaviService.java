package com.example.ncpmaps.service;

import com.example.ncpmaps.dto.*;
import com.example.ncpmaps.dto.direction.DirectionNcpResponse;
import com.example.ncpmaps.dto.geocoding.GeoNcpResponse;
import com.example.ncpmaps.dto.geolocation.GeoLocationNcpResponse;
import com.example.ncpmaps.dto.rgeocoding.RGeoNcpResponse;
import com.example.ncpmaps.dto.rgeocoding.RGeoRegion;
import com.example.ncpmaps.dto.rgeocoding.RGeoResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public RGeoResponseDto getAddress(PointDto pointDto) {
        Map<String, Object> params = new HashMap<>();
        params.put("coords", pointDto.toQueryValue());
        params.put("output", "json");
        RGeoNcpResponse response = mapApiService.reverseGeocode(params);
        RGeoRegion region = response.getResults()
                .get(0)
                .getRegion();

        String address = region.getArea0().getName() + " " +
                region.getArea1().getName() + " " +
                region.getArea2().getName() + " " +
                region.getArea3().getName() + " " +
                region.getArea4().getName();
        return new RGeoResponseDto(address.trim());
    }

    // geocode 사용
    public NaviRouteDto startQuery(NaviWithQueryDto dto) {
        // 주소의 좌표부터 찾기
        Map<String, Object> params = new HashMap<>();
        params.put("query", dto.getQuery());
        params.put("coordinate", dto.getStart().toQueryValue());
        params.put("page", 1);
        params.put("count", 1);
        GeoNcpResponse response = mapApiService.geocode(params);
        log.info(response.toString());
        Double lat = Double.valueOf(response.getAddresses().get(0).getY());
        Double lng = Double.valueOf(response.getAddresses().get(0).getX());
        PointDto goal = new PointDto(lat, lng);
        // 경로를 찾아 반환하기
        return this.twoPointRoute(new NaviWithPointsDto(
                dto.getStart(),
                goal
        ));
    }

    public NaviRouteDto withIpAddress(NaviWithIpsDto dto) {
        Map<String , Object> params = new HashMap<>();
        params.put("ip", dto.getStartIp());
        params.put("responseFormatType", "json");
        params.put("ext", "t");

        GeoLocationNcpResponse startInfo = geolocationService.geoLocation(params);
        log.info(startInfo.toString());

        params.put("ip", dto.getGoalIp());
        GeoLocationNcpResponse goalInfo = geolocationService.geoLocation(params);
        log.info(goalInfo.toString());

        PointDto start = new PointDto(
                startInfo.getGeoLocation().getLat(),
                startInfo.getGeoLocation().getLng()
        );

        PointDto goal = new PointDto(
                goalInfo.getGeoLocation().getLat(),
                goalInfo.getGeoLocation().getLng()
        );

        return twoPointRoute(new NaviWithPointsDto(start, goal));
    }

}
