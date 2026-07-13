package com.erp.montfortuganda.employee.validation;

import com.erp.montfortuganda.employee.dto.EmployeeCreateRequest;
import com.erp.montfortuganda.employee.repository.EmployeeRepository;
import com.erp.montfortuganda.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

import com.erp.montfortuganda.employee.dto.EmployeeContactRequest;
import com.erp.montfortuganda.employee.dto.EmployeeQualificationRequest;
import com.erp.montfortuganda.employee.dto.EmployeeExperienceRequest;
import com.erp.montfortuganda.employee.dto.EmployeeDocumentRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class EmployeeValidator {

    private final EmployeeRepository employeeRepository;

    public void validateCreation(EmployeeCreateRequest request, Integer branchId) {
        validateCoreFields(request, branchId);
        
        validateContacts(request.getContacts());
        validateQualifications(request.getQualifications());
        validateExperiences(request.getExperiences());
        validateDocuments(request.getDocuments());
    }

    public void validateUpdate(Long employeeId, EmployeeCreateRequest request, Integer branchId) {
        if (request.getReportingManagerId() != null && request.getReportingManagerId().equals(employeeId)) {
            throw new BadRequestException("An employee cannot report to themselves.");
        }
        
        validateCoreFields(request, branchId);
        
        validateContacts(request.getContacts());
        validateQualifications(request.getQualifications());
        validateExperiences(request.getExperiences());
        validateDocuments(request.getDocuments());
    }
    
    private void validateCoreFields(EmployeeCreateRequest request, Integer branchId) {
        if (request.getDateOfBirth() != null) {
            int age = Period.between(request.getDateOfBirth(), LocalDate.now()).getYears();
            if (age < 18) {
                throw new BadRequestException("Employee must be at least 18 years old.");
            }
        }
        
        if (request.getJoiningDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Joining date cannot be in the future.");
        }

        if (request.getOfficialEmail() != null && !request.getOfficialEmail().isEmpty()) {
            if (employeeRepository.existsByOfficialEmailAndBranch_BranchId(request.getOfficialEmail(), branchId)) {
                throw new BadRequestException("An employee with this email already exists in this branch.");
            }
        }
    }
    
    private void validateContacts(List<EmployeeContactRequest> contacts) {
        if (contacts == null || contacts.isEmpty()) {
            return;
        }

        long primaryCount = contacts.stream().filter(c -> Boolean.TRUE.equals(c.getEmployeeContactIsPrimary())).count();
        if (primaryCount != 1) {
            throw new BadRequestException("There must be exactly one primary contact.");
        }
        
        long emergencyCount = contacts.stream().filter(c -> Boolean.TRUE.equals(c.getEmployeeContactIsEmergency())).count();
        if (emergencyCount > contacts.size()) {
            throw new BadRequestException("Emergency contacts cannot exceed total contacts.");
        }
        
        if (contacts.size() == 1) {
            EmployeeContactRequest singleContact = contacts.get(0);
            if (Boolean.FALSE.equals(singleContact.getEmployeeContactIsPrimary()) || Boolean.FALSE.equals(singleContact.getEmployeeContactIsEmergency())) {
                throw new BadRequestException("If there is only one contact, it must be both primary and emergency.");
            }
        }
        
        Set<String> mobiles = new HashSet<>();
        Set<String> emails = new HashSet<>();
        
        for (EmployeeContactRequest contact : contacts) {
            if (contact.getEmployeeContactMobile() != null && !mobiles.add(contact.getEmployeeContactMobile())) {
                throw new BadRequestException("Duplicate mobile numbers found in contacts: " + contact.getEmployeeContactMobile());
            }
            if (contact.getEmployeeContactEmail() != null && !contact.getEmployeeContactEmail().isEmpty()) {
                if (!emails.add(contact.getEmployeeContactEmail())) {
                    throw new BadRequestException("Duplicate email addresses found in contacts: " + contact.getEmployeeContactEmail());
                }
            }
        }
    }

    private void validateQualifications(List<EmployeeQualificationRequest> qualifications) {
        if (qualifications == null || qualifications.isEmpty()) {
            return;
        }
        // Future business validations (e.g., overlapping years, unique degrees) will go here.
    }

    private void validateExperiences(List<EmployeeExperienceRequest> experiences) {
        if (experiences == null || experiences.isEmpty()) {
            return;
        }
        // Future business validations (e.g., overlapping periods) will go here.
    }

    private void validateDocuments(List<EmployeeDocumentRequest> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        // Future business validations (e.g., mandatory document check) will go here.
    }
}