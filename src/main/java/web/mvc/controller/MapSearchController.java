package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.mvc.dto.ReqPositionDTO;
import web.mvc.dto.ReqRegionDTO;
import web.mvc.service.MapSearchService;

// 비회원도 가능
@RestController
@RequiredArgsConstructor
@RequestMapping("/map")
public class MapSearchController {
    private final MapSearchService mapSearchService;
    /**
     * 좌표기반 반경 내 식당 검색
     */
    @GetMapping("/position")
    public ResponseEntity<?> searchByPosition(@RequestParam double latitude, @RequestParam double longitude, @RequestParam double radius) {
        System.out.println(radius);
        System.out.println((long)radius);

        return ResponseEntity.ok().body(mapSearchService.searchByPosition(ReqPositionDTO.builder()
                .latitude(latitude)
                .longitude(longitude)
                .radius((long) radius)
                .build()));
    }
    /**
     * 행정동 기반 식당검색
     */
    @GetMapping("/region")
    public ResponseEntity<?> searchByRegionName(@RequestParam String regionSido,
                                                @RequestParam String regionSigungu) {


        return ResponseEntity.ok().body(mapSearchService.searchByRegionName(ReqRegionDTO.builder().regionSido(regionSido).regionSigungu(regionSigungu).build()));
    }
}
