package web.mvc.service;

import org.springframework.stereotype.Service;
import web.mvc.domain.User;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void registerOrUpdateFcmToken(User user, String fcmToken) {

    }

    @Override
    public void sendNotification(User user, String title, String body, String data) {

    }
}
