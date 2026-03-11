<template>
  <div class="app-layout">
    <aside class="sidebar">
      <div class="sidebar-logo">
        <div class="logo-icon">📖</div>
        <h1>Novel RAG</h1>
      </div>
      <nav class="sidebar-nav">
        <router-link to="/" class="nav-item" :class="{ active: $route.path === '/' }">
          <span class="nav-icon">💬</span>
          <span>智能问答</span>
        </router-link>
        <router-link to="/upload" class="nav-item" :class="{ active: $route.path === '/upload' }">
          <span class="nav-icon">📄</span>
          <span>文档管理</span>
        </router-link>
        <router-link to="/settings" class="nav-item" :class="{ active: $route.path === '/settings' }">
          <span class="nav-icon">⚙️</span>
          <span>模型设置</span>
        </router-link>
      </nav>
      <div style="padding: 12px 16px; font-size: 12px; color: var(--text-muted);">
        人物关系分析系统 v1.0
      </div>
    </aside>
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
</script>
