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

    @Autowired
    private ReservationViewRepository reservationViewRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenLessonCreated_then_CREATE_1 (@Payload LessonCreated lessonCreated) {
        try {
            // 원데이클래스 생성
            if (!lessonCreated.validate()) return;

            System.out.println("\n\n##### listener lessonCreated : " + lessonCreated.toJson() + "\n\n");           
            // view 객체 생성
            ReservationView reservationView = new ReservationView();
            // view 객체에 이벤트의 Value 를 set 함
            reservationView.setAuthorId(lessonCreated.getAuthorId());
            reservationView.setAuthorName(lessonCreated.getAuthorName());
            reservationView.setLessonId(lessonCreated.getLessonId());
            reservationView.setLessonName(lessonCreated.getLessonName());
            reservationView.setLessonPrice(lessonCreated.getLessonPrice());
            reservationView.setLessonDate(lessonCreated.getLessonDate());
            reservationView.setReservationStatus(lessonCreated.getReservationStatus());
            // view 레파지 토리에 insert
            reservationViewRepository.save(reservationView);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenReservationRequested_then_UPDATE_1(@Payload ReservationRequested reservationRequested) {
        try {
            //예약 요청
            if (!reservationRequested.validate()) return;
    
            System.out.println("\n\n##### listener reservationRequested : " + reservationRequested.toJson() + "\n\n");           
            // view 객체 조회
            ReservationView reservationView = reservationViewRepository.findByLessonId(reservationRequested.getLessonId());
            if(reservationView != null){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                reservationView.setReservationId(reservationRequested.getId());
                reservationView.setCustomerId(reservationRequested.getCustomerId());
                reservationView.setCustomerName(reservationRequested.getCustomerName());
                reservationView.setReservationStatus(reservationRequested.getReservationStatus());
                reservationView.setPaymentStatus(reservationRequested.getPaymentStatus());
                // view 레파지 토리에 save
                reservationViewRepository.save(reservationView);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaymentFinished_then_UPDATE_2(@Payload PaymentFinished paymentFinished) {
        try {
            //결제 완료
            if (!paymentFinished.validate()) return;

            System.out.println("\n\n##### listener paymentFinished : " + paymentFinished.toJson() + "\n\n"); 
            // view 객체 조회
            ReservationView reservationView = reservationViewRepository.findByReservationId(paymentFinished.getReservationId());
            if(reservationView != null){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                reservationView.setPaymentStatus(paymentFinished.getPaymentStatus());
                // view 레파지 토리에 save
                reservationViewRepository.save(reservationView);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenReservationApproved_then_UPDATE_3(@Payload ReservationApproved reservationApproved) {
        try {
            //예약 승인
            if (!reservationApproved.validate()) return;

            System.out.println("\n\n##### listener reservationApproved : " + reservationApproved.toJson() + "\n\n"); 
            // view 객체 조회
            ReservationView reservationView = reservationViewRepository.findByReservationId(reservationApproved.getReservationId());
            if(reservationView != null){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                reservationView.setReservationStatus(reservationApproved.getReservationStatus());
                // view 레파지 토리에 save
                reservationViewRepository.save(reservationView);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenReservationRejected_then_UPDATE_4(@Payload ReservationRejected reservationRejected) {
        try {
            //예약 거절
            if (!reservationRejected.validate()) return;

            System.out.println("\n\n##### listener reservationRejected : " + reservationRejected.toJson() + "\n\n"); 
            // view 객체 조회
            ReservationView reservationView = reservationViewRepository.findByReservationId(reservationRejected.getReservationId());
            if(reservationView != null){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                reservationView.setReservationStatus(reservationRejected.getReservationStatus());
                // view 레파지 토리에 save
                reservationViewRepository.save(reservationView);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaymentCanceled_then_UPDATE_5(@Payload PaymentCanceled paymentCanceled) {
        try {
            //결제 취소
            if (!paymentCanceled.validate()) return;

            System.out.println("\n\n##### listener paymentCanceled : " + paymentCanceled.toJson() + "\n\n"); 
            // view 객체 조회
            ReservationView reservationView = reservationViewRepository.findByReservationId(paymentCanceled.getReservationId());
            if(reservationView != null){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                reservationView.setPaymentStatus(paymentCanceled.getPaymentStatus());
                // view 레파지 토리에 save
                reservationViewRepository.save(reservationView);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenReservationCanceled_then_UPDATE_6(@Payload ReservationCanceled reservationCanceled) {
        try {
            //예약 취소
            if (!reservationCanceled.validate()) return;

            System.out.println("\n\n##### listener reservationCanceled : " + reservationCanceled.toJson() + "\n\n"); 
            // view 객체 조회
            ReservationView reservationView = reservationViewRepository.findByReservationId(reservationCanceled.getId());
            if(reservationView != null){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                reservationView.setReservationStatus(reservationCanceled.getReservationStatus());
                // view 레파지 토리에 save
                reservationViewRepository.save(reservationView);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaymentCompleted_then_UPDATE_7(@Payload PaymentCompleted paymentCompleted) {
        try {
            //예약에 대한 전체적인 처리 중 작가에 의한 취소 처리에 대한 처리만 진행
            if (!paymentCompleted.validate()) return;

            System.out.println("\n\n##### listener paymentCompleted : " + paymentCompleted.toJson() + "\n\n"); 
            // view 객체 조회
            ReservationView reservationView = reservationViewRepository.findByReservationId(paymentCompleted.getId());
            if(reservationView != null){
                if(reservationView.getReservationStatus() == "RSV_REJECTED"){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    reservationView.setReservationStatus(paymentCompleted.getReservationStatus());
                    // view 레파지 토리에 save
                    reservationViewRepository.save(reservationView);
                }else{
                    reservationView.setReservationStatus(paymentCompleted.getReservationStatus());
                    reservationView.setPaymentStatus(paymentCompleted.getPaymentStatus());
                    reservationViewRepository.save(reservationView);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}

}
