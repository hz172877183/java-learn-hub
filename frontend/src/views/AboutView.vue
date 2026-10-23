<!-- 关于页：项目边界与内容规范。 -->
<script setup lang="ts">
</script>

<template>
  <main class="page">
    <h1>关于本项目</h1>
    <p>
      java-learn-hub 是一份可运行的学习地图：内容在仓库 <code>content/</code> 中维护，后端 Spring Boot 提供只读
      API，前端 Vue 3 负责目录与详情。
    </p>
    <h2>本地运行</h2>
    <ol>
      <li>JDK 21（本机默认若为 Java 8，请把 JAVA_HOME 指到 21）。</li>
      <li>
        后端：在 <code>backend</code> 执行
        <code>mvn spring-boot:run</code>，端口 8080。
      </li>
      <li>
        前端：在 <code>frontend</code> 执行 <code>npm install</code> 与
        <code>npm run dev</code>，端口 5173。
      </li>
    </ol>
    <h2>一期边界</h2>
    <p>不强制启动 MySQL / Redis / RabbitMQ。消息队列主推 RabbitMQ，Kafka 为对照页。示例不调用付费外部 API。</p>
  </main>
</template>
