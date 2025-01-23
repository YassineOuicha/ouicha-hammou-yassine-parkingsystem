package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        int inHour = ticket.getInTime().getHours();
        int outHour = ticket.getOutTime().getHours();


        int duration = outHour - inHour;

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                if (duration<1){
                    ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR*0.75);
                    break;
                } else if (duration>24) {
                    ticket.setPrice((24 + duration )* Fare.CAR_RATE_PER_HOUR);
                    break;
                } else {
                    ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                    break;
                }

            }
            case BIKE: {
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }

    }
}