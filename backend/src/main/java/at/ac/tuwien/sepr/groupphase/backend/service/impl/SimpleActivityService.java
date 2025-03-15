package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivitySearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivitySlotDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivitySlotSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityTimeslotInfoDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedActivityDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ActivityMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Activity;
import at.ac.tuwien.sepr.groupphase.backend.entity.ActivityImage;
import at.ac.tuwien.sepr.groupphase.backend.entity.ActivitySlot;
import at.ac.tuwien.sepr.groupphase.backend.entity.GuestActivityCategory;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ActivityRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.GuestActivityCategoryRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ActivityService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.ActivityValidator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SimpleActivityService implements ActivityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;
    private final ActivityValidator activityValidator;
    private final GuestActivityCategoryRepository guestActivityCategoryRepository;
    private final UserService userService;

    public SimpleActivityService(ActivityRepository activityRepository, ActivityMapper activityMapper, ActivityValidator activityValidator, GuestActivityCategoryRepository guestActivityCategoryRepository, UserService userService) {
        this.activityRepository = activityRepository;
        this.activityMapper = activityMapper;
        this.activityValidator = activityValidator;
        this.guestActivityCategoryRepository = guestActivityCategoryRepository;
        this.userService = userService;
    }

    @Transactional
    @Override
    public DetailedActivityDto create(ActivityCreateDto activityDto, MultipartFile image, List<MultipartFile> additionalImages, List<ActivityTimeslotInfoDto> timeslotsDto) throws ValidationException, IOException {
        LOGGER.debug("Create new activity {}", activityDto);
        activityValidator.validateForCreate(activityDto, image, additionalImages);

        try {
            Activity activity = activityMapper.activityCreateDtoToActivity(activityDto, image, additionalImages, timeslotsDto);
            if (timeslotsDto != null) {
                List<ActivitySlot> slots = generateTimeslots(activity, timeslotsDto);
                activity.setActivityTimeslots(slots);
            }

            return activityMapper.activityToDetailedActivityDto(activityRepository.save(activity));
        } catch (IOException e) {
            LOGGER.error("Error creating activity", e);
            throw new IOException("Failed to create activity due to image processing error", e);
        }
    }

    @Transactional
    @Override
    public DetailedActivityDto update(Long id, ActivityUpdateDto activityUpdateDto, MultipartFile mainImage, List<MultipartFile> additionalImages, List<ActivityTimeslotInfoDto> timeslotsDto) throws ValidationException, IOException {
        LOGGER.debug("Update activity with ID {}", id);
        Activity existingActivity = activityRepository.findById(id).orElseThrow(() ->
            new NotFoundException("Activity with id " + id + " not found"));

        activityValidator.validateForUpdate(activityUpdateDto, mainImage, additionalImages);

        Optional.ofNullable(activityUpdateDto.name()).ifPresent(existingActivity::setName);
        Optional.ofNullable(activityUpdateDto.description()).ifPresent(existingActivity::setDescription);
        Optional.ofNullable(activityUpdateDto.price()).ifPresent(existingActivity::setPrice);
        Optional.ofNullable(activityUpdateDto.capacity()).ifPresent(existingActivity::setCapacity);

        if (mainImage != null) {
            try {
                existingActivity.setMainImage(mainImage.getBytes());
            } catch (IOException e) {
                LOGGER.error("Error processing main image bytes", e);
                throw new IOException("Failed to process main image bytes", e);
            }
        } else {
            existingActivity.setMainImage(null);
        }

        if (additionalImages == null || additionalImages.isEmpty()) {
            existingActivity.getAdditionalImages().clear();
        } else {
            List<ActivityImage> newImages = new ArrayList<>();
            for (MultipartFile file : additionalImages) {
                if (!file.isEmpty()) {
                    try {
                        ActivityImage image = new ActivityImage();
                        image.setData(file.getBytes());
                        image.setAltText(file.getOriginalFilename());
                        newImages.add(image);
                    } catch (IOException e) {
                        LOGGER.error("Error processing additional image bytes", e);
                        throw new IOException("Failed to process additional image bytes", e);
                    }
                }
            }
            existingActivity.getAdditionalImages().clear();
            existingActivity.getAdditionalImages().addAll(newImages);
        }
        if (timeslotsDto != null) {
            existingActivity.setActivityTimeslots(generateTimeslots(existingActivity, timeslotsDto));
        }
        return activityMapper.activityToDetailedActivityDto(activityRepository.save(existingActivity));
    }

    @Override
    public Page<ActivityListDto> search(ActivitySearchDto activitySearchDto, Pageable pageable) throws ValidationException {
        LOGGER.debug("Search for activities with criteria: {}", activitySearchDto);

        activityValidator.validateForSearch(activitySearchDto);

        String name = activitySearchDto.name();
        LocalDate date = activitySearchDto.date();
        Integer guestCount = activitySearchDto.capacity();
        Double minPrice = activitySearchDto.minPrice();
        Double maxPrice = activitySearchDto.maxPrice();

        Page<Activity> activities = activityRepository.search(
            name,
            date,
            guestCount,
            minPrice,
            maxPrice,
            LocalDate.now(),
            LocalTime.now(),
            pageable
        );

        return activities.map(activityMapper::activityToActivityListDto);
    }


    @Override
    public Page<ActivitySlotDto> getPaginatedTimeslots(Long activityId, PageRequest pageRequest) {
        PageRequest sortedPageRequest = PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize(), Sort.by(Sort.Direction.ASC, "date"));
        Page<ActivitySlot> timeslots = activityRepository.findTimeslotsByActivityId(
            activityId,
            LocalDate.now(),
            LocalTime.now(),
            sortedPageRequest
        );
        return activityMapper.activitySlotPageToDtoPage(timeslots);
    }

    @Override
    public Page<ActivitySlotDto> getFilteredTimeslots(Long activityId, ActivitySlotSearchDto activitySlotSearchDto, PageRequest pageRequest) throws ValidationException {
        activityValidator.validateForTimeslotsFilter(activitySlotSearchDto, activityId);
        PageRequest sortedPageRequest = PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize(), Sort.by(Sort.Direction.ASC, "date"));
        Page<ActivitySlot> filteredTimeslots = activityRepository.findFilteredTimeslots(
            activityId,
            activitySlotSearchDto.date(),
            activitySlotSearchDto.participants(),
            LocalDate.now(),
            LocalTime.now(),
            sortedPageRequest
        );
        return activityMapper.activitySlotPageToDtoPage(filteredTimeslots);
    }

    @Transactional
    @Override
    public DetailedActivityDto findOne(Long id) {
        Activity activity = activityRepository.findActivityById(id)
            .orElseThrow(() -> new NotFoundException("Activity not found with ID: " + id));
        return activityMapper.activityToDetailedActivityDto(activity);
    }


    @Override
    public Page<ActivityListDto> findAll(Pageable pageable) {
        Page<ActivityListDto> returnValue = activityMapper.activityToActivityListDto(activityRepository.findAllByOrderByPriceAsc(pageable));

        LOGGER.debug("list of activities: {}", returnValue.stream()
            .map(activity -> String.format("\nActivity(id=%d, name=%s, price=%.2f, capacity=%d)",
                activity.id(),
                activity.name(),
                activity.price(),
                activity.capacity(),
                activity.activityTimeslotInfos()))
            .toList());
        return returnValue;
    }

    @Override
    public ActivityListDto getRecommendedActivity() {
        Map<Long, String> activityCategoryMap = createActivityCategoryMap();
        Map<String, Double> categoryWeightsMap = createCategoryWeightsMap();

        Map<Long, Double> activityMatchScore = calculateActivityMatchScore(activityCategoryMap, categoryWeightsMap);
        Long chosenActivityId = chooseActivityIdByPercentage(activityMatchScore);

        LOGGER.info("Activity with id " + chosenActivityId + " chosen!");
        return activityMapper.activityToActivityListDto(activityRepository.findActivityById(chosenActivityId).orElse(null));
    }

    private Map<Long, String> createActivityCategoryMap() {
        Map<Long, String> activityCategoryMap = new HashMap<>();

        List<Activity> activities = activityRepository.findAll();
        for (Activity activity : activities) {
            activityCategoryMap.put(activity.getId(), activity.getCategories());
        }
        LOGGER.info("Activity-Category Mapping: " + activityCategoryMap);

        return activityCategoryMap;
    }

    private Map<String, Double> createCategoryWeightsMap() {
        Long loggedInUserId = userService.getLoggedInUser().getId();
        GuestActivityCategory guestActivityCategory = guestActivityCategoryRepository.findGuestActivityCategoryByGuestId(loggedInUserId);

        Map<String, Double> categoryWeightsMap = new HashMap<>();
        categoryWeightsMap.put("education", guestActivityCategory.getEducation());
        categoryWeightsMap.put("music", guestActivityCategory.getMusic());
        categoryWeightsMap.put("fitness", guestActivityCategory.getFitness());
        categoryWeightsMap.put("nature", guestActivityCategory.getNature());
        categoryWeightsMap.put("cooking", guestActivityCategory.getCooking());
        categoryWeightsMap.put("teamwork", guestActivityCategory.getTeamwork());
        categoryWeightsMap.put("creativity", guestActivityCategory.getCreativity());
        categoryWeightsMap.put("wellness", guestActivityCategory.getWellness());
        categoryWeightsMap.put("recreation", guestActivityCategory.getRecreation());
        categoryWeightsMap.put("sports", guestActivityCategory.getSports());
        categoryWeightsMap.put("kids", guestActivityCategory.getKids());
        categoryWeightsMap.put("workshop", guestActivityCategory.getWorkshop());
        LOGGER.info("Guest with id: " + loggedInUserId + " got following categories: " + categoryWeightsMap);

        return categoryWeightsMap;
    }

    private static Map<Long, Double> calculateActivityMatchScore(Map<Long, String> activityCategoryMap, Map<String, Double> categoryWeightsMap) {
        Map<Long, Double> activityMatchScore = new HashMap<>();

        for (Map.Entry<Long, String> entry : activityCategoryMap.entrySet()) {
            Long activityId = entry.getKey();
            String categories = entry.getValue();

            String[] categoryArray = categories.split(", ");
            double matchScore = 0.0;

            for (String category : categoryArray) {
                double weight = categoryWeightsMap.getOrDefault(category.trim().toLowerCase(), 0.0);
                matchScore += weight / categoryArray.length;
            }

            activityMatchScore.put(activityId, matchScore);
        }
        LOGGER.info("activitymatchScore" + activityMatchScore);
        return activityMatchScore;
    }

    private static Long chooseActivityIdByPercentage(Map<Long, Double> productMatch) {
        double totalScore = productMatch.values().stream().mapToDouble(Double::doubleValue).sum();

        double randomNumber = Math.random();
        double cumulativePercentage = 0.0;

        if (totalScore <= 0) {
            for (Map.Entry<Long, Double> entry : productMatch.entrySet()) {
                cumulativePercentage += 1.0 / productMatch.size();
                if (randomNumber <= cumulativePercentage) {
                    return entry.getKey();
                }
            }
        } else {
            for (Map.Entry<Long, Double> entry : productMatch.entrySet()) {
                cumulativePercentage += entry.getValue() / totalScore;
                if (randomNumber <= cumulativePercentage) {
                    return entry.getKey();
                }
            }

        }
        return null;
    }

    @Override
    public ResponseEntity<byte[]> getMainImage(Long id) {
        Activity activity = activityRepository.findActivityById(id)
            .orElseThrow(() -> new NotFoundException("Activity not found with ID: " + id));
        if (activity == null) {
            throw new NotFoundException("Activity with id " + id + " not found");
        }
        byte[] image = activity.getMainImage();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "image/*");

        return new ResponseEntity<>(image, headers, org.springframework.http.HttpStatus.OK);
    }

    @Override
    public void delete(Long id) throws NotFoundException, ValidationException {
        LOGGER.debug("Delete activity {}", id);
        Activity existingActivity = activityRepository.findById(id).orElseThrow(() ->
            new NotFoundException("Activity with id " + id + " not found"));
        activityValidator.validateForDelete(existingActivity.getId());
        activityRepository.delete(existingActivity);
    }

    public List<ActivitySlot> generateTimeslots(Activity activity, List<ActivityTimeslotInfoDto> timeslotsDtos) {
        List<ActivitySlot> timeslots = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate sixMonthsFromNow = today.plusMonths(6);

        for (ActivityTimeslotInfoDto dto : timeslotsDtos) {
            if (dto.specificDate() != null) {
                if (!dto.specificDate().isBefore(today)) {
                    timeslots.add(createTimeslot(activity, activity.getCapacity(), dto.specificDate(), dto.startTime(), dto.endTime()));
                }
            } else if (dto.dayOfWeek() != null) {
                LocalDate current = today;
                while (!current.isAfter(sixMonthsFromNow)) {
                    if (current.getDayOfWeek() == dto.dayOfWeek()) {
                        timeslots.add(createTimeslot(activity, activity.getCapacity(), current, dto.startTime(), dto.endTime()));
                    }
                    current = current.plusDays(1);
                }
            } else {
                LocalDate current = today;
                while (!current.isAfter(sixMonthsFromNow)) {
                    timeslots.add(createTimeslot(activity, activity.getCapacity(), current, dto.startTime(), dto.endTime()));
                    current = current.plusDays(1);
                }
            }
        }

        return timeslots;
    }

    private ActivitySlot createTimeslot(Activity activity, int capacity, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return new ActivitySlot(
            null,
            activity,
            date,
            startTime,
            endTime,
            capacity,
            0
        );
    }
}
