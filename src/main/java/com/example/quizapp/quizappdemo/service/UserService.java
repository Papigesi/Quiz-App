package com.example.quizapp.quizappdemo.service;

import com.example.quizapp.quizappdemo.dto.RegisterRequest;
import com.example.quizapp.quizappdemo.dto.UserDto;
import com.example.quizapp.quizappdemo.exception.BadRequestException;
import com.example.quizapp.quizappdemo.exception.ResourceNotFoundException;
import com.example.quizapp.quizappdemo.exception.UnauthorizedException;
import com.example.quizapp.quizappdemo.model.User;
import com.example.quizapp.quizappdemo.repository.QuizResultRepository;
import com.example.quizapp.quizappdemo.repository.UserRepository;
import com.example.quizapp.quizappdemo.security.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final String ROLE_USER = "USER";
    private static final String ROLE_ADMIN = "ADMIN";

    private final UserRepository userRepository;
    private final QuizResultRepository quizResultRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       QuizResultRepository quizResultRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.quizResultRepository = quizResultRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDto register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Bu e-posta zaten kayıtlı");
        }
        User user = new User(
                request.getName().trim(),
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                ROLE_USER
        );
        return toDto(userRepository.save(user));
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public UserDto getById(Long id) {
        return toDto(findUserOrThrow(id));
    }

    public UserDto updateUser(Long id, UserDto userDto) {
        User user = findUserOrThrow(id);
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail().trim().toLowerCase());
        }
        if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        return toDto(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        User user = findUserOrThrow(id);
        if (user.isSuperAdmin()) {
            throw new BadRequestException("Süper admin silinemez");
        }
        quizResultRepository.deleteByUserId(id);
        userRepository.delete(user);
    }

    public User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bu ID ile bir kullanıcı bulunamadı: " + id));
    }

    public UserDto makeAdmin(Long targetId) {
        requireSuperAdmin();
        User target = findUserOrThrow(targetId);
        if (ROLE_ADMIN.equals(target.getRole())) {
            throw new BadRequestException("Bu kullanıcı zaten admin");
        }
        target.setRole(ROLE_ADMIN);
        return toDto(userRepository.save(target));
    }

    public UserDto revokeAdmin(Long targetId) {
        requireSuperAdmin();
        User target = findUserOrThrow(targetId);
        if (target.isSuperAdmin()) {
            throw new BadRequestException("Süper adminin yetkisi geri alınamaz");
        }
        if (!ROLE_ADMIN.equals(target.getRole())) {
            throw new BadRequestException("Bu kullanıcı zaten admin değil");
        }
        target.setRole(ROLE_USER);
        return toDto(userRepository.save(target));
    }

    private void requireSuperAdmin() {
        User requester = SecurityUtils.currentPrincipal().getUser();
        if (!requester.isSuperAdmin()) {
            throw new UnauthorizedException("Bu işlem yalnızca süper admin tarafından yapılabilir");
        }
    }

    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setSuperAdmin(user.isSuperAdmin());
        return dto;
    }
}
