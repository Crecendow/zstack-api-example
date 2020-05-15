# ZStack创建云主机和创建工单

[![license](https://img.shields.io/github/license/snakejordan/administrative-divisions-of-China-on-Python.svg)](https://github.com/snakejordan/administrative-divisions-of-China-on-Python/blob/master/LICENSE)

## 环境要求

- macOS or Linux or Windows

## 依赖包
```
<dependency>
            <groupId>com.squareup.okhttp3</groupId>
            <artifactId>okhttp</artifactId>
            <version>3.5.0</version>
        </dependency>
        <dependency>
            <groupId>org.zstack</groupId>
            <artifactId>sdk</artifactId>
            <version>3.7.4</version>
        </dependency>
        <!-- https://mvnrepository.com/artifact/com.google.code.gson/gson -->
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.8.5</version>
        </dependency>
        <dependency>
            <groupId>commons-beanutils</groupId>
            <artifactId>commons-beanutils</artifactId>
            <version>1.9.3</version>
        </dependency>
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>servlet-api</artifactId>
            <version>2.5</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>commons-codec</groupId>
            <artifactId>commons-codec</artifactId>
            <version>1.9</version>
        </dependency>
    </dependencies>
```


## API来源

https://www.zstack.io/help/dev_manual/dev_guide/11.3.html#c11_3_7

## 配置文件

```
#ZStack端口地址
zstackAddress = "172.18.***.**"
#用户名
zstackAccountName = "admin";
#密码
zstackAccountPassword = "password";
#路径和接口
zstackContextPath = "zstack";
ztackPort = 8080;

```

## 使用说明

完成环境配置及依赖安装后，运行主函数项目成功跑起来之后在地址框中输入相关的地址

#### 功能列表：

- 通过SDK的方式创建云主机
- 通过SDK的方式创建工单
- 判断输入的用户是否已经在ZStack中存在，如果存在就直接创建，不存在则创建一个用户

## 在线接口

#### 请求方法：

支持 GET POST PUT 等请求方法，支持 XHR fetch 等请求方式。

#### Postman 示例：

创建工单：





## Over

:black_nib:That's all. This is a test doc for training myself to write the DEV-DOC，but it is just a simple application, so  I  just write little content. BTW , the official document of ZSatck is incomplete, u need to ponder the field which is deficient when making workOrder by sdk.                  ----阿辉

------

项目过程我写在这了:https://mp.csdn.net/console/editor/html/106096743

