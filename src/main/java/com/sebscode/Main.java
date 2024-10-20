package com.sebscode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@RestController
@RequestMapping("api/v1/customers")
public class Main {
    private final CustomerRepository customerRepository;
    private final RedisTemplate<String, Customer> redisTemplate;

    public Main(CustomerRepository customerRepository, RedisTemplate<String, Customer> redisTemplate) {
        this.customerRepository = customerRepository;
        this.redisTemplate = redisTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @GetMapping
    public List<Customer> getCustomers() {
        return customerRepository.findAll();
    }

    @GetMapping("{customerId}")
    public Customer getCustomer(@PathVariable("customerId") Integer id) {
        String key = "customer:" + id;
        Customer customer = redisTemplate.opsForValue().get(key);
        if (customer == null) {
            System.out.println("Customer " + id + " not found in Redis, fetching from PostgreSQL");
            customer = customerRepository.findById(id).orElseThrow(() -> new RuntimeException("Customer not found"));
            redisTemplate.opsForValue().set(key, customer, 1, TimeUnit.HOURS);
        } else {
            System.out.println("Customer " + id + " found in Redis");
        }
        return customer;
    }

    record NewCustomerRequest(String name, String email, Integer age) {}

    @PostMapping
    public void addCustomer(@RequestBody NewCustomerRequest request) {
        Customer customer = new Customer();
        customer.setName(request.name());
        customer.setEmail(request.email());
        customer.setAge(request.age());
        customerRepository.save(customer);
    }

    @DeleteMapping("{customerId}")
    public void deleteCustomer(@PathVariable("customerId") Integer id) {
        customerRepository.deleteById(id);
        redisTemplate.delete("customer:" + id);
    }

    record UpdateCustomerRequest(String name, String email, Integer age) {}

    @PutMapping("{customerId}")
    public void updateCustomer(@RequestBody UpdateCustomerRequest request, @PathVariable("customerId") Integer id) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.setName(request.name());
        customer.setEmail(request.email());
        customer.setAge(request.age());
        customerRepository.save(customer);
        redisTemplate.opsForValue().set("customer:" + id, customer, 1, TimeUnit.HOURS);
    }
}