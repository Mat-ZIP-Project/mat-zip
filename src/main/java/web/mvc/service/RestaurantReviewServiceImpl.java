package web.mvc.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import web.mvc.domain.*;
import web.mvc.dto.ReqReviewDTO;
import web.mvc.dto.ResLocalDTO;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantReviewServiceImpl implements RestaurantReviewService{
    private final JPAQueryFactory jpaQueryFactory;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final PointRepository pointRepository;
    private final ReviewImageRepository reviewImageRepository;

    private final S3Service s3Service;

    private static final String REVIEW_FOLDER = "review";


    @Transactional(readOnly = true)
    @Override
    public ResLocalDTO checkReview(Long id, Long restaurantId, String visitDate) {
        LocalDate parsedVisitDate = LocalDate.parse(visitDate);
        QReview review=QReview.review;
        List<Review> res = jpaQueryFactory.selectFrom(review)
                .where(review.user.id.eq(id).and(review.restaurant.restaurantId.eq(restaurantId)).and(review.visitDate.eq(parsedVisitDate))).fetch();
        if(res.size()!=0) throw new BasicException(ErrorCode.DUPLICATED_REVIEW); // 리뷰 중복!

        String userId =userRepository.findById(id).get().getUserId();

        //로컬리뷰 여부 확인
        //1. 식당주소 알아내기
        Restaurant restaurant=restaurantRepository.findById(restaurantId).get();
        String regionSido = restaurant.getRegionSido();
        String regionSigungu = restaurant.getRegionSigungu();
        String address = regionSido+" "+regionSigungu;
        //2. 사용자의 badge 지역 알아내기
        QUserLocalBadge badge = QUserLocalBadge.userLocalBadge;
        List<UserLocalBadge> userLocalBadges=jpaQueryFactory.selectFrom(badge)
                .where(badge.user.id.eq(id)).fetch();
        //뱃지가 없으니까 일반인 리뷰!
        ResLocalDTO normal= ResLocalDTO.builder().localReview(false).userId(userId).build();
        ResLocalDTO local=ResLocalDTO.builder().localReview(true).userId(userId).build();
        if(userLocalBadges.size()==0) return normal;


        //3. 1,2 비교하기
        boolean isLocal = userLocalBadges.stream()
                .anyMatch(b -> b.getRegionName().equals(address));

        return isLocal? local:normal;
    }

    @Transactional
    @Override
    public String createReview(ReqReviewDTO dto, List<MultipartFile> images, Long id) {
       try {
           // 1. 리뷰 저장
           Review review = Review.builder()
                   .content(dto.getContent())
                   .rating(dto.getRating())
                   .visitDate(LocalDate.parse(dto.getVisitDate()))
                   .restaurant(Restaurant.builder().restaurantId(dto.getRestaurantId()).build())
                   .user(User.builder().id(id).build())
                   .localReview(dto.isLocal())
                   .reviewedAt(LocalDateTime.now())
                   .build();

           reviewRepository.save(review);


           // 2. 이미지들 S3 업로드 및 리뷰 이미지 테이블에 저장
           List<String> imageUrls = s3Service.uploadMultipleReviewImages(review.getReviewId(), images, REVIEW_FOLDER);
           for (String url : imageUrls) {
               ReviewImage image = ReviewImage.builder().imageName(url).review(review).build();
               reviewImageRepository.save(image);
           }

           // 3. 포인트 업데이트
           User user = userRepository.findById(id).get();
           user.setPointBalance(user.getPointBalance() + 100);

           QPoint point = QPoint.point;
           Point searchPoint = jpaQueryFactory.selectFrom(point)
                   .where(point.user.id.eq(id)).orderBy(point.createdAt.desc()).limit(1).fetchOne();
           int inputPoint = searchPoint == null ? 100 : searchPoint.getPointLog() + 100;
           pointRepository.save(
                   Point.builder()
                           .user(user)
                           .isEarned("적립")
                           .pointAmount(100)
                           .pointLog(inputPoint)
                           .createdAt(LocalDateTime.now())
                           .build()
           );


           //4. 평점 업데이트
           Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId()).get();
           QReview reviews = QReview.review;
           long reviewCnt = 0;
           if (dto.isLocal()) {
               reviewCnt = jpaQueryFactory.select(reviews.count()).from(reviews).where(reviews.restaurant.eq(restaurant).and(reviews.localReview.eq(true))).fetchOne();
               restaurant.setAvgRatingLocal(averageRating(restaurant.getAvgRatingLocal(), reviewCnt-1, dto.getRating()));
           } else {
               reviewCnt = jpaQueryFactory.select(reviews.count()).from(reviews).where(reviews.restaurant.eq(restaurant).and(reviews.localReview.eq(false))).fetchOne();
               restaurant.setAvgRating(averageRating(restaurant.getAvgRating(), reviewCnt-1, dto.getRating()));

           }


           return "리뷰가 등록되어 100p 적립되었습니다.";
       }catch (DataIntegrityViolationException e){
           throw new  BasicException(ErrorCode.DUPLICATED_REVIEW);
       }
    }



    private double averageRating(double averageRating, long reviewCnt, int newRating){
        return (double)(averageRating*reviewCnt + newRating) / (reviewCnt+1);
    }
    @Transactional
    @Override
    public void deleteReview(Long reviewId) {


        QReviewImage reviewImage = QReviewImage.reviewImage;
        List<ReviewImage> list =  jpaQueryFactory.selectFrom(reviewImage).where(reviewImage.review.reviewId.eq(reviewId)).fetch();

        list.forEach(image->s3Service.deleteImage(image.getImageName()));
        reviewRepository.deleteById(reviewId);

    }


}
