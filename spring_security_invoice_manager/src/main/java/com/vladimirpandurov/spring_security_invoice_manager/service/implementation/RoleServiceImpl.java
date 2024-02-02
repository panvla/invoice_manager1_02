package com.vladimirpandurov.spring_security_invoice_manager.service.implementation;

import com.vladimirpandurov.spring_security_invoice_manager.domain.Role;
import com.vladimirpandurov.spring_security_invoice_manager.repository.RoleRepository;
import com.vladimirpandurov.spring_security_invoice_manager.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository<Role> roleRepository;

    @Override
    public Role getRoleByUserId(Long userId) {
        return this.roleRepository.getRoleByUserId(userId);
    }
}
