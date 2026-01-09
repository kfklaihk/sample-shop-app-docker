package com.docker.atsea.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.docker.atsea.model.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
	
	Customer findByName(String name);
	
	// adding find by username
	@Query("SELECT c FROM Customer c WHERE c.username = :userName")
	Customer findByUserName(@Param("userName") String userName);
	
	// Case-insensitive username lookup (for authentication)
	Optional<Customer> findByUsernameIgnoreCase(String username);
	
	// Case-insensitive email lookup (for duplicate checks)
	Optional<Customer> findByEmailIgnoreCase(String email);
	
	// Check if username exists (case-insensitive)
	boolean existsByUsernameIgnoreCase(String username);
	
	// Check if email exists (case-insensitive)
	boolean existsByEmailIgnoreCase(String email);
}

