import React, { Component, PropTypes } from 'react';
import { connect } from 'react-redux';
import {
  createCustomer,
  loginCustomer,
} from '../../actions';
import {
  getIP,
  getHost,
} from '../../reducers';
import LoginForm from '../LoginForm';
import CreateUserForm from '../CreateUserForm';
import SuccessMessage from '../SuccessMessage';
import FlatButton from 'material-ui/FlatButton';
import Modal from 'react-modal';
import Logo from '../Logo';
import './styles.css';
import '../globalStyles.css';
import { useAuth } from '../../context/AuthContext';
import { SubmissionError } from 'redux-form'

const customStyles = {
  overlay: {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(255, 255, 255, 0.97)',
    boxShadow: '0 2px 4px 0 rgba(0, 0, 0, 0.27)',
    height: '100%',
    width: '100%',
  },
  content: {
    top: '30%',
    left: '50%',
    right: 'auto',
    bottom: 'auto',
    marginRight: '-50%',
    transform: 'translate(-50%, -50%)',
    border: '0x',
  },
};

const TopNav = ({ ip, host, createCustomer, loginCustomer }) => {
  const { user, logout, isAuthenticated } = useAuth();
  const [isCreateModalOpen, setIsCreateModalOpen] = React.useState(false);
  const [isLoginModalOpen, setIsLoginModalOpen] = React.useState(false);
  const [loginSuccessful, setLoginSuccessful] = React.useState(false);
  const [createUserSuccessful, setCreateUserSuccessful] = React.useState(false);

  const handleLoginSuccess = ({ value: { token } }, username) => {
    // Token is now handled by AuthContext
    setLoginSuccessful(true);
    setIsLoginModalOpen(false);
  };

  const handleCreateUserSuccess = (username, password) => {
    setCreateUserSuccessful(true);
    setIsCreateModalOpen(false);

    // temporary sleep so that login will work
    var start = new Date().getTime();
    for (var i = 0; i < 1e7; i++) {
      if (new Date().getTime() - start > 1000) {
        break;
      }
    }

    return loginCustomer(username, password)
      .then((response) => {
        handleLoginSuccess(response, username)
      })
      .catch(err => {
        throw new SubmissionError({ _error: "Error logging in." })
      });
  };

  const handleCreateUser = values => {
    const {
      username,
      password,
    } = values;
    return createCustomer(username, password)
      .then((response) => {
        handleCreateUserSuccess(username, password)
      })
      .catch(err => {
        throw new SubmissionError({ username: "Username already exists" })
      });
  };

  const handleLogin = (values) => {
    return loginCustomer(values.username, values.password)
      .then((response) => {
        handleLoginSuccess(response, values.username)
      })
      .catch(err => {
        throw new SubmissionError({ _error: "Invalid username or password" })
      });
  };

  const renderContainerId = () => {
    return (
      <div className="containerSection">
        {`IP: ${ip} HOST: ${host}`}
      </div>
    );
  };

  const toggleCreateModal = () => {
    setIsCreateModalOpen(!isCreateModalOpen);
    if (!isCreateModalOpen) {
      setCreateUserSuccessful(false);
    }
  };

  const toggleLoginModal = () => {
    setIsLoginModalOpen(!isLoginModalOpen);
    if (!isLoginModalOpen) {
      setLoginSuccessful(false);
    }
  };

  const renderCreateModal = () => {
    const successMessage = 'Congratulations! Your account has been created!';
    const content = createUserSuccessful
      ? <SuccessMessage
        message={successMessage}
        label={'Continue Shopping'}
        handleClick={toggleCreateModal}
      />
      : <CreateUserForm onSubmit={handleCreateUser} onSubmitFail={handleSubmitFail} />;
    return (
      <Modal
        isOpen={isCreateModalOpen}
        onRequestClose={toggleCreateModal}
        style={customStyles}
        contentLabel={''}
      >
        <div className="formContainer">
          {content}
        </div>
      </Modal>
    );
  };

  const renderLoginModal = () => {
    return (
      <Modal
        isOpen={isLoginModalOpen}
        onRequestClose={toggleLoginModal}
        style={customStyles}
        contentLabel={''}
      >
        <div className="formContainer">
          <LoginForm onSubmit={this.handleLogin} />
        </div>
      </Modal>
    );
  };

  const renderUnauthenticated = () => {
    const styles = {
      color: '#fff',
    };
    const labelStyles = {
      textTransform: 'none',
      fontFamily: 'Open Sans',
      fontWeight: 600,
    };

    return (
      <div>
        <FlatButton
          style={styles}
          labelStyle={labelStyles}
          onClick={toggleCreateModal}
          label="Create User"
        />
        <FlatButton
          style={styles}
          labelStyle={labelStyles}
          onClick={toggleLoginModal}
          label="Sign in"
        />
      </div>
    );
  };

  const renderAuthenticated = () => {
    const styles = {
      color: '#fff'
    };
    const labelStyles = {
      textTransform: 'none',
      fontFamily: 'Open Sans',
      fontWeight: 600,
    };
    const welcome = user ? `Welcome, ${user.username}!` : 'Welcome!'
    return (
      <div>
        <span className="welcomeMessage">
          {welcome}
        </span>
        <FlatButton
          style={styles}
          labelStyle={labelStyles}
          onClick={logout}
          label="Sign out"
        />
      </div>
    );
  };

  const handleSubmitFail = (errors) => {
    console.error('Form submission failed:', errors);
  };

  const render = () => {
    return (
      <div className="globalContainer">
        <div className="navHeader">
          <div className="navLogo">
            <Logo />
          </div>
          <div className="navUser">
            {renderContainerId()}
            <div className="buttonSection">
              {isAuthenticated
                ? renderAuthenticated()
                : renderUnauthenticated()}
            </div>
            {renderCreateModal()}
            {renderLoginModal()}
          </div>
        </div>
      </div>
    );
  };

  return render();
}

TopNav.propTypes = {
  ip: PropTypes.string.isRequired,
  host: PropTypes.string.isRequired,
  createCustomer: PropTypes.func.isRequired,
  loginCustomer: PropTypes.func.isRequired,
};

const mapStateToProps = state => ({
  ip: getIP(state),
  host: getHost(state),
});

export default connect(mapStateToProps, {
  createCustomer,
  loginCustomer,
})(TopNav);
