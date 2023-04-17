package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;

@DataJpaTest
public class ItemRepositoryTest {
    @Autowired
    private TestEntityManager em;
    @Autowired
    private ItemRepositoryJpa itemRepositoryJpa;
    private Item item;

    @BeforeEach
    public void setUp() {
        item = Item.builder()
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .build();
    }

    @Test
    public void contextLoads() {
        Assertions.assertNotNull(em);
    }

    @Test
    void verifyBootstrappingByPersistingAnItem() {
        Assertions.assertNull(item.getId());
        em.persist(item);
        Assertions.assertNotNull(item.getId());
    }

    @Test
    void searchTest() {
        String searchText = "дрель";
        Pageable pageable = PageRequest.of(0, 2);
        itemRepositoryJpa.save(item);

        Page<Item> pageItems = itemRepositoryJpa.search(searchText, pageable);
        Item itemFromDB = pageItems.getContent().get(0);

        Assertions.assertNotNull(pageItems);
        Assertions.assertEquals(1, pageItems.getTotalPages());
        Assertions.assertEquals(1, pageItems.getTotalElements());
        Assertions.assertEquals(item.getName(), itemFromDB.getName());
        Assertions.assertEquals(item.getDescription(), itemFromDB.getDescription());
        Assertions.assertEquals(item.getAvailable(), itemFromDB.getAvailable());
    }

}
