package com.ziponia.aws.service.impl;

import com.ziponia.aws.data.CreateEC2ImageData;
import com.ziponia.aws.data.CreateInstanceOption;
import com.ziponia.aws.data.TagBody;
import com.ziponia.aws.result.Ec2InstanceCreateResult;
import com.ziponia.aws.service.Ec2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.List;

import static java.lang.Thread.sleep;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class Ec2ServiceImpl implements Ec2Service {

    private final Ec2Client ec2Client;

    @Override
    public Ec2InstanceCreateResult createInstance(CreateInstanceOption option) {
        log.info("New Instance Settings...");

        Ec2InstanceCreateResult.Ec2InstanceCreateResultBuilder result = Ec2InstanceCreateResult.builder();

        if (option.getInstanceName() == null) {
            throw Ec2Exception.builder().message("[instanceName] is required.").build();
        }

        if (option.getScripts() != null && !Base64.isBase64(option.getScripts())) {
            throw Ec2Exception.builder().message("[scripts] '" + option.getScripts() + "' is must be Base64 Encoding").build();
        }


        String instance_id = "";
        try {
            RunInstancesRequest run_request = RunInstancesRequest.builder()
                    .instanceType(option.getInstanceType())
                    .maxCount(1)
                    .minCount(1)
                    .securityGroupIds(option.getSecurityGroupId())
                    .keyName(option.getKeyName())
                    .imageId(option.getImageId())
                    .userData(option.getScripts())
                    .build();

            RunInstancesResponse response = ec2Client.runInstances(run_request);

            instance_id = response.instances().get(0).instanceId();
            log.info("Instance Stand-by: {}", instance_id);

            Tag tag = Tag.builder()
                    .key("Name").value(option.getInstanceName())
                    .build();

            log.info("Instance Set Name Tag: {}", option.getInstanceName());

            CreateTagsRequest tag_request = CreateTagsRequest.builder()
                    .resources(instance_id)
                    .tags(tag)
                    .build();

            ec2Client.createTags(tag_request);

            if (!option.isProduction()) {
                terminateInstance(instance_id);
            }

            System.out.printf(
                    "Successfully started EC2 instance %s based on AMI %s",
                    instance_id, option.getImageId());
            // result build

            String getPublicIp = null;
            int queue = 0;

            while (getPublicIp == null) {
                getPublicIp = accessInstanceInformation(instance_id).reservations().get(0)
                        .instances().get(0).publicIpAddress();
                try {
                    sleep(300);
                    queue++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (queue == 10) break;
            }

            result
                    .instance_id(instance_id)
                    .instance_type(option.getInstanceType().name())
                    .key_name(option.getKeyName())
                    .ip_address(getPublicIp);


            return result.build();
        } catch (Ec2Exception e) {
            System.err.println(e.getMessage());
            terminateInstance(instance_id);
            throw Ec2Exception.builder().message("인스턴스 생성에 실패하였습니다. Message - " + e.getMessage()).build();
        }
    }

    @Override
    public List<String> stopInstance(String... instance_id) {

        StopInstancesRequest request = StopInstancesRequest.builder()
                .instanceIds(instance_id)
                .build();

        try {
            StopInstancesResponse response = ec2Client.stopInstances(request);
            return response
                    .stoppingInstances()
                    .stream()
                    .map(state -> state.currentState().name().toString())
                    .collect(toList());
        } catch (Exception e) {
            throw Ec2Exception.builder().message("인스턴트 정지에 실패하였습니다.").build();
        }
    }

    @Override
    public List<String> startInstance(String... instance_id) {

        StartInstancesRequest request = StartInstancesRequest.builder()
                .instanceIds(instance_id)
                .build();

        try {
            StartInstancesResponse response = ec2Client.startInstances(request);

            return response
                    .startingInstances()
                    .stream()
                    .map(instanceStateChange -> instanceStateChange.currentState().name().toString())
                    .collect(toList());
        } catch (Ec2Exception e) {
            throw Ec2Exception.builder().message("인스턴스 시작에 실패하였습니다.").build();
        }
    }

    @Override
    public boolean rebootInstance(String... instance_id) {

        RebootInstancesRequest request = RebootInstancesRequest.builder()
                .instanceIds(instance_id)
                .build();

        try {
            RebootInstancesResponse response = ec2Client.rebootInstances(request);

            return response
                    .sdkHttpResponse()
                    .isSuccessful();
        } catch (Exception e) {
            throw Ec2Exception.builder().message("인스턴스 재시작에 실패하였습니다.").build();
        }
    }

    @Override
    public List<String> terminateInstance(String... instance_id) {

        TerminateInstancesRequest request = TerminateInstancesRequest.builder()
                .instanceIds(instance_id)
                .build();

        try {
            TerminateInstancesResponse response = ec2Client.terminateInstances(request);

            return response
                    .terminatingInstances()
                    .stream()
                    .map(state -> state.currentState().name().toString())
                    .collect(toList());
        } catch (Exception e) {
            throw Ec2Exception.builder().message("인스턴스 종료에 실패하였습니다.").build();
        }
    }

    @Override
    public DescribeInstancesResponse listAwsInstance() {

        DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();
        return ec2Client.describeInstances(request);
    }

    @Override
    public AllocateAddressResponse createElasticIpAddress() {

        log.info("ElasticIP Address Create");
        AllocateAddressRequest allocateAddressRequest = AllocateAddressRequest.builder().domain(DomainType.VPC)
                .build();
        return ec2Client.allocateAddress(allocateAddressRequest);
    }

    @Override
    public AssociateAddressResponse associateAddressResponse(String elasticId, String instanceId) {

        log.info("ElasticIP Address Associate ElasticIP ID: {}, InstanceID: {}", elasticId, instanceId);

        ec2Client.describeInstanceStatus()
                .instanceStatuses()
                .forEach(instanceStatus -> log.info("status: {} - {}", instanceStatus.instanceId(), instanceStatus.instanceStatus().status().toString()));

        AssociateAddressRequest request = AssociateAddressRequest.builder()
                .allocationId(elasticId)
                .instanceId(instanceId)
                .build();

        return ec2Client.associateAddress(request);
    }

    @Override
    public DescribeInstancesResponse accessInstanceInformation(String... instance_ids) {
        try {

            DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                    .instanceIds(instance_ids)
                    .build();

            return ec2Client.describeInstances(request);
        } catch (Ec2Exception e) {
            throw Ec2Exception.builder().message(e.getMessage()).build();
        }
    }

    @Override
    public MonitorInstancesResponse instanceMonitor(String... instance_id) {

        MonitorInstancesRequest request = MonitorInstancesRequest.builder()
                .instanceIds(instance_id)
                .build();

        return ec2Client.monitorInstances(request);
    }

    @Override
    public UnmonitorInstancesResponse unMonitorInstance(String... instance_ids) {

        UnmonitorInstancesRequest request = UnmonitorInstancesRequest.builder()
                .instanceIds(instance_ids)
                .build();
        return ec2Client.unmonitorInstances(request);
    }

    @Override
    public DescribeImagesResponse listImages(String... imageIds) {

        Filter filter = Filter.builder()
                .name("is-public").values("false")
                .build();
        DescribeImagesRequest.Builder request = DescribeImagesRequest.builder();

        request.filters(filter);

        if (imageIds != null && imageIds.length > 0) request.imageIds(imageIds);

        return ec2Client.describeImages(request.build());
    }

    @Override
    public CreateTagsResponse createTag(TagBody body) {

        if (body.getResource_ids() == null || body.getResource_ids().length == 0)
            throw Ec2Exception.builder().message("least one [resource_ids] must exist.").build();

        if (body.getTags().size() == 0) throw Ec2Exception.builder().message("least one [tags] must exist").build();

        CreateTagsRequest request = CreateTagsRequest.builder()
                .resources(body.getResource_ids())
                .tags(body.toTags())
                .build();

        return ec2Client.createTags(request);
    }

    @Override
    public DeleteTagsResponse removeTag(TagBody body) {

        if (body.getResource_ids() == null || body.getResource_ids().length == 0)
            throw Ec2Exception.builder().message("least one [resource_ids] must exist.").build();

        if (body.getTags().size() == 0) throw Ec2Exception.builder().message("least one [tags] must exist").build();

        DeleteTagsRequest request = DeleteTagsRequest.builder()
                .resources(body.getResource_ids())
                .tags(body.toTags())
                .build();

        return ec2Client.deleteTags(request);
    }

    @Override
    public DescribeImagesResponse createImage(CreateEC2ImageData data) {

        try {

            CreateImageRequest request = CreateImageRequest.builder()
                    .name(data.getName())
                    .instanceId(data.getInstanceId())
                    .description(data.getDescription())
                    .build();

            CreateImageResponse response = ec2Client.createImage(request);
            return listImages(response.imageId());
        } catch (Ec2Exception e) {
            throw Ec2Exception.builder().message(e.getMessage()).build();
        }
    }

    @Override
    public DeregisterImageResponse removeImage(String imageId) {

        try {
            DeregisterImageRequest request = DeregisterImageRequest.builder()
                    .imageId(imageId)
                    .build();

            return ec2Client.deregisterImage(request);
        } catch (Ec2Exception e) {
            throw Ec2Exception.builder().message(e.getMessage()).build();
        }
    }

    @Override
    public DescribeSecurityGroupsResponse listSecurityGroups(String... sgIds) {
        try {

            DescribeSecurityGroupsRequest.Builder request = DescribeSecurityGroupsRequest.builder();

            if (sgIds != null) request.groupIds(sgIds);

            return ec2Client.describeSecurityGroups(request.build());
        } catch (Ec2Exception e) {
            throw Ec2Exception.builder().message(e.getMessage()).build();
        }
    }
}