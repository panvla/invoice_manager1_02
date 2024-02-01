package com.vladimirpandurov.spring_security_invoice_manager.query;

public class RoleQuery {

    public static final String SELECT_ROLE_BY_NAME_QUERY = "SELECT * FROM Roles WHERE name = :name";
    public static final String INSERT_ROLE_USER_QUERY = "INSERT INTO UserRoles (user_id, role_id) VALUES (:userId, :roleId)";
}
