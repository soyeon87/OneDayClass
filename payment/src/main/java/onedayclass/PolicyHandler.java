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
    @Autowired PaymentRepository paymentRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationRejected_CancelPayment(@Payload ReservationRejected reservationRejected){
        //작가가 결제 취소 요청 시 
        if(!reservationRejected.validate()) return;
        
        System.out.println("\n\n##### listener reservationRejected : " + reservationRejected.toJson() + "\n\n");
        // view 객체 조회
        Payment payment = paymentRepository.findByReservationId(reservationRejected.getReservationId());
        if(payment != null){
            payment.setPaymentStatus("PAY_CANCELED");
            // view 레파지 토리에 update
            paymentRepository.save(payment);
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCanceled_CancelPayment(@Payload ReservationCanceled reservationCanceled){
        //고객이 결제 취소 요청 시
        if(!reservationCanceled.validate()) return;

        System.out.println("\n\n##### listener reservationCanceled : " + reservationCanceled.toJson() + "\n\n");
        // view 객체 조회
        Payment payment = paymentRepository.findByReservationId(reservationCanceled.getId());
        if(payment != null){
            payment.setPaymentStatus("PAY_CANCELED");
            // view 레파지 토리에 update
            paymentRepository.save(payment);
        }
    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}

}
