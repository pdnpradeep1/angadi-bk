package com.ecom.pradeep.angadi_bk.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {
    private String name;
    private String email;
    private String phone;
}
