package web.mvc.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import web.mvc.domain.*;
import web.mvc.dto.ReqCustomDTO;
import web.mvc.dto.ReqTempDTO;
import web.mvc.dto.ResCustomDTO;
import web.mvc.dto.ResTempDTO;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j

public class CourseServiceImpl implements CourseService {
    private final JPAQueryFactory jpaQueryFactory;
    private final CourseSpotRepository courseSpotRepository;
    private final CustomCourseRepository customCourseRepository;
    private final TempCourseRepository tempCourseRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    //QueryDSL
    @Override
    @Transactional(readOnly = true)
    public List<ResTempDTO> searchTempCourses(Long id) {
        QTempCourseItem qTempCourseItem = QTempCourseItem.tempCourseItem;
        QRestaurant qRestaurant = QRestaurant.restaurant;
        List<TempCourseItem> list = jpaQueryFactory.selectFrom(qTempCourseItem).join(qRestaurant)
                .on(qTempCourseItem.restaurant.eq(qRestaurant))
                                                    .where(qTempCourseItem.user.id.eq(id)).fetch();
        return list.stream().map(dto ->ResTempDTO.builder()
                .visitOrder(dto.getVisitOrder())
                .restaurantId(dto.getRestaurant().getRestaurantId())
                .latitude(dto.getRestaurant().getLatitude())
                .longitude(dto.getRestaurant().getLongitude())
                .restaurantName(dto.getRestaurant().getRestaurantName())
                .build()).toList();
    }
    //JPA 기본
    @Override
    @Transactional
    public String insertTempCourse(ReqTempDTO reqTempDTO) {
        TempCourseItem addItem = TempCourseItem.builder()
                .user(userRepository.findById(reqTempDTO.getUserId()).orElseThrow(()->new BasicException(ErrorCode.USER_NOT_FOUND)))
                .restaurant(restaurantRepository.findById(reqTempDTO.getRestaurantId()).orElseThrow(()->new BasicException(ErrorCode.RESTAURANT_NOT_FOUND)))
                .restaurantName(reqTempDTO.getRestaurantName())
                .visitOrder(reqTempDTO.getVisitOrder())
                .build();

        tempCourseRepository.save(addItem);
        return "코스에 추가되었습니다.";
    }

    @Override
    @Transactional
    public void updateTempCorse(List<ReqTempDTO> reqTempDTOList) {
        long id = reqTempDTOList.get(0).getUserId();
        QTempCourseItem qTempCourseItem = QTempCourseItem.tempCourseItem;
        jpaQueryFactory.delete(qTempCourseItem).where(qTempCourseItem.user.id.eq(id)).execute();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BasicException(ErrorCode.USER_NOT_FOUND));

        List<TempCourseItem> toSave=reqTempDTOList.stream().map(dto->
            TempCourseItem.builder()
                    .restaurant(restaurantRepository.findById(dto.getRestaurantId()).get())
                    .restaurantName(dto.getRestaurantName())
                    .visitOrder(dto.getVisitOrder())
                    .user(user)
                    .build()
        ).toList();

