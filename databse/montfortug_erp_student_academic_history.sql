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
-- Table structure for table `erp_student_academic_history`
--

DROP TABLE IF EXISTS `erp_student_academic_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_academic_history` (
  `academic_history_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `former_school_name` varchar(255) DEFAULT NULL,
  `former_school_code` varchar(50) DEFAULT NULL,
  `former_school_lin` varchar(50) DEFAULT NULL,
  `former_school_address` varchar(255) DEFAULT NULL,
  `school_type` enum('GOVERNMENT','PRIVATE','INTERNATIONAL','OTHER') DEFAULT 'PRIVATE',
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
  `subject_marks` longtext DEFAULT NULL,
  `previous_report_card` varchar(255) DEFAULT NULL,
  `transfer_certificate` varchar(255) DEFAULT NULL,
  `leaving_certificate` varchar(255) DEFAULT NULL,
  `verification_status` enum('PENDING','VERIFIED','REJECTED') NOT NULL DEFAULT 'PENDING',
  `verified_by` bigint(20) DEFAULT NULL,
  `verified_at` datetime DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `remarks` longtext DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
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
  CONSTRAINT `fk_academic_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_academic_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_student_academic_history`
--

LOCK TABLES `erp_student_academic_history` WRITE;
/*!40000 ALTER TABLE `erp_student_academic_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_student_academic_history` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-11  0:29:15
