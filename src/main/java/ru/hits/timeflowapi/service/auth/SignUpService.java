package ru.hits.timeflowapi.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.hits.timeflowapi.exception.ConflictException;
import ru.hits.timeflowapi.mapper.UserMapper;
import ru.hits.timeflowapi.model.dto.signin.TokensDto;
import ru.hits.timeflowapi.model.dto.user.signup.EmployeeSignUpDto;
import ru.hits.timeflowapi.model.dto.user.signup.StudentSignUpDto;
import ru.hits.timeflowapi.model.dto.user.signup.UserSignUpDto;
import ru.hits.timeflowapi.model.entity.EmployeeDetailsEntity;
import ru.hits.timeflowapi.model.entity.StudentDetailsEntity;
import ru.hits.timeflowapi.model.entity.StudentGroupEntity;
import ru.hits.timeflowapi.model.entity.UserEntity;
import ru.hits.timeflowapi.model.enumeration.AccountStatus;
import ru.hits.timeflowapi.model.enumeration.Role;
import ru.hits.timeflowapi.repository.EmployeeDetailsRepository;
import ru.hits.timeflowapi.repository.StudentDetailsRepository;
import ru.hits.timeflowapi.repository.UserRepository;
import ru.hits.timeflowapi.security.JWTService;
import ru.hits.timeflowapi.service.LessonComponentsService;
import ru.hits.timeflowapi.service.helpingservices.CheckEmailService;
import ru.hits.timeflowapi.service.request.CreateRequestService;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private final JWTService jwtService;
    private final UserMapper userMapper;
    private final CheckEmailService checkEmailService;
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
        checkEmailService.checkEmail(userSignUpDTO.getEmail());

        UserEntity user = userMapper.basicSignUpDetailsToUser(
                userSignUpDTO,
                Role.ROLE_USER,
                AccountStatus.ACTIVATE
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
        checkEmailService.checkEmail(studentSignUpDto.getEmail());

        if (studentDetailsRepository.existsByStudentNumber(studentSignUpDto.getStudentNumber())) {
            throw new ConflictException("Пользователь с таким номером студенческого билета уже существует.");
        }

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
        checkEmailService.checkEmail(employeeSignUpDto.getEmail());

        if (employeeDetailsRepository.existsByContractNumber(employeeSignUpDto.getContractNumber())) {
            throw new ConflictException("Пользователь с таким номером трудового договора уже существует");
        }

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
        checkEmailService.checkEmail(employeeSignUpDto.getEmail());

        if (employeeDetailsRepository.existsByContractNumber(employeeSignUpDto.getContractNumber())) {
            throw new ConflictException("Пользователь с таким номером трудового договора уже существует");
        }

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