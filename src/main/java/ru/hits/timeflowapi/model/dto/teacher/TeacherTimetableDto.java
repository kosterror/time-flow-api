package ru.hits.timeflowapi.model.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.hits.timeflowapi.model.dto.lesson.LessonDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherTimetableDto {

    private TeacherDto teacher;

    private List<LessonDto> lessons;

}