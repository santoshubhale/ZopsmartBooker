package com.booker.utils;

import com.booker.pojo.Booking;
import com.booker.pojo.BookingDates;

import java.time.LocalDate;
import java.util.Random;

/** Creates a new random booking each time (dynamic test data). */
public class TestDataGenerator {

    private static final String[] FIRST_NAMES = {"Asha", "Ravi", "Meena", "Karthik", "Divya", "Arjun"};
    private static final String[] LAST_NAMES = {"Sharma", "Iyer", "Reddy", "Nair", "Gupta", "Patel"};
    private static final String[] NEEDS = {"Breakfast", "Parking", "Extra bed", "Late checkout"};
    private static final Random random = new Random();

    public static Booking newBooking() {
        // adding a random number to the last name makes it unique, so searches find only our booking
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)] + random.nextInt(100000);
        LocalDate checkin = LocalDate.now().plusDays(1 + random.nextInt(30));
        LocalDate checkout = checkin.plusDays(1 + random.nextInt(10));

        return new Booking(
                FIRST_NAMES[random.nextInt(FIRST_NAMES.length)],
                lastName,
                100 + random.nextInt(900),
                random.nextBoolean(),
                new BookingDates(checkin.toString(), checkout.toString()),   // toString() gives yyyy-MM-dd
                NEEDS[random.nextInt(NEEDS.length)]);
    }
}
