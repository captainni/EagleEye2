<template>
  <div class="bg-white rounded-lg shadow-sm p-6 card-hover">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-lg font-semibold text-gray-800 flex items-center">
        <i class="fas fa-chart-line text-primary mr-2"></i> 数据趋势
      </h3>
    </div>
    <div ref="chartRef" class="h-64"></div>
  </div>
</template>

<script lang="ts" setup>
import { defineProps, ref, onMounted, watch } from 'vue';
import * as echarts from 'echarts';

const props = defineProps<{
  chartData: any;
}>();

const chartRef = ref<HTMLDivElement | null>(null);
let chart: echarts.ECharts | null = null;

onMounted(() => {
  if (!chartRef.value) return;

  chart = echarts.init(chartRef.value);
  updateChart();

  window.addEventListener('resize', () => chart?.resize());
});

function updateChart() {
  if (!chart || !props.chartData) return;

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: props.chartData.datasets?.map((d: any) => d.label) || []
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: props.chartData.labels || []
    },
    yAxis: {
      type: 'value'
    },
    series: props.chartData.datasets?.map((dataset: any) => ({
      name: dataset.label,
      type: 'line',
      data: dataset.data || [],
      smooth: true
    })) || []
  };

  chart.setOption(option);
}

watch(() => props.chartData, () => {
  updateChart();
}, { deep: true });
</script>

<style scoped>
.card-hover:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}
.card-hover {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
</style>
