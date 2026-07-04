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
-- Table structure for table `erp_student_fee_assignments`
--

DROP TABLE IF EXISTS `erp_student_fee_assignments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_fee_assignments` (
  `fee_assignment_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `academic_year` varchar(20) NOT NULL,
  `term` varchar(30) NOT NULL,
  `fee_name` varchar(150) NOT NULL,
  `fee_type` varchar(50) NOT NULL,
  `total_fee` decimal(12,2) NOT NULL,
  `scholarship_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `concession_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `discount_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `fine_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `payable_amount` decimal(12,2) NOT NULL,
  `paid_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `balance_amount` decimal(12,2) NOT NULL,
  `assignment_date` date NOT NULL,
  `due_date` date DEFAULT NULL,
  `fee_status` enum('PENDING','PARTIAL','PAID','OVERDUE','CANCELLED') NOT NULL DEFAULT 'PENDING',
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `remarks` text DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `version` bigint(20) NOT NULL DEFAULT 0,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_student_fee_assignments`
--

LOCK TABLES `erp_student_fee_assignments` WRITE;
/*!40000 ALTER TABLE `erp_student_fee_assignments` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_student_fee_assignments` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-04 14:11:49
