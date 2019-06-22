package com.ziponia.aws.service;

import com.ziponia.aws.data.CreateEC2ImageData;
import com.ziponia.aws.data.CreateInstanceOption;
import com.ziponia.aws.data.TagBody;
import com.ziponia.aws.result.Ec2InstanceCreateResult;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.List;

@SuppressWarnings("all")
public interface Ec2Service {

    /**
     * 인스턴스를 생성합니다.
     * @return
     */
    Ec2InstanceCreateResult createInstance(CreateInstanceOption option);

    /**
     * 인스턴스를 중지 시킵니다.
     */
    List<String> stopInstance(String... instance_id);

    /**
     * 인스턴스를 시작합니다.
     */
    List<String> startInstance(String... instance_id);

    /**
     * 인스턴스를 재시작합니다.
     */
    boolean rebootInstance(String... instance_id);

    /**
     * 인스턴스를 종료합니다.
     */
    List<String> terminateInstance(String... instance_id);

    /**
     * 인스턴스를 시작합니다.
     * @return
     */
    DescribeInstancesResponse listAwsInstance();

    /**
     * 고정 아이피를 발급합니다.
     * @return 고정 아이피를 발급하고, 할당된 VPC id 를 반환합니다.
     */
    AllocateAddressResponse createElasticIpAddress();

    /**
     * 인스턴스와 Elastic IP 를 연결합니다.
     * @param elasticId 연결 할 Elastic IP
     * @param instanceId Target InstanceId
     * @return
     */
    AssociateAddressResponse associateAddressResponse(String elasticId, String instanceId);

    /**
     * 인스턴스의 정보를 가져옵니다.
     * @param instance_id[] instance_id 복수
     * @return
     */
    DescribeInstancesResponse accessInstanceInformation(String... instance_id);

    /**
     * 인스턴스의 모니터링을 활성화 합니다.
     */
    MonitorInstancesResponse instanceMonitor(String... instance_id);

    /**
     * 인스턴스의 모니터링을 비활성화 합니다.
     */
    UnmonitorInstancesResponse unMonitorInstance(String... instance_ids);

    /**
     * 현재 계정의 이미지 를 가져옵니다.
     */
    DescribeImagesResponse listImages(String... imageIds);

    /**
     * 태그를 생성합니다.
     */
    CreateTagsResponse createTag(TagBody body);

    /**
     * 태그를 삭제합니다.
     */
    DeleteTagsResponse removeTag(TagBody body);

    /**
     * 인스턴스 이미지를 생성합니다.
     */
    DescribeImagesResponse createImage(CreateEC2ImageData data);

    /**
     * 인스턴스 이미지를 삭제합니다.
     */
    DeregisterImageResponse removeImage(String imageId);

    /**
     * Security 그룹 리스트를 가져옵니다.
     */
    DescribeSecurityGroupsResponse listSecurityGroups(String... sgIds);
}
