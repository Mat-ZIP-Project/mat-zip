package web.mvc.service;

import web.mvc.dto.ReqPositionDTO;
import web.mvc.dto.ReqRegionDTO;
import web.mvc.dto.ResRestaurantDTO;
import web.mvc.dto.RestaurantListResponseDTO;

import java.util.List;

public interface MapSearchService {
    /**
     * 좌표기반 반경 내 식당 검색
     */
    List<ResRestaurantDTO> searchByPosition(ReqPositionDTO reqPositionDTO);
    /**
     * 행정동 기반 식당검색
     */
    List<ResRestaurantDTO> searchByRegionName(ReqRegionDTO reqRegionDTO);
}
