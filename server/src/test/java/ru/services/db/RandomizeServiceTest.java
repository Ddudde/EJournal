package ru.services.db;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.data.DAO.school.Period;
import ru.data.reps.ContactsRepository;
import ru.data.reps.NewsRepository;
import ru.data.reps.SystRepository;
import ru.data.reps.auth.RoleRepository;
import ru.data.reps.auth.SettingUserRepository;
import ru.data.reps.auth.UserRepository;
import ru.data.reps.school.*;
import ru.services.logic.school.analytics.PeriodService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RandomizeServiceTest {
    private final DBService dbService = mock(DBService.class, Answers.RETURNS_DEEP_STUBS);

    @Mock
    private SettingUserRepository settingUserRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private ContactsRepository contactsRepository;

    @Mock
    private SystRepository systRepository;

    @Mock
    private DayRepository dayRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private MarkRepository markRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private PeriodRepository periodRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PeriodService periodService;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(8);

    @InjectMocks
    private RandomizeService randomizeService;

    @Test
    void removeRandomData_whenGood() {
        final Period period = new Period("Четвертина", "16.01.26", "16.03.26");
        when(periodService.getActualPeriodBySchool(any())).thenReturn(period);

        when(newsRepository.saveAllAndFlush(any()))
            .then(invocation -> new ArrayList<>((List)invocation.getArguments()[0]));
        when(periodRepository.saveAllAndFlush(any()))
            .then(invocation -> new ArrayList<>((List)invocation.getArguments()[0]));
        when(dbService.createSyst(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(systRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(settingUserRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(roleRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(userRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(newsRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(contactsRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(schoolRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(groupRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(lessonRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(markRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dayRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);

        randomizeService.createRandomData();
        randomizeService.removeRandomData();

        assertNull(randomizeService.getSyst());
    }

    @Test
    void createRandomData_whenGood() {
        final Period period = new Period("Четвертина", "16.01.26", "16.03.26");
        when(periodService.getActualPeriodBySchool(any())).thenReturn(period);

        when(newsRepository.saveAllAndFlush(any()))
            .then(invocation -> new ArrayList<>((List)invocation.getArguments()[0]));
        when(periodRepository.saveAllAndFlush(any()))
            .then(invocation -> new ArrayList<>((List)invocation.getArguments()[0]));
        when(dbService.createSyst(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(systRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(settingUserRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(roleRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(userRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(newsRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(contactsRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(schoolRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(groupRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(lessonRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(markRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);
        when(dayRepository.saveAndFlush(any()))
            .then(invocation -> invocation.getArguments()[0]);

        randomizeService.createRandomData();

        assertNotNull(randomizeService.getSyst());
    }
}