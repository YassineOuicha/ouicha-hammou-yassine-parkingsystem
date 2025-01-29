package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        calculateFare(ticket, false);
    }
    public void calculateFare(Ticket ticket, boolean discount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        long inSeconds = ticket.getInTime().getTime(); // in time in seconds
        long outSeconds = ticket.getOutTime().getTime(); // out time in seconds

        double durationInSeconds = (outSeconds - inSeconds);
        double durationInHours = durationInSeconds / (60*60*1000); // milliseconds to hours

        if (discount) {
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    if (durationInSeconds < (30 * 60 * 1000)) {
                        ticket.setPrice(0);
                        break;
                    } else if (durationInSeconds < (1000 * 60 * 60)) {
                        ticket.setPrice(0.95 * 0.75 * Fare.CAR_RATE_PER_HOUR);
                        break;
                    } else {
                        ticket.setPrice(0.95 * durationInHours * Fare.CAR_RATE_PER_HOUR);
                        break;
                    }
                }
                case BIKE: {
                    if (durationInSeconds < (30 * 60 * 1000)) {
                        ticket.setPrice(0);
                        break;
                    } else if (durationInSeconds < (1000 * 60 * 60)) {
                        ticket.setPrice(0.95 * 0.75 * Fare.BIKE_RATE_PER_HOUR);
                        break;
                    } else {
                        ticket.setPrice(0.95 * durationInHours * Fare.BIKE_RATE_PER_HOUR);
                        break;
                    }
                }
                default:
                    throw new IllegalArgumentException("Unknown Parking Type");
            }
        } else {
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    if (durationInSeconds < (30 * 60 * 1000)) {
                        ticket.setPrice(0);
                        break;
                    } else if (durationInSeconds < (1000 * 60 * 60)) {
                        ticket.setPrice(0.75 * Fare.CAR_RATE_PER_HOUR);
                        break;
                    } else {
                        ticket.setPrice(durationInHours * Fare.CAR_RATE_PER_HOUR);
                        break;
                    }
                }
                case BIKE: {
                    if (durationInSeconds < (30 * 60 * 1000)) {
                        ticket.setPrice(0);
                        break;
                    } else if (durationInSeconds < (1000 * 60 * 60)) {
                        ticket.setPrice(0.75 * Fare.BIKE_RATE_PER_HOUR);
                        break;
                    } else {
                        ticket.setPrice(durationInHours * Fare.BIKE_RATE_PER_HOUR);
                        break;
                    }
                }
                default:
                    throw new IllegalArgumentException("Unknown Parking Type");
            }
        }
    }
}