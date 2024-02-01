package com.vladimirpandurov.spring_security_invoice_manager.repository.implementation;

import com.vladimirpandurov.spring_security_invoice_manager.domain.Role;
import com.vladimirpandurov.spring_security_invoice_manager.exception.ApiException;
import com.vladimirpandurov.spring_security_invoice_manager.repository.RoleRepository;
import com.vladimirpandurov.spring_security_invoice_manager.rowmapper.RoleRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static com.vladimirpandurov.spring_security_invoice_manager.query.RoleQuery.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RoleRepositoryImpl implements RoleRepository<Role> {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Role create(Role data) {
        return null;
    }

    @Override
    public Collection<Role> list(int page, int pageSize) {
        return null;
    }

    @Override
    public Role get(Long id) {
        return null;
    }

    @Override
    public Role update(Role data) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    @Override
    public void addRoleToUser(Long userId, String roleName) {
        log.info("Adding role {} to user id: {}", roleName, userId);
        try{
            Role role = jdbc.queryForObject(SELECT_ROLE_BY_NAME_QUERY, Map.of("name", roleName), new RoleRowMapper());
            jdbc.update(INSERT_ROLE_USER_QUERY, Map.of("userId", userId, "roleId", Objects.requireNonNull(role).getId()));
        }catch (EmptyResultDataAccessException exception){
            throw new ApiException("No role found by name: " + roleName);
        }catch (Exception exception){
            throw new ApiException("An error occurred. Please try again");
        }
    }

    @Override
    public Role getRoleByUserId(Long userId) {
        try{
            Role role = jdbc.queryForObject(SELECT_ROLE_BY_USER_ID_QUERY, Map.of("userId", userId), new RoleRowMapper());
            return role;
        }catch (EmptyResultDataAccessException exception){
            log.error("No role found by user id");
            throw new ApiException("No role found by user id:" + userId);
        }catch (Exception exception){
            log.error(exception.getMessage());
            throw new ApiException("An error occurred in getRoleByUserId in RoleRepository. Please try again.");
        }
    }

    @Override
    public Role getRoleByUserEmail(String email) {
        return null;
    }

    @Override
    public void updateUserRole(Long userId, String roleName) {

    }
}
