package web.mvc.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface S3Service {

    /** S3에 단일 이미지 업로드  (공개 URL 반환) */
    String uploadImage(String userId, MultipartFile image, String folderName);

    /** S3 다중 이미지 업로드 */
    List<String> uploadMultipleImages(String userId, List<MultipartFile> images, String folderName);

    /** S3 단일 이미지 삭제 */
    void deleteImage(String imageUrl);

    /** S3 다수 이미지 삭제 */
    //void deleteImages(List<String> urls);

    /** S3에 단일 리뷰 이미지 업로드  (공개 URL 반환) */
    public String uploadReviewImage(Long reviewId, MultipartFile image, String folderName);
    /** S3 다중 리뷰 이미지 업로드 */
    public List<String> uploadMultipleReviewImages(Long reviewId, List<MultipartFile> images, String folderName);

}
