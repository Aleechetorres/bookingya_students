package com.project.bookingya.services;

import com.project.bookingya.dtos.ReservationDto;
import com.project.bookingya.entities.GuestEntity;
import com.project.bookingya.entities.ReservationEntity;
import com.project.bookingya.entities.RoomEntity;
import com.project.bookingya.models.Reservation;
import com.project.bookingya.repositories.IGuestRepository;
import com.project.bookingya.repositories.IReservationRepository;
import com.project.bookingya.repositories.IRoomRepository;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.project.bookingya.exceptions.EntityNotExistsException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith({SerenityJUnit5Extension.class, MockitoExtension.class})
@DisplayName("Reservation Service - CRUD Tests")
class ReservationServiceTest {

    @Mock
    private IReservationRepository reservationRepository;

    @Mock
    private IRoomRepository roomRepository;

    @Mock
    private IGuestRepository guestRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ReservationService reservationService;

    private UUID roomId;
    private UUID guestId;
    private RoomEntity testRoom;
    private GuestEntity testGuest;
    private ReservationDto validReservationDto;
    private ReservationEntity savedReservationEntity;
    private Reservation expectedReservation;

    @BeforeEach
    void setUp() {
        roomId = UUID.randomUUID();
        guestId = UUID.randomUUID();

        testRoom = new RoomEntity();
        testRoom.setId(roomId);
        testRoom.setCode("101");
        testRoom.setName("Deluxe Room");
        testRoom.setCity("Bogotá");
        testRoom.setMaxGuests(2);
        testRoom.setNightlyPrice(BigDecimal.valueOf(150.00));
        testRoom.setAvailable(true);

        testGuest = new GuestEntity();
        testGuest.setId(guestId);
        testGuest.setIdentification("1234567890");
        testGuest.setName("John Doe");
        testGuest.setEmail("john.doe@example.com");

        validReservationDto = new ReservationDto();
        validReservationDto.setRoomId(roomId);
        validReservationDto.setGuestId(guestId);
        validReservationDto.setCheckIn(LocalDateTime.now().plusDays(1));
        validReservationDto.setCheckOut(LocalDateTime.now().plusDays(3));
        validReservationDto.setGuestsCount(2);

        savedReservationEntity = new ReservationEntity();
        savedReservationEntity.setId(UUID.randomUUID());
        savedReservationEntity.setRoomId(roomId);
        savedReservationEntity.setGuestId(guestId);
        savedReservationEntity.setCheckIn(validReservationDto.getCheckIn());
        savedReservationEntity.setCheckOut(validReservationDto.getCheckOut());
        savedReservationEntity.setGuestsCount(validReservationDto.getGuestsCount());

        expectedReservation = new Reservation();
        expectedReservation.setId(savedReservationEntity.getId());
        expectedReservation.setGuestsCount(2);
    }

    @Test
    @DisplayName("Should create a reservation successfully with valid data")
    void shouldCreateReservationSuccessfully() {
        // Given
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(testGuest));
        when(reservationRepository.existsOverlappingReservationForRoom(
                eq(roomId), any(LocalDateTime.class), any(LocalDateTime.class), eq(null)))
                .thenReturn(false);
        when(reservationRepository.existsOverlappingReservationForGuest(
                eq(guestId), any(LocalDateTime.class), any(LocalDateTime.class), eq(null)))
                .thenReturn(false);
        when(modelMapper.map(validReservationDto, ReservationEntity.class)).thenReturn(savedReservationEntity);
        when(reservationRepository.saveAndFlush(any(ReservationEntity.class))).thenReturn(savedReservationEntity);
        when(modelMapper.map(savedReservationEntity, Reservation.class)).thenReturn(expectedReservation);

        // When
        Reservation result = reservationService.create(validReservationDto);

