package com.vladimirpandurov.spring_security_invoice_manager.service;

import com.vladimirpandurov.spring_security_invoice_manager.domain.Role;

public interface RoleService {

    Role getRoleByUserId(Long userId);

}
