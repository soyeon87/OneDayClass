package onedayclass;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long customerId;
    private String customerName;
    private Long authorId;
    private String authorName;
    private Long lessonId;
    private String lessonName;
    private Date lessonDate;
    private Long lessonPrice;
    private String reservationStatus;
    private String paymentStatus;

    @PostPersist
    public void onPostPersist(){
        //원데이클래스 예약 요청
        onedayclass.external.Payment payment = new onedayclass.external.Payment();
        payment.setReservationId(this.getId());
        payment.setCustomerId(this.getCustomerId());
        payment.setCustomerName(this.getCustomerName());
        payment.setAuthorId(this.getAuthorId());
        payment.setAuthorName(this.getAuthorName());
        payment.setLessonId(this.getLessonId());
        payment.setLessonName(this.getLessonName());
        payment.setLessonPrice(this.getLessonPrice());
        payment.setPaymentStatus("PAY_FINISHED");

        Boolean result = ReservationApplication.applicationContext.getBean(onedayclass.external.PaymentService.class)
            .requestPayment(payment);

        if(result){
             //Kafka Push
            ReservationRequested reservationRequested = new ReservationRequested();
            BeanUtils.copyProperties(this, reservationRequested);
            reservationRequested.publishAfterCommit();
        }
    }
    @PostUpdate
    public void onPostUpdate(){
        if(this.getReservationStatus().equals("RSV_CANCELED") && this.getPaymentStatus().equals("PAY_FINISHED")){
            //원데이클래스 예약 취소 요청
            ReservationCanceled reservationCanceled = new ReservationCanceled();
            BeanUtils.copyProperties(this, reservationCanceled);
            reservationCanceled.publishAfterCommit();
        }else if(!this.getReservationStatus().equals("RSV_APPROVED")){
            //결재 취소/승인 처리 완료
            PaymentCompleted paymentCompleted = new PaymentCompleted();
            BeanUtils.copyProperties(this, paymentCompleted);
            paymentCompleted.publishAfterCommit();
        }

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }
    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }
    public String getLessonName() {
        return lessonName;
    }

    public void setLessonName(String lessonName) {
        this.lessonName = lessonName;
    }
    
    public Date getLessonDate() {
        return lessonDate;
    }

    public void setLessonDate(Date lessonDate) {
        this.lessonDate = lessonDate;
    }
    public Long getLessonPrice() {
        return lessonPrice;
    }

    public void setLessonPrice(Long lessonPrice) {
        this.lessonPrice = lessonPrice;
    }
    public String getReservationStatus() {
        return reservationStatus;
    }

    public void setReservationStatus(String reservationStatus) {
        this.reservationStatus = reservationStatus;
    }
    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }




}