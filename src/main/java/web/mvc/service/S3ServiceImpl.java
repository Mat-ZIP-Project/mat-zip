package web.mvc.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import web.mvc.domain.OwnerInfo;
import web.mvc.domain.Restaurant;
import web.mvc.exception.BasicException;
import web.mvc.exception.ErrorCode;
import web.mvc.repository.OwnerInfoRepository;
import web.mvc.repository.RestaurantRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3ServiceImpl implements S3Service {

    private final AmazonS3 amazonS3;
    private final RestaurantRepository restaurantRepository;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");

    /**
     * 단일 이미지 업로드
     */
    public String uploadImage(String userId, MultipartFile image, String folderName) {
        if (image.isEmpty() || Objects.isNull(image.getOriginalFilename())) {
            throw new BasicException(ErrorCode.INVALID_INPUT);
        }
        validateImageExtension(image.getOriginalFilename());

        try {
            Restaurant restaurant = restaurantRepository.findByOwnerUserId(userId)
                    .orElseThrow(() -> new BasicException(ErrorCode.RESTAURANT_NOT_FOUND));

            String restaurantId = restaurant.getRestaurantId().toString();
            return uploadToS3(image, String.format("restaurant/%s/%s", restaurantId, folderName));
        } catch (IOException e) {
            throw new BasicException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 다중 이미지 업로드
     */
    public List<String> uploadMultipleImages(String userId, List<MultipartFile> images, String folderName) {
        if (images == null || images.isEmpty()) {
            throw new BasicException(ErrorCode.INVALID_INPUT);
        }
        return images.stream()
                .filter(image -> !image.isEmpty())
                .map(image -> uploadImage(userId, image, folderName))
                .collect(Collectors.toList());
    }

    /**
     * 단일이미지 업로드 - 리뷰
     */
    public String uploadReviewImage(Long reviewId, MultipartFile image, String folderName) {
        if (image.isEmpty() || Objects.isNull(image.getOriginalFilename())) {
            throw new BasicException(ErrorCode.INVALID_INPUT);
        }
        validateImageExtension(image.getOriginalFilename());
        try {
            return uploadToS3(image, String.format("review/%s/%s", reviewId, folderName));
        }catch (IOException e) {
            throw new BasicException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 다중이미지 업로드 - 리뷰
     */
    public List<String> uploadMultipleReviewImages(Long reviewId, List<MultipartFile> images, String folderName){
        if (images == null || images.isEmpty()) {
            throw new BasicException(ErrorCode.INVALID_INPUT);
        }
        return images.stream()
                .filter(image -> !image.isEmpty())
                .map(image -> uploadReviewImage(reviewId, image, folderName))
                .collect(Collectors.toList());
    }


    /**
     * 이미지 삭제
     */
    public void deleteImage(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            String key = URLDecoder.decode(url.getPath().substring(1), "UTF-8");
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
        } catch (IOException e) {
            throw new BasicException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * S3 업로드 공통 로직
     */
    private String uploadToS3(MultipartFile image, String folderPath) throws IOException {
        String extension = getExtension(image.getOriginalFilename());
        String s3FileName = String.format("%s/%s.%s", folderPath, UUID.randomUUID(), extension);

        InputStream is = image.getInputStream();
        byte[] bytes = IOUtils.toByteArray(is);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/" + extension);
        metadata.setContentLength(bytes.length);

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            amazonS3.putObject(new PutObjectRequest(bucketName, s3FileName, byteArrayInputStream, metadata));
        } catch (Exception e) {
            throw new BasicException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            is.close();
        }

        return amazonS3.getUrl(bucketName, s3FileName).toString();
    }

    /** 이미지 유효성 검사 */
    private void validateImageExtension(String filename) {
        String ext = getExtension(filename);
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new BasicException(ErrorCode.INVALID_INPUT);
        }
    }

    private String getExtension(String filename) {
        int dotIdx = filename.lastIndexOf('.');
        if (dotIdx == -1) {
            throw new BasicException(ErrorCode.INVALID_INPUT);
        }
        return filename.substring(dotIdx + 1);
    }
}
