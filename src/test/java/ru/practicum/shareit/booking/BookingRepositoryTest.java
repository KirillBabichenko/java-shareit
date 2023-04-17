package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.ItemRepositoryJpa;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepositoryJpa;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
public class BookingRepositoryTest {
    @Autowired
    private TestEntityManager em;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ItemRepositoryJpa itemRepositoryJpa;
    @Autowired
    private UserRepositoryJpa userRepositoryJpa;
    private Item item;
    private Booking booking;
    private User user;

    @BeforeEach
    public void setUp() {
        item = Item.builder()
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .build();

        user = User.builder()
                .email("test@test.com")
                .name("testName")
                .build();

        booking = Booking.builder()
                .start(LocalDateTime.now().plusNanos(1))
                .end(LocalDateTime.now().plusNanos(2))
                .item(item)
                .booker(user)
                .status(Status.WAITING)
                .build();
    }

    @Test
    public void contextLoads() {
        Assertions.assertNotNull(em);
    }

    @Test
    void verifyBootstrappingByPersistingAnItem() {
        Assertions.assertNull(booking.getId());
        em.persist(user);
        em.persist(item);
        em.persist(booking);
        Assertions.assertNotNull(booking.getId());
    }

    @Test
    void getAllBookingsByIdTest() {
        Pageable pageable = PageRequest.of(0, 2);
        User userFromDB = userRepositoryJpa.save(user);
        itemRepositoryJpa.save(item);
        bookingRepository.save(booking);

        Page<Booking> pageBookings = bookingRepository.getAllBookingsById(userFromDB.getId(), pageable);
        Booking bookingFromDB = pageBookings.getContent().get(0);

        Assertions.assertNotNull(pageBookings);
        Assertions.assertEquals(1, pageBookings.getTotalPages());
        Assertions.assertEquals(1, pageBookings.getTotalElements());
        Assertions.assertEquals(booking.getStart(), bookingFromDB.getStart());
        Assertions.assertEquals(booking.getEnd(), bookingFromDB.getEnd());
        Assertions.assertEquals(booking.getItem(), bookingFromDB.getItem());
        Assertions.assertEquals(booking.getBooker(), bookingFromDB.getBooker());
        Assertions.assertEquals(booking.getStatus(), bookingFromDB.getStatus());
    }

    @Test
    void findBookingsByItemTest() {
        User userFromDB = userRepositoryJpa.save(user);
        Item itemFromDB = itemRepositoryJpa.save(item);
        Booking bookingSaved = bookingRepository.save(booking);

        List<Booking> listBookings = bookingRepository.findBookingsByItem(
                itemFromDB, bookingSaved.getStatus(), userFromDB.getId(), LocalDateTime.now().plusNanos(3));
        Booking bookingFromDB = listBookings.get(0);

        Assertions.assertNotNull(listBookings);
        Assertions.assertEquals(1, listBookings.size());
        Assertions.assertEquals(booking.getStart(), bookingFromDB.getStart());
        Assertions.assertEquals(booking.getEnd(), bookingFromDB.getEnd());
        Assertions.assertEquals(booking.getItem(), bookingFromDB.getItem());
        Assertions.assertEquals(booking.getBooker(), bookingFromDB.getBooker());
        Assertions.assertEquals(booking.getStatus(), bookingFromDB.getStatus());
    }

}
