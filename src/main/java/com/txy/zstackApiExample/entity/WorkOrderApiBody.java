package com.txy.zstackApiExample.entity;
/**
 * Project Name: zstack.
 * Package Name: com.txy.zstack.entity.
 * File Name: ApiBody
 * Copyright (c) 2020, 南京天芯云数据服务有限公司.
 */

import java.util.List;

/**
 *
 * Class Name: ApiBody
 * FQDN：com.txy.zstack.entity.ApiBody
 * Author: 阿辉
 * Date: 2020/3/9 10:57
 * Description:
 *
 */
public class WorkOrderApiBody {
    private String name;
    private String instanceOfferingUuid;
    private String imageUuid;
    private List<String> l3NetworkUuids;
    private String strategy ;
    private String timeout ;
    private WorkOrderHeaders headers;
    private String id;
    private String hypervisorType;
    private String defaultL3NetworkUuid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstanceOfferingUuid() {
        return instanceOfferingUuid;
    }

    public void setInstanceOfferingUuid(String instanceOfferingUuid) {
        this.instanceOfferingUuid = instanceOfferingUuid;
    }

    public String getImageUuid() {
        return imageUuid;
    }

    public void setImageUuid(String imageUuid) {
        this.imageUuid = imageUuid;
    }

    public List<String> getL3NetworkUuids() {
        return l3NetworkUuids;
    }

    public void setL3NetworkUuids(List<String> l3NetworkUuids) {
        this.l3NetworkUuids = l3NetworkUuids;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public WorkOrderHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(WorkOrderHeaders headers) {
        this.headers = headers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHypervisorType() {
        return hypervisorType;
    }

    public void setHypervisorType(String hypervisorType) {
        this.hypervisorType = hypervisorType;
    }

    public String getDefaultL3NetworkUuid() {
        return defaultL3NetworkUuid;
    }

    public void setDefaultL3NetworkUuid(String defaultL3NetworkUuid) {
        this.defaultL3NetworkUuid = defaultL3NetworkUuid;
    }
}
