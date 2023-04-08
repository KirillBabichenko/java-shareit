package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.List;

public interface BookingService {

    BookingDto createBooking(Long userId, BookingShortDto bookingDto);

    BookingDto approveBooking(Long idUser, Long idBooking, Boolean approved);

    BookingDto getBookingById(Long idUser, Long idBooking);

    List<BookingDto> getAllBookings(Long idUser, String state);

    List<BookingDto> getAllOwnerBookings(Long idUser, String state);

}
