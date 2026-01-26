import { combineReducers } from 'redux'
import {
  ITEMS_REQUEST,
  DUMMY_ITEMS_REQUEST,
} from '../constants/ActionTypes'

const byId = (state = {}, action) => {
  switch (action.type) {
    case `${ITEMS_REQUEST}_ACK`:
      return {
        ...state,
        ...action.payload.reduce((obj, product) => {
          obj[product.productId] = product
          return obj
        }, {})
      }
    case DUMMY_ITEMS_REQUEST:
      return {
        ...state,
        ...action.products.reduce((obj, product) => {
          obj[product.productId] = product
          return obj
        }, {})
      }
    default:
      return state
  }
}

const visibleIds = (state = [], action) => {
  switch (action.type) {
    case `${ITEMS_REQUEST}_ACK`:
      return action.payload.map(product => product.productId)
    case DUMMY_ITEMS_REQUEST:
      return action.products.map(product => product.productId)
    default:
      return state
  }
}

export default combineReducers({
  byId,
  visibleIds
})

export const getProduct = (state, id) =>
  state.byId[id]

export const getVisibleProducts = state =>
  state.visibleIds.map(id => getProduct(state, id))
