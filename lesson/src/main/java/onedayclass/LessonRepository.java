package onedayclass;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="lessons", path="lessons")
public interface LessonRepository extends PagingAndSortingRepository<Lesson, Long>{

    Lesson findByReservationId(Long reservationId);
    Lesson findByLessonId(Long lessonId);
}
