package ru.hits.timeflowapi.repository.requestconfirm;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.hits.timeflowapi.model.entity.requestconfirm.ScheduleMakerRequestConfirmEntity;
import ru.hits.timeflowapi.model.entity.requestconfirm.StudentRequestConfirmEntity;

import java.util.UUID;

@Repository
public interface ScheduleMakerRequestConfirmRepository extends JpaRepository<ScheduleMakerRequestConfirmEntity, UUID> {

    Page<StudentRequestConfirmEntity> findAllByIsClosed(Pageable pageable, boolean isClosed);

}
