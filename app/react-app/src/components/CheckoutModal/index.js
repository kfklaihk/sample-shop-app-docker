import React, { PropTypes } from 'react';
import FlatButton from 'material-ui/FlatButton';
import RaisedButton from 'material-ui/RaisedButton';
import './styles.css';

const CheckoutModal = ({
  open,
  cartItems,
  total,
  onClose,
  onConfirm,
  onFlush,
  isProcessing,
  errorMessage,
}) => {
  if (!open) {
    return null;
  }

  const hasItems = cartItems && cartItems.length > 0;

  return (
    <div className="checkout-modal-backdrop">
      <div className="checkout-modal">
        <div className="checkout-modal__header">
          <div className="checkout-modal__title">Review Your Cart</div>
          <button
            className="checkout-modal__close"
            onClick={onClose}
            type="button"
            aria-label="Close checkout dialog"
            disabled={isProcessing}
          >
            X
          </button>
        </div>

        <div className="checkout-modal__body">
          {hasItems ? (
            <div>
              <div className="checkout-modal__list">
                {cartItems.map(item => (
                  <div className="checkout-modal__row" key={item.productId}>
                    <div className="checkout-modal__product">
                      <div className="checkout-modal__product-name">{item.name}</div>
                      <div className="checkout-modal__product-meta">Qty: {item.quantity}</div>
                    </div>
                    <div className="checkout-modal__price">
                      ${(item.price * item.quantity).toFixed(2)}
                    </div>
                  </div>
                ))}
              </div>
              <div className="checkout-modal__total">
                <span>Total</span>
                <span>${total}</span>
              </div>
            </div>
          ) : (
            <div className="checkout-modal__empty">Your cart is empty.</div>
          )}
        </div>

        {errorMessage && (
          <div className="checkout-modal__error" role="alert">
            {errorMessage}
          </div>
        )}

        <div className="checkout-modal__footer">
          <FlatButton
            label="Flush Cart"
            className="checkout-modal__flush"
            onClick={onFlush}
            disabled={!hasItems || isProcessing}
          />
          <RaisedButton
            label={isProcessing ? 'Submitting...' : 'Confirm Order'}
            primary
            onClick={onConfirm}
            disabled={!hasItems || isProcessing}
          />
        </div>
      </div>
    </div>
  );
};

CheckoutModal.propTypes = {
  open: PropTypes.bool.isRequired,
  cartItems: PropTypes.arrayOf(PropTypes.shape({
    productId: PropTypes.number.isRequired,
    name: PropTypes.string,
    price: PropTypes.number,
    quantity: PropTypes.number,
  })),
  total: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.number,
  ]),
  onClose: PropTypes.func.isRequired,
  onConfirm: PropTypes.func.isRequired,
  onFlush: PropTypes.func.isRequired,
  isProcessing: PropTypes.bool,
  errorMessage: PropTypes.string,
};

CheckoutModal.defaultProps = {
  cartItems: [],
  total: '0.00',
  isProcessing: false,
  errorMessage: '',
};

export default CheckoutModal;
