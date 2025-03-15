package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivitySlotDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityTimeslotInfoDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedActivityDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Activity;
import at.ac.tuwien.sepr.groupphase.backend.entity.ActivityImage;

import at.ac.tuwien.sepr.groupphase.backend.entity.ActivitySlot;
import at.ac.tuwien.sepr.groupphase.backend.entity.ActivityTimeslotInfo;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Collections;

@Mapper
public interface ActivityMapper {

    @Named("activityList")
    default ActivityListDto activityToActivityListDto(Activity activity) {
        if (activity == null) {
            return null;
        }

        String mainImage = (activity.getMainImage() != null)
            ? activity.getMainImageAsString()
            : null;

        List<ActivityTimeslotInfoDto> timeslotDtos = Optional.ofNullable(activity.getActivityTimeslotInfos())
            .orElse(Collections.emptyList())
            .stream()
            .map(timeslot -> new ActivityTimeslotInfoDto(
                timeslot.getId(),
                timeslot.getDayOfWeek(),
                timeslot.getSpecificDate(),
                timeslot.getStartTime(),
                timeslot.getEndTime()
            ))
            .collect(Collectors.toList());

        return new ActivityListDto(
            activity.getId(),
            activity.getName(),
            activity.getPrice(),
            activity.getCapacity(),
            mainImage,
            timeslotDtos
        );
    }

    @IterableMapping(qualifiedByName = "activityList")
    default Page<ActivityListDto> activityToActivityListDto(Page<Activity> activities) {
        if (activities == null) {
            return Page.empty();
        }
        return activities.map(this::activityToActivityListDto);
    }

    default DetailedActivityDto activityToDetailedActivityDto(Activity activity) {
        if (activity == null) {
            return null;
        }

        String mainImage = (activity.getMainImage() != null)
            ? Base64.getEncoder().encodeToString(activity.getMainImage())
            : null;

        List<String> additionalImages = Optional.ofNullable(activity.getAdditionalImages())
            .orElse(Collections.emptyList())
            .stream()
            .map(image -> Base64.getEncoder().encodeToString(image.getData()))
            .collect(Collectors.toList());

        List<ActivityTimeslotInfoDto> timeslotInfoDtos = Optional.ofNullable(activity.getActivityTimeslotInfos())
            .orElse(Collections.emptyList())
            .stream()
            .map(timeslot -> new ActivityTimeslotInfoDto(
                timeslot.getId(),
                timeslot.getDayOfWeek(),
                timeslot.getSpecificDate(),
                timeslot.getStartTime(),
                timeslot.getEndTime()
            ))
            .collect(Collectors.toList());

        return new DetailedActivityDto(
            activity.getId(),
            activity.getName(),
            activity.getDescription(),
            activity.getPrice(),
            activity.getCapacity(),
            mainImage,
            additionalImages,
            timeslotInfoDtos,
            activity.getCategories()
        );
    }

    default Activity activityCreateDtoToActivity(ActivityCreateDto activityDto, MultipartFile mainImage, List<MultipartFile> additionalImages, List<ActivityTimeslotInfoDto> timeslotsDto) throws IOException {
        if (activityDto == null) {
            return null;
        }

        Activity activity = new Activity();
        activity.setName(activityDto.name());
        activity.setDescription(activityDto.description());
        activity.setPrice(activityDto.price());
        activity.setCapacity(activityDto.capacity());
        activity.setCategories(activityDto.categories());

        if (mainImage != null && !mainImage.isEmpty()) {
            activity.setMainImage(mainImage.getBytes());
        }

        if (additionalImages != null) {
            List<ActivityImage> images = new ArrayList<>();
            for (MultipartFile image : additionalImages) {
                ActivityImage activityImage = new ActivityImage();
                activityImage.setData(image.getBytes());
                images.add(activityImage);
            }
            activity.setAdditionalImages(images);
        }

        if (timeslotsDto != null) {
            List<ActivityTimeslotInfo> timeslots = timeslotsDto
                .stream()
                .map(dto -> {
                    ActivityTimeslotInfo timeslot = new ActivityTimeslotInfo();
                    timeslot.setDayOfWeek(dto.dayOfWeek());
                    timeslot.setStartTime(dto.startTime());
                    timeslot.setEndTime(dto.endTime());
                    timeslot.setSpecificDate(dto.specificDate());
                    return timeslot;
                })
                .collect(Collectors.toList());
            activity.setActivityTimeslotInfos(timeslots);
        }

        return activity;
    }

    default Page<ActivitySlotDto> activitySlotPageToDtoPage(Page<ActivitySlot> activitySlotPage) {
        if (activitySlotPage == null) {
            return null;
        }
        List<ActivitySlotDto> activitySlotDtos = activitySlotPage.getContent().stream()
            .map(this::activitySlotPageToDtoPage)
            .collect(Collectors.toList());

        return new PageImpl<>(activitySlotDtos, activitySlotPage.getPageable(), activitySlotPage.getTotalElements());
    }


    default ActivitySlotDto activitySlotPageToDtoPage(ActivitySlot activitySlot) {
        if (activitySlot == null) {
            return null;
        }

        return new ActivitySlotDto(
            activitySlot.getId(),
            activitySlot.getDate(),
            activitySlot.getStartTime(),
            activitySlot.getEndTime(),
            activitySlot.getCapacity(),
            activitySlot.getOccupied()
        );
    }
}
