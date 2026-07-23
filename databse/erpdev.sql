-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: montfortug_erp_dev
-- ------------------------------------------------------
-- Server version	8.0.46-0ubuntu0.24.04.3

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `erp_academic_terms`
--

DROP TABLE IF EXISTS `erp_academic_terms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_academic_terms` (
  `term_id` bigint NOT NULL AUTO_INCREMENT,
  `academic_year_id` bigint NOT NULL,
  `term_code` varchar(20) NOT NULL,
  `term_name` varchar(100) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `display_order` int NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'PLANNED',
  `current_term` tinyint(1) NOT NULL DEFAULT '0',
  `description` varchar(500) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `version` bigint NOT NULL DEFAULT '0',
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`term_id`),
  UNIQUE KEY `uk_year_term_code` (`academic_year_id`,`term_code`),
  CONSTRAINT `fk_term_year` FOREIGN KEY (`academic_year_id`) REFERENCES `erp_academic_years` (`academic_year_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_academic_years`
--

DROP TABLE IF EXISTS `erp_academic_years`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_academic_years` (
  `academic_year_id` bigint NOT NULL AUTO_INCREMENT,
  `academic_year_code` varchar(20) NOT NULL,
  `academic_year_name` varchar(100) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `admission_start_date` date DEFAULT NULL,
  `admission_end_date` date DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'PLANNED',
  `current_year` tinyint(1) NOT NULL DEFAULT '0',
  `description` varchar(500) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `version` bigint NOT NULL DEFAULT '0',
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`academic_year_id`),
  UNIQUE KEY `uk_academic_year_code` (`academic_year_code`),
  CONSTRAINT `chk_academic_year_status` CHECK ((`status` in (_utf8mb3'PLANNED',_utf8mb3'ACTIVE',_utf8mb3'CLOSED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_application_documents`
--

DROP TABLE IF EXISTS `erp_application_documents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_application_documents` (
  `document_id` bigint NOT NULL AUTO_INCREMENT,
  `application_id` bigint NOT NULL,
  `document_type` varchar(50) NOT NULL,
  `verification_status` varchar(20) NOT NULL DEFAULT 'PENDING',
  `original_file_name` varchar(255) NOT NULL,
  `stored_file_name` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `uploaded_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT NULL,
  `file_size` bigint DEFAULT NULL,
  `content_type` varchar(100) DEFAULT NULL,
  `file_hash` varchar(64) DEFAULT NULL,
  `uploaded_by` bigint DEFAULT NULL,
  `version` bigint NOT NULL DEFAULT '0',
  `active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`document_id`),
  KEY `idx_application_document_branch_status` (`verification_status`),
  KEY `idx_application_document_application_status` (`application_id`,`verification_status`),
  CONSTRAINT `fk_application_document_application` FOREIGN KEY (`application_id`) REFERENCES `erp_applications` (`application_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_application_fees`
--

DROP TABLE IF EXISTS `erp_application_fees`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_application_fees` (
  `fee_id` bigint NOT NULL AUTO_INCREMENT,
  `application_id` bigint NOT NULL,
  `base_fee_amount` decimal(12,2) NOT NULL,
  `scholarship_discount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `final_payable` decimal(12,2) NOT NULL,
  `amount_paid` decimal(12,2) NOT NULL DEFAULT '0.00',
  `payment_status` varchar(30) NOT NULL DEFAULT 'PENDING',
  `payment_date` datetime DEFAULT NULL,
  `payment_mode` varchar(30) DEFAULT NULL,
  `receipt_no` varchar(150) DEFAULT NULL,
  `receipt_reference` varchar(150) DEFAULT NULL,
  `collected_by` bigint DEFAULT NULL,
  `remarks` varchar(500) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `version` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`fee_id`),
  UNIQUE KEY `uk_application_fee` (`application_id`),
  UNIQUE KEY `receipt_no` (`receipt_no`),
  KEY `idx_fee_application` (`application_id`),
  KEY `idx_fee_status` (`payment_status`),
  KEY `idx_fee_receipt` (`receipt_no`),
  CONSTRAINT `fk_application_fee` FOREIGN KEY (`application_id`) REFERENCES `erp_applications` (`application_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_application_interviews`
--

DROP TABLE IF EXISTS `erp_application_interviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_application_interviews` (
  `interview_id` bigint NOT NULL AUTO_INCREMENT,
  `application_id` bigint NOT NULL,
  `teacher_id` bigint NOT NULL,
  `interview_date` datetime DEFAULT NULL,
  `test_score` decimal(5,2) DEFAULT NULL,
  `teacher_remarks` text,
  `recommendation` enum('NOT_RECOMMENDED','RECOMMENDED','WAITLIST') NOT NULL,
  `status` enum('IN_PROGRESS','PENDING','REVIEWED','SUBMITTED') NOT NULL,
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `version` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`interview_id`),
  UNIQUE KEY `uk_application_interview` (`application_id`),
  KEY `idx_interview_application` (`application_id`),
  KEY `idx_interview_teacher` (`teacher_id`),
  KEY `idx_interview_status` (`status`),
  CONSTRAINT `fk_interview_application` FOREIGN KEY (`application_id`) REFERENCES `erp_applications` (`application_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_application_status_history`
--

DROP TABLE IF EXISTS `erp_application_status_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_application_status_history` (
  `history_id` bigint NOT NULL AUTO_INCREMENT,
  `application_id` bigint NOT NULL,
  `old_status` varchar(30) DEFAULT NULL,
  `new_status` varchar(30) NOT NULL,
  `changed_by` bigint DEFAULT NULL,
  `changed_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `remarks` text,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `version` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`history_id`),
  KEY `fk_application_status_history_application` (`application_id`),
  CONSTRAINT `fk_application_status_history_application` FOREIGN KEY (`application_id`) REFERENCES `erp_applications` (`application_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_applications`
--

DROP TABLE IF EXISTS `erp_applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_applications` (
  `application_id` bigint NOT NULL AUTO_INCREMENT,
  `application_no` varchar(50) NOT NULL,
  `branch_id` bigint NOT NULL,
  `academic_year_id` bigint NOT NULL,
  `branch_class_id` bigint NOT NULL,
  `term` varchar(20) NOT NULL DEFAULT '',
  `admission_type` varchar(20) DEFAULT 'NEW',
  `student_id` bigint DEFAULT NULL,
  `student_created` tinyint(1) NOT NULL DEFAULT '0',
  `primary_email` varchar(100) DEFAULT NULL,
  `primary_mobile` varchar(20) DEFAULT NULL,
  `first_name` varchar(50) NOT NULL,
  `middle_name` varchar(50) DEFAULT NULL,
  `last_name` varchar(50) NOT NULL,
  `gender` varchar(20) NOT NULL,
  `date_of_birth` date DEFAULT NULL,
  `nationality` varchar(50) DEFAULT 'Uganda',
  `date_of_registration` varchar(20) NOT NULL DEFAULT '',
  `scholarship_status` varchar(50) NOT NULL DEFAULT '',
  `previous_school` varchar(150) DEFAULT NULL,
  `former_school` text,
  `former_school_code` varchar(50) NOT NULL DEFAULT '',
  `former_school_lin` varchar(50) NOT NULL DEFAULT '',
  `ple_ref` varchar(50) NOT NULL DEFAULT '',
  `ple_score` double DEFAULT NULL,
  `uce_ref` varchar(50) NOT NULL DEFAULT '',
  `uce_score` double DEFAULT NULL,
  `subject_marks` text,
  `prev_marks_doc` text,
  `father_name` varchar(50) NOT NULL DEFAULT '',
  `father_contact` varchar(20) NOT NULL DEFAULT '',
  `father_email` varchar(100) NOT NULL DEFAULT '',
  `father_occupation` text,
  `father_education` varchar(50) NOT NULL DEFAULT '',
  `father_age` int DEFAULT '0',
  `mother_name` varchar(50) NOT NULL DEFAULT '',
  `mother_contact` varchar(20) NOT NULL DEFAULT '',
  `mother_email` varchar(100) NOT NULL DEFAULT '',
  `mother_occupation` text,
  `mother_education` varchar(50) NOT NULL DEFAULT '',
  `mother_age` int DEFAULT '0',
  `guardian_name` varchar(50) NOT NULL DEFAULT '',
  `guardian_mobile` varchar(20) DEFAULT NULL,
  `guardian_contact` varchar(20) NOT NULL DEFAULT '',
  `guardian_email` varchar(100) DEFAULT NULL,
  `guardian_relation` varchar(50) NOT NULL DEFAULT '',
  `guardian_occupation` text,
  `guardian_education` varchar(50) NOT NULL DEFAULT '',
  `guardian_location` text,
  `guardian_age` int DEFAULT '0',
  `address_region` varchar(50) NOT NULL DEFAULT '',
  `address_district` varchar(50) NOT NULL DEFAULT '',
  `address_village` varchar(50) NOT NULL DEFAULT '',
  `address_street` text,
  `address_house` varchar(50) NOT NULL DEFAULT '',
  `address_postal` varchar(50) NOT NULL DEFAULT '',
  `application_status` varchar(50) DEFAULT 'DRAFT',
  `photo_path` text,
  `more_info` text,
  `remarks` text,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `status` int DEFAULT '1',
  PRIMARY KEY (`application_id`),
  UNIQUE KEY `uk_application_no` (`application_no`),
  KEY `fk_applications_student` (`student_id`),
  CONSTRAINT `fk_applications_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_audit_log`
--

DROP TABLE IF EXISTS `erp_audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `branch_id` int NOT NULL,
  `user_id` int NOT NULL,
  `action` varchar(255) NOT NULL,
  `entity` varchar(255) NOT NULL,
  `entity_id` bigint DEFAULT NULL,
  `audit_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `details` text,
  PRIMARY KEY (`id`),
  KEY `fk_erp_audit_user` (`user_id`),
  KEY `idx_erp_audit_branch_user` (`branch_id`,`user_id`),
  KEY `idx_erp_audit_timestamp` (`audit_timestamp`),
  KEY `idx_erp_audit_entity` (`entity`),
  KEY `idx_erp_audit_action` (`action`),
  CONSTRAINT `fk_erp_audit_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`),
  CONSTRAINT `fk_erp_audit_user` FOREIGN KEY (`user_id`) REFERENCES `erp_users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_branch_fund_allocations`
--

DROP TABLE IF EXISTS `erp_branch_fund_allocations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_branch_fund_allocations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `branch_id` bigint NOT NULL,
  `donation_id` bigint DEFAULT NULL,
  `amount_allocated_ugx` decimal(15,2) NOT NULL,
  `purpose` varchar(255) DEFAULT 'Branch Scholarship Pool',
  `academic_year` varchar(20) NOT NULL,
  `allocated_by_user_id` bigint NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `allocated_amount_ugx` decimal(38,2) NOT NULL,
  `term` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKq42vuybrj74gyw9csy6angbis` (`donation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_branch_levels`
--

DROP TABLE IF EXISTS `erp_branch_levels`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_branch_levels` (
  `branch_level_id` int NOT NULL AUTO_INCREMENT,
  `branch_id` int NOT NULL,
  `level_id` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`branch_level_id`),
  UNIQUE KEY `uk_branch_level` (`branch_id`,`level_id`),
  UNIQUE KEY `UKsac1pi66it0u88um2v2yfkwrq` (`branch_id`,`level_id`),
  KEY `fk_branch_level_level` (`level_id`),
  CONSTRAINT `fk_branch_level_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_branch_level_level` FOREIGN KEY (`level_id`) REFERENCES `erp_levels` (`level_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_branches`
--

DROP TABLE IF EXISTS `erp_branches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_branches` (
  `branch_id` int NOT NULL AUTO_INCREMENT,
  `branch_name` varchar(255) NOT NULL,
  `school_code` varchar(10) DEFAULT NULL,
  `branch_location` varchar(255) DEFAULT NULL,
  `address_line_1` varchar(255) DEFAULT NULL,
  `address_line_2` varchar(255) DEFAULT NULL,
  `po_box` varchar(100) DEFAULT NULL,
  `locality` varchar(150) DEFAULT NULL,
  `city` varchar(150) DEFAULT NULL,
  `district` varchar(150) DEFAULT NULL,
  `region` varchar(150) DEFAULT NULL,
  `country` varchar(100) NOT NULL DEFAULT 'Uganda',
  `postal_code` varchar(30) DEFAULT NULL,
  `is_active` int DEFAULT '1',
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `contact_details` text,
  `primary_phone` varchar(30) DEFAULT NULL,
  `secondary_phone` varchar(30) DEFAULT NULL,
  `whatsapp_phone` enum('NONE','PRIMARY','SECONDARY','BOTH') NOT NULL DEFAULT 'NONE',
  `branch_email` varchar(150) DEFAULT NULL,
  `email_from_name` varchar(150) DEFAULT NULL,
  `email_reply_to` varchar(150) DEFAULT NULL,
  `email_enabled` tinyint(1) NOT NULL DEFAULT '1',
  `foundation_date` varchar(255) DEFAULT NULL,
  `gov_document_url` varchar(255) DEFAULT NULL,
  `incharge_details` text,
  `school_photo_url` varchar(255) DEFAULT NULL,
  `branch_logo_url` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_classes`
--

DROP TABLE IF EXISTS `erp_classes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_classes` (
  `class_id` int NOT NULL AUTO_INCREMENT,
  `level_id` int NOT NULL,
  `class_code` varchar(255) NOT NULL,
  `class_name` varchar(255) NOT NULL,
  `display_order` int NOT NULL,
  `status` int DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`class_id`),
  UNIQUE KEY `class_code` (`class_code`),
  KEY `fk_school_class_level` (`level_id`),
  CONSTRAINT `fk_school_class_level` FOREIGN KEY (`level_id`) REFERENCES `erp_levels` (`level_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_departments`
--

DROP TABLE IF EXISTS `erp_departments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_departments` (
  `department_id` bigint NOT NULL AUTO_INCREMENT,
  `branch_id` int NOT NULL,
  `department_code` varchar(20) NOT NULL,
  `department_name` varchar(100) NOT NULL,
  `is_academic` tinyint(1) NOT NULL DEFAULT '1',
  `description` varchar(500) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `version` bigint NOT NULL DEFAULT '0',
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`department_id`),
  UNIQUE KEY `uk_branch_dept_code` (`branch_id`,`department_code`),
  UNIQUE KEY `uk_branch_dept_name` (`branch_id`,`department_name`),
  CONSTRAINT `fk_department_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_designations`
--

DROP TABLE IF EXISTS `erp_designations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_designations` (
  `designation_id` bigint NOT NULL AUTO_INCREMENT,
  `designation_code` varchar(20) NOT NULL,
  `designation_name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `version` bigint NOT NULL DEFAULT '0',
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`designation_id`),
  UNIQUE KEY `uk_designation_code` (`designation_code`),
  UNIQUE KEY `uk_designation_name` (`designation_name`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_document_sequences`
--

DROP TABLE IF EXISTS `erp_document_sequences`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_document_sequences` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `branch_id` bigint NOT NULL,
  `module_code` varchar(255) NOT NULL,
  `running_year` int NOT NULL,
  `current_sequence` bigint NOT NULL DEFAULT '0',
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `active` bit(1) NOT NULL,
  `version` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_doc_seq` (`branch_id`,`module_code`,`running_year`),
  UNIQUE KEY `UKhhwyasi3o241inhufw6ki7k1l` (`branch_id`,`module_code`,`running_year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_employee_contacts`
--

DROP TABLE IF EXISTS `erp_employee_contacts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_employee_contacts` (
  `employee_contact_id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL,
  `employee_contact_name` varchar(255) NOT NULL,
  `employee_contact_relationship` enum('BROTHER','DAUGHTER','FATHER','FRIEND','GUARDIAN','MANAGER','MOTHER','OTHER','REFERENCE','RELATIVE','SISTER','SON','SPOUSE') NOT NULL,
  `employee_contact_type` enum('EMERGENCY','GUARDIAN','NEXT_OF_KIN','OTHER','REFERENCE') NOT NULL,
  `employee_contact_mobile` varchar(30) NOT NULL,
  `employee_contact_alternate_mobile` varchar(30) DEFAULT NULL,
  `employee_contact_email` varchar(150) DEFAULT NULL,
  `employee_contact_country` varchar(100) DEFAULT NULL,
  `employee_contact_state` varchar(100) DEFAULT NULL,
  `employee_contact_district` varchar(100) DEFAULT NULL,
  `employee_contact_village` varchar(150) DEFAULT NULL,
  `employee_contact_street` varchar(255) DEFAULT NULL,
  `employee_contact_postal_code` varchar(30) DEFAULT NULL,
  `employee_contact_occupation` varchar(150) DEFAULT NULL,
  `employee_contact_workplace` varchar(255) DEFAULT NULL,
  `employee_contact_is_primary` tinyint(1) NOT NULL DEFAULT '0',
  `employee_contact_is_emergency` tinyint(1) NOT NULL DEFAULT '1',
  `employee_contact_active` tinyint(1) NOT NULL DEFAULT '1',
  `employee_contact_remarks` text,
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  `version` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`employee_contact_id`),
  KEY `fk_empcontact_employee` (`employee_id`),
  KEY `fk_empcontact_createdby` (`created_by`),
  KEY `fk_empcontact_updatedby` (`updated_by`),
  CONSTRAINT `fk_empcontact_employee` FOREIGN KEY (`employee_id`) REFERENCES `erp_employees` (`employee_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_employee_documents`
--

DROP TABLE IF EXISTS `erp_employee_documents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_employee_documents` (
  `employee_document_id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL,
  `employee_document_type` varchar(100) NOT NULL,
  `employee_document_name` varchar(255) NOT NULL,
  `employee_document_description` text,
  `employee_document_file_name` varchar(255) NOT NULL,
  `employee_document_original_file_name` varchar(255) DEFAULT NULL,
  `employee_document_file_path` varchar(500) NOT NULL,
  `employee_document_file_extension` varchar(20) DEFAULT NULL,
  `employee_document_mime_type` varchar(100) DEFAULT NULL,
  `employee_document_file_size` bigint DEFAULT NULL,
  `employee_document_issue_date` date DEFAULT NULL,
  `employee_document_expiry_date` date DEFAULT NULL,
  `employee_document_verified` tinyint(1) NOT NULL DEFAULT '0',
  `employee_document_verified_by` int DEFAULT NULL,
  `employee_document_verified_at` datetime DEFAULT NULL,
  `employee_document_is_mandatory` tinyint(1) NOT NULL DEFAULT '0',
  `employee_document_active` tinyint(1) NOT NULL DEFAULT '1',
  `employee_document_remarks` text,
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  `version` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`employee_document_id`),
  KEY `fk_empdocument_employee` (`employee_id`),
  KEY `fk_empdocument_verifiedby` (`employee_document_verified_by`),
  CONSTRAINT `fk_empdocument_employee` FOREIGN KEY (`employee_id`) REFERENCES `erp_employees` (`employee_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_empdocument_verifiedby` FOREIGN KEY (`employee_document_verified_by`) REFERENCES `erp_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_employee_experience`
--

DROP TABLE IF EXISTS `erp_employee_experience`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_employee_experience` (
  `employee_experience_id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL,
  `employee_experience_type` enum('FULL_TIME','PART_TIME','CONTRACT','TEMPORARY','INTERNSHIP','CONSULTANT','VOLUNTEER','SELF_EMPLOYED','OTHER') DEFAULT 'FULL_TIME',
  `employee_experience_company_name` varchar(255) NOT NULL,
  `employee_experience_company_address` varchar(255) DEFAULT NULL,
  `employee_experience_company_country` varchar(100) DEFAULT NULL,
  `employee_experience_company_state` varchar(100) DEFAULT NULL,
  `employee_experience_company_district` varchar(100) DEFAULT NULL,
  `employee_experience_designation` varchar(150) DEFAULT NULL,
  `employee_experience_department` varchar(150) DEFAULT NULL,
  `employee_experience_employment_type` enum('FULL_TIME','PART_TIME','CONTRACT','TEMPORARY','INTERNSHIP','CONSULTANT','VOLUNTEER','SELF_EMPLOYED','OTHER') NOT NULL,
  `employee_experience_start_date` date NOT NULL,
  `employee_experience_end_date` date DEFAULT NULL,
  `employee_experience_current_job` tinyint(1) NOT NULL DEFAULT '0',
  `employee_experience_total_months` int DEFAULT NULL,
  `employee_experience_salary` decimal(15,2) DEFAULT NULL,
  `employee_experience_currency` varchar(10) DEFAULT NULL,
  `employee_experience_supervisor_name` varchar(255) DEFAULT NULL,
  `employee_experience_supervisor_contact` varchar(100) DEFAULT NULL,
  `employee_experience_reason_for_leaving` varchar(255) DEFAULT NULL,
  `employee_experience_responsibilities` text,
  `employee_experience_achievements` text,
  `employee_experience_experience_certificate_file` varchar(500) DEFAULT NULL,
  `employee_experience_relieving_letter_file` varchar(500) DEFAULT NULL,
  `employee_experience_verified` tinyint(1) NOT NULL DEFAULT '0',
  `employee_experience_verified_by` int DEFAULT NULL,
  `employee_experience_verified_at` datetime DEFAULT NULL,
  `employee_experience_active` tinyint(1) NOT NULL DEFAULT '1',
  `employee_experience_remarks` text,
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  `version` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`employee_experience_id`),
  KEY `fk_empexperience_employee` (`employee_id`),
  KEY `fk_empexperience_verifiedby` (`employee_experience_verified_by`),
  CONSTRAINT `fk_empexperience_employee` FOREIGN KEY (`employee_id`) REFERENCES `erp_employees` (`employee_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_empexperience_verifiedby` FOREIGN KEY (`employee_experience_verified_by`) REFERENCES `erp_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_employee_qualifications`
--

DROP TABLE IF EXISTS `erp_employee_qualifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_employee_qualifications` (
  `employee_qualification_id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL,
  `employee_qualification_level` enum('PRIMARY','SECONDARY','SENIOR_SECONDARY','DIPLOMA','CERTIFICATE','GRADUATION','POST_GRADUATION','DR_PHD','OTHER') NOT NULL,
  `custom_level` varchar(255) DEFAULT NULL,
  `employee_qualification_name` varchar(255) NOT NULL,
  `employee_qualification_specialization` varchar(255) DEFAULT NULL,
  `employee_qualification_institution_name` varchar(255) NOT NULL,
  `qualification_grade` varchar(100) DEFAULT NULL,
  `employee_qualification_board_university` varchar(255) DEFAULT NULL,
  `employee_qualification_country` varchar(100) DEFAULT NULL,
  `employee_qualification_start_year` int DEFAULT NULL,
  `employee_qualification_completion_year` int DEFAULT NULL,
  `employee_qualification_duration_months` int DEFAULT NULL,
  `employee_qualification_grade` varchar(50) DEFAULT NULL,
  `employee_qualification_percentage` decimal(5,2) DEFAULT NULL,
  `employee_qualification_cgpa` decimal(4,2) DEFAULT NULL,
  `employee_qualification_certificate_number` varchar(100) DEFAULT NULL,
  `employee_qualification_registration_number` varchar(100) DEFAULT NULL,
  `employee_qualification_document_file` varchar(500) DEFAULT NULL,
  `employee_qualification_verified` tinyint(1) NOT NULL DEFAULT '0',
  `employee_qualification_verified_by` int DEFAULT NULL,
  `employee_qualification_verified_at` datetime DEFAULT NULL,
  `employee_qualification_remarks` text,
  `employee_qualification_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  `version` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`employee_qualification_id`),
  KEY `fk_empqualification_employee` (`employee_id`),
  KEY `fk_empqualification_verifiedby` (`employee_qualification_verified_by`),
  CONSTRAINT `fk_empqualification_employee` FOREIGN KEY (`employee_id`) REFERENCES `erp_employees` (`employee_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_empqualification_verifiedby` FOREIGN KEY (`employee_qualification_verified_by`) REFERENCES `erp_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_employee_sequences`
--

DROP TABLE IF EXISTS `erp_employee_sequences`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_employee_sequences` (
  `sequence_id` bigint NOT NULL AUTO_INCREMENT,
  `branch_id` int NOT NULL,
  `employee_category` varchar(50) NOT NULL,
  `sequence_year` int NOT NULL,
  `last_number` int NOT NULL DEFAULT '0',
  `version` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`sequence_id`),
  UNIQUE KEY `uk_employee_sequence` (`branch_id`,`employee_category`,`sequence_year`),
  CONSTRAINT `fk_employee_sequence_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_employees`
--

DROP TABLE IF EXISTS `erp_employees`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_employees` (
  `employee_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `branch_id` int NOT NULL,
  `department_id` bigint DEFAULT NULL,
  `designation_id` bigint DEFAULT NULL,
  `reporting_manager_id` bigint DEFAULT NULL,
  `employee_no` varchar(50) NOT NULL,
  `title` varchar(20) DEFAULT NULL,
  `first_name` varchar(100) NOT NULL,
  `middle_name` varchar(100) DEFAULT NULL,
  `last_name` varchar(100) NOT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `gender` enum('FEMALE','MALE','OTHER') DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `profile_photo` varchar(500) DEFAULT NULL,
  `signature_file` varchar(500) DEFAULT NULL,
  `nationality` varchar(100) DEFAULT NULL,
  `national_id` varchar(100) DEFAULT NULL,
  `passport_no` varchar(100) DEFAULT NULL,
  `tin_number` varchar(100) DEFAULT NULL,
  `marital_status` varchar(50) DEFAULT NULL,
  `blood_group` varchar(20) DEFAULT NULL,
  `religion` varchar(100) DEFAULT NULL,
  `sub_religion` varchar(100) DEFAULT NULL,
  `official_email` varchar(150) DEFAULT NULL,
  `personal_email` varchar(150) DEFAULT NULL,
  `mobile_no` varchar(30) DEFAULT NULL,
  `alternate_mobile` varchar(30) DEFAULT NULL,
  `address_country` varchar(100) DEFAULT NULL,
  `address_state` varchar(100) DEFAULT NULL,
  `address_district` varchar(100) DEFAULT NULL,
  `address_county` varchar(100) DEFAULT NULL,
  `address_sub_county` varchar(100) DEFAULT NULL,
  `address_parish` varchar(100) DEFAULT NULL,
  `address_village` varchar(150) DEFAULT NULL,
  `address_street` varchar(255) DEFAULT NULL,
  `postal_code` varchar(30) DEFAULT NULL,
  `employee_category` enum('TEACHING','NON_TEACHING','MANAGEMENT_TEACHING','MANAGEMENT_NON_TEACHING','SUPPORT_STAFF') DEFAULT NULL,
  `employee_type` enum('PERMANENT','CONTRACT','TEMPORARY','PART_TIME','INTERN','VOLUNTEER','HONORY') NOT NULL,
  `employment_mode` enum('FULL_TIME','ON_CALL','PART_TIME','REMOTE') NOT NULL,
  `employment_status` enum('ACTIVE','ON_LEAVE','PROBATION','RESIGNED','RETIRED','SUSPENDED','TERMINATED') NOT NULL,
  `joining_date` date DEFAULT NULL,
  `probation_end_date` date DEFAULT NULL,
  `confirmation_date` date DEFAULT NULL,
  `retirement_date` date DEFAULT NULL,
  `resignation_date` date DEFAULT NULL,
  `termination_date` date DEFAULT NULL,
  `work_permit_number` varchar(100) DEFAULT NULL,
  `work_permit_expiry_date` date DEFAULT NULL,
  `passport_expiry_date` date DEFAULT NULL,
  `employment_end_date` date DEFAULT NULL,
  `exit_reason` text,
  `skills` text,
  `languages_spoken` text,
  `employee_remarks` text,
  `login_enabled` tinyint(1) NOT NULL DEFAULT '0',
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  `version` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`employee_id`),
  UNIQUE KEY `employee_no` (`employee_no`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `fk_employee_reporting_manager` (`reporting_manager_id`),
  KEY `idx_emp_branch` (`branch_id`),
  KEY `idx_emp_department` (`department_id`),
  KEY `idx_emp_designation` (`designation_id`),
  KEY `idx_emp_status` (`employment_status`),
  KEY `idx_emp_category` (`employee_category`),
  CONSTRAINT `fk_employee_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_employee_department` FOREIGN KEY (`department_id`) REFERENCES `erp_departments` (`department_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_employee_designation` FOREIGN KEY (`designation_id`) REFERENCES `erp_designations` (`designation_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_employee_reporting_manager` FOREIGN KEY (`reporting_manager_id`) REFERENCES `erp_employees` (`employee_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_employee_user` FOREIGN KEY (`user_id`) REFERENCES `erp_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_import_errors`
--

DROP TABLE IF EXISTS `erp_import_errors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_import_errors` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_id` varchar(36) NOT NULL,
  `excel_row_number` int NOT NULL,
  `row_index` int DEFAULT NULL,
  `column_name` varchar(100) DEFAULT NULL,
  `cell_value` varchar(1000) DEFAULT NULL,
  `error_code` varchar(50) NOT NULL,
  `severity` varchar(20) DEFAULT NULL,
  `message` varchar(1000) NOT NULL,
  `suggested_fix` varchar(1000) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_import_errors_job_id` (`job_id`),
  KEY `idx_import_errors_job_row` (`job_id`,`excel_row_number`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_import_jobs`
--

DROP TABLE IF EXISTS `erp_import_jobs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_import_jobs` (
  `job_id` varchar(36) NOT NULL,
  `branch_id` varchar(36) DEFAULT NULL,
  `module` varchar(50) NOT NULL,
  `status` varchar(50) DEFAULT NULL,
  `total_rows` int DEFAULT '0',
  `processed_rows` int DEFAULT '0',
  `success_rows` int DEFAULT '0',
  `failed_rows` int DEFAULT '0',
  `uploaded_file_name` varchar(255) DEFAULT NULL,
  `error_report_url` varchar(1000) DEFAULT NULL,
  `file_hash` varchar(64) DEFAULT NULL,
  `import_mode` varchar(20) NOT NULL,
  `last_checkpoint` varchar(500) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `completed_at` timestamp NULL DEFAULT NULL,
  `started_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_levels`
--

DROP TABLE IF EXISTS `erp_levels`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_levels` (
  `level_id` int NOT NULL AUTO_INCREMENT,
  `level_name` varchar(255) NOT NULL,
  `display_order` int NOT NULL,
  `status` int DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`level_id`),
  UNIQUE KEY `level_name` (`level_name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_login_history`
--

DROP TABLE IF EXISTS `erp_login_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_login_history` (
  `login_history_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `branch_id` int DEFAULT NULL,
  `login_time` datetime NOT NULL,
  `logout_time` datetime DEFAULT NULL,
  `ip_address` varchar(100) DEFAULT NULL,
  `device_name` varchar(255) DEFAULT NULL,
  `browser_name` varchar(255) DEFAULT NULL,
  `login_status` enum('FAILED','LOCKED','LOGOUT','SUCCESS') NOT NULL,
  `remarks` varchar(500) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `version` bigint NOT NULL DEFAULT '0',
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`login_history_id`),
  KEY `idx_loginhistory_user` (`user_id`),
  KEY `idx_loginhistory_branch` (`branch_id`),
  KEY `idx_loginhistory_status` (`login_status`),
  KEY `idx_loginhistory_time` (`login_time`),
  CONSTRAINT `fk_loginhistory_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_loginhistory_user` FOREIGN KEY (`user_id`) REFERENCES `erp_users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_parents`
--

DROP TABLE IF EXISTS `erp_parents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_parents` (
  `parent_id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `branch_id` int NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `father_name` varchar(150) DEFAULT NULL,
  `father_uin` varchar(20) DEFAULT NULL,
  `father_phone` varchar(30) DEFAULT NULL,
  `father_alternate_phone` varchar(30) DEFAULT NULL,
  `father_email` varchar(150) DEFAULT NULL,
  `father_occupation` varchar(150) DEFAULT NULL,
  `father_employer` varchar(200) DEFAULT NULL,
  `father_designation` varchar(150) DEFAULT NULL,
  `father_annual_income` decimal(15,2) DEFAULT NULL,
  `mother_name` varchar(150) DEFAULT NULL,
  `mother_uin` varchar(20) DEFAULT NULL,
  `mother_phone` varchar(30) DEFAULT NULL,
  `mother_alternate_phone` varchar(30) DEFAULT NULL,
  `mother_email` varchar(150) DEFAULT NULL,
  `mother_occupation` varchar(150) DEFAULT NULL,
  `mother_employer` varchar(200) DEFAULT NULL,
  `mother_designation` varchar(150) DEFAULT NULL,
  `mother_annual_income` decimal(15,2) DEFAULT NULL,
  `guardian_name` varchar(150) DEFAULT NULL,
  `guardian_uin` varchar(20) DEFAULT NULL,
  `guardian_relationship` varchar(100) DEFAULT NULL,
  `guardian_phone` varchar(30) DEFAULT NULL,
  `guardian_alternate_phone` varchar(30) DEFAULT NULL,
  `guardian_email` varchar(150) DEFAULT NULL,
  `guardian_occupation` varchar(150) DEFAULT NULL,
  `preferred_contact` enum('FATHER','GUARDIAN','MOTHER') NOT NULL,
  `fee_responsibility` enum('FATHER','GUARDIAN','MOTHER','SPONSOR') NOT NULL,
  `parents_living_together` tinyint(1) NOT NULL DEFAULT '1',
  `emergency_contact_name` varchar(150) DEFAULT NULL,
  `emergency_contact_phone` varchar(30) DEFAULT NULL,
  `emergency_contact_relationship` varchar(100) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `remarks` text,
  `created_by` bigint DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `version` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`parent_id`),
  UNIQUE KEY `uk_parent_student` (`student_id`),
  KEY `idx_parent_branch` (`branch_id`),
  KEY `idx_parent_admission` (`admission_no`),
  KEY `idx_father_phone` (`father_phone`),
  KEY `idx_mother_phone` (`mother_phone`),
  KEY `idx_guardian_phone` (`guardian_phone`),
  KEY `idx_father_uin` (`father_uin`),
  KEY `idx_mother_uin` (`mother_uin`),
  KEY `idx_guardian_uin` (`guardian_uin`),
  KEY `idx_active` (`active`),
  KEY `idx_father_email` (`father_email`),
  KEY `idx_mother_email` (`mother_email`),
  KEY `idx_guardian_email` (`guardian_email`),
  KEY `idx_emergency_phone` (`emergency_contact_phone`),
  CONSTRAINT `fk_parent_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_parent_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_permissions`
--

DROP TABLE IF EXISTS `erp_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_permissions` (
  `permission_id` bigint NOT NULL AUTO_INCREMENT,
  `permission_code` varchar(100) NOT NULL,
  `permission_name` varchar(150) NOT NULL,
  `module_name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `version` bigint NOT NULL DEFAULT '0',
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`permission_id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_role_permissions`
--

DROP TABLE IF EXISTS `erp_role_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_role_permissions` (
  `role_permission_id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `permission_id` bigint NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `version` bigint NOT NULL DEFAULT '0',
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`role_permission_id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`),
  KEY `fk_rolepermission_permission` (`permission_id`),
  CONSTRAINT `fk_rolepermission_permission` FOREIGN KEY (`permission_id`) REFERENCES `erp_permissions` (`permission_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_rolepermission_role` FOREIGN KEY (`role_id`) REFERENCES `erp_roles` (`role_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_roles`
--

DROP TABLE IF EXISTS `erp_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_roles` (
  `role_id` bigint NOT NULL AUTO_INCREMENT,
  `role_code` varchar(50) NOT NULL,
  `role_name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `version` bigint NOT NULL DEFAULT '0',
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_scholarship_allocations`
--

DROP TABLE IF EXISTS `erp_scholarship_allocations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_scholarship_allocations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `branch_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `donation_id` bigint NOT NULL,
  `amount_allocated_ugx` decimal(15,2) NOT NULL,
  `terms_covered` varchar(100) NOT NULL,
  `academic_year` varchar(20) NOT NULL,
  `allocated_by_user_id` bigint NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `allocated_amount_ugx` decimal(38,2) NOT NULL,
  `term` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKs8cxscphnnfi82r2h9153yyw3` (`donation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_scholarship_application_docs`
--

DROP TABLE IF EXISTS `erp_scholarship_application_docs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_scholarship_application_docs` (
  `document_id` bigint NOT NULL AUTO_INCREMENT,
  `scholarship_app_id` bigint NOT NULL,
  `document_type` varchar(50) NOT NULL,
  `verification_status` varchar(20) NOT NULL DEFAULT 'PENDING',
  `original_file_name` varchar(255) NOT NULL,
  `stored_file_name` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `file_size` bigint DEFAULT NULL,
  `content_type` varchar(100) DEFAULT NULL,
  `file_hash` varchar(64) DEFAULT NULL,
  `uploaded_by` bigint DEFAULT NULL,
  `uploaded_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `version` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`document_id`),
  KEY `idx_document_scholarship` (`scholarship_app_id`),
  CONSTRAINT `fk_scholarship_doc_app` FOREIGN KEY (`scholarship_app_id`) REFERENCES `erp_scholarship_applications` (`scholarship_app_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_scholarship_applications`
--

DROP TABLE IF EXISTS `erp_scholarship_applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_scholarship_applications` (
  `scholarship_app_id` bigint NOT NULL AUTO_INCREMENT,
  `branch_id` bigint NOT NULL DEFAULT '1',
  `application_id` bigint DEFAULT NULL,
  `student_id` bigint DEFAULT NULL,
  `academic_year` varchar(20) NOT NULL,
  `amount_requested_ugx` decimal(38,2) NOT NULL DEFAULT '0.00',
  `term_requested` varchar(50) NOT NULL DEFAULT 'TERM_1',
  `category` varchar(100) NOT NULL DEFAULT 'GENERAL',
  `scholarship_type` enum('MERIT','NEED_BASED','SPORTS','STAFF_CHILD','SIBLING','DONOR','OTHER') DEFAULT 'OTHER',
  `requested_percentage` decimal(5,2) NOT NULL,
  `approved_percentage` decimal(5,2) DEFAULT NULL,
  `approved_amount` decimal(12,2) DEFAULT NULL,
  `valid_until` date DEFAULT NULL,
  `parent_income_declared` decimal(15,2) DEFAULT NULL,
  `reason` varchar(500) DEFAULT NULL,
  `reviewer_remarks` varchar(500) DEFAULT NULL,
  `status` varchar(50) NOT NULL DEFAULT 'Pending',
  `reviewed_by` bigint DEFAULT NULL,
  `approved_at` datetime DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `version` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`scholarship_app_id`),
  UNIQUE KEY `uk_scholarship_application` (`application_id`),
  UNIQUE KEY `uk_scholarship_student` (`student_id`),
  KEY `idx_scholarship_application` (`application_id`),
  KEY `idx_scholarship_student` (`student_id`),
  KEY `idx_scholarship_status` (`status`),
  KEY `idx_scholarship_year` (`academic_year`),
  CONSTRAINT `fk_scholarship_application` FOREIGN KEY (`application_id`) REFERENCES `erp_applications` (`application_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_scholarship_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_sections`
--

DROP TABLE IF EXISTS `erp_sections`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_sections` (
  `section_id` bigint NOT NULL AUTO_INCREMENT,
  `branch_id` int NOT NULL,
  `academic_year_id` bigint NOT NULL,
  `class_id` int NOT NULL,
  `section_code` varchar(20) NOT NULL,
  `section_name` varchar(100) NOT NULL,
  `capacity` int NOT NULL DEFAULT '40',
  `description` varchar(500) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `version` bigint NOT NULL DEFAULT '0',
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`section_id`),
  UNIQUE KEY `uk_branch_year_class_section` (`branch_id`,`academic_year_id`,`class_id`,`section_code`),
  KEY `fk_section_year` (`academic_year_id`),
  KEY `fk_section_class` (`class_id`),
  CONSTRAINT `fk_section_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_section_class` FOREIGN KEY (`class_id`) REFERENCES `erp_classes` (`class_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_section_year` FOREIGN KEY (`academic_year_id`) REFERENCES `erp_academic_years` (`academic_year_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_site_settings`
--

DROP TABLE IF EXISTS `erp_site_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_site_settings` (
  `setting_id` int NOT NULL AUTO_INCREMENT,
  `setting_key` varchar(100) DEFAULT NULL,
  `setting_value` text,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`setting_id`),
  UNIQUE KEY `setting_key` (`setting_key`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_student_academic_history`
--

DROP TABLE IF EXISTS `erp_student_academic_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_academic_history` (
  `academic_history_id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `branch_id` int NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `version` bigint NOT NULL DEFAULT '0',
  `former_school_name` varchar(255) DEFAULT NULL,
  `former_school_code` varchar(50) DEFAULT NULL,
  `former_school_lin` varchar(50) DEFAULT NULL,
  `former_school_address` varchar(255) DEFAULT NULL,
  `school_type` enum('GOVERNMENT','INTERNATIONAL','OTHER','PRIVATE') DEFAULT NULL,
  `transfer_reason` varchar(255) DEFAULT NULL,
  `previous_academic_year` varchar(20) DEFAULT NULL,
  `previous_class` varchar(50) DEFAULT NULL,
  `previous_section` varchar(50) DEFAULT NULL,
  `previous_stream` varchar(50) DEFAULT NULL,
  `ple_index_number` varchar(50) DEFAULT NULL,
  `ple_aggregate` varchar(20) DEFAULT NULL,
  `uce_index_number` varchar(50) DEFAULT NULL,
  `uce_result` varchar(50) DEFAULT NULL,
  `uace_index_number` varchar(50) DEFAULT NULL,
  `uace_result` varchar(50) DEFAULT NULL,
  `subject_marks` longtext,
  `previous_report_card` varchar(255) DEFAULT NULL,
  `transfer_certificate` varchar(255) DEFAULT NULL,
  `leaving_certificate` varchar(255) DEFAULT NULL,
  `verification_status` enum('PENDING','REJECTED','VERIFIED') NOT NULL,
  `verified_by` bigint DEFAULT NULL,
  `verified_at` datetime DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `remarks` longtext,
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`academic_history_id`),
  UNIQUE KEY `uk_student_academic_history` (`student_id`),
  KEY `idx_academic_student` (`student_id`),
  KEY `idx_academic_branch` (`branch_id`),
  KEY `idx_academic_admission` (`admission_no`),
  KEY `idx_academic_school` (`former_school_code`),
  KEY `idx_academic_verification` (`verification_status`),
  KEY `idx_branch_ple` (`branch_id`,`ple_index_number`),
  KEY `idx_branch_uce` (`branch_id`,`uce_index_number`),
  KEY `idx_branch_uace` (`branch_id`,`uace_index_number`),
  KEY `idx_ple` (`ple_index_number`),
  KEY `idx_uce` (`uce_index_number`),
  KEY `idx_uace` (`uace_index_number`),
  CONSTRAINT `fk_academic_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_academic_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_student_accounts`
--

DROP TABLE IF EXISTS `erp_student_accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_accounts` (
  `account_id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `branch_id` int NOT NULL,
  `username` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `account_status` enum('ACTIVE','DISABLED','LOCKED','SUSPENDED') NOT NULL,
  `password_changed` tinyint(1) NOT NULL DEFAULT '0',
  `password_reset_required` tinyint(1) NOT NULL DEFAULT '0',
  `failed_attempts` int NOT NULL DEFAULT '0',
  `account_locked` tinyint(1) NOT NULL DEFAULT '0',
  `last_login` datetime DEFAULT NULL,
  `last_login_ip` varchar(100) DEFAULT NULL,
  `last_login_device` varchar(255) DEFAULT NULL,
  `last_password_change` datetime DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `remarks` text,
  `version` bigint NOT NULL DEFAULT '0',
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`account_id`),
  UNIQUE KEY `uk_student_username` (`username`),
  UNIQUE KEY `uk_student_account` (`student_id`),
  KEY `idx_student_account_student` (`student_id`),
  KEY `idx_student_account_branch` (`branch_id`),
  KEY `idx_student_account_admission` (`admission_no`),
  CONSTRAINT `fk_student_account_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_student_account_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_student_alumni`
--

DROP TABLE IF EXISTS `erp_student_alumni`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_alumni` (
  `alumni_id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `branch_id` int NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `graduation_year` int NOT NULL,
  `graduation_date` date DEFAULT NULL,
  `final_class` varchar(50) DEFAULT NULL,
  `final_stream` varchar(50) DEFAULT NULL,
  `final_grade` varchar(50) DEFAULT NULL,
  `certificate_number` varchar(100) DEFAULT NULL,
  `notes` text,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `version` bigint NOT NULL DEFAULT '0',
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`alumni_id`),
  UNIQUE KEY `uk_alumni_student` (`student_id`),
  KEY `idx_alumni_branch` (`branch_id`),
  KEY `idx_alumni_admission` (`admission_no`),
  KEY `idx_alumni_grad_year` (`graduation_year`),
  KEY `idx_alumni_graduation_date` (`graduation_date`),
  KEY `idx_alumni_certificate` (`certificate_number`),
  KEY `idx_alumni_student` (`student_id`),
  CONSTRAINT `fk_alumni_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_alumni_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_student_archives`
--

DROP TABLE IF EXISTS `erp_student_archives`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_archives` (
  `archive_id` bigint NOT NULL AUTO_INCREMENT,
  `version` bigint NOT NULL DEFAULT '0',
  `student_id` bigint NOT NULL,
  `branch_id` int NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `archive_status` enum('ARCHIVED','RESTORED') NOT NULL,
  `archive_reason` enum('DECEASED','DROPPED_OUT','EXPELLED','GRADUATED','OTHER','TRANSFERRED','WITHDRAWN') NOT NULL,
  `date_of_leaving` date NOT NULL,
  `restored_by` bigint DEFAULT NULL,
  `restored_at` datetime DEFAULT NULL,
  `restore_reason` varchar(255) DEFAULT NULL,
  `remarks` tinytext,
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`archive_id`),
  KEY `idx_archive_student` (`student_id`),
  KEY `idx_archive_branch` (`branch_id`),
  KEY `idx_archive_admission` (`admission_no`),
  KEY `idx_archive_status` (`archive_status`),
  KEY `idx_archive_reason` (`archive_reason`),
  KEY `idx_archive_leaving_date` (`date_of_leaving`),
  CONSTRAINT `fk_archive_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_archive_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_student_documents`
--

DROP TABLE IF EXISTS `erp_student_documents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_documents` (
  `document_id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `branch_id` int NOT NULL,
  `document_type` varchar(100) NOT NULL,
  `document_name` varchar(150) NOT NULL,
  `document_number` varchar(100) DEFAULT NULL,
  `file_name` varchar(255) NOT NULL,
  `original_file_name` varchar(255) DEFAULT NULL,
  `file_path` varchar(500) NOT NULL,
  `file_extension` varchar(20) DEFAULT NULL,
  `mime_type` varchar(100) DEFAULT NULL,
  `file_size` bigint DEFAULT NULL,
  `document_status` enum('PENDING','VERIFIED','REJECTED','EXPIRED') DEFAULT 'PENDING',
  `remarks` tinytext,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `uploaded_by` bigint DEFAULT NULL,
  `uploaded_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `verified_by` bigint DEFAULT NULL,
  `verified_at` timestamp NULL DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`document_id`),
  KEY `idx_student_documents_student` (`student_id`),
  KEY `idx_student_documents_branch` (`branch_id`),
  KEY `idx_student_documents_status` (`document_status`),
  KEY `idx_student_documents_type` (`document_type`),
  KEY `idx_student_documents_admission_no` (`admission_no`),
  CONSTRAINT `fk_student_documents_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_student_documents_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_student_enrollment`
--

DROP TABLE IF EXISTS `erp_student_enrollment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_enrollment` (
  `enrollment_id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `branch_id` int NOT NULL,
  `academic_year_id` bigint NOT NULL,
  `class_id` bigint NOT NULL,
  `section_id` bigint DEFAULT NULL,
  `stream_id` bigint DEFAULT NULL,
  `house_id` bigint DEFAULT NULL,
  `hostel_id` bigint DEFAULT NULL,
  `bed_id` bigint DEFAULT NULL,
  `roll_no` varchar(20) DEFAULT NULL,
  `admission_type` enum('NEW','READMISSION','TRANSFER') NOT NULL,
  `promotion_type` enum('NEW','PROMOTED','RETAINED','TRANSFERRED') NOT NULL,
  `enrollment_status` enum('ACTIVE','EXPELLED','GRADUATED','PROMOTED','SUSPENDED','TRANSFERRED','WITHDRAWN') NOT NULL,
  `joining_date` date NOT NULL,
  `leaving_date` date DEFAULT NULL,
  `class_teacher_id` bigint DEFAULT NULL,
  `fee_structure_id` bigint DEFAULT NULL,
  `scholarship_id` bigint DEFAULT NULL,
  `approved_by` bigint DEFAULT NULL,
  `approved_at` datetime DEFAULT NULL,
  `is_locked` tinyint(1) NOT NULL DEFAULT '0',
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `remarks` tinytext,
  `created_by` bigint DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `version` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`enrollment_id`),
  UNIQUE KEY `uk_student_current` (`student_id`),
  KEY `idx_enrollment_student` (`student_id`),
  KEY `idx_enrollment_branch` (`branch_id`),
  KEY `idx_enrollment_admission` (`admission_no`),
  KEY `idx_enrollment_year` (`academic_year_id`),
  KEY `idx_enrollment_class` (`class_id`),
  KEY `idx_enrollment_status` (`enrollment_status`),
  CONSTRAINT `fk_enrollment_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_enrollment_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_student_enrollment_history`
--

DROP TABLE IF EXISTS `erp_student_enrollment_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_enrollment_history` (
  `enrollment_history_id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `enrollment_id` bigint DEFAULT NULL,
  `branch_id` int NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `academic_year_id` bigint NOT NULL,
  `class_id` bigint NOT NULL,
  `section_id` bigint DEFAULT NULL,
  `stream_id` bigint DEFAULT NULL,
  `house_id` bigint DEFAULT NULL,
  `hostel_id` bigint DEFAULT NULL,
  `bed_id` bigint DEFAULT NULL,
  `roll_no` varchar(20) DEFAULT NULL,
  `admission_type` varchar(20) NOT NULL,
  `promotion_type` varchar(20) NOT NULL,
  `enrollment_status` varchar(20) NOT NULL,
  `joining_date` date NOT NULL,
  `leaving_date` date DEFAULT NULL,
  `effective_date` date NOT NULL,
  `change_reason` varchar(255) DEFAULT NULL,
  `remarks` tinytext,
  `approved_by` bigint DEFAULT NULL,
  `approved_at` datetime DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`enrollment_history_id`),
  KEY `idx_hist_student` (`student_id`),
  KEY `idx_hist_enrollment` (`enrollment_id`),
  KEY `idx_hist_branch` (`branch_id`),
  KEY `idx_hist_admission` (`admission_no`),
  KEY `idx_hist_year` (`academic_year_id`),
  KEY `idx_hist_class` (`class_id`),
  KEY `idx_hist_status` (`enrollment_status`),
  KEY `idx_hist_effective_date` (`effective_date`),
  KEY `idx_hist_branch_year` (`branch_id`,`academic_year_id`),
  CONSTRAINT `fk_hist_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_hist_enrollment` FOREIGN KEY (`enrollment_id`) REFERENCES `erp_student_enrollment` (`enrollment_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_hist_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_student_fee_assignments`
--

DROP TABLE IF EXISTS `erp_student_fee_assignments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_fee_assignments` (
  `fee_assignment_id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `branch_id` int NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `academic_year` varchar(20) NOT NULL,
  `term` varchar(30) NOT NULL,
  `fee_name` varchar(150) NOT NULL,
  `fee_type` varchar(50) NOT NULL,
  `total_fee` decimal(12,2) NOT NULL,
  `scholarship_amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `concession_amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `discount_amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `fine_amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `payable_amount` decimal(12,2) NOT NULL,
  `paid_amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `balance_amount` decimal(12,2) NOT NULL,
  `assignment_date` date NOT NULL,
  `due_date` date DEFAULT NULL,
  `fee_status` enum('CANCELLED','OVERDUE','PAID','PARTIAL','PENDING') NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `remarks` tinytext,
  `created_by` bigint DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `version` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`fee_assignment_id`),
  UNIQUE KEY `uk_student_fee_assignment` (`student_id`,`academic_year`,`term`,`fee_name`),
  KEY `idx_fee_assignment_student` (`student_id`),
  KEY `idx_fee_assignment_branch` (`branch_id`),
  KEY `idx_fee_assignment_admission` (`admission_no`),
  KEY `idx_fee_assignment_year` (`academic_year`),
  KEY `idx_fee_assignment_term` (`term`),
  KEY `idx_fee_assignment_status` (`fee_status`),
  KEY `idx_fee_assignment_due_date` (`due_date`),
  KEY `idx_fee_assignment_fee_name` (`fee_name`),
  CONSTRAINT `fk_fee_assignment_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_fee_assignment_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_student_fee_ledger`
--

DROP TABLE IF EXISTS `erp_student_fee_ledger`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_fee_ledger` (
  `fee_ledger_id` bigint NOT NULL AUTO_INCREMENT,
  `fee_assignment_id` bigint NOT NULL,
  `fee_receipt_id` bigint DEFAULT NULL,
  `student_id` bigint NOT NULL,
  `branch_id` int NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `academic_year` varchar(20) NOT NULL,
  `term` varchar(30) NOT NULL,
  `fee_name` varchar(150) NOT NULL,
  `fee_type` varchar(50) NOT NULL,
  `transaction_type` enum('ADJUSTMENT','CONCESSION','DISCOUNT','FEE_ASSIGNED','FINE','PARTIAL_PAYMENT','PAYMENT','REFUND','REVERSAL','SCHOLARSHIP','WAIVER') NOT NULL,
  `payment_mode` enum('BANK_TRANSFER','CASH','CHEQUE','CREDIT_CARD','DEBIT_CARD','MOBILE_MONEY','ONLINE','SCHOLARSHIP','WAIVER') DEFAULT NULL,
  `transaction_reference` varchar(150) DEFAULT NULL,
  `transaction_date_time` datetime NOT NULL,
  `debit_amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `credit_amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `running_balance` decimal(12,2) NOT NULL,
  `currency` char(3) NOT NULL DEFAULT 'UGX',
  `ledger_status` enum('ACTIVE','CANCELLED','REVERSED') NOT NULL,
  `remarks` text,
  `version` bigint NOT NULL DEFAULT '0',
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`fee_ledger_id`),
  KEY `idx_fee_ledger_assignment` (`fee_assignment_id`),
  KEY `idx_fee_ledger_receipt` (`fee_receipt_id`),
  KEY `idx_fee_ledger_student` (`student_id`),
  KEY `idx_fee_ledger_branch` (`branch_id`),
  KEY `idx_fee_ledger_admission` (`admission_no`),
  KEY `idx_fee_ledger_transaction` (`transaction_type`),
  KEY `idx_fee_ledger_date` (`transaction_date_time`),
  KEY `idx_fee_ledger_status` (`ledger_status`),
  KEY `idx_fee_ledger_year_term` (`academic_year`,`term`),
  KEY `idx_fee_ledger_fee_name` (`fee_name`),
  KEY `idx_fee_ledger_student_date` (`student_id`,`transaction_date_time`),
  KEY `idx_fee_ledger_assignment_date` (`fee_assignment_id`,`transaction_date_time`),
  CONSTRAINT `fk_fee_ledger_assignment` FOREIGN KEY (`fee_assignment_id`) REFERENCES `erp_student_fee_assignments` (`fee_assignment_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_fee_ledger_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_fee_ledger_receipt` FOREIGN KEY (`fee_receipt_id`) REFERENCES `erp_student_fee_payments` (`fee_receipt_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_fee_ledger_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE,
  CONSTRAINT `chk_fee_ledger_credit` CHECK ((`credit_amount` >= 0)),
  CONSTRAINT `chk_fee_ledger_currency` CHECK ((`currency` = _utf8mb3'UGX')),
  CONSTRAINT `chk_fee_ledger_debit` CHECK ((`debit_amount` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_student_fee_payments`
--

DROP TABLE IF EXISTS `erp_student_fee_payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_fee_payments` (
  `fee_receipt_id` bigint NOT NULL AUTO_INCREMENT,
  `fee_assignment_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `branch_id` int NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `receipt_no` varchar(150) NOT NULL,
  `payment_date_time` datetime NOT NULL,
  `payment_amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `excess_amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `payment_mode` enum('BANK_TRANSFER','CASH','CHEQUE','CREDIT_CARD','DEBIT_CARD','MOBILE_MONEY','ONLINE','SCHOLARSHIP','WAIVER') NOT NULL,
  `transaction_reference` varchar(150) DEFAULT NULL,
  `collection_point` varchar(100) DEFAULT NULL,
  `payment_status` enum('CANCELLED','FAILED','PENDING','REFUNDED','REVERSED','SUCCESS') NOT NULL,
  `receipt_printed` tinyint(1) NOT NULL DEFAULT '0',
  `collected_by` bigint DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `remarks` longtext,
  `cancel_reason` longtext,
  `version` bigint NOT NULL DEFAULT '0',
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`fee_receipt_id`),
  UNIQUE KEY `uk_fee_payment_receipt` (`receipt_no`),
  KEY `idx_fee_payment_assignment` (`fee_assignment_id`),
  KEY `idx_fee_payment_student` (`student_id`),
  KEY `idx_fee_payment_branch` (`branch_id`),
  KEY `idx_fee_payment_admission` (`admission_no`),
  KEY `idx_fee_payment_date` (`payment_date_time`),
  KEY `idx_fee_payment_status` (`payment_status`),
  KEY `idx_fee_payment_receipt` (`receipt_no`),
  CONSTRAINT `fk_fee_payment_assignment` FOREIGN KEY (`fee_assignment_id`) REFERENCES `erp_student_fee_assignments` (`fee_assignment_id`),
  CONSTRAINT `fk_fee_payment_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`),
  CONSTRAINT `fk_fee_payment_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_student_hostel`
--

DROP TABLE IF EXISTS `erp_student_hostel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_hostel` (
  `hostel_allocation_id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `branch_id` int NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `academic_year` varchar(20) NOT NULL,
  `hostel_id` bigint NOT NULL,
  `room_id` bigint DEFAULT NULL,
  `bed_id` bigint DEFAULT NULL,
  `allocation_start_date` date NOT NULL,
  `allocation_end_date` date DEFAULT NULL,
  `monthly_fee` decimal(12,2) NOT NULL DEFAULT '0.00',
  `annual_fee` decimal(12,2) NOT NULL DEFAULT '0.00',
  `discount_amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `payable_amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `allocation_status` enum('ACTIVE','CANCELLED','INACTIVE','SUSPENDED','VACATED') NOT NULL,
  `payment_status` enum('PAID','PARTIAL','PENDING') NOT NULL,
  `local_guardian_name` varchar(150) DEFAULT NULL,
  `local_guardian_mobile` varchar(20) DEFAULT NULL,
  `local_guardian_relation` varchar(50) DEFAULT NULL,
  `remarks` varchar(500) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `version` bigint NOT NULL DEFAULT '0',
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`hostel_allocation_id`),
  UNIQUE KEY `uk_student_hostel_allocation` (`student_id`,`academic_year`),
  KEY `idx_hostel_student` (`student_id`),
  KEY `idx_hostel_branch` (`branch_id`),
  KEY `idx_hostel_master` (`hostel_id`),
  KEY `idx_hostel_room` (`room_id`),
  KEY `idx_hostel_bed` (`bed_id`),
  KEY `idx_hostel_status` (`allocation_status`),
  KEY `idx_hostel_payment` (`payment_status`),
  KEY `idx_hostel_year` (`academic_year`),
  KEY `idx_hostel_admission` (`admission_no`),
  CONSTRAINT `fk_hostel_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_hostel_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE,
  CONSTRAINT `chk_hostel_annual_fee` CHECK ((`annual_fee` >= 0)),
  CONSTRAINT `chk_hostel_dates` CHECK (((`allocation_end_date` is null) or (`allocation_end_date` >= `allocation_start_date`))),
  CONSTRAINT `chk_hostel_discount` CHECK ((`discount_amount` >= 0)),
  CONSTRAINT `chk_hostel_monthly_fee` CHECK ((`monthly_fee` >= 0)),
  CONSTRAINT `chk_hostel_payable` CHECK ((`payable_amount` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_student_medical`
--

DROP TABLE IF EXISTS `erp_student_medical`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_medical` (
  `medical_id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `branch_id` int NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `blood_group` enum('AB_MINUS','AB_PLUS','A_MINUS','A_PLUS','B_MINUS','B_PLUS','O_MINUS','O_PLUS','UNKNOWN') NOT NULL,
  `height_cm` decimal(5,2) DEFAULT NULL,
  `weight_kg` decimal(5,2) DEFAULT NULL,
  `allergies` varchar(500) DEFAULT NULL,
  `chronic_conditions` varchar(500) DEFAULT NULL,
  `ongoing_medication` varchar(500) DEFAULT NULL,
  `special_needs` varchar(500) DEFAULT NULL,
  `fit_for_sports` tinyint(1) NOT NULL DEFAULT '1',
  `emergency_doctor_name` varchar(150) DEFAULT NULL,
  `emergency_doctor_mobile` varchar(20) DEFAULT NULL,
  `preferred_hospital` varchar(150) DEFAULT NULL,
  `remarks` varchar(500) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `version` bigint NOT NULL DEFAULT '0',
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`medical_id`),
  UNIQUE KEY `uk_student_medical` (`student_id`),
  KEY `idx_medical_branch` (`branch_id`),
  KEY `idx_medical_admission` (`admission_no`),
  KEY `idx_medical_blood_group` (`blood_group`),
  KEY `idx_medical_sports` (`fit_for_sports`),
  CONSTRAINT `fk_medical_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_medical_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE,
  CONSTRAINT `chk_medical_height` CHECK (((`height_cm` is null) or (`height_cm` > 0))),
  CONSTRAINT `chk_medical_weight` CHECK (((`weight_kg` is null) or (`weight_kg` > 0)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_student_transport`
--

DROP TABLE IF EXISTS `erp_student_transport`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_transport` (
  `transport_id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `branch_id` int NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `academic_year` varchar(20) NOT NULL,
  `route_id` bigint NOT NULL,
  `vehicle_id` bigint DEFAULT NULL,
  `pickup_point_id` bigint DEFAULT NULL,
  `transport_start_date` date NOT NULL,
  `transport_end_date` date DEFAULT NULL,
  `monthly_fee` decimal(12,2) NOT NULL DEFAULT '0.00',
  `annual_fee` decimal(12,2) NOT NULL DEFAULT '0.00',
  `discount_amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `payable_amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `transport_status` enum('ACTIVE','CANCELLED','COMPLETED','INACTIVE','SUSPENDED') NOT NULL,
  `payment_status` enum('PAID','PARTIAL','PENDING') NOT NULL,
  `seat_number` varchar(20) DEFAULT NULL,
  `emergency_contact` varchar(100) DEFAULT NULL,
  `emergency_mobile` varchar(20) DEFAULT NULL,
  `remarks` varchar(500) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `version` bigint NOT NULL DEFAULT '0',
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`transport_id`),
  UNIQUE KEY `uk_student_transport` (`student_id`,`academic_year`),
  KEY `idx_transport_student` (`student_id`),
  KEY `idx_transport_branch` (`branch_id`),
  KEY `idx_transport_route` (`route_id`),
  KEY `idx_transport_vehicle` (`vehicle_id`),
  KEY `idx_transport_pickup` (`pickup_point_id`),
  KEY `idx_transport_status` (`transport_status`),
  KEY `idx_transport_payment` (`payment_status`),
  KEY `idx_transport_year` (`academic_year`),
  KEY `idx_transport_admission` (`admission_no`),
  CONSTRAINT `fk_transport_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_transport_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE,
  CONSTRAINT `chk_annual_fee` CHECK ((`annual_fee` >= 0)),
  CONSTRAINT `chk_discount` CHECK ((`discount_amount` >= 0)),
  CONSTRAINT `chk_monthly_fee` CHECK ((`monthly_fee` >= 0)),
  CONSTRAINT `chk_payable` CHECK ((`payable_amount` >= 0)),
  CONSTRAINT `chk_transport_dates` CHECK (((`transport_end_date` is null) or (`transport_end_date` >= `transport_start_date`)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_students`
--

DROP TABLE IF EXISTS `erp_students`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_students` (
  `student_id` bigint NOT NULL AUTO_INCREMENT,
  `application_id` bigint DEFAULT NULL,
  `branch_id` int NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `learner_lin` varchar(50) DEFAULT NULL,
  `admission_year` int NOT NULL,
  `student_code` varchar(50) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `middle_name` varchar(100) DEFAULT NULL,
  `last_name` varchar(100) DEFAULT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `gender` varchar(20) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `nationality` varchar(100) DEFAULT NULL,
  `blood_group_id` bigint DEFAULT NULL,
  `religion_id` bigint DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `house_no` varchar(50) DEFAULT NULL,
  `street` varchar(150) DEFAULT NULL,
  `village` varchar(100) DEFAULT NULL,
  `town_city` varchar(100) DEFAULT NULL,
  `district` varchar(100) DEFAULT NULL,
  `state` varchar(100) DEFAULT NULL,
  `country` varchar(100) DEFAULT NULL,
  `postal_code` varchar(20) DEFAULT NULL,
  `photo_path` varchar(255) DEFAULT NULL,
  `student_status` varchar(30) NOT NULL DEFAULT 'ACTIVE',
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_by` bigint DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `version` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`student_id`),
  UNIQUE KEY `admission_no` (`admission_no`),
  UNIQUE KEY `student_code` (`student_code`),
  KEY `fk_students_application` (`application_id`),
  KEY `fk_students_branch` (`branch_id`),
  CONSTRAINT `fk_students_application` FOREIGN KEY (`application_id`) REFERENCES `erp_applications` (`application_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_students_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_subjects`
--

DROP TABLE IF EXISTS `erp_subjects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_subjects` (
  `subject_id` bigint NOT NULL AUTO_INCREMENT,
  `subject_code` varchar(20) NOT NULL,
  `subject_name` varchar(100) NOT NULL,
  `subject_short_name` varchar(50) DEFAULT NULL,
  `subject_type` varchar(30) NOT NULL DEFAULT 'CORE',
  `is_practical` tinyint(1) NOT NULL DEFAULT '0',
  `display_order` int NOT NULL DEFAULT '1',
  `description` varchar(500) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `version` bigint NOT NULL DEFAULT '0',
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`subject_id`),
  UNIQUE KEY `uk_subject_code` (`subject_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_tasks`
--

DROP TABLE IF EXISTS `erp_tasks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_tasks` (
  `task_id` bigint NOT NULL AUTO_INCREMENT,
  `module` varchar(30) NOT NULL DEFAULT 'ADMISSION',
  `reference_type` varchar(30) NOT NULL,
  `reference_id` bigint NOT NULL,
  `task_type` varchar(100) NOT NULL,
  `action_code` varchar(100) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `description` text,
  `assigned_to` int DEFAULT NULL,
  `assigned_role` varchar(50) DEFAULT NULL,
  `assigned_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `accepted_at` datetime DEFAULT NULL,
  `status` enum('PENDING','IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING',
  `priority` enum('LOW','NORMAL','HIGH','URGENT') NOT NULL DEFAULT 'NORMAL',
  `due_date` datetime DEFAULT NULL,
  `started_at` datetime DEFAULT NULL,
  `completed_at` datetime DEFAULT NULL,
  `completed_by` int DEFAULT NULL,
  `parent_task_id` bigint DEFAULT NULL,
  `sequence_no` int NOT NULL DEFAULT '0',
  `branch_id` int DEFAULT NULL,
  `school_id` int DEFAULT NULL,
  `remarks` text,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  `version` int NOT NULL DEFAULT '0',
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`task_id`),
  KEY `idx_task_reference` (`reference_type`,`reference_id`),
  KEY `idx_task_assigned` (`assigned_to`,`status`),
  KEY `idx_task_module` (`module`,`status`),
  KEY `idx_task_due` (`status`,`due_date`),
  KEY `idx_task_branch` (`branch_id`,`status`),
  KEY `idx_task_parent` (`parent_task_id`),
  KEY `fk_task_completed_user` (`completed_by`),
  CONSTRAINT `fk_task_assigned_user` FOREIGN KEY (`assigned_to`) REFERENCES `erp_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_task_completed_user` FOREIGN KEY (`completed_by`) REFERENCES `erp_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_task_parent` FOREIGN KEY (`parent_task_id`) REFERENCES `erp_tasks` (`task_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_user_roles`
--

DROP TABLE IF EXISTS `erp_user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_user_roles` (
  `user_role_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `role_id` bigint NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `version` bigint NOT NULL DEFAULT '0',
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`user_role_id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `fk_userrole_role` (`role_id`),
  CONSTRAINT `fk_userrole_role` FOREIGN KEY (`role_id`) REFERENCES `erp_roles` (`role_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_userrole_user` FOREIGN KEY (`user_id`) REFERENCES `erp_users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_user_sessions`
--

DROP TABLE IF EXISTS `erp_user_sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_user_sessions` (
  `session_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `session_token` varchar(255) NOT NULL,
  `login_time` datetime NOT NULL,
  `last_activity_time` datetime NOT NULL,
  `expiry_time` datetime NOT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `device_name` varchar(255) DEFAULT NULL,
  `browser` varchar(150) DEFAULT NULL,
  `operating_system` varchar(150) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `version` bigint NOT NULL DEFAULT '0',
  `created_by` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`session_id`),
  UNIQUE KEY `uk_session_token` (`session_token`),
  KEY `fk_usersession_user` (`user_id`),
  KEY `idx_usersession_expiry` (`expiry_time`),
  KEY `idx_usersession_active` (`active`),
  CONSTRAINT `fk_usersession_user` FOREIGN KEY (`user_id`) REFERENCES `erp_users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_users`
--

DROP TABLE IF EXISTS `erp_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(50) DEFAULT NULL,
  `assigned_branch` int DEFAULT NULL,
  `is_active` int DEFAULT '1',
  `must_change_password` tinyint(1) NOT NULL DEFAULT '0',
  `temporary_password_created_at` datetime DEFAULT NULL,
  `temporary_password_expires_at` datetime DEFAULT NULL,
  `password_changed_at` datetime DEFAULT NULL,
  `credential_delivery_status` varchar(30) NOT NULL DEFAULT 'NOT_REQUIRED',
  `credentials_sent_at` datetime DEFAULT NULL,
  `credential_delivery_attempts` int NOT NULL DEFAULT '0',
  `credential_version` int NOT NULL DEFAULT '0',
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `assigned_branch` (`assigned_branch`),
  KEY `idx_erp_users_temp_credentials` (`must_change_password`,`temporary_password_expires_at`),
  CONSTRAINT `erp_users_ibfk_1` FOREIGN KEY (`assigned_branch`) REFERENCES `erp_branches` (`branch_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-23 23:13:45
