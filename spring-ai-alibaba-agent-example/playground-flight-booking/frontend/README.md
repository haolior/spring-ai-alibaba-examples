## 启动说明

```shell
# 创建 Vite Vue 项目
# https://vuejs.org/guide/scaling-up/tooling.html
# Vue CLI 是官方基于 webpack 的工具链，目前已进入维护模式。
# 除非依赖 webpack 专属能力，推荐新项目优先使用 Vite。
# 大多数场景下，Vite 能提供更好的开发体验。

# 注意本地环境版本
# node v18.0.0
# yarn 1.22.21

npm create vue@latest

yarn

yarn format

# 启动开发服务
yarn dev

# 主要组件库
yarn add ant-design-vue@4.x
```

## 开发说明

1. 待办功能

如果功能尚未完成，但页面上已经展示入口，请使用 `notification.todo` 标记。

```shell
/**
 * 这个函数用于展示问答提示
 * TODO
 */
function globalQuestion() {
  devTool.todo("展示问答提示")
}
```

2. 全局 CSS 变量

```js
// 如果需要使用全局 CSS 变量，例如主色
// 可以通过 ref 创建响应式引用
export const PRIMARY_COLOR = ref('#17b392')

// 在 JS 中使用
import {PRIMARY_COLOR} from '@/base/constants'

// 在 CSS 中使用 __null 显式引用，避免被代码格式化清理
let __null = PRIMARY_COLOR
```

3. Provide / Inject

按 Vue 组合式 API 的方式使用即可。

4. 图标

图标资源地址：https://icones.js.org/

```html
<Icon icon="svg-spinners:pulse-ring" />
```

5. API

约定：如果接口路径以 `mock` 开头，则返回 mock 结果。

声明接口：

```ts
import request from '@/base/http/request'

export const getClusterInfo = (params: any):Promise<any> => {
    return request({
        url: '/metrics/cluster',
        method: 'get',
        params
    })
}
```

声明 mock 接口：

```ts
// 定义 mock 接口
import Mock from 'mockjs'
Mock.mock('/mock/metrics/cluster', 'get', {
    code: 200,
    message: '成功',
    data: {
        all: Mock.mock('@integer(100, 500)'),
        application: Mock.mock('@integer(80, 200)'),
        consumers: Mock.mock('@integer(80, 200)'),
        providers: Mock.mock('@integer(80, 200)'),
        services: Mock.mock('@integer(80, 200)'),
        versions: ["dubbo-golang-3.0.4"],
        protocols: ["tri"],
        rules: [],
        configCenter: "127.0.0.1:2181",
        registry: "127.0.0.1:2181",
        metadataCenter: "127.0.0.1:2181",
        grafana: "127.0.0.1:3000",
        prometheus: "127.0.0.1:9090"
    }
})

// 在 main.ts 中引入
import './api/mock/mockCluster.js'
```

调用接口：

```ts
onMounted(async () => {
  let {data} = await getClusterInfo({})
})
```

请求目标由 `request.ts` 决定：

```ts
const service: AxiosInstance = axios.create({
    // 修改这里决定请求转发到哪里
    baseURL: '/mock',
    timeout: 30 * 1000
})
```

## 构建与部署

构建：

```shell
yarn build
```

部署：

将 `dist/assets` 文件夹复制到 `src/main/resources/static`。
将 `dist/index.html` 文件复制到 `src/main/resources/templates`。
