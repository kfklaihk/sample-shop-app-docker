import React, { Component } from 'react';
import { connect } from 'react-redux';
import { AuthConsumer } from '../../context/AuthContext';
import {
  getIP,
  getHost,
} from '../../reducers';
import FlatButton from 'material-ui/FlatButton';
import Logo from '../Logo';
import './styles.css';
import '../globalStyles.css';

class TopNav extends Component {
  constructor(props) {
    super(props);
    this.state = {
      isCreateModalOpen: false,
      isLoginModalOpen: false,
      loginSuccessful: false,
      createUserSuccessful: false,
    };
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

  render() {
    const { ip, host, auth } = this.props;
    const { isAuthenticated, user } = auth;

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
                  Welcome, {user?.username || 'User'}
                </span>
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
  }),
  null
)(TopNavWithAuth);