        // Then
        assertNotNull(result);
        assertEquals(expectedReservation.getId(), result.getId());
        assertEquals(2, result.getGuestsCount());
        verify(roomRepository, times(1)).findById(roomId);
        verify(guestRepository, times(1)).findById(guestId);
        verify(reservationRepository, times(1)).saveAndFlush(any(ReservationEntity.class));
        verify(modelMapper, times(1)).map(validReservationDto, ReservationEntity.class);
        verify(modelMapper, times(1)).map(savedReservationEntity, Reservation.class);
    }

    @Test
    @DisplayName("Should retrieve a reservation by ID successfully")
    void shouldGetReservationByIdSuccessfully() {
        // Given
        UUID reservationId = savedReservationEntity.getId();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(savedReservationEntity));
        when(modelMapper.map(savedReservationEntity, Reservation.class)).thenReturn(expectedReservation);

        // When
        Reservation result = reservationService.getById(reservationId);

        // Then
        assertNotNull(result);
        assertEquals(reservationId, result.getId());
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(modelMapper, times(1)).map(savedReservationEntity, Reservation.class);
    }

    @Test
    @DisplayName("Should return all existing reservations")
    void shouldGetAllReservationsSuccessfully() {
        // Given
        ReservationEntity secondEntity = new ReservationEntity();
        secondEntity.setId(UUID.randomUUID());
        secondEntity.setRoomId(roomId);
        secondEntity.setGuestId(guestId);
        secondEntity.setGuestsCount(1);

        List<ReservationEntity> entityList = List.of(savedReservationEntity, secondEntity);

        Reservation secondReservation = new Reservation();
        secondReservation.setId(secondEntity.getId());
        secondReservation.setGuestsCount(1);

        when(reservationRepository.findAll()).thenReturn(entityList);
        when(modelMapper.map(eq(entityList), any(java.lang.reflect.Type.class)))
                .thenReturn(List.of(expectedReservation, secondReservation));

        // When
        List<Reservation> result = reservationService.getAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(reservationRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should update a reservation successfully")
    void shouldUpdateReservationSuccessfully() {
        // Given
        UUID reservationId = savedReservationEntity.getId();

        ReservationDto updateDto = new ReservationDto();
        updateDto.setRoomId(roomId);
        updateDto.setGuestId(guestId);
        updateDto.setCheckIn(LocalDateTime.now().plusDays(5));
        updateDto.setCheckOut(LocalDateTime.now().plusDays(7));
        updateDto.setGuestsCount(1);

        Reservation updatedReservation = new Reservation();
        updatedReservation.setId(reservationId);
        updatedReservation.setGuestsCount(1);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(savedReservationEntity));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(testGuest));
        when(reservationRepository.existsOverlappingReservationForRoom(
                eq(roomId), any(LocalDateTime.class), any(LocalDateTime.class), eq(reservationId)))
                .thenReturn(false);
        when(reservationRepository.existsOverlappingReservationForGuest(
                eq(guestId), any(LocalDateTime.class), any(LocalDateTime.class), eq(reservationId)))
                .thenReturn(false);
        when(reservationRepository.saveAndFlush(any(ReservationEntity.class))).thenReturn(savedReservationEntity);
        when(modelMapper.map(savedReservationEntity, Reservation.class)).thenReturn(updatedReservation);

        // When
        Reservation result = reservationService.update(updateDto, reservationId);

        // Then
        assertNotNull(result);
        assertEquals(reservationId, result.getId());
        assertEquals(1, result.getGuestsCount());
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(reservationRepository, times(1)).saveAndFlush(any(ReservationEntity.class));
    }

    @Test
    @DisplayName("Should delete a reservation successfully")
    void shouldDeleteReservationSuccessfully() {
        // Given
        UUID reservationId = savedReservationEntity.getId();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(savedReservationEntity));

        // When
        reservationService.delete(reservationId);

        // Then
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(reservationRepository, times(1)).delete(savedReservationEntity);
        verify(reservationRepository, times(1)).flush();
    }

    @Test
    @DisplayName("Should throw exception when deleting a non-existent reservation")
    void shouldThrowExceptionWhenDeletingNonExistentReservation() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(reservationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(EntityNotExistsException.class, () -> reservationService.delete(nonExistentId));
        verify(reservationRepository, times(1)).findById(nonExistentId);
        verify(reservationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw exception when reservation ID does not exist")
    void shouldThrowExceptionWhenReservationNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(reservationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(EntityNotExistsException.class, () -> reservationService.getById(nonExistentId));
        verify(reservationRepository, times(1)).findById(nonExistentId);
    }
}
