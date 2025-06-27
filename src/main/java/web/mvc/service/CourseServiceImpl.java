package web.mvc.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.*;
import web.mvc.dto.ReqCustomDTO;
import web.mvc.dto.ReqTempDTO;
import web.mvc.dto.ResCustomDTO;
import web.mvc.dto.ResTempDTO;
import web.mvc.repository.CourseSpotRepository;
import web.mvc.repository.CustomCourseRepository;
import web.mvc.repository.RestaurantRepository;
import web.mvc.repository.TempCourseRepository;

import java.util.List;
@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class CourseServiceImpl implements CourseService {
    private final JPAQueryFactory jpaQueryFactory;
    private final CourseSpotRepository courseSpotRepository;
    private final CustomCourseRepository customCourseRepository;
    private final TempCourseRepository tempCourseRepository;
    private final RestaurantRepository restaurantRepository;
    private final ModelMapper modelMapper;
    @Override
    public List<ResTempDTO> searchTempCourses(Long id) {
        QTempCourseItem qTempCourseItem = QTempCourseItem.tempCourseItem;
        QRestaurant qRestaurant = QRestaurant.restaurant;
        List<TempCourseItem> list = jpaQueryFactory.selectFrom(qTempCourseItem).join(qRestaurant)
                .on(qTempCourseItem.restaurant.eq(qRestaurant))
                                                    .where(qTempCourseItem.user.id.eq(id)).fetch();
        return list.stream().map(TempCourseItem->modelMapper.map(TempCourseItem,ResTempDTO.class)).toList();
    }

    @Override
    public String insertTempCourse(ReqTempDTO reqTempDTO) {
        TempCourseItem addItem= modelMapper.map(reqTempDTO,TempCourseItem.class);
        addItem.setUser(User.builder().id(reqTempDTO.getId()).build());
        log.info("{}",restaurantRepository.findById(reqTempDTO.getRestaurantId()).get().getRestaurantName());
        addItem.setRestaurant(restaurantRepository.findById(reqTempDTO.getRestaurantId()).get());
        tempCourseRepository.save(addItem);
        return "코스에 추가되었습니다.";
    }

    @Override
    public List<ResCustomDTO> searchCustomCourseList(ReqTempDTO reqTempDTO) {
        return List.of();
    }

    @Override
    public String insertCustomCourse(List<ReqCustomDTO> list) {
        Long id = list.get(0).getId();
        //custom_courses , course_spots table 에 추가
        CustomCourse customCourse=CustomCourse.builder().user(User.builder().id(id).build()).title(list.get(0).getTitle()).build();
        customCourseRepository.save(customCourse);
        CourseSpots courseSpots = null;
        for (ReqCustomDTO dto : list) {
            courseSpots = CourseSpots.builder().customCourse(customCourse).visitOrder(dto.getVisitOrder()).restaurant(restaurantRepository.findById(dto.getRestaurantId()).get()).restaurantName(dto.getRestaurantName()).build();
            courseSpotRepository.save(courseSpots);
        }
        // temp_course_item 레코드 삭제
        tempCourseRepository.deleteAll();

        return "코스에 저장되었습니다.";
    }

    @Override
    public ResCustomDTO searchCustomCourse(Long id,Long courseId) {
        CustomCourse customCourse=customCourseRepository.searchCustomCourse(id,courseId);
        List<ResTempDTO> spots=customCourse.getCourseSpotsList().stream()
                .map(spot -> ResTempDTO.builder()
                        .restaurantId(spot.getRestaurant().getRestaurantId())
                        .restaurantName(spot.getRestaurant().getRestaurantName())
                        .latitude(0.0)
                        .longitude(0.0)
                        .visitOrder(spot.getVisitOrder())
                        .build()).toList();

        return  ResCustomDTO.builder().courseId(customCourse.getCourseId()).title(customCourse.getTitle())
                .resTempDTOList(spots)
                .build();
    }

    @Override
    public void updateCustomCourse(Long courseId, List<ReqCustomDTO> list) {

    }

    @Override
    public void deleteCustomCourse(Long courseId) {

    }
}
