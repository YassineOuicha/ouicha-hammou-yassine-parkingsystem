package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    private final double DISCOUNT_RATE = 0.95;
    private final double LESS_THAN_AN_HOUR_RATE = 0.75;

    public void calculateFare(Ticket ticket){
        calculateFare(ticket, false);
    }
    public void calculateFare(Ticket ticket, boolean discount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        long inMilliSeconds = ticket.getInTime().getTime(); // in time in milliseconds
        long outMilliSeconds = ticket.getOutTime().getTime(); // out time in milliseconds

        double durationInMilliSeconds = (outMilliSeconds - inMilliSeconds);
        double durationInHours = durationInMilliSeconds / (60*60*1000); // milliseconds to hours

            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    if (durationInMilliSeconds < (30 * 60 * 1000)) {
                        ticket.setPrice(0);
                        break;
                    } else if (durationInMilliSeconds < (60 * 60 * 1000)) {
                        ticket.setPrice(LESS_THAN_AN_HOUR_RATE * Fare.CAR_RATE_PER_HOUR);
                        break;
                    } else {
                        ticket.setPrice(durationInHours * Fare.CAR_RATE_PER_HOUR);
                        break;
                    }
                }
                case BIKE: {
                    if (durationInMilliSeconds < (30 * 60 * 1000)) {
                        ticket.setPrice(0);
                        break;
                    } else if (durationInMilliSeconds < (60 * 60 * 1000)) {
                        ticket.setPrice(LESS_THAN_AN_HOUR_RATE * Fare.BIKE_RATE_PER_HOUR);
                        break;
                    } else {
                        ticket.setPrice(durationInHours * Fare.BIKE_RATE_PER_HOUR);
                        break;
                    }
                }
                default:
                    throw new IllegalArgumentException("Unknown Parking Type");
            }
            if(discount){
                ticket.setPrice(ticket.getPrice()*DISCOUNT_RATE);
            }
    }
}