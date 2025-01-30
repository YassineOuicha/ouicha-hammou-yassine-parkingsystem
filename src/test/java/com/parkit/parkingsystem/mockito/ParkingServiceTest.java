package com.parkit.parkingsystem.mockito;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Date;

import static com.parkit.parkingsystem.constants.ParkingType.CAR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private ParkingService parkingService;

    @Mock
    private InputReaderUtil inputReaderUtil;
    @Mock
    private ParkingSpotDAO parkingSpotDAO;
    @Mock
    private TicketDAO ticketDAO;

    @BeforeEach
    public void setUpPerTest() {
        try {
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

            lenient().when(inputReaderUtil.readSelection()).thenReturn(1); // pour CAR
            lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");

            lenient().when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            lenient().when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            lenient().when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test // Mockito test N1
    public void processExitingVehicleTest(){
        // Arrange
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1);

        // Act
        parkingService.processExitingVehicle();

        // Assert
        verify(ticketDAO).getNbTicket("ABCDEF");
        verify(ticketDAO).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));

    }

    @Test // Mockito test N2
    public void testProcessIncomingVehicle() {
        // Arrange
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(1);

        // Act
        parkingService.processIncomingVehicle();

        // Assert
        verify(parkingSpotDAO, times(1)).updateParking(parkingSpot);
        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
    }

    @Test // Mockito test N3
    public void processExitingVehicleTestUnableUpdate(){

        // Arrange
        ParkingSpot parkingSpot = new ParkingSpot(1, CAR,false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        when(ticketDAO.getTicket("ABCDEF")).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        // Act
        parkingService.processExitingVehicle();

        // Assert
        verify(ticketDAO, Mockito.times(1)).updateTicket(ticket);
    }

    @Test // Mockito test N4
    public void testGetNextParkingNumberIfAvailable(){
        // Arrange
        ParkingSpot expectedParkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        // Act
        ParkingSpot generatedParkingSpot = parkingService.getNextParkingNumberIfAvailable();

        // Assert
        assertNotNull(generatedParkingSpot);
        assertEquals(expectedParkingSpot.getId(), generatedParkingSpot.getId());
        assertEquals(expectedParkingSpot.getParkingType(), generatedParkingSpot.getParkingType());
        assertTrue(generatedParkingSpot.isAvailable());
        verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(ParkingType.CAR);
    }

    @Test // Mockito test N5
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound(){

        // Arrange
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(-1);

        // Act
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        // Assert
        assertNull(parkingSpot);

    }

    @Test // Mockito test N6
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument(){

        // Arrange
        lenient().when(inputReaderUtil.readSelection()).thenReturn(-1);

        // Act
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        // Assert
        assertNull(parkingSpot);
    }
}
