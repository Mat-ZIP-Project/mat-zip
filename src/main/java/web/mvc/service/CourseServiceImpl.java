package web.mvc.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import web.mvc.domain.QTempCourseItem;
import web.mvc.domain.TempCourseItem;
import web.mvc.dto.ReqCustomDTO;
import web.mvc.dto.ReqTempDTO;
import web.mvc.dto.ResCustomDTO;
import web.mvc.dto.ResTempDTO;
import web.mvc.repository.CourseSpotRepository;
import web.mvc.repository.CustomCourseRepository;
import web.mvc.repository.TempCourseRepository;

import java.util.List;
@RequiredArgsConstructor
@Service
public class CourseServiceImpl implements CourseService {
    private final JPAQueryFactory jpaQueryFactory;
    private final CourseSpotRepository courseSpotRepository;
    private final CustomCourseRepository customCourseRepository;
    private final TempCourseRepository tempCourseRepository;
    private final ModelMapper modelMapper;
    @Override
    public List<ResTempDTO> searchTempCourses(Long id) {
        QTempCourseItem qTempCourseItem = QTempCourseItem.tempCourseItem;
        List<TempCourseItem> list = jpaQueryFactory.selectFrom(qTempCourseItem)
                                                    .where(qTempCourseItem.user.id.eq(id)).fetch();
        return list.stream().map(TempCourseItem->modelMapper.map(TempCourseItem,ResTempDTO.class)).toList();
    }

    @Override
    public String insertTempCourse(ReqTempDTO reqTempDTO) {
        TempCourseItem addItem= modelMapper.map(reqTempDTO,TempCourseItem.class);
        tempCourseRepository.save(addItem);
        return "코스에 추가되었습니다.";
    }

    @Override
    public List<ResCustomDTO> searchCustomCourseList(ReqTempDTO reqTempDTO) {
        return List.of();
    }

    @Override
    public String insertCustomCourse(List<ReqCustomDTO> list) {
        return "";
    }

    @Override
    public ResCustomDTO searchCustomCourse(Long courseId) {
        return null;
    }

    @Override
    public void updateCustomCourse(Long courseId, List<ReqCustomDTO> list) {

    }

    @Override
    public void deleteCustomCourse(Long courseId) {

    }
}
