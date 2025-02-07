package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    public static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    public void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    public void testParkingACar(){
        // We initialise the parking service
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // We process an incoming vehicle
        parkingService.processIncomingVehicle();

        // We ensure that the ticket exist means the ticket is saved in database
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket);

        // We ensure the next parkingSpot id doesn't match with the current parkingSpot id
        int idNextAvailableSpot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        assertNotEquals(ticket.getParkingSpot().getId(), idNextAvailableSpot);
    }

    @Test
    public void testParkingLotExit(){

        // We simulate the vehicle entry to the parking
        testParkingACar();

        // We initialise the parking service
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // We process an exiting vehicle
        parkingService.processExitingVehicle();

        // We ensure a ticket is generated after exiting
        Ticket ticketAfterExit = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticketAfterExit);
        ticketDAO.saveTicket(ticketAfterExit);

        // We ensure a checkout time is generated
        assertTrue(ticketAfterExit.getOutTime().getTime()>0, "The checkout time should be calculated");

        // We ensure a fare is generated
        assertTrue(ticketAfterExit.getPrice() >= 0, "The fare should be generated");
    }

    @Test
    public void testParkingLotExitRecurringUser(){

        // Initializing of the parking service
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // First entry and exit to register the user as a recurring customer
        parkingService.processIncomingVehicle();
        parkingService.processExitingVehicle();

        // We verify the user is now recurring
        int nbTickets = ticketDAO.getNbTicket("ABCDEF");
        assertTrue(nbTickets > 0, "User should be recognized as recurring");

        // Second entry
        parkingService.processIncomingVehicle();

        // Update of the entry time to simulate 3 hours of parking
        Ticket ticketBeforeExit = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticketBeforeExit, "Ticket should exist after entry");
        ticketBeforeExit.setInTime(new Date(System.currentTimeMillis() - (3 * 60 * 60 * 1000))); // 3 hours ago

        // We make sure the inTime is updated, very important
        ticketDAO.updateTicket(ticketBeforeExit);
        ticketDAO.saveTicket(ticketBeforeExit);

        // Process vehicle exit
        parkingService.processExitingVehicle();

        // Retrieve the ticket and verify the fare
        Ticket ticketAfterExit = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticketAfterExit, "The ticket should exist after exit");
        assertNotNull(ticketAfterExit.getOutTime(), "Out time should be set after exit");

        // Checking parking time
        System.out.println("CheckIn time is : " + ticketAfterExit.getInTime());
        System.out.println("-----------------------------------------------------");
        System.out.println("CheckOut time is : " + ticketAfterExit.getOutTime());

        // We ensure a fare is generated
        assertTrue(ticketAfterExit.getPrice() > 0, "Fare should be greater than 0");

        // We verify that the 5% discount is applied
        double expectedPrice = 3 * Fare.CAR_RATE_PER_HOUR * 0.95;
        assertEquals(expectedPrice, ticketAfterExit.getPrice(), 0.01, "Fare should be reduced by 5% for a recurring user");
    }
}

