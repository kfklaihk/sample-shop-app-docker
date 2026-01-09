import React, { useEffect } from 'react'
import { hashHistory } from 'react-router'
import { useAuth } from '../context/AuthContext'
import GradientBackground from '../components/GradientBackground'
import TopNav from '../components/TopNav'
import Footer from '../components/Footer'
import Header from '../components/Header'
import TitleContainer from './TitleContainer'
import ProductsContainer from './ProductsContainer'

const App = () => {
  const { isAuthenticated, loading } = useAuth();

  useEffect(() => {
    if (!loading && !isAuthenticated) {
      hashHistory.push('/auth');
    }
  }, [isAuthenticated, loading]);

  if (loading) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        fontSize: '18px'
      }}>
        Loading...
      </div>
    );
  }

  if (!isAuthenticated) {
    return null; // Will redirect in useEffect
  }

  return (
    <div>
      <GradientBackground />
      <TopNav />
      <Header />
      <TitleContainer />
      <ProductsContainer />
      <Footer />
    </div>
  );
}

export default App
