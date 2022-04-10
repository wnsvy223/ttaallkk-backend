package security.ttaallkk.service.notification;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.TopicManagementResponse;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;


@Service
@Log4j2
public class FcmService {
    
    /**
     * FCM 푸시 메시지 비동기 전송
     * @param message
     */
    public void sendFcm(Message message) {
        try {
            String response = FirebaseMessaging.getInstance().sendAsync(message).get();
            log.info("FCM Send : " + response);
        } catch (InterruptedException e) {
            log.error("FCM InterruptedException : " + e);
        } catch (ExecutionException e) {
            log.error("FCM ExecutionException : " + e);
        }
    }

    /**
     * FCM 주제 구독 등록
     * @param registrationTokens
     * @param topic
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public TopicManagementResponse subscribeTokenToTopic(List<String> registrationTokens, String topic) throws InterruptedException, ExecutionException {
        return FirebaseMessaging.getInstance().subscribeToTopicAsync(registrationTokens, topic).get();
    }

    /**
     * FCM 주제 구독 취소
     * @param registrationTokens
     * @param topic
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public TopicManagementResponse unSubscribeTokenToTopic(List<String> registrationTokens, String topic) throws InterruptedException, ExecutionException {
        return FirebaseMessaging.getInstance().unsubscribeFromTopicAsync(registrationTokens, topic).get();
    }
}
