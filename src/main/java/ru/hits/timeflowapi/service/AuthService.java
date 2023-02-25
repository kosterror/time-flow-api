package ru.hits.timeflowapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.hits.timeflowapi.exception.EmailAlreadyUsedException;
import ru.hits.timeflowapi.exception.NotFoundException;
import ru.hits.timeflowapi.mapper.UserMapper;
import ru.hits.timeflowapi.model.dto.user.EmployeeDto;
import ru.hits.timeflowapi.model.dto.user.StudentDto;
import ru.hits.timeflowapi.model.dto.user.UserDto;
import ru.hits.timeflowapi.model.dto.user.signup.EmployeeSignUpDto;
import ru.hits.timeflowapi.model.dto.user.signup.StudentSignUpDto;
import ru.hits.timeflowapi.model.dto.user.signup.UserSignUpDto;
import ru.hits.timeflowapi.model.entity.EmployeeDetailsEntity;
import ru.hits.timeflowapi.model.entity.StudentDetailsEntity;
import ru.hits.timeflowapi.model.entity.StudentGroupEntity;
import ru.hits.timeflowapi.model.entity.UserEntity;
import ru.hits.timeflowapi.model.entity.requestconfirm.EmployeeRequestConfirmEntity;
import ru.hits.timeflowapi.model.entity.requestconfirm.ScheduleMakerRequestConfirmEntity;
import ru.hits.timeflowapi.model.entity.requestconfirm.StudentRequestConfirmEntity;
import ru.hits.timeflowapi.model.enumeration.AccountStatus;
import ru.hits.timeflowapi.model.enumeration.Role;
import ru.hits.timeflowapi.repository.EmployeeDetailsRepository;
import ru.hits.timeflowapi.repository.StudentDetailsRepository;
import ru.hits.timeflowapi.repository.StudentGroupRepository;
import ru.hits.timeflowapi.repository.UserRepository;
import ru.hits.timeflowapi.repository.requestconfirm.EmployeeRequestConfirmRepository;
import ru.hits.timeflowapi.repository.requestconfirm.ScheduleMakerRequestConfirmRepository;
import ru.hits.timeflowapi.repository.requestconfirm.StudentRequestConfirmRepository;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EmployeeDetailsRepository employeeDetailsRepository;
    private final StudentDetailsRepository studentDetailsRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final EmployeeRequestConfirmRepository employeeRequestConfirmRepository;
    private final ScheduleMakerRequestConfirmRepository scheduleMakerRequestConfirmRepository;
    private final StudentRequestConfirmRepository studentRequestConfirmRepository;
    private final UserMapper userMapper;

    /**
     * Метод для регистрации внешнего пользователя.
     *
     * @param userSignUpDTO информация для регистрации внешнего пользователя.
     * @return сохраненная информация о внешнем пользователе.
     */
    public UserDto userSignUp(UserSignUpDto userSignUpDTO) {
        checkEmail(userSignUpDTO.getEmail());

        UserEntity user = userMapper.basicSignUpDetailsToUser(
                userSignUpDTO,
                Role.ROLE_USER,
                AccountStatus.ACTIVATE
        );

        user = userRepository.save(user);

        return userMapper.userToUserDto(user);
    }

    /**
     * Метод для регистрации студента.
     *
     * @param studentSignUpDTO информация о студента для регистрации.
     * @return сохраненная информация о студенте.
     */
    public StudentDto studentSignUp(StudentSignUpDto studentSignUpDTO) {
        checkEmail(studentSignUpDTO.getEmail());

        UserEntity user = userMapper.basicSignUpDetailsToUser(
                studentSignUpDTO,
                Role.ROLE_STUDENT,
                AccountStatus.PENDING
        );

        Optional<StudentGroupEntity> studentGroupEntity =
                studentGroupRepository.findById(studentSignUpDTO.getGroupId());

        if (studentGroupEntity.isEmpty()) {
            throw new NotFoundException("Группа с таким ID не найдена");
        }

        user = userRepository.save(user);

        StudentDetailsEntity studentDetails = StudentDetailsEntity
                .builder()
                .user(user)
                .studentNumber(studentSignUpDTO.getStudentNumber())
                .group(studentGroupEntity.get())
                .build();

        studentDetails = studentDetailsRepository.save(studentDetails);

        StudentRequestConfirmEntity studentRequestConfirm = StudentRequestConfirmEntity
                .builder()
                .studentDetails(studentDetails)
                .isClosed(false)
                .creationDate(new Date())
                .build();

        studentRequestConfirmRepository.save(studentRequestConfirm);

        return userMapper.studentDetailsToStudentDto(studentDetails);
    }

    /**
     * Метод для регистрации сотрудника.
     *
     * @param employeeSignUpDTO информация о сотруднике для регистрации.
     * @return сохраненная информация о сотруднике.
     */
    public EmployeeDto employeeSignUp(EmployeeSignUpDto employeeSignUpDTO) {
        EmployeeDetailsEntity employeeDetails = basicEmployeeSignUp(employeeSignUpDTO);

        EmployeeRequestConfirmEntity employeeRequestConfirm = EmployeeRequestConfirmEntity
                .builder()
                .employeeDetails(employeeDetails)
                .creationDate(new Date())
                .isClosed(false)
                .build();

        employeeRequestConfirmRepository.save(employeeRequestConfirm);

        return userMapper.employeeDetailsToEmployeeDto(employeeDetails);
    }

    /**
     * Метод для регистрации составителя расписаний.
     *
     * @param employeeSignUpDTO информация для регистрации составителя расписаний.
     * @return сохраненная информация о составителе расписаний.
     */
    public EmployeeDto scheduleMakerSignUp(EmployeeSignUpDto employeeSignUpDTO) {
        EmployeeDetailsEntity employeeDetails = basicEmployeeSignUp(employeeSignUpDTO);

        ScheduleMakerRequestConfirmEntity scheduleMakerRequestConfirm = ScheduleMakerRequestConfirmEntity
                .builder()
                .employeeDetails(employeeDetails)
                .creationDate(new Date())
                .isClosed(false)
                .build();

        scheduleMakerRequestConfirmRepository.save(scheduleMakerRequestConfirm);

        return userMapper.employeeDetailsToEmployeeDto(employeeDetails);
    }

    /**
     * Общая логика для создания и сохранения сущности сотрудника в БД.
     *
     * @param employeeSignUpDTO детали о сотруднике.
     * @return сохраненную сущность сотрудника в БД.
     */
    public EmployeeDetailsEntity basicEmployeeSignUp(EmployeeSignUpDto employeeSignUpDTO) {
        checkEmail(employeeSignUpDTO.getEmail());

        UserEntity user = userMapper.basicSignUpDetailsToUser(
                employeeSignUpDTO,
                Role.ROLE_EMPLOYEE,
                AccountStatus.PENDING
        );

        user = userRepository.save(user);

        EmployeeDetailsEntity employeeDetails = EmployeeDetailsEntity
                .builder()
                .user(user)
                .contactNumber(employeeSignUpDTO.getContractNumber())
                .build();

        return employeeDetailsRepository.save(employeeDetails);
    }

    /**
     * Метод для проверки существования пользователя с заданной почтой. Если эта почта занята,
     * то выбросится исключение.
     *
     * @param email почта.
     * @throws EmailAlreadyUsedException выбрасывается, если заданная почта уже используется.
     */
    private void checkEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException("Пользователь с такой почтой уже существует");
        }
    }

}
