package com.poly.controller.request;

import com.poly.common.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@ToString
public class UserUpdateRequest implements Serializable {

    @NotNull(message = "id must be not null")
    @Min(value = 1, message = "userId must be equals or greater than 1")
    private Long id;

    @NotBlank(message = "UserName must be not blank")
    private String username;
    private Date birthday;
    private Gender gender;
    private String phone;

    @Email(message = "Email invalid")
    private String email;

    private List<AddressRequest> addresses;
}
