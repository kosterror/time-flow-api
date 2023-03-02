package ru.hits.timeflowapi.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.hits.timeflowapi.model.dto.TimeslotDto;
import ru.hits.timeflowapi.model.entity.TimeslotEntity;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TimeslotMapper {
    public List<TimeslotDto> timeslotListToDtoList(List<TimeslotEntity> entityList) {
        List<TimeslotDto> dtoList = new ArrayList<>();

        if (entityList != null) {
            for (TimeslotEntity entity : entityList) {
                dtoList.add(new TimeslotDto(entity));
            }
        }
        return dtoList;
    }
}
