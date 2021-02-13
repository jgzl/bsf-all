package com.github.jgzl.bsf.demo.transaction;


import com.github.jgzl.bsf.core.base.EtTime;
import com.github.jgzl.bsf.transaction.annotation.EffortTransaction;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
@RestController
public class TransactionApplication {
    public static void main(String[] args){
        SpringApplication.run(TransactionApplication.class, args);
    }
    @GetMapping("/effortTransaction/{id}")
    @EffortTransaction(value={EtTime.S01,EtTime.S05})
    public void effortTransaction(@PathVariable  Long id) {
        int num=0/0;
    }

}