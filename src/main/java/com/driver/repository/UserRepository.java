package com.driver.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.driver.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>{

}
