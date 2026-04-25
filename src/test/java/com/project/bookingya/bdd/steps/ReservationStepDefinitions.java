package com.project.bookingya.bdd.steps;

import com.project.bookingya.dtos.ReservationDto;
import com.project.bookingya.entities.GuestEntity;
import com.project.bookingya.entities.ReservationEntity;
import com.project.bookingya.entities.RoomEntity;
import com.project.bookingya.exceptions.EntityNotExistsException;
import com.project.bookingya.models.Reservation;
import com.project.bookingya.repositories.IGuestRepository;
import com.project.bookingya.repositories.IReservationRepository;
import com.project.bookingya.repositories.IRoomRepository;
import com.project.bookingya.services.ReservationService;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ReservationStepDefinitions {

    private IReservationRepository reservationRepository;
    private IRoomRepository roomRepository;
    private IGuestRepository guestRepository;
    private ModelMapper modelMapper;
    private ReservationService reservationService;

    private UUID roomId;
    private UUID guestId;
    private RoomEntity testRoom;
    private GuestEntity testGuest;
    private ReservationEntity savedEntity;
    private Reservation result;
    private List<Reservation> resultList;
    private Exception thrownException;

    @Before
    public void initMocks() {
        reservationRepository = mock(IReservationRepository.class);
        roomRepository = mock(IRoomRepository.class);
        guestRepository = mock(IGuestRepository.class);
        modelMapper = mock(ModelMapper.class);
        reservationService = new ReservationService(
                reservationRepository, roomRepository, guestRepository, modelMapper
        );
    }

    @Given("a room with code {string} and name {string} exists in the system")
    public void aRoomExists(String code, String name) {
        roomId = UUID.randomUUID();
        testRoom = new RoomEntity();
        testRoom.setId(roomId);
        testRoom.setCode(code);
        testRoom.setName(name);
        testRoom.setCity("Bogotá");
        testRoom.setMaxGuests(2);
        testRoom.setNightlyPrice(BigDecimal.valueOf(150.00));
        testRoom.setAvailable(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
    }

    @And("a guest with identification {string} and name {string} exists in the system")
    public void aGuestExists(String identification, String name) {
        guestId = UUID.randomUUID();
        testGuest = new GuestEntity();
        testGuest.setId(guestId);
        testGuest.setIdentification(identification);
        testGuest.setName(name);
        testGuest.setEmail("guest@example.com");
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(testGuest));
    }

    @When("I create a reservation for the room with check-in in {int} day and check-out in {int} days with {int} guests")
    public void iCreateAReservation(int checkInDays, int checkOutDays, int guestsCount) {
        ReservationDto dto = buildDto(checkInDays, checkOutDays, guestsCount);
        savedEntity = buildEntity(dto);

        when(reservationRepository.existsOverlappingReservationForRoom(
                eq(roomId), any(LocalDateTime.class), any(LocalDateTime.class), eq(null)))
                .thenReturn(false);
        when(reservationRepository.existsOverlappingReservationForGuest(
                eq(guestId), any(LocalDateTime.class), any(LocalDateTime.class), eq(null)))
                .thenReturn(false);
        when(modelMapper.map(any(ReservationDto.class), eq(ReservationEntity.class))).thenReturn(savedEntity);
        when(reservationRepository.saveAndFlush(any(ReservationEntity.class))).thenReturn(savedEntity);

        Reservation expected = new Reservation();
        expected.setId(savedEntity.getId());
        expected.setGuestsCount(guestsCount);
        when(modelMapper.map(savedEntity, Reservation.class)).thenReturn(expected);

        result = reservationService.create(dto);
    }

    @Given("a reservation has been created for the room with check-in in {int} day and check-out in {int} days")
    public void aReservationHasBeenCreated(int checkInDays, int checkOutDays) {
        savedEntity = buildEntity(buildDto(checkInDays, checkOutDays, 2));
        when(reservationRepository.findById(savedEntity.getId())).thenReturn(Optional.of(savedEntity));

        Reservation expected = new Reservation();
        expected.setId(savedEntity.getId());
        expected.setGuestsCount(2);
        when(modelMapper.map(savedEntity, Reservation.class)).thenReturn(expected);
    }

    @When("I request the reservation by its ID")
    public void iRequestTheReservationById() {
        result = reservationService.getById(savedEntity.getId());
    }

    @When("I request all reservations")
    public void iRequestAllReservations() {
        when(reservationRepository.findAll()).thenReturn(List.of(savedEntity));
        Reservation expected = new Reservation();
        expected.setId(savedEntity.getId());
        expected.setGuestsCount(2);
        when(modelMapper.map(eq(List.of(savedEntity)), any(java.lang.reflect.Type.class)))
                .thenReturn(List.of(expected));
        resultList = reservationService.getAll();
    }

    @When("I update the reservation with check-in in {int} days and check-out in {int} days with {int} guest")
    public void iUpdateTheReservation(int checkInDays, int checkOutDays, int guestsCount) {
        ReservationDto updateDto = buildDto(checkInDays, checkOutDays, guestsCount);
        UUID reservationId = savedEntity.getId();

        when(reservationRepository.existsOverlappingReservationForRoom(
                eq(roomId), any(LocalDateTime.class), any(LocalDateTime.class), eq(reservationId)))
                .thenReturn(false);
        when(reservationRepository.existsOverlappingReservationForGuest(
                eq(guestId), any(LocalDateTime.class), any(LocalDateTime.class), eq(reservationId)))
                .thenReturn(false);
        when(reservationRepository.saveAndFlush(any(ReservationEntity.class))).thenReturn(savedEntity);

        Reservation updated = new Reservation();
        updated.setId(reservationId);
        updated.setGuestsCount(guestsCount);
        when(modelMapper.map(savedEntity, Reservation.class)).thenReturn(updated);

        result = reservationService.update(updateDto, reservationId);
    }

    @When("I delete the reservation")
    public void iDeleteTheReservation() {
        reservationService.delete(savedEntity.getId());
    }

    @When("I try to delete a reservation with a random non-existent ID")
    public void iTryToDeleteNonExistentReservation() {
        UUID nonExistentId = UUID.randomUUID();
        when(reservationRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        try {
            reservationService.delete(nonExistentId);
        } catch (EntityNotExistsException e) {
            thrownException = e;
        }
    }

    @Then("the reservation should be created successfully")
    public void theReservationShouldBeCreatedSuccessfully() {
        assertNotNull(result);
        assertNotNull(result.getId());
    }

    @And("the reservation should have {int} guests")
    public void theReservationShouldHaveGuests(int expectedCount) {
        assertEquals(expectedCount, result.getGuestsCount());
    }

    @Then("the reservation should be returned successfully")
    public void theReservationShouldBeReturnedSuccessfully() {
        assertNotNull(result);
        assertEquals(savedEntity.getId(), result.getId());
    }

    @Then("the response should contain at least 1 reservation")
    public void theResponseShouldContainAtLeastOneReservation() {
        assertNotNull(resultList);
        assertFalse(resultList.isEmpty());
    }

    @Then("the reservation should be updated successfully")
    public void theReservationShouldBeUpdatedSuccessfully() {
        assertNotNull(result);
        assertEquals(savedEntity.getId(), result.getId());
    }

    @Then("the reservation should be deleted successfully")
    public void theReservationShouldBeDeletedSuccessfully() {
        verify(reservationRepository, times(1)).delete(savedEntity);
        verify(reservationRepository, times(1)).flush();
    }

    @Then("the system should throw a not found exception")
    public void theSystemShouldThrowNotFoundException() {
        assertNotNull(thrownException);
        assertInstanceOf(EntityNotExistsException.class, thrownException);
    }

    private ReservationDto buildDto(int checkInDays, int checkOutDays, int guestsCount) {
        ReservationDto dto = new ReservationDto();
        dto.setRoomId(roomId);
        dto.setGuestId(guestId);
        dto.setCheckIn(LocalDateTime.now().plusDays(checkInDays));
        dto.setCheckOut(LocalDateTime.now().plusDays(checkOutDays));
        dto.setGuestsCount(guestsCount);
        return dto;
    }

    private ReservationEntity buildEntity(ReservationDto dto) {
        ReservationEntity entity = new ReservationEntity();
        entity.setId(UUID.randomUUID());
        entity.setRoomId(dto.getRoomId());
        entity.setGuestId(dto.getGuestId());
        entity.setCheckIn(dto.getCheckIn());
        entity.setCheckOut(dto.getCheckOut());
        entity.setGuestsCount(dto.getGuestsCount());
        return entity;
    }
}
