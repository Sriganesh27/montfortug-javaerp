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
-- Table structure for table `erp_scholarship_applications`
--

DROP TABLE IF EXISTS `erp_scholarship_applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_scholarship_applications` (
  `scholarship_app_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) NOT NULL DEFAULT 1,
  `application_id` bigint(20) DEFAULT NULL,
  `student_id` bigint(20) DEFAULT NULL,
  `academic_year` varchar(20) NOT NULL,
  `amount_requested_ugx` decimal(38,2) NOT NULL DEFAULT 0.00,
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
  `reviewed_by` bigint(20) DEFAULT NULL,
  `approved_at` datetime DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `version` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`scholarship_app_id`),
  UNIQUE KEY `uk_scholarship_application` (`application_id`),
  UNIQUE KEY `uk_scholarship_student` (`student_id`),
  KEY `idx_scholarship_application` (`application_id`),
  KEY `idx_scholarship_student` (`student_id`),
  KEY `idx_scholarship_status` (`status`),
  KEY `idx_scholarship_year` (`academic_year`),
  CONSTRAINT `fk_scholarship_application` FOREIGN KEY (`application_id`) REFERENCES `erp_applications` (`application_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_scholarship_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_scholarship_applications`
--

LOCK TABLES `erp_scholarship_applications` WRITE;
/*!40000 ALTER TABLE `erp_scholarship_applications` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_scholarship_applications` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-14 12:46:21
