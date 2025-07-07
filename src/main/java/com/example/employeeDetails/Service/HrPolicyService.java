package com.example.employeeDetails.Service;

import com.example.employeeDetails.Entity.HrPolicy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HrPolicyService
{

    public List<HrPolicy> getPoliciesByDepartment(String department, String sessionId){
        return new ArrayList<>();
    }
}
