package com.parkit.parkingsystem.mock;


import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketDAOTest {

    private TicketDAO ticketDAO;
    @Mock
    private DataBaseConfig dataBaseConfig;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @BeforeEach
    public void setUp() throws Exception {

        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseConfig;

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    public void testSaveTicket() throws Exception {
        // Arrange
        when(preparedStatement.execute()).thenReturn(true);

        // we need to create a ticket to save it later
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setInTime(new Date());
        ticket.setPrice(5 * Fare.CAR_RATE_PER_HOUR);
        ticket.setVehicleRegNumber("ABCDEF");

        // Act
        boolean result =  ticketDAO.saveTicket(ticket);

        // Assert
        assertFalse(result);
        verify(preparedStatement, times(1)).execute();

    }

    @Test
    public void testGetTicket() throws Exception {
        // Arrange
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);
        when(resultSet.getString(6)).thenReturn("CAR");

        // Act
        Ticket ticket = ticketDAO.getTicket("ABCDEF");

        // Assert
        assertNotNull(ticket);
        assertEquals("ABCDEF", ticket.getVehicleRegNumber());
        assertEquals(ParkingType.CAR, ticket.getParkingSpot().getParkingType());
        assertEquals(1, ticket.getParkingSpot().getId());

    }

    @Test
    public void testUpdateTicket() throws Exception {

        // Arrange
        Ticket ticket = new Ticket();
        ticket.setId(1);
        ticket.setPrice(10 * Fare.CAR_RATE_PER_HOUR);
        ticket.setOutTime(new Date());

        when(preparedStatement.execute()).thenReturn(true);

        // Act
        boolean result = ticketDAO.updateTicket(ticket);

        // Assert
        assertTrue(result);
        verify(preparedStatement, times(1)).execute();
    }

}
