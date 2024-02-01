package com.vladimirpandurov.spring_security_invoice_manager.dtomapper;

import com.vladimirpandurov.spring_security_invoice_manager.domain.User;
import com.vladimirpandurov.spring_security_invoice_manager.dto.UserDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class UserDTOMapper {
    public static UserDTO fromUser(User user){
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return userDTO;
    }

    public static User toUser(UserDTO userDTO){
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        return user;
    }
}
