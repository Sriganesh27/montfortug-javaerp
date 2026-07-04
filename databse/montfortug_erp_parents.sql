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
-- Table structure for table `erp_parents`
--

DROP TABLE IF EXISTS `erp_parents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_parents` (
  `parent_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL,
  `branch_id` int(11) NOT NULL,
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
  `preferred_contact` enum('FATHER','MOTHER','GUARDIAN') DEFAULT 'FATHER',
  `fee_responsibility` enum('FATHER','MOTHER','GUARDIAN','SPONSOR') NOT NULL DEFAULT 'FATHER',
  `parents_living_together` tinyint(1) NOT NULL DEFAULT 1,
  `emergency_contact_name` varchar(150) DEFAULT NULL,
  `emergency_contact_phone` varchar(30) DEFAULT NULL,
  `emergency_contact_relationship` varchar(100) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `remarks` text DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `version` bigint(20) NOT NULL DEFAULT 0,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_parents`
--

LOCK TABLES `erp_parents` WRITE;
/*!40000 ALTER TABLE `erp_parents` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_parents` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-04 14:12:15
