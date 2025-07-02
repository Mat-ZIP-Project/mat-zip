package web.mvc.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.QRestaurant;
import web.mvc.domain.Restaurant;
import web.mvc.domain.RestaurantImage;
import web.mvc.dto.ReqPositionDTO;
import web.mvc.dto.ReqRegionDTO;
import web.mvc.dto.ResRestaurantDTO;
import web.mvc.dto.RestaurantListResponseDTO;
import web.mvc.repository.RestaurantImageRepository;
import web.mvc.repository.RestaurantRepository;
import web.mvc.repository.ReviewRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MapSearchServiceImpl implements MapSearchService {
    private final ModelMapper modelMapper;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantImageRepository  restaurantImageRepository;
    private final ReviewRepository reviewRepository;
    private final JPAQueryFactory jpaQueryFactory;

    //메소드 이름 기반 JPA
    @Transactional(readOnly = true)
    @Override
    public List<ResRestaurantDTO> searchByPosition(ReqPositionDTO reqPositionDTO) {
        List<Restaurant> list=restaurantRepository.searchByPosition(reqPositionDTO.getLongitude(), reqPositionDTO.getLatitude(), reqPositionDTO.getRadius());

        return list.stream()
                .map(restaurant -> {
                    ResRestaurantDTO dto = modelMapper.map(restaurant, ResRestaurantDTO.class);
                    dto.setThumbnailImageUrl(Optional.ofNullable(getImgUrl(restaurant)).orElse(""));//사진이미지 세팅
                    dto.setReviewCount((int)getReviewCnt(restaurant));//리뷰수 세팅
                    return dto;
                })
                .toList();
    }
    //QueryDSL + BooleanBuilder
    @Transactional(readOnly = true)
    @Override
    public List<ResRestaurantDTO> searchByRegionName(ReqRegionDTO reqRegionDTO) {
        QRestaurant qRestaurant = QRestaurant.restaurant;
        BooleanBuilder  booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(qRestaurant.regionSido.eq(reqRegionDTO.getRegionSido()));
        String sigunGu = reqRegionDTO.getRegionSigungu();
        if (sigunGu!=null && !sigunGu.equals("")) {
            booleanBuilder.and(qRestaurant.regionSigungu.eq(sigunGu));
        }

        List<Restaurant> list = jpaQueryFactory.selectFrom(qRestaurant)
                .where(booleanBuilder).fetch();

        return list.stream()
                .map(restaurant -> {
                    ResRestaurantDTO dto = modelMapper.map(restaurant, ResRestaurantDTO.class);
                    dto.setThumbnailImageUrl(Optional.ofNullable(getImgUrl(restaurant)).orElse(""));//사진이미지 세팅
                    dto.setReviewCount((int)getReviewCnt(restaurant));//리뷰수 세팅
                    return dto;
                })
                .toList();
    }
    
    public String getImgUrl(Restaurant restaurant) {

        return restaurantImageRepository.findFirstByRestaurant(restaurant)
                .map(RestaurantImage::getImageUrl)
                .orElse("");
    }
    public long getReviewCnt(Restaurant restaurant) {
        return reviewRepository.countReviewByRestaurant(restaurant);
    }
    
    
}
