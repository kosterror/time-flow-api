package ru.hits.timeflowapi.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.hits.timeflowapi.dto.signin.TokensDto;
import ru.hits.timeflowapi.dto.signup.EmployeeSignUpDto;
import ru.hits.timeflowapi.dto.signup.StudentSignUpDto;
import ru.hits.timeflowapi.dto.signup.UserSignUpDto;
import ru.hits.timeflowapi.entity.EmployeeDetailsEntity;
import ru.hits.timeflowapi.entity.StudentDetailsEntity;
import ru.hits.timeflowapi.entity.StudentGroupEntity;
import ru.hits.timeflowapi.entity.UserEntity;
import ru.hits.timeflowapi.enumeration.AccountStatus;
import ru.hits.timeflowapi.enumeration.Role;
import ru.hits.timeflowapi.mapper.UserMapper;
import ru.hits.timeflowapi.repository.EmployeeDetailsRepository;
import ru.hits.timeflowapi.repository.StudentDetailsRepository;
import ru.hits.timeflowapi.repository.UserRepository;
import ru.hits.timeflowapi.security.JWTService;
import ru.hits.timeflowapi.service.LessonComponentsService;
import ru.hits.timeflowapi.service.request.CreateRequestService;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private final JWTService jwtService;
    private final UserMapper userMapper;
    private final LessonComponentsService lessonComponentsService;
    private final UserRepository userRepository;
    private final EmployeeDetailsRepository employeeDetailsRepository;
    private final StudentDetailsRepository studentDetailsRepository;
    private final CreateRequestService createRequestService;

    /**
     * Метод для регистрации внешнего пользователя.
     *
     * @param userSignUpDTO информация для регистрации внешнего пользователя.
     * @return пара {@code access} и {@code refresh} токенов.
     */
    public TokensDto userSignUp(UserSignUpDto userSignUpDTO) {
        UserEntity user = userMapper.basicSignUpDetailsToUser(
                userSignUpDTO,
                Role.ROLE_USER,
                AccountStatus.ACTIVATED
        );

        user = userRepository.save(user);
        return jwtService.generateTokens(user.getId());
    }

    /**
     * Метод для регистрации студентов.
     *
     * @param studentSignUpDto dto с информации о студенте.
     * @return пара {@code access} и {@code refresh} токенов.
     */
    public TokensDto studentSignUp(StudentSignUpDto studentSignUpDto) {
        StudentGroupEntity studentGroupEntity
                = lessonComponentsService.getGroupEntityById(studentSignUpDto.getGroupId());

        UserEntity user = userMapper.basicSignUpDetailsToUser(
                studentSignUpDto,
                Role.ROLE_STUDENT,
                AccountStatus.PENDING
        );

        user = userRepository.save(user);

        StudentDetailsEntity studentDetails = StudentDetailsEntity
                .builder()
                .user(user)
                .studentNumber(studentSignUpDto.getStudentNumber())
                .group(studentGroupEntity)
                .build();

        studentDetails = studentDetailsRepository.save(studentDetails);
        createRequestService.createAndSaveStudentRequest(studentDetails);

        return jwtService.generateTokens(user.getId());
    }

    /**
     * Метод для регистрации сотрудника.
     *
     * @param employeeSignUpDto dto с информацией о сотруднике.
     * @return пара {@code access} и {@code refresh} токенов.
     */
    public TokensDto employeeSignUp(EmployeeSignUpDto employeeSignUpDto) {
        EmployeeDetailsEntity employeeDetails = basicEmployeeSignUp(employeeSignUpDto);
        createRequestService.createAndSaveEmployeeRequest(employeeDetails);

        return jwtService.generateTokens(employeeDetails.getUser().getId());
    }

    /**
     * Метод для регистрации сотрудника с должностью "Составитель расписаний".
     *
     * @param employeeSignUpDto dto с информацией о сотруднике.
     * @return пара {@code access} и {@code refresh} токенов.
     */
    public TokensDto scheduleMakerSignUp(EmployeeSignUpDto employeeSignUpDto) {
        EmployeeDetailsEntity employeeDetails = basicEmployeeSignUp(employeeSignUpDto);
        createRequestService.createAndSaveScheduleMakerRequest(employeeDetails);

        return jwtService.generateTokens(employeeDetails.getUser().getId());
    }

    /**
     * Общая логика для создания и сохранения сущности сотрудника в БД.
     *
     * @param employeeSignUpDTO детали о сотруднике.
     * @return сохраненную сущность сотрудника в БД.
     */
    public EmployeeDetailsEntity basicEmployeeSignUp(EmployeeSignUpDto employeeSignUpDTO) {
        UserEntity user = userMapper.basicSignUpDetailsToUser(
                employeeSignUpDTO,
                Role.ROLE_EMPLOYEE,
                AccountStatus.PENDING
        );

        user = userRepository.save(user);

        EmployeeDetailsEntity employeeDetails = EmployeeDetailsEntity
                .builder()
                .user(user)
                .contractNumber(employeeSignUpDTO.getContractNumber())
                .build();

        return employeeDetailsRepository.save(employeeDetails);
    }

}
