package onedayclass;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Lesson_table")
public class Lesson {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long authorId;
    private String authorName;
    private Long lessonId;
    private String lessonName;
    private Long lessonPrice;
    private Date lessonDate;
    private Long reservationId;
    private Long customerId;
    private String customerName;
    private String reservationStatus;
    private String paymentStatus;

    @PostPersist
    public void onPostPersist(){
        //원데이 클래스 생성 
        LessonCreated lessonCreated = new LessonCreated();
        BeanUtils.copyProperties(this, lessonCreated);
        lessonCreated.publishAfterCommit();

    }
    @PostUpdate
    public void onPostUpdate(){
        //원데이 클래스 예약 승인 처리
        System.out.println("######"+this.reservationStatus);
        if(this.getReservationStatus().equals("RSV_APPROVED")){
            ReservationApproved reservationApproved = new ReservationApproved();
            BeanUtils.copyProperties(this, reservationApproved);
            reservationApproved.publishAfterCommit();
        }else if(this.getReservationStatus().equals("RSV_REJECTED")){
        //원데이 클래스 예약 거절 처리
            ReservationRejected reservationRejected = new ReservationRejected();
            BeanUtils.copyProperties(this, reservationRejected);
            reservationRejected.publishAfterCommit();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
    public Long getLessonPrice() {
        return lessonPrice;
    }

    public void setLessonPrice(Long lessonPrice) {
        this.lessonPrice = lessonPrice;
    }
    
    public Date getLessonDate() {
        return lessonDate;
    }

    public void setLessonDate(Date lessonDate) {
        this.lessonDate = lessonDate;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
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