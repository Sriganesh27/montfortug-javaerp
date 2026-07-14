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
-- Table structure for table `erp_employee_documents`
--

DROP TABLE IF EXISTS `erp_employee_documents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_employee_documents` (
  `employee_document_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `employee_id` bigint(20) NOT NULL,
  `employee_document_type` enum('PASSPORT_PHOTO','SIGNATURE','RESUME','CURRICULUM_VITAE','NATIONAL_ID','PASSPORT','BIRTH_CERTIFICATE','MARRIAGE_CERTIFICATE','MEDICAL_CERTIFICATE','POLICE_CLEARANCE','APPOINTMENT_LETTER','EMPLOYMENT_CONTRACT','CONFIDENTIALITY_AGREEMENT','CODE_OF_CONDUCT_AGREEMENT','EXPERIENCE_CERTIFICATE','RELIEVING_LETTER','SALARY_CERTIFICATE','BANK_DOCUMENT','NSSF_DOCUMENT','TIN_CERTIFICATE','WORK_PERMIT','VISA','TEACHING_LICENSE','PROFESSIONAL_LICENSE','ACADEMIC_CERTIFICATE','OTHER') NOT NULL,
  `employee_document_name` varchar(255) NOT NULL,
  `employee_document_description` text DEFAULT NULL,
  `employee_document_file_name` varchar(255) NOT NULL,
  `employee_document_original_file_name` varchar(255) DEFAULT NULL,
  `employee_document_file_path` varchar(500) NOT NULL,
  `employee_document_file_extension` varchar(20) DEFAULT NULL,
  `employee_document_mime_type` varchar(100) DEFAULT NULL,
  `employee_document_file_size` bigint(20) DEFAULT NULL,
  `employee_document_issue_date` date DEFAULT NULL,
  `employee_document_expiry_date` date DEFAULT NULL,
  `employee_document_verified` tinyint(1) NOT NULL DEFAULT 0,
  `employee_document_verified_by` int(11) DEFAULT NULL,
  `employee_document_verified_at` datetime DEFAULT NULL,
  `employee_document_is_mandatory` tinyint(1) NOT NULL DEFAULT 0,
  `employee_document_active` tinyint(1) NOT NULL DEFAULT 1,
  `employee_document_remarks` text DEFAULT NULL,
  `created_by` int(11) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` int(11) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  `version` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`employee_document_id`),
  KEY `fk_empdocument_employee` (`employee_id`),
  KEY `fk_empdocument_verifiedby` (`employee_document_verified_by`),
  CONSTRAINT `fk_empdocument_employee` FOREIGN KEY (`employee_id`) REFERENCES `erp_employees` (`employee_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_empdocument_verifiedby` FOREIGN KEY (`employee_document_verified_by`) REFERENCES `erp_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_employee_documents`
--

LOCK TABLES `erp_employee_documents` WRITE;
/*!40000 ALTER TABLE `erp_employee_documents` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_employee_documents` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-14 12:47:12
