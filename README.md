
## 结构

- **前端** `frontend/` — Vue.js 14 + vite
- **后端** `backend/` — Java Spring Boot

## 能力

- 由于周内时间关系，只能简洁实现和智能体交互的流程，其他页面和功能此版本还是静态

## 快速开始

### 后端

```bash
cd backend
# 需安装 JDK 17+，首次启动需要联网下载 Maven 依赖
# 编辑 src/main/resources/application.yml，填写有效的 SiliconFlow API Key
./mvnw spring-boot:run
# 服务地址：http://localhost:8080
# 接口地址：POST http://localhost:8080/api/
```
Windows 使用：
```bash
cd backend
mvnw.cmd spring-boot:run
```

### 前端

```bash
cd frontend
npm install
npm run dev
# 访问 http://localhost:5173
```
