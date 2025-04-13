package com.poly.controller.response;

import com.poly.common.Gender;
import com.poly.common.Status;
import com.poly.common.UserType;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse implements Serializable {
    private Long id;
    private String username;
    private Date birthday;
    private Gender gender;
    private String phone;
    private String email;
    private UserType type;
    private Status status;
    private Date createdAt;
    private List<AddressResponse> addresses;
}