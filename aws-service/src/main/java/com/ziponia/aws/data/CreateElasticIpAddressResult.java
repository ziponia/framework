package com.ziponia.aws.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateElasticIpAddressResult {

    private Boolean isSuccessFul;
    private String allocationId;
    private String message;
    private Integer statusCode;
    private String domain;
}
