package com.ziponia.aws.data;

import lombok.Data;

@Data
public class Ec2MonitoringBody {

    private String[] instance_ids;
    private Boolean enable;
}
