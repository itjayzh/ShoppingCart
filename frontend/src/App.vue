<script setup lang="ts">
import HelloWorld from './components/HelloWorld.vue'
</script>

<template>
  <router-view></router-view>
</template>

<style>
html, body {
  margin: 0;
  padding: 0;
  font-family: Arial, sans-serif;
  height: 100%;
  width: 100%;
  overflow-x: hidden;
}

body {
  background-color: #f5f7fa;
}

#app {
  width: 100%;
  min-height: 100vh;

}

.el-card {
  overflow: hidden;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.el-button {
  border-radius: 6px;
}

.el-input__wrapper {
  transition: all 0.3s ease;
}
</style>
