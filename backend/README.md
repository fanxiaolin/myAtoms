# myAtoms-backend

基于 Spring Boot 的大模型对话后端服务，通过 SiliconFlow Chat Completions API 调用指定模型。

## 架构

- `controller/` — 提供对外 REST API，负责请求参数校验与响应返回
- `service/` — 处理模型配置、请求组装及异常转换
- `utils/` — 基于 OkHttp 封装 SiliconFlow HTTP 调用
- `application.yml` — 配置服务端口、模型地址、模型名称及 API Key

## 快速开始

### 后端

运行环境：JDK 17 或更高版本。项目已提供 Maven Wrapper，无需单独安装 Maven。

启动前配置 `src/main/resources/application.yml`：

```yaml
server:
  port: 8080

llm:
  base-url: https://api.siliconflow.cn
  model: Pro/zai-org/GLM-5.1
  api-key: YOUR_API_KEY
```

将 `YOUR_API_KEY` 替换为有效的 SiliconFlow API Key，然后启动服务：

```bash
./mvnw spring-boot:run
# 服务地址：http://localhost:8080
```

## 接口调用

外部请求不需要指定 `model`，服务端统一使用 `application.yml` 中的 `llm.model`。

```bash
curl --request POST \
  --url http://localhost:8080/api/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [
      {"role": "system", "content": "你是一个有用的助手"},
      {"role": "user", "content": "你好，请介绍一下你自己"}
    ]
  }'
```

### 流式输出

需要实时接收思考过程和最终 HTML 时，调用 SSE 接口：

```bash
curl -N --request POST \
  --url http://localhost:8080/api/chat/completions/stream \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [
      {"role": "user", "content": "生成一个登录页面，只返回完整 HTML"}
    ]
  }'
```

接口会强制启用流式输出和思考模式，并原样转发 SiliconFlow SSE。前端分别读取
`choices[0].delta.reasoning_content`（思考过程）与
`choices[0].delta.content`（最终 HTML）；流以 `data: [DONE]` 结束。

## 验证

```bash
./mvnw test
```
