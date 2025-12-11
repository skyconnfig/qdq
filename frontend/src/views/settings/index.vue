<script setup lang="ts">
// Settings page placeholder
</script>

<template>
  <div class="settings">
    <div class="page-header">
      <h1 class="page-title">系统设置</h1>
    </div>
    <n-card title="基本设置">
      <n-form label-placement="left" label-width="120">
        <n-form-item label="抢答超时时间">
          <n-input-number :default-value="100" :min="50" :max="500" style="width: 200px">
            <template #suffix>毫秒</template>
          </n-input-number>
        </n-form-item>
        <n-form-item label="默认答题时限">
          <n-input-number :default-value="30" :min="10" :max="120" style="width: 200px">
            <template #suffix>秒</template>
          </n-input-number>
        </n-form-item>
        <n-form-item label="答对得分">
          <n-input-number :default-value="10" :min="1" style="width: 200px">
            <template #suffix>分</template>
          </n-input-number>
        </n-form-item>
        <n-form-item label="答错得分">
          <n-input-number :default-value="0" style="width: 200px">
            <template #suffix>分</template>
          </n-input-number>
        </n-form-item>
        <n-form-item label="启用抢答音效">
          <n-switch :default-value="true" />
        </n-form-item>
        <n-form-item>
          <n-button type="primary">保存设置</n-button>
        </n-form-item>
      </n-form>
    </n-card>
  </div>
</template>
