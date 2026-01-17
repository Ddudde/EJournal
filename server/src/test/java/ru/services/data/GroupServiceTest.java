package ru.services.data;

import com.google.gson.Gson;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.data.DAO.auth.User;
import ru.data.DAO.school.School;
import ru.data.DTO.service.data.GroupServiceDTO;
import ru.services.interfaces.db.IDBService;
import utils.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {
    protected static final Gson gson = new Gson();
    private final TestUtils testUtils = new TestUtils();

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IDBService dbService;

    @InjectMocks
    private GroupService groupService;

    /** RU: подаёт пустой список и должен получить условно пустой JSON */
    @Test @Tag("groupsBySchoolOfUser")
    void groupsBySchoolOfUser_whenEmpty() {
        final GroupServiceDTO dto = groupService.groupsBySchoolOfUser(null);

        assertEquals("null", gson.toJson(dto));
    }

    /** RU: подаёт список из случайных групп должен вернуть заполненный JSON */
    @Test @Tag("groupsBySchoolOfUser")
    void groupsBySchoolOfUser_whenGood(@Mock User user, @Mock(answer = Answers.RETURNS_DEEP_STUBS) School school) {
        when(dbService.getFirstRole(anyMap()).getYO()).thenReturn(school);
        when(school.getGroups()).thenReturn(testUtils.groups);

        final GroupServiceDTO dto = groupService.groupsBySchoolOfUser(user);

        assertEquals(2323L, dto.firstG());
        assertEquals("{\"firstG\":2323,\"bodyG\":{\"3456\":\"1Б\",\"4354\":\"1В\",\"2323\":\"1А\"}}",
            gson.toJson(dto));
    }
}