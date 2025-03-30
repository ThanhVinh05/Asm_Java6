package com.poly.controller.request;

import com.poly.common.Gender;
import com.poly.common.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.ToString;


import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@ToString
public class UserCreationRequest implements Serializable {

    @NotBlank(message = "UserName must be not blank")
    private String username;
    private Date birthday;
    private Gender gender;
    private String phone;

    @Email(message = "Email invalid")
    private String email;
    private UserType type;
    private List<AddressRequest> addresses; // home,office
}
