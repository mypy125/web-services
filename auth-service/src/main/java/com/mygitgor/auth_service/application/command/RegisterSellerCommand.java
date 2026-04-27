package com.mygitgor.auth_service.application.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RegisterSellerCommand {
    String email;
    String sellerName;
    String mobile;
    BusinessDetailsDto businessDetails;
    BankDetailsDto bankDetails;
    String ipAddress;
}
