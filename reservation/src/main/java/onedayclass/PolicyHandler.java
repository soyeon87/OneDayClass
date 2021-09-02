package onedayclass;

import onedayclass.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PolicyHandler{
    @Autowired ReservationRepository reservationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentFinished_UpdateReservation(@Payload PaymentFinished paymentFinished){

        if(!paymentFinished.validate()) return;

        System.out.println("\n\n##### listener paymentFinished : " + paymentFinished.toJson() + "\n\n");
        // view 객체 조회
        Optional<Reservation> res = reservationRepository.findById(paymentFinished.getReservationId());
        Reservation reservation = res.get();

        if(reservation != null){
            reservation.setPaymentStatus("PAY_FINISHED");
            // view 레파지 토리에 update
            reservationRepository.save(reservation);
        }

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationApproved_UpdateReservation(@Payload ReservationApproved reservationApproved){
        //원데이클래스 예약 승인 시 
        if(!reservationApproved.validate()) return;

        System.out.println("\n\n##### listener reservationApproved : " + reservationApproved.toJson() + "\n\n");
        // view 객체 조회
        Optional<Reservation> res = reservationRepository.findById(reservationApproved.getReservationId());
        Reservation reservation = res.get();

        if(reservation != null){
            reservation.setReservationStatus("RSV_APPROVED");
            // view 레파지 토리에 update
            reservationRepository.save(reservation);
        }

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentCanceled_UpdateReservation(@Payload PaymentCanceled paymentCanceled){

        if(!paymentCanceled.validate()) return;

        System.out.println("\n\n##### listener paymentCanceled : " + paymentCanceled.toJson() + "\n\n");
        // view 객체 조회
        Optional<Reservation> res = reservationRepository.findById(paymentCanceled.getReservationId());
        Reservation reservation = res.get();

        if(reservation != null){
            reservation.setReservationStatus("RSV_CANCELED");
            reservation.setPaymentStatus("PAY_CANCELED");
            // view 레파지 토리에 update
            reservationRepository.save(reservation);
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
