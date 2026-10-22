# java-learn-hub

面向 Java / Spring 生态的学习门户。仓库路径：`D:\worksapce\java-learn-hub`。

- 需求：[docs/requirements/PRD.md](docs/requirements/PRD.md)
- 内容格式：[docs/content-schema.md](docs/content-schema.md)
- 学习项源文件：`content/topics/*.json`

## 技术栈（一期）

- Java 21 + Spring Boot 3.5（只读 API，内容从 classpath JSON 加载）
- Vue 3 + Vite + TypeScript + Element Plus
- 不强制 MySQL / Redis / 消息队列

本机默认 `java` 若是 8，请先指定 JDK 21。若安装了 IntelliJ，可用其自带 JBR：

```powershell
$env:JAVA_HOME = "D:\soft\IntelliJ IDEA 2024.3.6\jbr"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
java -version
```

## 启动

后端：

```powershell
cd D:\worksapce\java-learn-hub\backend
mvn spring-boot:run
```

前端（另开终端）：

```powershell
cd D:\worksapce\java-learn-hub\frontend
npm install
npm run dev
```

浏览器打开 http://localhost:5173 。API：http://localhost:8080/api/v1/topics

## 新增学习项

1. 在 `content/topics/{slug}.json` 按 schema 编写。
2. `published` 必须有概述、学习路线、至少 1 个示例、至少 3 条常见坑。
3. 重启后端即可，前端无需加路由。
