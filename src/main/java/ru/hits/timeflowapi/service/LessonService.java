package ru.hits.timeflowapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.hits.timeflowapi.exception.NotFoundException;
import ru.hits.timeflowapi.mapper.CreateLessonForAFewWeeksDtoMapper;
import ru.hits.timeflowapi.model.dto.SubjectDto;
import ru.hits.timeflowapi.model.dto.TimeslotDto;
import ru.hits.timeflowapi.model.dto.classroom.ClassroomDto;
import ru.hits.timeflowapi.model.dto.classroom.ClassroomTimetableDto;
import ru.hits.timeflowapi.model.dto.lesson.CreateLessonDto;
import ru.hits.timeflowapi.model.dto.lesson.CreateLessonForAFewWeeksDto;
import ru.hits.timeflowapi.model.dto.lesson.LessonDto;
import ru.hits.timeflowapi.model.dto.studentgroup.StudentGroupBasicDto;
import ru.hits.timeflowapi.model.dto.studentgroup.StudentGroupTimetableDto;
import ru.hits.timeflowapi.model.dto.teacher.TeacherDto;
import ru.hits.timeflowapi.model.dto.teacher.TeacherTimetableDto;
import ru.hits.timeflowapi.model.entity.*;
import ru.hits.timeflowapi.repository.*;
import ru.hits.timeflowapi.service.helpingservices.CheckClassroomAndTeacherAndTimeslotAccessibility;
import ru.hits.timeflowapi.service.helpingservices.CheckCreateLessonDtoValidity;
import ru.hits.timeflowapi.service.helpingservices.VerificationOfDates;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final TimeslotRepository timeslotRepository;
    private final ClassroomRepository classroomRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final CheckClassroomAndTeacherAndTimeslotAccessibility checkClassroomAndTeacherAndTimeslotAccessibility;
    private final CheckCreateLessonDtoValidity checkCreateLessonDtoValidity;
    private final VerificationOfDates verificationOfDates;
    private final CreateLessonForAFewWeeksDtoMapper createLessonForAFewWeeksDtoMapper;

    public StudentGroupTimetableDto getWeekLessonsByGroupId(UUID groupId, LocalDate startDate, LocalDate endDate) {

        verificationOfDates.checkDates(startDate, endDate);

        StudentGroupEntity studentGroup = studentGroupRepository.findById(groupId)
                .orElseThrow(() ->
                        new NotFoundException("Студенческой группы с таким ID " + groupId + " не существует"));

        List<LessonEntity> lessons = lessonRepository.findByStudentGroup(studentGroup, Sort.by("date"));

        List<LessonDto> lessonDtos = new ArrayList<>();

        for (LessonEntity lesson: lessons) {
            if (lesson.getDate().isAfter(startDate.minusDays(1))
                    && lesson.getDate().isBefore(endDate.plusDays(1))) {
                lessonDtos.add(new LessonDto(
                        lesson.getId(),
                        new StudentGroupBasicDto(lesson.getStudentGroup()),
                        new SubjectDto(lesson.getSubject()),
                        new TeacherDto(lesson.getTeacher()),
                        new ClassroomDto(lesson.getClassroom()),
                        new TimeslotDto(lesson.getTimeslot()),
                        lesson.getDate(),
                        lesson.getLessonType())
                );
            }
        }

        return new StudentGroupTimetableDto(new StudentGroupBasicDto(studentGroup), lessonDtos);

    }

    public TeacherTimetableDto getWeekLessonsByTeacherId(UUID teacherId, LocalDate startDate, LocalDate endDate) {

        verificationOfDates.checkDates(startDate, endDate);

        TeacherEntity teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() ->
                        new NotFoundException("Преподавателя с таким ID " + teacherId + " не существует"));

        List<LessonEntity> lessons = lessonRepository.findByTeacher(teacher, Sort.by("date"));

        List<LessonDto> lessonDtos = new ArrayList<>();

        for (LessonEntity lesson: lessons) {
            if (lesson.getDate().isAfter(startDate.minusDays(1))
                    && lesson.getDate().isBefore(endDate.plusDays(1))) {
                lessonDtos.add(new LessonDto(
                        lesson.getId(),
                        new StudentGroupBasicDto(lesson.getStudentGroup()),
                        new SubjectDto(lesson.getSubject()),
                        new TeacherDto(lesson.getTeacher()),
                        new ClassroomDto(lesson.getClassroom()),
                        new TimeslotDto(lesson.getTimeslot()),
                        lesson.getDate(),
                        lesson.getLessonType())
                );
            }
        }

        return new TeacherTimetableDto(new TeacherDto(teacher), lessonDtos);

    }

    public ClassroomTimetableDto getWeekLessonsByClassroomId(UUID classroomId, LocalDate startDate, LocalDate endDate) {

        verificationOfDates.checkDates(startDate, endDate);

        ClassroomEntity classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() ->
                        new NotFoundException("Аудитории с таким ID " + classroomId + " не существует"));

        List<LessonEntity> lessons = lessonRepository.findByClassroom(classroom, Sort.by("date"));

        List<LessonDto> lessonDtos = new ArrayList<>();

        for (LessonEntity lesson: lessons) {
            if (lesson.getDate().isAfter(startDate.minusDays(1))
                    && lesson.getDate().isBefore(endDate.plusDays(1))) {
                lessonDtos.add(new LessonDto(
                        lesson.getId(),
                        new StudentGroupBasicDto(lesson.getStudentGroup()),
                        new SubjectDto(lesson.getSubject()),
                        new TeacherDto(lesson.getTeacher()),
                        new ClassroomDto(lesson.getClassroom()),
                        new TimeslotDto(lesson.getTimeslot()),
                        lesson.getDate(),
                        lesson.getLessonType())
                );
            }
        }

        return new ClassroomTimetableDto(new ClassroomDto(classroom), lessonDtos);

    }

    public LessonDto getLessonById(UUID id) {

        LessonEntity lesson = lessonRepository.findById(id).orElse(null);

        if (lesson == null) {
            throw new NotFoundException("Пары с таким ID " + id + " не существует");
        }

        return new LessonDto(lesson);

    }

    public LessonDto addLesson(CreateLessonDto createLessonDto) {

        LessonEntity lesson = new LessonEntity();

        checkCreateLessonDtoValidity.checkIdValidity(createLessonDto);
        checkClassroomAndTeacherAndTimeslotAccessibility.checkAccessibility(
                createLessonDto.getTimeslotId(),
                createLessonDto.getTeacherId(),
                createLessonDto.getClassroomId(),
                createLessonDto.getStudentGroupId(),
                createLessonDto.getDate()
        );

        lesson.setStudentGroup(studentGroupRepository.findById(createLessonDto.getStudentGroupId()).orElse(null));
        lesson.setSubject(subjectRepository.findById(createLessonDto.getSubjectId()).orElse(null));
        lesson.setTeacher(teacherRepository.findById(createLessonDto.getTeacherId()).orElse(null));
        lesson.setClassroom(classroomRepository.findById(createLessonDto.getClassroomId()).orElse(null));
        lesson.setTimeslot(timeslotRepository.findById(createLessonDto.getTimeslotId()).orElse(null));
        lesson.setDate(createLessonDto.getDate());
        lesson.setLessonType(createLessonDto.getLessonType());

        lessonRepository.save(lesson);

        return new LessonDto(lesson);

    }

    public List<LessonDto> addLessonForAFewWeeks(CreateLessonForAFewWeeksDto createLessonForAFewWeeksDto) {

        CreateLessonDto createLessonDto = createLessonForAFewWeeksDtoMapper.
                createLessonForAFewWeeksDtoToCreateLessonDto(createLessonForAFewWeeksDto);

        checkCreateLessonDtoValidity.checkIdValidity(createLessonDto);

        List<LessonDto> lessonDtos = new ArrayList<>();

        for (long i = 0; i < createLessonForAFewWeeksDto.getNumberOfWeeks(); i++) {
            checkClassroomAndTeacherAndTimeslotAccessibility.checkAccessibility(
                    createLessonDto.getTimeslotId(),
                    createLessonDto.getTeacherId(),
                    createLessonDto.getClassroomId(),
                    createLessonDto.getStudentGroupId(),
                    createLessonDto.getDate().plusDays(i*7)
            );

            LessonEntity lesson = new LessonEntity();

            lesson.setStudentGroup(studentGroupRepository.findById(createLessonDto.getStudentGroupId()).orElse(null));
            lesson.setSubject(subjectRepository.findById(createLessonDto.getSubjectId()).orElse(null));
            lesson.setTeacher(teacherRepository.findById(createLessonDto.getTeacherId()).orElse(null));
            lesson.setClassroom(classroomRepository.findById(createLessonDto.getClassroomId()).orElse(null));
            lesson.setTimeslot(timeslotRepository.findById(createLessonDto.getTimeslotId()).orElse(null));
            lesson.setDate(createLessonDto.getDate().plusDays(i*7));
            lesson.setLessonType(createLessonDto.getLessonType());

            lessonRepository.save(lesson);

            lessonDtos.add(new LessonDto(lesson));
        }

        return lessonDtos;

    }

    public void deleteLesson(UUID id) {

        if (lessonRepository.findById(id).isEmpty()) {
            throw new NotFoundException("Пары с таким ID " + id + " не существует");
        }

        lessonRepository.deleteById(id);

    }

    public void deleteAllLessonsByWeek(LocalDate startDate, LocalDate endDate) {

        List<LessonEntity> lessons = lessonRepository.findAll();

        for (LessonEntity lesson: lessons) {
            if (lesson.getDate().isAfter(startDate.minusDays(1))
                    && lesson.getDate().isBefore(endDate.plusDays(1))) {
                lessonRepository.deleteById(lesson.getId());
            }
        }

    }

    public LessonDto updateLesson(UUID id, CreateLessonDto updatedLessonDto) {

        LessonEntity lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пары с таким ID " + id + " не существует"));

        checkCreateLessonDtoValidity.checkIdValidity(updatedLessonDto);
        checkClassroomAndTeacherAndTimeslotAccessibility.checkAccessibility(
                updatedLessonDto.getTimeslotId(),
                updatedLessonDto.getTeacherId(),
                updatedLessonDto.getClassroomId(),
                updatedLessonDto.getStudentGroupId(),
                updatedLessonDto.getDate()
        );

        lesson.setStudentGroup(studentGroupRepository.findById(updatedLessonDto.getStudentGroupId()).orElse(null));
        lesson.setSubject(subjectRepository.findById(updatedLessonDto.getSubjectId()).orElse(null));
        lesson.setTeacher(teacherRepository.findById(updatedLessonDto.getTeacherId()).orElse(null));
        lesson.setClassroom(classroomRepository.findById(updatedLessonDto.getClassroomId()).orElse(null));
        lesson.setTimeslot(timeslotRepository.findById(updatedLessonDto.getTimeslotId()).orElse(null));
        lesson.setDate(updatedLessonDto.getDate());
        lesson.setLessonType(updatedLessonDto.getLessonType());

        lessonRepository.save(lesson);

        return new LessonDto(lesson);

    }

}
