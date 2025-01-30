package com.parkit.parkingsystem.mock;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ParkingSpotDAOTest {

    private ParkingSpotDAO parkingSpotDAO;

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
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseConfig;

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    public void testGetNextAvailableSlot() throws Exception {

        // Arrange
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(2);

        // Act
        int nextAvailableSlot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        // Assert
        assertEquals(2, nextAvailableSlot);
        verify(preparedStatement, times(1)).executeQuery();
    }

    @Test
    public void testUpdateParking() throws Exception {
        // Arrange
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        // Assert
        assertTrue(result);
        verify(preparedStatement, times(1)).executeUpdate();
    }
}
