package web.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public ResponseEntity<?> searchByPosition(@RequestBody ReqPositionDTO reqPositionDTO) {
        

        return ResponseEntity.ok().body(mapSearchService.searchByPosition(reqPositionDTO));
    }
    /**
     * 행정동 기반 식당검색
     */
    @GetMapping("/region")
    public ResponseEntity<?> searchByRegionName(@RequestBody ReqRegionDTO reqRegionDTO) {


        return ResponseEntity.ok().body(mapSearchService.searchByRegionName(reqRegionDTO));
    }
}
