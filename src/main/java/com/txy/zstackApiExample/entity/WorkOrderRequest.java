package com.txy.zstackApiExample.entity;
/**
 * Project Name: zstack.
 * Package Name: com.txy.zstack.entity.
 * File Name: Request
 * Copyright (c) 2020, 南京天芯云数据服务有限公司.
 */

/**
 *
 * Class Name: Request
 * FQDN：com.txy.zstack.entity.Request
 * Author: 阿辉
 * Date: 2020/3/9 10:58
 * Description:
 *
 */
public class WorkOrderRequest {
    private String requestName;
    private String apiName;
    private String executeTimes;
    private WorkOrderApiBody apiBody;

    public String getRequestName() {
        return requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getExecuteTimes() {
        return executeTimes;
    }

    public void setExecuteTimes(String executeTimes) {
        this.executeTimes = executeTimes;
    }

    public WorkOrderApiBody getapiBody() {
        return apiBody;
    }

    public void setapiBody(WorkOrderApiBody apiBody) {
        this.apiBody = apiBody;
    }
}
