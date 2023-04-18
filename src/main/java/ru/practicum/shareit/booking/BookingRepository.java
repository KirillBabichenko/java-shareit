package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query(" select b from Booking b join User as u on b.booker = u.id " +
            "where u.id = ?1 order by b.start desc")
    List<Booking> getAllBookingsById(Long idUser, Pageable pageable);

    List<Booking> findDByBookerAndStartBeforeAndEndAfterOrderByStartDesc(User booker,
                                                                         LocalDateTime dateTime,
                                                                         LocalDateTime dateTime2,
                                                                         Pageable pageable);

    List<Booking> findDByBookerAndStartAfterOrderByStartDesc(User booker, LocalDateTime dateTime, Pageable pageable);

    List<Booking> findDByBookerAndEndBeforeOrderByStartDesc(User booker, LocalDateTime dateTime, Pageable pageable);

    List<Booking> findDByBookerAndStatusOrderByStartDesc(User booker, Status status, Pageable pageable);

    List<Booking> findDByItemInOrderByStartDesc(List<Item> items, Pageable pageable);

    List<Booking> findDByItemInAndStartBeforeAndEndAfterOrderByStartDesc(List<Item> items,
                                                                         LocalDateTime dateTime,
                                                                         LocalDateTime dateTime2,
                                                                         Pageable pageable);

    List<Booking> findDByItemInAndStartAfterOrderByStartDesc(List<Item> items, LocalDateTime dateTime, Pageable pageable);

    List<Booking> findDByItemInAndEndBeforeOrderByStartDesc(List<Item> items, LocalDateTime dateTime, Pageable pageable);

    List<Booking> findDByItemInAndStatusOrderByStartDesc(List<Item> items, Status status, Pageable pageable);

    List<Booking> findDByItemAndStatusAndStartBeforeOrderByEndDesc(Item item, Status status, LocalDateTime dateTime);

    List<Booking> findDByItemAndStatusAndStartAfterOrderByStartAsc(Item item, Status status, LocalDateTime dateTime);

    @Query("select b from Booking as b join User as u on b.booker = u.id " +
            "where b.item = ?1 and b.status = ?2 and u.id = ?3 and b.end < ?4")
    List<Booking> findBookingsByItem(Item item, Status status, Long idUser, LocalDateTime dateTime);

}
