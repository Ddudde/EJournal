package ru.services.logic.school;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.controllers.school.RequestController;
import ru.data.DAO.school.Request;
import ru.data.DTO.controller.school.request.RequestBodyDTO;
import ru.data.DTO.controller.school.request.RequestInnerDTO;
import ru.data.DTO.controller.school.request.RequestOutDTO;
import ru.data.reps.school.RequestRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** RU: сервис для контроллера
 * @see RequestController */
@Service
@RequiredArgsConstructor
public class RequestService implements IRequestService {
    private final RequestRepository requestRepository;

    @Override
    public RequestOutDTO addRequest(RequestInnerDTO body) {
        final RequestOutDTO.RequestOutDTOBuilder dtoBuilder = RequestOutDTO.builder();
        final RequestBodyDTO.RequestBodyDTOBuilder bodyDTOBuilder = RequestBodyDTO.builder();

        final Request request = new Request(body.email, body.date, body.fio);
        requestRepository.saveAndFlush(request);
        dtoBuilder.id(request.getId());
        bodyDTOBuilder.title(request.getEmail())
            .date(request.getDate())
            .text(request.getFio());
        dtoBuilder.body(bodyDTOBuilder.build());
        return dtoBuilder.build();
    }

    @Override
    public RequestOutDTO deleteRequest(Request request) {
        final RequestOutDTO.RequestOutDTOBuilder dtoBuilder = RequestOutDTO.builder();

        requestRepository.delete(request);
        dtoBuilder.id(request.getId());
        return dtoBuilder.build();
    }

    @Override
    public RequestOutDTO changeTitle(String title, Request request) {
        final RequestOutDTO.RequestOutDTOBuilder dtoBuilder = RequestOutDTO.builder();

        request.setEmail(title);
        requestRepository.saveAndFlush(request);

        dtoBuilder.id(request.getId())
            .title(request.getEmail());
        return dtoBuilder.build();
    }

    @Override
    public RequestOutDTO changeDate(String date, Request request) {
        final RequestOutDTO.RequestOutDTOBuilder dtoBuilder = RequestOutDTO.builder();

        request.setDate(date);
        requestRepository.saveAndFlush(request);

        dtoBuilder.id(request.getId())
            .date(request.getDate());
        return dtoBuilder.build();
    }

    @Override
    public RequestOutDTO changeText(String text, Request request) {
        final RequestOutDTO.RequestOutDTOBuilder dtoBuilder = RequestOutDTO.builder();

        request.setFio(text);
        requestRepository.saveAndFlush(request);

        dtoBuilder.id(request.getId())
            .text(request.getFio());
        return dtoBuilder.build();
    }

    @Override
    public Map<Long, RequestBodyDTO> prepareRequests(List<Request> requests) {
        final Map<Long, RequestBodyDTO> requestById = new HashMap<>();

        for(Request reqR : requests){
            final RequestBodyDTO.RequestBodyDTOBuilder dtoBuilder = RequestBodyDTO.builder();
            dtoBuilder.title(reqR.getEmail())
                .date(reqR.getDate())
                .text(reqR.getFio());
            requestById.put(reqR.getId(), dtoBuilder.build());
        }
        return requestById;
    }
}
