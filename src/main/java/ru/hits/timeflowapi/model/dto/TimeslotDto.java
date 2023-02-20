package ru.hits.timeflowapi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeslotDto {

    private UUID id;

    private int sequenceNumber;

    private Date beginTime;

    private Date endTime;

}
