package com.example.employeeDetails.Repository;

import com.example.employeeDetails.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String>
{

    List<Employee> findByDepartment(String department);

    List<Employee> findByStatus(String status);

    List<Employee> findByManagerId(String managerId);
}
