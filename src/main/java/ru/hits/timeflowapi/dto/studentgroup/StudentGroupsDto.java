package ru.hits.timeflowapi.dto.studentgroup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StudentGroupsDto {

    private List<StudentGroupBasicDto> studentGroups;
}
