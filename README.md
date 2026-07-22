# myAtoms

myAtoms 是一个面向产品 Demo 构建的 AI 工作台。用户可以通过自然语言、项目文件或私有知识资料，让智能体生成可直接预览的网页代码，并通过 Skill、团队模式和深度研究扩展生成流程。

## 当前能力

- 基于 SiliconFlow Chat Completions API 的 SSE 流式对话
- 实时提取并预览模型生成的 HTML，支持复制代码
- 生成过程中可主动终止输出，并保留已生成内容
- 工程师、智能体团队和深度研究三种工作模式
- 首页和对话页支持上传文件或文件夹，提交后分析项目内容
- 私有知识库上传、文本分块、关键词召回和对话 RAG
- 面向产品 Demo 的 Skill 广场，支持安装、直接使用和关键词自动匹配
- Tavily 多查询搜索、来源去重与研究引用
- 统一的发现、Skill 广场、知识库和对话页导航

## 技术栈

| 模块 | 技术 |
| --- | --- |
| 前端 | Vue 3、Vue Router、Vite 8、Marked |
| 后端 | Java 17、Spring Boot 4、OkHttp、Maven Wrapper |
| 大模型 | SiliconFlow Chat Completions API |
| 联网搜索 | Tavily Search API（可选） |

## 项目结构

```text
myAtoms/
├── frontend/                 # Vue 前端
│   ├── src/components/       # 公共组件，例如统一导航
│   ├── src/stores/           # 页面间共享状态，例如待提交附件
│   ├── src/views/            # 首页、对话、Skill、知识库页面
│   └── vite.config.js        # 开发服务器及 /api 代理
├── backend/                  # Spring Boot 后端
│   ├── src/main/java/.../
│   │   ├── controller/       # Chat、Skill、Knowledge API
│   │   ├── service/          # 智能体、搜索、知识库与 Skill 逻辑
│   │   └── utils/            # SiliconFlow HTTP/SSE 客户端
│   └── src/main/resources/application.yml
└── README.md
```

## 环境要求

- Node.js 20 或更高版本
- npm
- JDK 17 或更高版本
- SiliconFlow API Key
- Tavily API Key（仅深度研究的联网搜索需要）

## 快速开始

### 1. 配置后端密钥

复制示例配置：

```bash
cd backend
cp .env.example .env
```

编辑 `backend/.env`：

```properties
SILICONFLOW_API_KEY=你的_siliconflow_api_key
TAVILY_API_KEY=你的_tavily_api_key
```

`SILICONFLOW_API_KEY` 是普通对话和代码生成的必需配置。`TAVILY_API_KEY` 只负责互联网搜索，SiliconFlow 本身是模型推理服务，不能替代搜索引擎获取实时网页资料。

服务端默认模型配置位于 `backend/src/main/resources/application.yml`：

```yaml
llm:
  base-url: https://api.siliconflow.cn
  model: Pro/zai-org/GLM-5.1
```

### 2. 启动后端

macOS 或 Linux：

```bash
cd backend
./mvnw spring-boot:run
```

Windows：

```powershell
cd backend
mvnw.cmd spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

### 3. 启动前端

打开另一个终端：

```bash
cd frontend
npm install
npm run dev
```

访问 `http://localhost:5173`。开发环境会把 `/api` 请求代理到 `http://localhost:8080`。

## 功能使用

### 生成产品 Demo

在首页输入需求并发送，系统会进入对话页流式生成结果。生成的完整 HTML 会同步显示在右侧代码区和预览区。生成期间，发送按钮会变成红色终止按钮。

### 上传文件或项目文件夹

1. 点击输入框左下角的 `+`。
2. 选择“上传文件”或“上传文件夹”。
3. 文件会先作为附件卡片显示，可以在提交前移除。
4. 点击发送后，系统才读取文件内容并提交分析。

附件适合代码、Markdown、TXT、JSON、CSV、YAML 和常见配置文件。当前限制为最多 100 个文件、单文件 512 KB、总内容约 24 万字符，并自动忽略 `node_modules`、`.git`、`dist`、`build`、`target` 等目录。图片、PDF、Word、压缩包等二进制内容暂不解析。

### 使用 Skill

进入“Skill 广场”，安装需要的 Skill 后可直接使用。当前 Skill 主要覆盖：

- 高质量落地页
- SaaS 产品界面
- 移动端 Web App
- 电商商城 Demo
- 表单与 CRUD 应用
- 登录与用户中心
- 数据可视化看板
- 动效与视觉升级
- Spring Boot API
- Demo 测试与修复

直接从 Skill 卡片进入对话会显式启用该 Skill；已经安装的 Skill 也会根据用户提示词自动匹配。

### 使用知识库

1. 进入“知识库”。
2. 选择 UTF-8 文本文件，可填写知识标题。
3. 点击“上传并记忆”。
4. 使用“测试知识召回”输入相关问题，确认能检索到对应片段。
5. 在对话中提出相关问题，后端会检索知识片段并加入模型上下文。

当前知识库使用后端内存保存，后端重启后数据会消失，适合 Demo 验证。生产环境应替换为数据库或向量数据库，并增加 PDF、Word 等格式解析。

### 使用深度研究

在输入菜单中开启“深度研究”后发送研究任务。配置了 `TAVILY_API_KEY` 时，后端会执行联网搜索并把结果来源加入研究上下文；未配置或搜索服务异常时，普通 SiliconFlow 对话仍可使用，但无法获取实时互联网资料。

## 主要 API

| 方法 | 地址 | 用途 |
| --- | --- | --- |
| POST | `/api/chat/completions` | 非流式模型对话 |
| POST | `/api/chat/completions/stream` | SSE 流式对话、Skill、附件和知识上下文 |
| GET | `/api/skills` | 获取 Skill 列表 |
| POST | `/api/skills/{id}/install` | 安装 Skill |
| DELETE | `/api/skills/{id}` | 卸载 Skill |
| GET | `/api/knowledge` | 获取知识列表 |
| POST | `/api/knowledge` | 上传知识文件，使用 multipart/form-data |
| GET | `/api/knowledge/search?q=...` | 测试知识召回 |
| DELETE | `/api/knowledge/{id}` | 删除知识 |

流式对话示例：

```bash
curl -N --request POST \
  --url http://localhost:8080/api/chat/completions/stream \
  --header 'Content-Type: application/json' \
  --data '{
    "messages": [
      {"role": "user", "content": "生成一个可交互的产品落地页"}
    ],
    "mode": "engineer",
    "skillIds": [],
    "knowledgeIds": [],
    "stream": true
  }'
```

模型名称和 API Key 由后端统一控制，客户端不应提交或保存密钥。

## 验证与构建

前端生产构建：

```bash
cd frontend
npm run build
```

后端测试：

```bash
cd backend
./mvnw test
```

## 常见问题

### 返回 HTTP 503

通常表示后端没有读取到 `SILICONFLOW_API_KEY`，或者上游模型服务暂时不可用。确认 `backend/.env` 存在、密钥有效，并重启后端。

### 深度研究无法联网或超时

确认 `TAVILY_API_KEY` 有效，并检查 Tavily 配额及后端日志。当前流式请求以连续 3 分钟没有新数据作为前端空闲超时，Spring 异步请求超时为 15 分钟。

### 上传知识后重启服务，资料消失

这是当前内存知识库的预期行为。需要长期保存时，应接入数据库、对象存储或向量数据库。

### 上传文件夹后部分文件被跳过

依赖目录、构建产物、二进制文件、超出大小限制的文件会被自动忽略，以避免请求体和模型上下文过大。
