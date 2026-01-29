import React, { Component } from 'react'
import { hashHistory } from 'react-router'
import { connect } from 'react-redux'
import { AuthConsumer } from '../context/AuthContext'
import GradientBackground from '../components/GradientBackground'
import TopNav from '../components/TopNav'
import Footer from '../components/Footer'
import Header from '../components/Header'
import TitleContainer from './TitleContainer'
import ProductsContainer from './ProductsContainer'
import { fetchAllItems } from '../actions'

class App extends Component {
  componentDidMount() {
    const { auth, fetchAllItems } = this.props;
    if (!auth.loading && !auth.isAuthenticated) {
      hashHistory.push('/auth');
    } else if (auth.isAuthenticated) {
      fetchAllItems();
    }
  }

  componentDidUpdate(prevProps) {
    const { auth, fetchAllItems } = this.props;
    if (!auth.loading && !auth.isAuthenticated) {
      hashHistory.push('/auth');
    }
    
    // Re-fetch products when authentication status changes
    if (auth.isAuthenticated && !prevProps.auth.isAuthenticated) {
      console.log('User authenticated, re-fetching products...');
      fetchAllItems();
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

const ConnectedApp = connect(
  null,
  { fetchAllItems }
)(App);

export default (props) => (
  <AuthConsumer>
    {(auth) => <ConnectedApp {...props} auth={auth} />}
  </AuthConsumer>
);
