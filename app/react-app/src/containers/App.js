import React, { Component } from 'react'
import { hashHistory } from 'react-router'
import { AuthConsumer } from '../context/AuthContext'
import GradientBackground from '../components/GradientBackground'
import TopNav from '../components/TopNav'
import Footer from '../components/Footer'
import Header from '../components/Header'
import TitleContainer from './TitleContainer'
import ProductsContainer from './ProductsContainer'

class App extends Component {
  componentDidUpdate() {
    const { auth } = this.props;
    if (!auth.loading && !auth.isAuthenticated) {
      hashHistory.push('/auth');
    }
  }

  render() {
    const { auth } = this.props;

    if (auth.loading) {
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

    if (!auth.isAuthenticated) {
      return null; // Will redirect in componentDidUpdate
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
}

export default (props) => (
  <AuthConsumer>
    {(auth) => <App {...props} auth={auth} />}
  </AuthConsumer>
);
