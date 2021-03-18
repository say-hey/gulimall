# gulimall

#### 商城介绍
谷粒商城，B2C模式，类似京东商城，供应商直接卖给用户的模式。（尚未完成，目前做到检索服务，总进度1/2）
#### 演示地址
- 后台管理：http://39.101.172.171/
- 商城首页：http://39.101.172.171:10000/
- 登录账号：admin
- 登录密码：admin
- 程序目前正常运行，如果不显示页面，等待几秒即可。
#### 项目介绍
分布式微服务项目，前后分离开发，其中每个完整项目对应一个服务，项目规划如下。
- 商品服务：商品的增删改查、商品的上下架、商品详情
- 支付服务：支付服务
- 优惠服务：打折和优惠券
- 用户服务：用户的个人中心、收货地址
- 仓储服务：商品的库存
- 秒杀服务：秒杀商品
- 订单服务：订单增删改查
- 检索服务：商品的检索ES
- 中央认证服务：登录、注册、单点登录、社交登录
- 购物车服务：购物车管理
- 后台管理系统：添加优惠信息等
#### 项目说明
- 目前完成：商品服务、仓储服务、缓存服务、检索服务功能和Nacos注册中心、Gateway网关、OpenFeign远程调用、Redis缓存、Elasticsearch检索、Nginx代理等技术
- 后续增加：单点登录、购物车、订单服务、秒杀系统、熔断降级等功能和权限认证OAuth2、消息队列RabbitMQ、分布式事务Seata、K8s、Kubesphere、Jenkins等技术
#### 项目结构
```text
gulimall
├─gulimall-common 公共组件
├─gulimall-coupon 优惠服务
├─gulimall-gateway 网关
├─gulimall-member 会员服务
├─gulimall-order 订单服务
├─gulimall-product 商品服务
├─gulimall-search 检索服务
├─gulimall-ware 库存服务
├─renren-fast 后台服务
├─renren-generator 代码生成器
```
#### 项目环境
- Java
- MySQL
- SpringBoot
- SpringCloud&Alibaba
- MyBatis
- Docker
- Redis
- Elasticsearch
- Nginx


#### 安装教程

1.  推荐Docker安装MySQL、Redis、Naocs、Elasticsearch、Nginx
2.  人人开源项目：renren-fast（后台）、renren-fast-vue（前端）、renren-generator（代码生成器）
3.  修改配置文件中各个服务地址
4.  数据库测试文件后续上传 

#### 使用说明

1.  登录后台，根据消息框提示使用。
2.  接口文档：https://easydoc.xyz/s/78237135/ZUqEdvA4/HqQGp9TI

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request

#### 鸣谢

- 感谢雷丰阳老师的公开教程