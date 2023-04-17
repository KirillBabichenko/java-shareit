package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

import static ru.practicum.shareit.user.Variables.ID_SHARER;

@Validated
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto createBooking(@RequestHeader(ID_SHARER) Long idUser,
                                    @Valid @RequestBody BookingShortDto bookingDto) {
        return bookingService.createBooking(idUser, bookingDto);
    }

    @PatchMapping("/{idBooking}")
    public BookingDto approveBooking(@RequestHeader(ID_SHARER) Long idUser,
                                     @PathVariable Long idBooking,
                                     @RequestParam(name = "approved") Boolean approved) {
        return bookingService.approveBooking(idUser, idBooking, approved);
    }

    @GetMapping("/{idBooking}")
    public BookingDto getBookingById(@RequestHeader(ID_SHARER) Long idUser,
                                     @PathVariable Long idBooking) {
        return bookingService.getBookingById(idUser, idBooking);
    }

    @GetMapping
    public List<BookingDto> getAllBookings(@RequestHeader(ID_SHARER) Long idUser,
                                           @RequestParam(required = false, defaultValue = "ALL") String state,
                                           @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                           @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size) {
        return bookingService.getAllBookings(idUser, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllOwnerBookings(@RequestHeader(ID_SHARER) Long idUser,
                                                @RequestParam(required = false, defaultValue = "ALL") String state,
                                                @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                                @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size) {
        return bookingService.getAllOwnerBookings(idUser, state, from, size);
    }
}
