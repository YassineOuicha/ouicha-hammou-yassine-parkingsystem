package com.parkit.parkingsystem.integration;

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
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

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

        // We ensure a ticket is generated after entry
        Ticket ticketBeforeExit = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticketBeforeExit);

        // We initialise the parking service
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // We process an exiting vehicle
        parkingService.processExitingVehicle();

        // We ensure a ticket is generated after exiting
        Ticket ticketAfterExit = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticketAfterExit);

        // We ensure a checkout time is generated
        assertTrue(ticketAfterExit.getOutTime().getTime()>0, "The checkout time should be calculated");

        // We ensure a fare is generated
        assertTrue(ticketAfterExit.getPrice() >= 0, "The fare should be generated");
    }
}
