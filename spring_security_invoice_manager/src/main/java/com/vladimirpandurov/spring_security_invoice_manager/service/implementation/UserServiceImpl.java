package com.vladimirpandurov.spring_security_invoice_manager.service.implementation;

import com.vladimirpandurov.spring_security_invoice_manager.domain.User;
import com.vladimirpandurov.spring_security_invoice_manager.dto.UserDTO;
import com.vladimirpandurov.spring_security_invoice_manager.dtomapper.UserDTOMapper;
import com.vladimirpandurov.spring_security_invoice_manager.repository.UserRepository;
import com.vladimirpandurov.spring_security_invoice_manager.repository.implementation.UserRepositoryImpl;
import com.vladimirpandurov.spring_security_invoice_manager.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository<User> userRepository;

    @Override
    public UserDTO createUser(User user) {
        return UserDTOMapper.fromUser(userRepository.create(user));
    }
}