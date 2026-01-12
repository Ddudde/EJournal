package ru.services.logic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.configs.AppConfig;
import ru.controllers.TestController;
import ru.data.DAO.Syst;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.Group;
import ru.data.DAO.school.School;
import ru.data.DTO.SubscriberDTO;
import ru.data.DTO.controller.test.TestInnerDTO;
import ru.data.DTO.controller.test.TestOutDTO;
import ru.data.DTO.controller.test.body.TestBodyDTO;
import ru.data.DTO.controller.test.body.TestServiceBodyDTO;
import ru.data.DTO.controller.test.body.TestServiceBodyGroupDTO;
import ru.data.DTO.controller.test.body.TestServiceBodySchoolDTO;
import ru.data.reps.school.LessonRepository;
import ru.data.reps.school.SchoolRepository;
import ru.security.user.CustomToken;
import ru.security.user.Roles;
import ru.services.db.IRandomizeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** RU: сервис для контроллера
 * @see TestController */
@Service
@RequiredArgsConstructor
public class TestService implements ITestService {
    private final IRandomizeService randomizeService;
    private final SchoolRepository schoolRepository;
    private final LessonRepository lessonRepository;

    @Override
    public TestOutDTO changeTests(TestInnerDTO body) {
        TestOutDTO outDTO = null;
        switch (body.id) {
            case "checkbox_debug" -> AppConfig.DEBUG = body.val;
            case "checkbox_test" -> {
                AppConfig.TEST = body.val;
                if(AppConfig.TEST) {
                    randomizeService.createRandomData();
                } else {
                    randomizeService.removeRandomData();
                }
                outDTO = getTestInfo();
            }
            default -> {}
        }
        return outDTO;
    }

    @Override
    public TestOutDTO prepareInfo() {
        final var dtoBuilder = TestOutDTO.builder();
        final var bodyDTOBuilder = TestBodyDTO.builder();

        bodyDTOBuilder.checkbox_debug(AppConfig.DEBUG)
            .checkbox_test(AppConfig.TEST);
        dtoBuilder.bodyS(bodyDTOBuilder.build())
            .bodyT(getTestInfo().bodyT());
        return dtoBuilder.build();
    }

    /** RU: готовит JSON с данными списка пользователей
     * <pre>
     * user.id : {
     *     "fio",
     *     "login",
     *     "code"
     * }
     * </pre>
     * @see #getTestInfo() Пример использования */
    private Map<Long, TestServiceBodyDTO> getUsersForTestDTO(List<User> users, Map<Long, TestServiceBodyDTO> usersByIdDTO) {
        for (User user : users) {
            final var dtoBuilder = TestServiceBodyDTO.builder();

            dtoBuilder.fio(user.getFio())
                .login(user.getUsername())
                .code(user.getCode());
            usersByIdDTO.put(user.getId(), dtoBuilder.build());
        }
        return usersByIdDTO;
    }

    private Map<Long, TestServiceBodyDTO> getUsersForTestDTO(List<User> users) {
        return getUsersForTestDTO(users, new HashMap<>());
    }

    /** RU: готовит JSON с данными приготовленными для тестирования по всей системе
     * <pre>
     * "bodyT" : {
     *     "admins" :{{@link #getUsersForTestDTO}},
     *     "schools" :{
     *         school.id : {
     *             "name",
     *             "hteachers" :{{@link #getUsersForTestDTO}},
     *             "teachers" :{{@link #getUsersForTestDTO}},
     *             "groups" :{
     *                 group.id : {
     *                     "name",
     *                     "kids" :{{@link #getUsersForTestDTO}},
     *                     "parents" :{{@link #getUsersForTestDTO}}
     *                 }
     *             }
     *         }
     *     }
     * }
     * </pre>
     * toDo: добавление testPassword в bodyT
     * @see TestController#getInfo(SubscriberDTO, CustomToken)  Пример использования */
    public TestOutDTO getTestInfo() {
        final var dtoBuilder = TestServiceBodyDTO.builder();
        final Map<Long, TestServiceBodySchoolDTO> schoolsByIdDTO = new HashMap<>();
        final Set<Long> schools = randomizeService.getSchools();
        final Syst syst = randomizeService.getSyst();

        if(syst != null) {
            dtoBuilder.testPassword(syst.getTestPassword());
            dtoBuilder.admins(getUsersForTestDTO(syst.getAdmins()));
        }
        if(ObjectUtils.isEmpty(schools)) {
            return new TestOutDTO(dtoBuilder.build());
        }

        final List<School> listSchools = schoolRepository.findAllById(schools);

        for (School school : listSchools) {
            prepareSchoolToDTO(school, schoolsByIdDTO);
        }
        dtoBuilder.schools(schoolsByIdDTO);
        return new TestOutDTO(dtoBuilder.build());
    }

    private void prepareSchoolToDTO(School school, Map<Long, TestServiceBodySchoolDTO> schoolsByIdDTO) {
        final var schoolDTOBuilder = TestServiceBodySchoolDTO.builder();
        final List<User> teachersUBySchool = lessonRepository.uniqTeachersUBySchool(school.getId());
        final Map<Long, TestServiceBodyGroupDTO> groupsByIdDTO = new HashMap<>();

        schoolDTOBuilder.name(school.getName())
            .hteachers(getUsersForTestDTO(school.getHteachers()))
            .teachers(getUsersForTestDTO(teachersUBySchool));
        for (Group group : school.getGroups()) {
            final var groupDTOBuilder = TestServiceBodyGroupDTO.builder();
            final Map<Long, TestServiceBodyDTO> parentsById = new HashMap<>();

            groupDTOBuilder.name(group.getName())
                .kids(getUsersForTestDTO(group.getKids()));
            for (User user : group.getKids()) {
                if(!user.getRoles().containsKey(Roles.KID)) continue;

                groupDTOBuilder.parents(getUsersForTestDTO(user.getRole(Roles.KID).getParents(), parentsById));
            }
            groupsByIdDTO.put(group.getId(), groupDTOBuilder.build());
        }
        schoolDTOBuilder.groups(groupsByIdDTO);
        schoolsByIdDTO.put(school.getId(), schoolDTOBuilder.build());
    }
}
