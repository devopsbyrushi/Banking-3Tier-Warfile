package com.rushi.securebank.controller;

import com.rushi.securebank.model.Customer;
import com.rushi.securebank.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * ================================================================
 *  SecureBankController - SecureBank Application
 *  Trainer: Rushi | DevOps Multi-Cloud Training
 * ================================================================
 *
 *  Web layer that handles HTTP requests. Replaces the trainer's
 *  BankController with cleaner naming, customer terminology, and
 *  same routes as before so the URLs students see don't change.
 * ================================================================
 */
@Controller
public class SecureBankController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customerService.findCustomerByUsername(username);
        model.addAttribute("customer", customer);
        return "dashboard";
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerCustomer(@RequestParam String username,
                                   @RequestParam String password,
                                   Model model) {
        try {
            customerService.registerCustomer(username, password);
            return "redirect:/login?registered";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam BigDecimal amount) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customerService.findCustomerByUsername(username);
        customerService.deposit(customer, amount);
        return "redirect:/dashboard";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam BigDecimal amount, Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customerService.findCustomerByUsername(username);

        try {
            customerService.withdraw(customer, amount);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("customer", customer);
            return "dashboard";
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/transactions")
    public String transactionHistory(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer customer = customerService.findCustomerByUsername(username);
        model.addAttribute("transactions", customerService.getTransactionHistory(customer));
        return "transactions";
    }

    @PostMapping("/transfer")
    public String transferAmount(@RequestParam String toUsername,
                                 @RequestParam BigDecimal amount,
                                 Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Customer fromCustomer = customerService.findCustomerByUsername(username);

        try {
            customerService.transferAmount(fromCustomer, toUsername, amount);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("customer", fromCustomer);
            return "dashboard";
        }
        return "redirect:/dashboard";
    }
}
