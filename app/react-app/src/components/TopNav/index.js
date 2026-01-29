import React, { Component } from 'react';
import { connect } from 'react-redux';
import { AuthConsumer } from '../../context/AuthContext';
import {
  getIP,
  getHost,
  getCartProducts,
  getTotal,
  getQuantityById,
  getCustomerId,
} from '../../reducers';
import FlatButton from 'material-ui/FlatButton';
import Logo from '../Logo';
import './styles.css';
import '../globalStyles.css';
import CheckoutModal from '../CheckoutModal';
import { fetchCart, createOrder, clearCartContents } from '../../actions';

class TopNav extends Component {
  constructor(props) {
    super(props);
    this.state = {
      isCreateModalOpen: false,
      isLoginModalOpen: false,
      loginSuccessful: false,
      createUserSuccessful: false,
      isCheckoutOpen: false,
      isProcessingCheckout: false,
      checkoutError: '',
    };
  }

  componentDidMount() {
    const { auth, fetchCart } = this.props;
    if (auth && auth.isAuthenticated) {
      fetchCart();
    }
  }

  componentDidUpdate(prevProps) {
    const { auth, fetchCart } = this.props;
    if (auth.isAuthenticated && !prevProps.auth.isAuthenticated) {
      fetchCart();
    }

    if (!auth.isAuthenticated && prevProps.auth.isAuthenticated && this.state.isCheckoutOpen) {
      this.setState({ isCheckoutOpen: false, checkoutError: '', isProcessingCheckout: false });
    }
  }

  openCreateModal = () => {
    this.setState({ isCreateModalOpen: true });
  };

  closeCreateModal = () => {
    this.setState({ isCreateModalOpen: false, createUserSuccessful: false });
  };

  openLoginModal = () => {
    this.setState({ isLoginModalOpen: true });
  };

  closeLoginModal = () => {
    this.setState({ isLoginModalOpen: false, loginSuccessful: false });
  };

  handleLogout = () => {
    const { auth } = this.props;
    auth.logout();
  };

  openCheckoutModal = () => {
    const { fetchCart } = this.props;
    fetchCart();
    this.setState({ isCheckoutOpen: true, checkoutError: '' });
  };

  closeCheckoutModal = () => {
    if (this.state.isProcessingCheckout) {
      return;
    }
    this.setState({ isCheckoutOpen: false, checkoutError: '' });
  };

  handleConfirmCheckout = () => {
    const { createOrder, quantityById, customerId } = this.props;
    const orderQuantities = quantityById || {};
    const itemsInCart = Object.keys(orderQuantities).length;

    if (!itemsInCart) {
      this.setState({ checkoutError: 'Your cart is empty.' });
      return;
    }

    this.setState({ isProcessingCheckout: true, checkoutError: '' });

    const payload = {
      orderDate: new Date().toISOString(),
      customerId: customerId || null,
      quantityById: orderQuantities,
    };

    createOrder(payload)
      .then(() => {
        this.setState({ isProcessingCheckout: false, isCheckoutOpen: false });
      })
      .catch(() => {
        this.setState({
          isProcessingCheckout: false,
          checkoutError: 'Failed to submit order. Please try again.',
        });
      });
  };

  handleFlushCheckout = () => {
    const { clearCartContents } = this.props;

    this.setState({ isProcessingCheckout: true, checkoutError: '' });

    clearCartContents()
      .then(() => {
        this.setState({ isProcessingCheckout: false, isCheckoutOpen: false });
      })
      .catch(() => {
        this.setState({
          isProcessingCheckout: false,
          checkoutError: 'Failed to clear the cart. Please try again.',
        });
      });
  };

  render() {
    const {
      ip,
      host,
      auth,
      cartItems,
      cartTotal,
    } = this.props;
    const { isAuthenticated, user } = auth;
    const { isCheckoutOpen, isProcessingCheckout, checkoutError } = this.state;
    const cartCount = cartItems ? cartItems.length : 0;

    return (
      <div className="top-nav">
        <div className="nav-container">
          <Logo />
          <div className="nav-info">
            <span>Host: {host}</span>
            <span>IP: {ip}</span>
          </div>
          <div className="nav-actions">
            {isAuthenticated ? (
              <div style={{ display: 'flex', alignItems: 'center' }}>
                <span style={{ marginRight: '10px' }}>
                  Welcome, {user && user.username || 'User'}
                </span>
                <FlatButton
                  label={`Checkout (${cartCount})`}
                  onClick={this.openCheckoutModal}
                  style={{ color: '#fff', border: '1px solid rgba(255,255,255,0.5)', marginRight: '10px' }}
                />
                <FlatButton
                  label="Logout"
                  onClick={this.handleLogout}
                  style={{ color: '#fff' }}
                />
              </div>
            ) : (
              <div>
                <FlatButton
                  label="Login"
                  onClick={this.openLoginModal}
                  style={{ color: '#fff' }}
                />
                <FlatButton
                  label="Register"
                  onClick={this.openCreateModal}
                  style={{ color: '#fff' }}
                />
              </div>
            )}
          </div>
        </div>
        <CheckoutModal
          open={isCheckoutOpen}
          cartItems={cartItems}
          total={cartTotal}
          onClose={this.closeCheckoutModal}
          onConfirm={this.handleConfirmCheckout}
          onFlush={this.handleFlushCheckout}
          isProcessing={isProcessingCheckout}
          errorMessage={checkoutError}
        />
      </div>
    );
  }
}

const TopNavWithAuth = (props) => (
  <AuthConsumer>
    {(auth) => <TopNav {...props} auth={auth} />}
  </AuthConsumer>
);

export default connect(
  (state) => ({
    ip: getIP(state),
    host: getHost(state),
    cartItems: getCartProducts(state),
    cartTotal: getTotal(state),
    quantityById: getQuantityById(state),
    customerId: getCustomerId(state),
  }),
  {
    fetchCart,
    createOrder,
    clearCartContents,
  }
)(TopNavWithAuth);
