package com.docker.atsea.service;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docker.atsea.configuration.RabbitMQConfig;
import com.docker.atsea.model.Order;
import com.docker.atsea.repositories.CustomerRepository;
import com.docker.atsea.repositories.OrderRepository;

@Service("orderService")
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;
		
	public Order findById(Long orderId) {
		return orderRepository.findById(orderId).orElse(null) ;
	}

	public Order createOrder(Order order) {		
		order = orderRepository.save(order);
		orderRepository.flush();

		// Post to RabbitMQ for payment gateway
		rabbitTemplate.convertAndSend(RabbitMQConfig.ORDERS_EXCHANGE, RabbitMQConfig.ORDERS_ROUTING_KEY, order);

		return order;
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
