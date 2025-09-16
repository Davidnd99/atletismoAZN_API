package com.running.service;

import com.running.model.CarouselItem;
import com.running.model.CarouselItemDto;
import com.running.model.User;
import com.running.repository.CarouselRepository;
import com.running.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarouselService {

    private final CarouselRepository carouselRepository;
    private final UserRepository userRepository;

    /* ======= Auth helpers ======= */
    private boolean isAdmin(User u) {
        return userRepository.existsByIdAndRole_Name(u.getId(), "admin")
                || userRepository.existsByIdAndRole_Name(u.getId(), "administrator");
    }

    private User requireAdmin(String uid) {
        User u = userRepository.findByUID(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found by uid"));
        if (!isAdmin(u)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin can manage carousel");
        return u;
    }

    /* ======= Read ======= */
    public List<CarouselItem> findAll() {
        return carouselRepository.findAll();
    }

    public CarouselItem findById(Long id) {
        return carouselRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carousel image not found"));
    }

    /* ======= Create ======= */
    public CarouselItem create(String uid, CarouselItemDto dto) {
        requireAdmin(uid);
        String photo = sanitize(dto.getPhoto());
        if (photo == null || photo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "photo is required");
        }
        if (photo.length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "photo must be <= 255 chars");
        }
        CarouselItem item = CarouselItem.builder()
                .photo(photo)
                .build();
        return carouselRepository.save(item);
    }

    /* ======= Update ======= */
    public CarouselItem update(String uid, Long id, CarouselItemDto dto) {
        requireAdmin(uid);
        CarouselItem item = findById(id);

        if (dto.getPhoto() != null) {
            String photo = sanitize(dto.getPhoto());
            if (photo.isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "photo cannot be blank");
            if (photo.length() > 255)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "photo must be <= 255 chars");
            item.setPhoto(photo);
        }

        return carouselRepository.save(item);
    }

    /* ======= Delete ======= */
    public void delete(String uid, Long id) {
        requireAdmin(uid);
        CarouselItem item = findById(id);
        carouselRepository.delete(item);
    }

    private String sanitize(String s) {
        return s == null ? null : s.trim();
    }
}
