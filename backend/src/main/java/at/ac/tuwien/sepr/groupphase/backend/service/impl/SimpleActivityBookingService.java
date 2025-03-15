package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityBookingCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ActivityBookingMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Activity;
import at.ac.tuwien.sepr.groupphase.backend.entity.ActivityBooking;
import at.ac.tuwien.sepr.groupphase.backend.entity.ActivitySlot;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.GuestActivityCategory;
import at.ac.tuwien.sepr.groupphase.backend.enums.BookingStatus;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ActivityBookingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ActivityRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ActivitySlotRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ApplicationUserRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.GuestActivityCategoryRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ActivityBookingService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.ActivityBookingValidator;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;


@Service
@Slf4j
public class SimpleActivityBookingService implements ActivityBookingService {

    private final ActivityBookingRepository bookingRepository;
    private final ApplicationUserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final ActivitySlotRepository activitySlotRepository;
    private final ActivityBookingMapper activityBookingMapper;
    private final GuestActivityCategoryRepository guestActivityCategoryRepository;
    private final UserService userService;

    private final ActivityBookingValidator activityBookingValidator;


    public SimpleActivityBookingService(ActivityBookingRepository bookingRepository,
                                        ApplicationUserRepository userRepository,
                                        ActivityRepository activityRepository,
                                        ActivitySlotRepository activitySlotRepository,
                                        ActivityBookingMapper activityBookingMapper,
                                        GuestActivityCategoryRepository guestActivityCategoryRepository,
                                        UserService userService, ActivityBookingValidator activityBookingValidator) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.activitySlotRepository = activitySlotRepository;
        this.activityRepository = activityRepository;
        this.activityBookingMapper = activityBookingMapper;
        this.guestActivityCategoryRepository = guestActivityCategoryRepository;
        this.userService = userService;
        this.activityBookingValidator = activityBookingValidator;
    }


    public ActivityBookingDto createBooking(ActivityBookingCreateDto bookingDto, String email) throws NotFoundException, ValidationException {
        ApplicationUser user = userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found with email: " + bookingDto.userEmail()));

        Activity activity = activityRepository.findActivityById(bookingDto.activityId())
            .orElseThrow(() -> new NotFoundException("Activity not found with ID: " + bookingDto.activityId()));

        ActivitySlot timeslot = activitySlotRepository.findById(bookingDto.activitySlotId())
            .orElseThrow(() -> new NotFoundException("ActivitySlot not found with ID: " + bookingDto.activitySlotId()));

        activityBookingValidator.validateForCreate(bookingDto, activity, timeslot);

        ActivityBooking booking = new ActivityBooking();
        booking.setUser(user);
        booking.setActivity(activity);
        booking.setBookingDate(bookingDto.bookingDate());
        booking.setActivitySlot(timeslot);
        booking.setParticipants(bookingDto.participants());
        booking.setStatus(BookingStatus.PENDING);

        int currentlyOccupied = timeslot.getOccupied();
        timeslot.setOccupied(currentlyOccupied + bookingDto.participants());

        bookingRepository.save(booking);
        activitySlotRepository.save(timeslot);

        if (activity.getCategories() != null && !activity.getCategories().isEmpty()) {
            updateCategoryWeights(activity);
        }


        return activityBookingMapper.activityBookingToActivityBookingDto(booking, activity);
    }

    @Override
    public void updatePaymentStatus(Long bookingId) throws NotFoundException {
        ActivityBooking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new NotFoundException("Booking not found with ID: " + bookingId));

        try {
            Session session = Session.retrieve(booking.getStripeSessionId());
            PaymentIntent paymentIntent = PaymentIntent.retrieve(session.getPaymentIntent());
            log.debug("Payment for session {} with bookingId {} has status {}", session.getId(), bookingId, paymentIntent.getStatus());
            log.debug("PaymentIntent: {}", paymentIntent.getStatus());
            booking.setStripePaymentIntentId(paymentIntent.getId());
            if (paymentIntent.getStatus().equals("succeeded") || paymentIntent.getStatus().equals("processing")) {
                booking.setPaid(true);
                booking.setStatus(BookingStatus.ACTIVE);
                bookingRepository.save(booking);
                return;
            }

        } catch (InvalidRequestException e) {
            log.error("Invalid request: {}", e.getMessage(), e);
        } catch (StripeException e) {
            log.error("Stripe exception: {}", e.getMessage(), e);
        }
        bookingRepository.save(booking);
        if (!booking.getStatus().equals(BookingStatus.ACTIVE)) {
            ActivitySlot timeslot = booking.getActivitySlot();
            timeslot.setOccupied(timeslot.getOccupied() - booking.getParticipants());
            activitySlotRepository.save(timeslot);
            bookingRepository.delete(booking);
        }
    }

    @Override
    public List<ActivityBookingDto> findByUserEmail(String email) throws NotFoundException {
        log.debug("Finding bookings for user with email: {}", email);
        ApplicationUser user = userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        List<ActivityBooking> bookings = bookingRepository.findActiveBookingsByUserId(user.getId());
        List<ActivityBookingDto> activityBookingDtos = activityBookingMapper.activityBookingsToActivityBookingDtos(bookings);
        return activityBookingDtos;
    }

    private void updateCategoryWeights(Activity activity) {
        String categories = activity.getCategories();
        String[] categoryArray = categories.split(", ");

        Long loggedInUserId = userService.getLoggedInUser().getId();
        GuestActivityCategory guestActivityCategory = guestActivityCategoryRepository
            .findGuestActivityCategoryByGuestId(loggedInUserId);

        for (String categoryName : categoryArray) {
            try {
                Field categoryField = findFieldIgnoringCase(GuestActivityCategory.class, categoryName);

                if (categoryField != null) {
                    categoryField.setAccessible(true);

                    // Get the current weight
                    double currentWeight = (double) categoryField.get(guestActivityCategory);

                    // Update the weight
                    double updatedWeight = currentWeight + (1.0 / categoryArray.length);
                    categoryField.set(guestActivityCategory, updatedWeight);
                } else {
                    System.err.println("Unknown category: " + categoryName);
                }
            } catch (IllegalAccessException e) {
                System.err.println("Error accessing the field for category: " + categoryName);
                e.printStackTrace();
            }
        }

        guestActivityCategoryRepository.save(guestActivityCategory);
    }

    private Field findFieldIgnoringCase(Class<?> clazz, String fieldName) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equalsIgnoreCase(fieldName)) {
                return field;
            }
        }
        return null;
    }

}
