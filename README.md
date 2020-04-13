# ZStack创建云主机和创建工单

[![license](https://img.shields.io/github/license/snakejordan/administrative-divisions-of-China-on-Python.svg)](https://github.com/snakejordan/administrative-divisions-of-China-on-Python/blob/master/LICENSE)

## 环境要求

- macOS or Linux or Windows

## 依赖包

[![redis](https://img.shields.io/pypi/v/redis.svg?label=redis)](https://pypi.org/project/redis/)

## API来源

https://www.zstack.io/help/dev_manual/dev_guide/11.3.html#c11_3_7

## 配置文件

```python
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

