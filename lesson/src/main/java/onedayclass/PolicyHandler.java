package onedayclass;

import onedayclass.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired LessonRepository lessonRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentCompleted_UpdateReservation(@Payload PaymentCompleted paymentCompleted){
        //예약 요청/취소에 따른 결제 처리 완료 시
        if(!paymentCompleted.validate()) return;
              
        System.out.println("\n\n##### listener paymentCompleted : " + paymentCompleted.toJson() + "\n\n");
        // view 객체 조회
        Lesson lesson = lessonRepository.findByReservationId(paymentCompleted.getId());
        if(lesson != null){
             // 객체가 있다면 이미 예약이 되어, 취소 처리인 경우 
            lesson.setReservationStatus(paymentCompleted.getReservationStatus());
            lesson.setPaymentStatus(paymentCompleted.getPaymentStatus());
            // view 레파지 토리에 update
            lessonRepository.save(lesson);
        }else{
            //객체가 없다면, 신규 예약요청 건인 경우
            Lesson newlesson = lessonRepository.findByLessonId(paymentCompleted.getLessonId());
            if(newlesson != null){
                newlesson.setReservationId(paymentCompleted.getId());
                newlesson.setCustomerId(paymentCompleted.getCustomerId());
                newlesson.setCustomerName(paymentCompleted.getCustomerName());
                newlesson.setReservationStatus(paymentCompleted.getReservationStatus());
                newlesson.setPaymentStatus(paymentCompleted.getPaymentStatus());
                lessonRepository.save(newlesson);
            }
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}

}
