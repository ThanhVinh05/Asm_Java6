package com.poly.controller.response;

import com.poly.common.Gender;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

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
    // more
}