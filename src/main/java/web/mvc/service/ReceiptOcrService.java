package web.mvc.service;

import org.springframework.web.multipart.MultipartFile;
import web.mvc.dto.ResOcrDTO;

public interface ReceiptOcrService {
    /**
     * 영수증 인증
     * @param image
     * @param restaurantId
     * @param id
     * @return
     */
    ResOcrDTO parseReceipt(MultipartFile image,Long restaurantId,Long id);


}
