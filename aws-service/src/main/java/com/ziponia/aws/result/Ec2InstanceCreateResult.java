package com.ziponia.aws.result;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Ec2InstanceCreateResult {

    private String instance_type;
    private String instance_id;
    private String key_name;
    private String ip_address;
}
