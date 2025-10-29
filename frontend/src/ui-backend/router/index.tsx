import { Routes, Route } from 'react-router-dom'
import { lazy } from 'react'
import { Suspense } from 'react'

// 懒加载模块
const Error = lazy(() => import('@/components/error'))
const Layout = lazy(() => import('@/ui-backend/pages/Layout'))
const Home = lazy(() => import('@/ui-backend/pages/Home'))
const Agent = lazy(() => import('@/ui-backend/pages/Agent'))
const UnityHelper = lazy(() => import('@/ui-backend/pages/UnityHelper'))
const Generator = lazy(() => import('@/ui-backend/pages/Generator'))
const Logging = lazy(() => import('@/ui-backend/pages/Logging'))

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
          path="unityhelper"
          element={
            <Suspense fallback={'加载中'}>
              <UnityHelper />
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
          path="generator"
          element={
            <Suspense fallback={'加载中'}>
              <Generator />
            </Suspense>
          }
        />
        <Route
          path="logging"
          element={
            <Suspense fallback={'加载中'}>
              <Logging />
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
