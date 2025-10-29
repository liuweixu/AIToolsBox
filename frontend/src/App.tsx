import { Router } from '@/ui-backend/router'
import { useLocation } from 'react-router-dom'
import 'react-quill-new/dist/quill.snow.css'

function App() {
  const location = useLocation()

  if (location.pathname.startsWith('/')) {
    return <Router />
  }
}

export default App
