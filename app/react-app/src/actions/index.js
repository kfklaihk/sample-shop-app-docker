const request = require('superagent-promise')(require('superagent'), Promise)
import { getJwtToken } from './storage'
import shop from '../api/shop'
import * as types from '../constants/ActionTypes'

const API = '/api'
const UTILITY = '/utility'

export const createOrder = (values) => (dispatch) => {
  const url = `${API}/order/`
  let dispatchObj = {
    type: types.CREATE_ORDER,
    payload: {
      promise:
      request
        .post(url)
        // TODO: will there ever be some sort of authentication here? for username and password.
        .set('Content-Type', 'application/json')
        .accept('application/json')
        .send(
        {
          /*
            TODO: orderId is hard coded in because the api will return a null pointer exception without it.
            However, the orderId is decided by the backend. If we pass an id in the request, and it already exists,
            we will get an "Unable to create."
            0 was chosen because the backend begins incrementing it's order id at 1.
          */
          "orderId": 0,
          "orderDate": values.orderDate,
          "customerId": values.customerId,
          "productsOrdered": values.quantityById,
        }
        )
        .end()
        .then((res) => res.body)
    },
  }
  return dispatch(dispatchObj)
};

export const purchaseOrder = () => (dispatch) => {
  const token = getJwtToken()
  let dispatchObj = {
    type: types.PURCHASE,
    payload: {
      promise:
      request
        .get('/purchase/')
        .set('Authorization', 'Bearer ' + token)
        .accept('application/json')
        .end()
        .then((res) => res.body)
    },
  }
  return dispatch(dispatchObj)
}

export const fetchAllItems = () => (dispatch) => {
  console.log('Fetching all products from API...');
  let dispatchObj = {
    type: types.ITEMS_REQUEST,
    payload: {
      promise:
      request
        .get(`${API}/product/`)
        .accept('application/json')
        .end()
        .then((res) => {
          console.log('Successfully fetched products from API:', res.body);
          return res.body;
        })
        .catch(err => {
          console.error("Error fetching products from API:", err);
          return [];
        })
    },
  }
  return dispatch(dispatchObj)
};

export const fetchAllCustomers = () => (dispatch) => {
  let dispatchObj = {
    type: types.FETCH_CUSTOMERS,
    payload: {
      promise:
      request
        .get(`${API}/customer/`)
        .accept('application/json')
        .end()
        .then((res) => res.body)
    },
  }
  return dispatch(dispatchObj)
};

export const createCustomer = (username, password) => (dispatch) => {
  const url = `${API}/customer/`
  let dispatchObj = {
    type: types.CREATE_CUSTOMER,
    payload: {
      promise:
      request
        .post(url)
        .set('Content-Type', 'application/json')
        .accept('application/json')
        .send(
        //TODO: take out hard coded values for customer information
        { address: "144 Townsend Street", email: "test@gmail.com", name: "Jess", password: password, phone: "9999999999", username: username, customerId: 0, enabled: "true", role: "user" }
        )
        .end()
        .then((res) => res.body)
    },
  }

  return dispatch(dispatchObj)
};


export const getCustomer = (username, password) => (dispatch) => {
  //TODO: update actions 
  let dispatchObj = {
    type: types.LOGIN_CUSTOMER,
    payload: {
      promise:
      request
        .get(`${API}/customer/username=${username}`)
        .accept('application/json')
        .end()
        .then((res) => res.body)
    },
  }
  return dispatch(dispatchObj)
}

export const loginCustomer = (username, password) => (dispatch) => {
  let dispatchObj = {
    type: types.LOGIN_CUSTOMER,
    payload: {
      promise:
      request
        .post('/login/')
        .set('Content-Type', 'application/json')
        .accept('application/json')
        .send(
        {
          username: username,
          password: password,
        }
        )
        .end()
        .then((res) => res.body)
    },
  }
  return dispatch(dispatchObj)
}

export const fetchContainerId = () => (dispatch) => {
  const url = `${UTILITY}/containerid/`
  let dispatchObj = {
    type: types.FETCH_CONTAINER_ID,
    payload: {
      promise:
      request
        .get(url)
        .accept('application/json')
        .end()
        .then((res) => res.body)
    },
  }
  return dispatch(dispatchObj)
}


export const logoutCustomer = () => (dispatch) => {
  dispatch({
    type: types.LOGOUT_CUSTOMER
  })
}

export const addUser = (username) => (dispatch) => {
  dispatch({
    type: types.ADD_USER,
    username
  })
}

// DUMMY ITEMS
const fetchDummyItems = products => ({
  type: types.DUMMY_ITEMS_REQUEST,
  products: products
})

export const fetchAllDummyItems = () => dispatch => {
  shop.getProducts(products => {
    dispatch(fetchDummyItems(products))
  })
}

// success message shows for 2.5 seconds
export const resetItemAdded = () => (dispatch) => {
  dispatch({
    type: types.RESET_ADD_TO_CART,
  })
}

const addToCartUnsafe = productId => ({
  type: types.ADD_TO_CART,
  productId
})

export const showAddToCart = () => (dispatch) => {
  dispatch({
    type: types.SHOW_ADD_TO_CART,
  })
}

export const addToCart = productId => (dispatch, getState) => {
  console.log('addToCart action called with productId:', productId)
  try {
    // Update local store state
    dispatch(addToCartUnsafe(productId))
    console.log('addToCartUnsafe dispatched successfully')
    dispatch(showAddToCart())
    console.log('showAddToCart dispatched successfully')
    
    // Get current cart state
    const { cart } = getState()
    console.log('Current cart state:', cart)
    
    // Create an order object for the API
    const orderData = {
      orderId: 0, // The backend will assign a real ID
      orderDate: new Date().toISOString(),
      customerId: 0, // If user is not logged in, use 0 or get from state if logged in
      productsOrdered: {}
    }
    
    // Add products from cart to the order
    Object.keys(cart.quantityById).forEach(productId => {
      orderData.productsOrdered[productId] = cart.quantityById[productId]
    })
    
    console.log('Sending order data to API:', orderData)
    
    // Send request to backend API - use the correct endpoint
    const url = `${API}/order/`
    console.log('Sending API request to:', url)
    request
      .post(url)
      .set('Content-Type', 'application/json')
      .accept('application/json')
      .send(orderData)
      .end()
      .then(res => {
        console.log('Successfully created order on server:', res.body)
      })
      .catch(err => {
        console.error('Error creating order on server:', err)
      })
    
    setTimeout(() => {
      dispatch(resetItemAdded())
      console.log('resetItemAdded dispatched after timeout')
    }, 2500)
  } catch (error) {
    console.error('Error in addToCart action:', error)
  }
}

export const checkout = products => (dispatch, getState) => {
  const { cart } = getState()

  dispatch({
    type: types.CHECKOUT_REQUEST
  })
  shop.buyProducts(products, () => {
    dispatch({
      type: types.CHECKOUT_SUCCESS,
      cart
    })
    // Replace the line above with line below to rollback on failure:
    // dispatch({ type: types.CHECKOUT_FAILURE, cart })
  })
}
