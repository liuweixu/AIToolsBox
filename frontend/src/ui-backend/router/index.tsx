import { Routes, Route } from 'react-router-dom'
import { lazy } from 'react'
import { Suspense } from 'react'

// 懒加载模块
const Error = lazy(() => import('@/components/error'))
const Layout = lazy(() => import('@/ui-backend/pages/Layout'))
const Home = lazy(() => import('@/ui-backend/pages/Home'))
const Agent = lazy(() => import('@/ui-backend/pages/Agent'))
const UnityHelper = lazy(() => import('@/ui-backend/pages/UnityHelper'))
const Text2Image = lazy(() => import('@/ui-backend/pages/Text2Image'))
const Text2Video = lazy(() => import('@/ui-backend/pages/Text2Video'))

export const Router = () => {
  return (
    <Routes>
      <Route
        path="/"
        element={
          <Suspense fallback={'加载中'}>
            <Layout />
          </Suspense>
        }>
        <Route
          path=""
          element={
            <Suspense fallback={'加载中'}>
              <Home />
            </Suspense>
          }
        />
        <Route
          path="unityhelper/:id"
          element={
            <Suspense fallback={'加载中'}>
              <UnityHelper />
            </Suspense>
          }
        />
        <Route
          path="unityhelper"
          element={
            <Suspense fallback={'加载中'}>
              <UnityHelper />
            </Suspense>
          }
        />
        <Route
          path="agent/:id"
          element={
            <Suspense fallback={'加载中'}>
              <Agent />
            </Suspense>
          }
        />
        <Route
          path="agent"
          element={
            <Suspense fallback={'加载中'}>
              <Agent />
            </Suspense>
          }
        />
        <Route
          path="text2image"
          element={
            <Suspense fallback={'加载中'}>
              <Text2Image />
            </Suspense>
          }
        />
        <Route
          path="text2video"
          element={
            <Suspense fallback={'加载中'}>
              <Text2Video />
            </Suspense>
          }
        />
      </Route>
      <Route
        path="*"
        element={
          <Suspense fallback={'加载中'}>
            <Error />
          </Suspense>
        }
      />
    </Routes>
  )
}
