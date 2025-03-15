package at.ac.tuwien.sepr.groupphase.backend.basetest;

import at.ac.tuwien.sepr.groupphase.backend.entity.*;

import at.ac.tuwien.sepr.groupphase.backend.enums.BookingStatus;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public interface TestData {
    Long TEST_ROOM_ID = 1L;
    String TEST_ROOM_NAME = "Deluxe Room";
    String TEST_ROOM_DESCRIPTION = "A luxurious room with a king-sized bed and sea view.";
    int TEST_ROOM_CAPACITY = 2;
    double TEST_ROOM_PRICE = 200.00;
    boolean TEST_ROOM_HALF_BOARD = false;
    LocalDateTime TEST_ROOM_LAST_CLEANED_AT = null;
    LocalDateTime TEST_ROOM_CREATED_AT = LocalDateTime.of(2024, 11, 23, 12, 0);

    String TEST_ROOM_MAIN_IMAGE_STRING = "Test Image Data";
    byte[] TEST_ROOM_MAIN_IMAGE = TEST_ROOM_MAIN_IMAGE_STRING.getBytes();
    String TEST_ROOM_MAIN_IMAGE_STRING_BASE64 = Base64.getEncoder().encodeToString(TEST_ROOM_MAIN_IMAGE);

    List<RoomImage> TEST_ROOM_ADDITIONAL_IMAGES_AS_ROOMIMAGE = new ArrayList<>();
    List<String> TEST_ROOM_ADDITIONAL_IMAGES_AS_STRING = new ArrayList<>();
    Long TEST_ACTIVITY_ID = 1L;
    String TEST_ACTIVITY_NAME = "Deluxe Room";
    String TEST_ACTIVITY_DESCRIPTION = "A luxurious room with a king-sized bed and sea view.";
    int TEST_ACTIVITY_CAPACITY = 2;
    double TEST_ACTIVITY_PRICE = 200.00;
    LocalDateTime TEST_ACTIVITY_CREATED_AT = LocalDateTime.of(2024, 11, 23, 12, 0);
    String TEST_ACTIVITY_MAIN_IMAGE_STRING = "Test Image Data";
    byte[] TEST_ACTIVITY_MAIN_IMAGE = TEST_ACTIVITY_MAIN_IMAGE_STRING.getBytes();
    String TEST_ACTIVITY_MAIN_IMAGE_STRING_BASE64 = Base64.getEncoder().encodeToString(TEST_ACTIVITY_MAIN_IMAGE);

    List<ActivityImage> TEST_ACTIVITY_ADDITIONAL_IMAGES_AS_ACTIVITYIMAGE = new ArrayList<>();
    List<String> TEST_ACTIVITY_ADDITIONAL_IMAGES_AS_STRING = new ArrayList<>();
    List<ActivityTimeslotInfo> TEST_ACTIVITY_TIMESLOTS1 = new ArrayList<>() {
        {
            ActivityTimeslotInfo timeslot1 = new ActivityTimeslotInfo();
            timeslot1.setDayOfWeek(DayOfWeek.MONDAY);
            timeslot1.setStartTime(LocalTime.of(10, 0));
            timeslot1.setEndTime(LocalTime.of(12, 0));
            add(timeslot1);
            ActivityTimeslotInfo timeslot2 = new ActivityTimeslotInfo();
            timeslot2.setDayOfWeek(DayOfWeek.WEDNESDAY);
            timeslot2.setStartTime(LocalTime.of(12, 0));
            timeslot2.setEndTime(LocalTime.of(15, 0));
            add(timeslot2);
        }
    };
    List<ActivityTimeslotInfo> TEST_ACTIVITY_TIMESLOTS2 = new ArrayList<>() {
        {
            ActivityTimeslotInfo timeslot = new ActivityTimeslotInfo();
            LocalDate inAWeek = LocalDate.now().plusWeeks(1);
            timeslot.setSpecificDate(inAWeek);
            timeslot.setStartTime(LocalTime.of(14, 0));
            timeslot.setEndTime(LocalTime.of(16, 0));
            add(timeslot);

        }
    };
    String TEST_ACTIVITY_CATEGORIES = "Education";

    // UI Config-related Test Data
    Long TEST_UI_CONFIG_ID = 1L;
    String TEST_UI_CONFIG_HOTEL_NAME = "Test Hotel";
    String TEST_UI_CONFIG_DESCRIPTION_SHORT = "Test Description";
    String TEST_UI_CONFIG_DESCRIPTION = "Default configuration for UI testing.";
    String TEST_UI_CONFIG_ADDRESS = "Test Address";
    Boolean TEST_UI_CONFIG_ROOM_CLEANING = true;
    Boolean TEST_UI_CONFIG_DIGITAL_CHECKIN = false;
    Boolean TEST_UI_CONFIG_ACTIVITIES = true;
    Boolean TEST_UI_CONFIG_COMMUNICATION = true;
    Boolean TEST_UI_CONFIG_NUKI = true;
    Boolean TEST_UI_CONFIG_HALF_BOARD = false;
    Double TEST_UI_CONFIG_PRICE_HALF_BOARD = 0.0;

    Long TEST_USER_ID = 1L;
    String TEST_USER_EMAIL = "user@email.com";
    String TEST_USER_PASSWORD = "password123";
    Boolean TEST_USER_ADMIN = false;
    ApplicationUser TEST_USER = new ApplicationUser();

    String TEST_UI_IMAGE_ALT_TEXT = "Default Image Alt Text";
    byte[] TEST_UI_IMAGE_DATA = "Test Image Content".getBytes();
    List<UiImage> TEST_UI_CONFIG_IMAGES = new ArrayList<>() {
        {
            add(new UiImage() {{
                setAltText(TEST_UI_IMAGE_ALT_TEXT);
                setData(TEST_UI_IMAGE_DATA);
                setCreatedAt(LocalDateTime.now());
            }});
        }
    };

    Long TEST_BOOKING_ID = 1L;
    Room TEST_ROOM = new Room(TEST_ROOM_ID, TEST_ROOM_NAME, TEST_ROOM_DESCRIPTION, TEST_ROOM_PRICE,
        TEST_ROOM_CAPACITY, TEST_ROOM_HALF_BOARD, TEST_ROOM_ADDITIONAL_IMAGES_AS_ROOMIMAGE,
        TEST_ROOM_LAST_CLEANED_AT, TEST_ROOM_CREATED_AT, null, null, TEST_ROOM_MAIN_IMAGE);
    Booking TEST_BOOKING = new Booking(TEST_ROOM, TEST_USER, LocalDate.of(2024, 12, 1), LocalDate.of(2024, 12, 5), true, BookingStatus.PENDING);
    LocalDate TEST_BOOKING_START_DATE = LocalDate.of(2024, 12, 1);
    LocalDate TEST_BOOKING_END_DATE = LocalDate.of(2024, 12, 5);

    String BASE_URI = "/api/v1";
    String MESSAGE_BASE_URI = BASE_URI + "/messages";
    String ROOM_BASE_URI = BASE_URI + "/room";
    String ACTIVITY_BASE_URI = BASE_URI + "/activity";
    String BOOKING_BASE_URI = BASE_URI + "/bookings";
    String UI_CONFIG_BASE_URI = BASE_URI + "/ui-config";
    String CHECK_IN_BASE_URI = BASE_URI + "/checkin";

    String ADMIN_USER = "admin@email.com";
    List<String> ADMIN_ROLES = new ArrayList<>() {
        {
            add("ROLE_ADMIN");
            add("ROLE_GUEST");
        }
    };
    List<String> GUEST_ROLES = new ArrayList<>() {
        {
            add("ROLE_GUEST");
        }
    };

    String CHECK_IN_USER = "guest1@example.com";
    Long CHECK_IN_USER_ID = -1L;
    Long CHECK_IN_BOOKING_ID = TEST_BOOKING_ID + 1;
    Guest CHECK_IN_USER_GUEST = new Guest();

    String BOOKING_USER = "guest2@example.com";
    Long BOOKING_USER_ID = -2L;
    Long BOOKING_BOOKING_ID = TEST_BOOKING_ID + 2;
    Guest BOOKING_USER_GUEST = new Guest();

    String TEST_GUEST_EMAIL = "testguest@example.com";
    String TEST_GUEST_FIRST_NAME = "TestFirstName";
    String TEST_GUEST_LAST_NAME = "TestLastName";
    String TEST_GUEST_PHONE = "1234567890";
    String UNIQUE_TEST_EMAIL_1 = "unique1@test.com";
    String UNIQUE_TEST_EMAIL_2 = "unique2@test.com";
    String UNIQUE_TEST_EMAIL_3 = "unique3@test.com";
    String TEST_GUEST_PASSWORD = "Password@123";

    LocalDate TEST_GUEST_DATE_OF_BIRTH = LocalDate.of(1990, 1, 1);
    String TEST_GUEST_PLACE_OF_BIRTH = "TestCity";
    String TEST_GUEST_GENDER = "FEMALE";
    String TEST_GUEST_NATIONALITY = "Austria";
    String TEST_GUEST_ADDRESS = "123 Test Street";
    String TEST_GUEST_PASSPORT_NUMBER = "A1234567";


}
