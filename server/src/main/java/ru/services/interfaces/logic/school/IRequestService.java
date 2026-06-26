package ru.services.interfaces.logic.school;

import ru.data.DAO.school.Request;
import ru.data.DTO.controller.school.request.RequestBodyDTO;
import ru.data.DTO.controller.school.request.RequestInnerDTO;
import ru.data.DTO.controller.school.request.RequestOutDTO;

import java.util.List;
import java.util.Map;

public interface IRequestService {
    RequestOutDTO addRequest(RequestInnerDTO body);

    RequestOutDTO deleteRequest(Request request);

    RequestOutDTO changeTitle(String title, Request request);

    RequestOutDTO changeDate(String date, Request request);

    RequestOutDTO changeText(String text, Request request);

    Map<Long, RequestBodyDTO> prepareRequests(List<Request> requests);
}
