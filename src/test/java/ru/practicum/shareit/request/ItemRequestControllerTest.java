package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private RequestService requestService;
    @Autowired
    private MockMvc mvc;
    private ItemRequestDto itemRequestDto;
    private ItemRequestDto secondRequestDto;
    private List<ItemRequestDto> requests;

    @BeforeEach
    void setUp() {
        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужна новая вещь")
                .created(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .requestorId(1L)
                .build();
        secondRequestDto = ItemRequestDto.builder()
                .id(2L)
                .description("Второй запрос на нужную вещь")
                .created(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .requestorId(1L)
                .build();

        requests = List.of(itemRequestDto, secondRequestDto);
    }

    @Test
    void createRequestTest() throws Exception {
        when(requestService.createRequest(anyLong(), any()))
                .thenReturn(itemRequestDto);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequestDto.getCreated().toString())))
                .andExpect(jsonPath("$.requestorId", is(itemRequestDto.getRequestorId()), Long.class));
    }

    @Test
    void getRequestByIdTest() throws Exception {
        when(requestService.getRequestById(anyLong(), anyLong()))
                .thenReturn(itemRequestDto);

        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequestDto.getCreated().toString())))
                .andExpect(jsonPath("$.requestorId", is(itemRequestDto.getRequestorId()), Long.class));
    }

    @Test
    void getAllRequestsByIdTest() throws Exception {
        when(requestService.getAllRequestsById(anyLong()))
                .thenReturn(requests);

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[1].id", is(secondRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.[1].description", is(secondRequestDto.getDescription())))
                .andExpect(jsonPath("$.[1].created", is(secondRequestDto.getCreated().toString())))
                .andExpect(jsonPath("$.[1].requestorId", is(secondRequestDto.getRequestorId()), Long.class));
    }

    @Test
    void getAllRequestsTest() throws Exception {
        when(requestService.getAllRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(requests);

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[1].id", is(secondRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.[1].description", is(secondRequestDto.getDescription())))
                .andExpect(jsonPath("$.[1].created", is(secondRequestDto.getCreated().toString())))
                .andExpect(jsonPath("$.[1].requestorId", is(secondRequestDto.getRequestorId()), Long.class));
    }
}
