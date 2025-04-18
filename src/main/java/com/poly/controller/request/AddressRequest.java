package com.poly.controller.request;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AddressRequest implements Serializable {
    private String streetNumber;
    private String commune;
    private String district;
    private String city;
    private String country;
    private Integer addressType;

}
