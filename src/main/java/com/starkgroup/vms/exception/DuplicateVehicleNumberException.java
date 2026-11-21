package com.starkgroup.vms.exception;

public class DuplicateVehicleNumberException extends RuntimeException {

    public DuplicateVehicleNumberException(String message) {
        super(message);
    }
}
