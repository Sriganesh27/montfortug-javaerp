-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: 68.178.237.26    Database: montfortug
-- ------------------------------------------------------
-- Server version	5.5.5-10.11.17-MariaDB-cll-lve

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
-- Table structure for table `erp_employees`
--

DROP TABLE IF EXISTS `erp_employees`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_employees` (
  `employee_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `branch_id` int(11) NOT NULL,
  `department_id` bigint(20) DEFAULT NULL,
  `designation_id` bigint(20) DEFAULT NULL,
  `reporting_manager_id` bigint(20) DEFAULT NULL,
  `employee_no` varchar(50) NOT NULL,
  `title` varchar(20) DEFAULT NULL,
  `first_name` varchar(100) NOT NULL,
  `middle_name` varchar(100) DEFAULT NULL,
  `last_name` varchar(100) NOT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `gender` enum('MALE','FEMALE','OTHER') DEFAULT NULL,
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
  `official_email` varchar(150) DEFAULT NULL,
  `personal_email` varchar(150) DEFAULT NULL,
  `mobile_no` varchar(30) DEFAULT NULL,
  `alternate_mobile` varchar(30) DEFAULT NULL,
  `address_country` varchar(100) DEFAULT NULL,
  `address_state` varchar(100) DEFAULT NULL,
  `address_district` varchar(100) DEFAULT NULL,
  `address_village` varchar(150) DEFAULT NULL,
  `address_street` varchar(255) DEFAULT NULL,
  `postal_code` varchar(30) DEFAULT NULL,
  `employee_category` enum('TEACHING','NON_TEACHING','MANAGEMENT','SUPPORT_STAFF') DEFAULT NULL,
  `employee_type` enum('PERMANENT','CONTRACT','TEMPORARY','PART_TIME','INTERN','VOLUNTEER') NOT NULL,
  `employment_mode` enum('FULL_TIME','PART_TIME','REMOTE','ON_CALL') NOT NULL DEFAULT 'FULL_TIME',
  `employment_status` enum('ACTIVE','PROBATION','ON_LEAVE','SUSPENDED','RESIGNED','RETIRED','TERMINATED') NOT NULL DEFAULT 'ACTIVE',
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
  `exit_reason` text DEFAULT NULL,
  `employee_remarks` text DEFAULT NULL,
  `login_enabled` tinyint(1) NOT NULL DEFAULT 0,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `created_by` int(11) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` int(11) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  `version` bigint(20) NOT NULL DEFAULT 0,
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_employees`
--

LOCK TABLES `erp_employees` WRITE;
/*!40000 ALTER TABLE `erp_employees` DISABLE KEYS */;
INSERT INTO `erp_employees` VALUES (1,NULL,1,15,11,NULL,'U011-NT-26-001','Mr.','Sri',NULL,'Ganesh','Sri Ganesh','MALE','2008-07-13',NULL,NULL,NULL,NULL,NULL,NULL,'SINGLE','A_NEGATIVE',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NON_TEACHING','PERMANENT','FULL_TIME','ACTIVE','2026-07-13','2028-11-15','2026-07-13',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,1,1,'2026-07-13 16:53:23',1,'2026-07-13 16:53:23',0),(2,NULL,1,8,11,NULL,'U011-MGT-26-001','Mr.','Montbrsug',NULL,'INP','Montbrsug INP','MALE','2008-07-06',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'montbrsuginp@gmail.com','montbrsuginp@gmail.com',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'MANAGEMENT','PERMANENT','FULL_TIME','ACTIVE','2026-07-13',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,1,1,'2026-07-13 16:54:19',1,'2026-07-13 16:54:19',0),(3,NULL,1,9,13,NULL,'U011-SS-26-001','Mrs.','sri',NULL,'ha','sri ha','MALE','2008-07-09',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'sriganeshgoud9154@gmail.com','sriganeshgoud9154@gmail.com',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'SUPPORT_STAFF','PERMANENT','FULL_TIME','ACTIVE','2026-07-13',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,1,1,'2026-07-13 16:55:47',1,'2026-07-13 16:55:47',0);
/*!40000 ALTER TABLE `erp_employees` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-14 12:47:07
