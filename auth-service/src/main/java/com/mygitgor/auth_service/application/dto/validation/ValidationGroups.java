package com.mygitgor.auth_service.application.dto.validation;

public interface ValidationGroups {

    interface Create {}

    interface Update {}

    interface Delete {}

    interface Login {}

    interface Register {}

    interface Verify {}

    interface ChangePassword {}

    interface ResetPassword {}

    interface SendOtp {}

    interface VerifyOtp {}
}
