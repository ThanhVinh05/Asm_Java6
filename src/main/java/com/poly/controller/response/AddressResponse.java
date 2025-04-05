package com.poly.controller.response;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse implements Serializable {
    private String streetNumber;
    private String commune;
    private String district;
    private String city;
    private String country;
    private Integer addressType;
}

