package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;


    @BeforeEach
    public void setUpPerTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test // Mockito test N1
    public void processExitingVehicleTest(){
        // Arrange
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1); // Mocks getNbTicket() to simulate a regular vehicle

        // Act
        parkingService.processExitingVehicle(); // methode to be tested

        // Assert
        verify(ticketDAO).getNbTicket("ABCDEF"); // Verify that getNbTicket() was called with the vehicle registration number
        verify(ticketDAO).updateTicket(any(Ticket.class)); // Verify that updateTicket() was called on the ticket
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));  // Verify that updateParking() was called once on the parking spot
    }

    @Test // Mockito test N2
    public void testProcessIncomingVehicle(){
        // Arrange
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        lenient().when(parkingService.getNextParkingNumberIfAvailable()).thenReturn(parkingSpot);
        when(parkingService.getVehicleType()).thenReturn(ParkingType.CAR);
        when(inputReaderUtil.readSelection()).thenReturn(1);

        // Act
        parkingService.processIncomingVehicle();

        // Assert
        verify(parkingService, Mockito.times(1)).getNextParkingNumberIfAvailable();
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
    }

}
