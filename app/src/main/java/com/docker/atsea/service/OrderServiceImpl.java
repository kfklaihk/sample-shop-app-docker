package com.docker.atsea.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docker.atsea.configuration.RabbitMQConfig;
import com.docker.atsea.dto.OrderEvent;
import com.docker.atsea.model.Order;
import com.docker.atsea.model.Product;
import com.docker.atsea.repositories.CustomerRepository;
import com.docker.atsea.repositories.OrderRepository;
import com.docker.atsea.repositories.ProductRepository;

@Service("orderService")
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;
		
	public Order findById(Long orderId) {
		return orderRepository.findById(orderId).orElse(null) ;
	}

	public Order createOrder(Order order) {		
		order = orderRepository.save(order);
		orderRepository.flush();

		// Post to RabbitMQ for payment gateway with enriched data
		try {
			OrderEvent event = enrichOrderEvent(order);
			rabbitTemplate.convertAndSend(RabbitMQConfig.ORDERS_EXCHANGE, RabbitMQConfig.ORDERS_ROUTING_KEY, event);
		} catch (Exception e) {
			// Fail-safe to send the order as it is
			rabbitTemplate.convertAndSend(RabbitMQConfig.ORDERS_EXCHANGE, RabbitMQConfig.ORDERS_ROUTING_KEY, order);
		}

		return order;
	}

	private OrderEvent enrichOrderEvent(Order order) {
		OrderEvent event = new OrderEvent();
		event.setOrderId(order.getOrderId());
		
		// Set customer info
		if (order.getCustomerId() != null) {
			customerRepository.findById(order.getCustomerId()).ifPresent(customer -> {
				event.setCustomerName(customer.getName());
				event.setCustomerEmail(customer.getEmail());
			});
		}

		// Set products and calculate total
		List<OrderEvent.ProductDetail> productDetails = new ArrayList<>();
		double total = 0;
		for (Map.Entry<Integer, Integer> entry : order.getProductsOrdered().entrySet()) {
			Long productId = Long.valueOf(entry.getKey());
			Integer quantity = entry.getValue();
			
			Product product = productRepository.findById(productId).orElse(null);
			if (product != null) {
				double price = product.getPrice();
				productDetails.add(new OrderEvent.ProductDetail(product.getName(), quantity, price));
				total += price * quantity;
			}
		}
		event.setProducts(productDetails);
		event.setTotalPrice(total);
		
		return event;
	}

	public void saveOrder(Order order) {
		orderRepository.save(order);
	}
	
	public void updateOrder(Order order) {
		orderRepository.save(order);
	}

	public void deleteOrderById(Long orderId) {
		orderRepository.deleteById(orderId);
	}

	public void deleteAllItems() {
		orderRepository.deleteAll();
	}

	public boolean orderExists(Order order) {
		Long orderId = order.getOrderId();
		if (orderId == null) {
			return false;
		}
		return findById(orderId) != null;
	}

	public List<Order> findAllOrders() {
		return (List<Order>) orderRepository.findAll();
	}	
}