        tempCourseRepository.saveAll(toSave);

    }

    @Transactional
    @Override
    public void deleteTempCorse(Long id) {
        QTempCourseItem qTempCourseItem = QTempCourseItem.tempCourseItem;
        jpaQueryFactory.delete(qTempCourseItem).where(qTempCourseItem.user.id.eq(id)).execute();

    }

    //Query DSL
    @Transactional(readOnly = true)
    @Override
    public List<ResCustomDTO> searchCustomCourseList(Long id) {
        QCustomCourse customCourse = QCustomCourse.customCourse;
        QCourseSpots courseSpots = QCourseSpots.courseSpots;
        List<CustomCourse> list =jpaQueryFactory.selectFrom(customCourse)
                .leftJoin(customCourse.courseSpotsList, courseSpots).fetchJoin()
                .where(customCourse.user.id.eq(id))
                .distinct() // 중복 방지 (중요)
                .fetch();


        return list.stream().map(course -> ResCustomDTO.builder()
                .title(course.getTitle())
                .courseId(course.getCourseId())
                .resTempDTOList(
                        course.getCourseSpotsList().stream().map(spot -> ResTempDTO.builder()
                                .restaurantId(spot.getRestaurant().getRestaurantId())
                                .restaurantName(spot.getRestaurant().getRestaurantName())
                                .visitOrder(spot.getVisitOrder())
                                .latitude(spot.getRestaurant().getLatitude())
                                .longitude(spot.getRestaurant().getLongitude())
                                .build()
                        ).toList()
                )
                .build()).toList();
    }

    //기본 JPA
    @Override
    @Transactional
    public String insertCustomCourse(List<ReqCustomDTO> list) {
        QTempCourseItem  qTempCourseItem = QTempCourseItem.tempCourseItem;
        Long id = list.get(0).getId();
        //custom_courses , course_spots table 에 추가
        CustomCourse customCourse=CustomCourse.builder().user(userRepository.findById(id).orElseThrow(()->new BasicException(ErrorCode.USER_NOT_FOUND))).title(list.get(0).getTitle()).build();
        customCourseRepository.save(customCourse);
        CourseSpots courseSpots = null;
        for (ReqCustomDTO dto : list) {
            courseSpots = CourseSpots.builder().customCourse(customCourse).visitOrder(dto.getVisitOrder()).restaurant(restaurantRepository.findById(dto.getRestaurantId()).orElseThrow(()->new BasicException(ErrorCode.RESTAURANT_NOT_FOUND))).restaurantName(dto.getRestaurantName()).build();
            courseSpotRepository.save(courseSpots);
        }
        // temp_course_item 레코드 삭제
        jpaQueryFactory.delete(qTempCourseItem).where(qTempCourseItem.user.id.eq(id)).execute();

        return "코스에 저장되었습니다.";
    }
    //JPQL Query작성
    @Override
    @Transactional(readOnly = true)
    public ResCustomDTO searchCustomCourse(Long id,Long courseId) {
        CustomCourse customCourse=customCourseRepository.searchCustomCourse(id,courseId);
        List<ResTempDTO> spots=customCourse.getCourseSpotsList().stream()
                .map(spot -> ResTempDTO.builder()
                        .restaurantId(spot.getRestaurant().getRestaurantId())
                        .restaurantName(spot.getRestaurant().getRestaurantName())
                        .latitude(spot.getRestaurant().getLatitude())
                        .longitude(spot.getRestaurant().getLongitude())
                        .visitOrder(spot.getVisitOrder())
                        .build()).toList();

        return  ResCustomDTO.builder().courseId(customCourse.getCourseId()).title(customCourse.getTitle())
                .resTempDTOList(spots)
                .build();
    }
    //QueryDSL + JPA기본
    @Override
    @Transactional
    public void updateCustomCourse(Long courseId, List<ReqCustomDTO> list) {
        //custom_courses update
        CustomCourse customCourse=customCourseRepository.findById(courseId).orElseThrow(()->new BasicException(ErrorCode.COURSE_NOT_FOUND));
        customCourse.setTitle(list.get(0).getTitle());

        //list를 map 으로 변환 (이중for문 없애기!)
        Map<String,Integer> map=list.stream().collect(Collectors.toMap(ReqCustomDTO::getRestaurantName,ReqCustomDTO::getVisitOrder));

        //course_spots update
        QCourseSpots courseSpots = QCourseSpots.courseSpots;
        List<CourseSpots> spotList =jpaQueryFactory.selectFrom(courseSpots)
                .where(courseSpots.customCourse.courseId.eq(courseId)).fetch();

        // 삭제 대상과 업데이트 대상 분리
        List<CourseSpots> toRemove = new ArrayList<>();

        spotList.forEach(spot->{
                Integer newVisitOrder = map.get(spot.getRestaurantName());
                if(newVisitOrder == null ||newVisitOrder==0) toRemove.add(spot);
                else spot.setVisitOrder(newVisitOrder);
        });
        // 삭제 처리
        toRemove.forEach(courseSpotRepository::delete);
    }
    //JPA 기본
    @Override
    @Transactional
    public void deleteCustomCourse(Long courseId) {
        customCourseRepository.deleteById(courseId);

    }
}
