package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> createItem(Long idUser, ItemDto itemDto) {
        return post("", idUser, itemDto);
    }

    public ResponseEntity<Object> updateItem(Long idUser, Long id, ItemDto itemDto) {
        return patch("/" + id, idUser, itemDto);
    }

    public ResponseEntity<Object> getItemById(Long idUser, Long id) {
        return get("/" + id, idUser);
    }

    public ResponseEntity<Object> getAllUserItems(Long idUser, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", idUser, parameters);
    }


    public ResponseEntity<Object> findItems(Long idUser, String text, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return get("/search?text={text}&from={from}&size={size}", idUser, parameters);
    }

    public ResponseEntity<Object> addComment(Long idUser, Long id, CommentDto comment) {
        return post("/" + id + "/comment", idUser, comment);
    }

}
