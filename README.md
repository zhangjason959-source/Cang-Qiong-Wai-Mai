# 🍱 Cang-Qiong-Wai-Mai 苍穹外卖系统

一个基于 **Spring Boot + MyBatis + Redis + 微信小程序** 的前后端分离外卖点餐系统。

本项目包含：

- 🖥 SkyTakeOut —— 后端服务（Spring Boot）
- 🌐 Nginx —— 后台管理系统（静态页面）
- 📱 mp-weixin —— 微信小程序客户端

---

# 📁 项目结构

```
Cang-Qiong-Wai-Mai
├── SkyTakeOut-master        # 后端（Spring Boot）
├── nginx-1.20.2             # 后台管理系统（静态页面）
├── mp-weixin                # 微信小程序源码
└── README.md
```

---

# 🛠 技术栈

## 后端

- Spring Boot
- MyBatis
- MySQL
- Redis
- JWT
- 阿里云 OSS
- 微信支付

## 前端

- Nginx 静态部署
- 微信小程序

---

# 🚀 一、后端部署（SkyTakeOut）

## 1️⃣ 创建数据库

执行项目中提供的 SQL 脚本初始化数据库。

---

## 2️⃣ 修改配置文件

文件路径：

```
SkyTakeOut-master/src/main/resources/application.yml
```

需要根据自己的环境进行修改。

---

### ✅ 数据库配置

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/你的数据库名
    username: 你的数据库用户名
    password: 你的数据库密码
```

---

### ✅ Redis 配置

```yaml
redis:
  host: localhost
  port: 6379
  password: 你的redis密码
  database: 0
```

---

### ✅ JWT 配置（建议修改）

```yaml
sky:
  jwt:
    admin-secret-key: 自定义密钥
    user-secret-key: 自定义密钥
```

---

### ✅ 阿里云 OSS 配置

```yaml
alioss:
  endpoint: 你的endpoint
  access-key-id: 你的AccessKeyId
  access-key-secret: 你的AccessKeySecret
  bucket-name: 你的bucket
```

---

### ✅ 微信支付 / 小程序配置

```yaml
wechat:
  appid: 你的appid
  secret: 你的secret
  mchid: 商户号
  mchSerialNo: 商户证书编号
  privateKeyFilePath: 私钥路径
  apiV3Key: APIv3Key
```

⚠️ 请勿将真实密钥上传至 GitHub。

---

## 3️⃣ 启动后端

运行：

```
SkyTakeOutApplication.java
```

默认端口：

```
http://localhost:8080
```

---

# 🌐 二、后台管理系统部署（Nginx）

后台管理系统为静态页面，通过 Nginx 部署。

---

## 1️⃣ 修改配置文件

文件路径：

```
nginx-1.20.2/conf/nginx.conf
```

---

### 修改静态资源路径

```nginx
root  /path/to/nginx/html/sky;
```

改为你本机实际路径，例如：

```nginx
root  F:/Java/xxx/nginx-1.20.2/html/sky;
```

---

### 修改后端接口地址

```nginx
proxy_pass http://127.0.0.1:8080/admin/;
```

如果后端部署在服务器，请改为服务器 IP 地址。

---

## 2️⃣ 启动 Nginx

Windows：

```
nginx.exe
```

访问后台管理系统：

```
http://localhost
```

---

# 📱 三、微信小程序部署（mp-weixin）

## 1️⃣ 打开项目

使用微信开发者工具打开：

```
mp-weixin
```

---

## 2️⃣ 修改接口地址

通常在：

```
utils/request.js
或
config.js
```

修改接口地址：

```javascript
const baseUrl = "http://localhost:8080"
```

如果部署在服务器：

```javascript
const baseUrl = "http://服务器IP"
```

---

## 3️⃣ 注意事项

- 需要在微信公众平台配置服务器域名
- 上线环境必须使用 HTTPS
- 修改为自己的 AppID

---

# 🔐 安全说明

以下内容不要上传至 GitHub：

- 数据库密码
- Redis 密码
- OSS AccessKey
- 微信支付密钥
- API 密钥

建议将真实配置放入：

```
application-prod.yml
```

并加入 `.gitignore`

---

# 🧩 系统访问地址

| 模块 | 地址 |
|------|------|
| 后端接口 | http://localhost:8080 |
| 后台管理系统 | http://localhost |
| 微信小程序 | 微信开发者工具 |

---

# 📌 核心功能

- 用户注册登录
- 商品浏览
- 加入购物车
- 下单支付
- 订单管理
- 商家后台管理
- 数据统计

---

# 🎯 项目用途

本项目用于：

- Spring Boot 实战练习
- 微信小程序开发实践
- 前后端分离架构练习
- Nginx 部署实践

---

# 👤 作者

GitHub: usernameJackson

