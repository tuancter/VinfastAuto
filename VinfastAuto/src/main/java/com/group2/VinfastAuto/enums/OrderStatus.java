package com.group2.VinfastAuto.enums;

/**
 * Represents the possible states of an order in the system.
 * This enum must match the values defined in the database schema for the 'status' column.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED;
}
