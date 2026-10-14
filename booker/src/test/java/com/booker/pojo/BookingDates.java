package com.booker.pojo;

/** Maps to the nested "bookingdates" object in the JSON. */
public class BookingDates {
    private String checkin;
    private String checkout;

    public BookingDates() { }   // needed by Jackson when reading JSON into this class

    public BookingDates(String checkin, String checkout) {
        this.checkin = checkin;
        this.checkout = checkout;
    }

    public String getCheckin() { return checkin; }
    public void setCheckin(String checkin) { this.checkin = checkin; }
    public String getCheckout() { return checkout; }
    public void setCheckout(String checkout) { this.checkout = checkout; }
}
