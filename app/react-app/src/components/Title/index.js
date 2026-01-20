import React, { PropTypes } from 'react'
import Cart from '../../components/Cart'
import './styles.css'
import '../globalStyles.css'

const Title = ({ totalProducts, showItemAdded }) => (
  <div className='globalContainer'>
    <div className='titleBar'>
      <div className='productsSection'>
        Products
            </div>
      <Cart
        total={totalProducts}
        showItemAdded={showItemAdded}
      />
    </div>
  </div>
)

Title.propTypes = {
  totalProducts: PropTypes.number,
  showItemAdded: PropTypes.bool,
}

export default Title
