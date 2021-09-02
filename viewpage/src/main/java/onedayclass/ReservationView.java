package onedayclass;

import javax.persistence.*;
import java.util.List;
import java.util.Date;
//import org.springframework.data.mongodb.core.mapping.Document;

@Entity
@Table(name="ReservationView_table")
//@Document(collection="myCols")
public class ReservationView {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private Long reservationId;
        private Long customerId;
        private String customerName;
        private Long authorId;
        private String authorName;
        private Long lessonId;
        private String lessonName;
        private Long lessonPrice;
        private Date lessonDate;
        private String reservationStatus;
        private String paymentStatus;


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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
