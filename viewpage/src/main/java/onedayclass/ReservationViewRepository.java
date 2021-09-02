package onedayclass;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
//import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReservationViewRepository extends CrudRepository<ReservationView, Long> {
//public interface ReservationViewRepository extends CrudRepository<ReservationView, Long> {

    ReservationView findByLessonId(Long lessonId);
    ReservationView findByReservationId(Long reservationId);

}