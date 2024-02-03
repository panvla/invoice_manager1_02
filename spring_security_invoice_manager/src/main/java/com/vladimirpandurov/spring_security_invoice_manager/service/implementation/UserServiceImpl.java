package com.vladimirpandurov.spring_security_invoice_manager.service.implementation;

import com.vladimirpandurov.spring_security_invoice_manager.domain.Role;
import com.vladimirpandurov.spring_security_invoice_manager.domain.User;
import com.vladimirpandurov.spring_security_invoice_manager.dto.UserDTO;
import com.vladimirpandurov.spring_security_invoice_manager.dtomapper.UserDTOMapper;
import com.vladimirpandurov.spring_security_invoice_manager.repository.RoleRepository;
import com.vladimirpandurov.spring_security_invoice_manager.repository.UserRepository;
import com.vladimirpandurov.spring_security_invoice_manager.repository.implementation.UserRepositoryImpl;
import com.vladimirpandurov.spring_security_invoice_manager.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.vladimirpandurov.spring_security_invoice_manager.dtomapper.UserDTOMapper.fromUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository<User> userRepository;
    private final RoleRepository<Role> roleRepository;

    @Override
    public UserDTO createUser(User user) {
        return mapToUserDTO(userRepository.create(user));
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        return mapToUserDTO(userRepository.getUserByEmail(email));
    }

    @Override
    public void sendVerificationCode(UserDTO userDTO) {
        this.userRepository.sendVerificationCode(userDTO);
    }

    @Override
    public UserDTO verifyCode(String email, String code) {
        return mapToUserDTO(userRepository.verifyCode(email, code));
    }

    @Override
    public void resetPassword(String email) {
        this.userRepository.resetPassword(email);
    }

    @Override
    public UserDTO verifyPasswordKey(String key) {
        return mapToUserDTO(this.userRepository.verifyPasswordKey(key));
    }

    @Override
    public void renewPassword(String key, String password, String confirmPassword) {
        this.userRepository.renewPassword(key, password, confirmPassword);
    }

    @Override
    public UserDTO verifyAccount(String key) {
        return mapToUserDTO(this.userRepository.verifyAccountKey(key));
    }

    private UserDTO mapToUserDTO (User user){
        return fromUser(user, roleRepository.getRoleByUserId(user.getId()));
    }
}
