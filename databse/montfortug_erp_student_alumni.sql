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
-- Table structure for table `erp_student_alumni`
--

DROP TABLE IF EXISTS `erp_student_alumni`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_alumni` (
  `alumni_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `graduation_year` int(11) NOT NULL,
  `graduation_date` date DEFAULT NULL,
  `final_class` varchar(50) DEFAULT NULL,
  `final_stream` varchar(50) DEFAULT NULL,
  `final_grade` varchar(50) DEFAULT NULL,
  `certificate_number` varchar(100) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`alumni_id`),
  UNIQUE KEY `uk_alumni_student` (`student_id`),
  KEY `idx_alumni_branch` (`branch_id`),
  KEY `idx_alumni_admission` (`admission_no`),
  KEY `idx_alumni_grad_year` (`graduation_year`),
  KEY `idx_alumni_graduation_date` (`graduation_date`),
  KEY `idx_alumni_certificate` (`certificate_number`),
  CONSTRAINT `fk_alumni_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_alumni_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_student_alumni`
--

LOCK TABLES `erp_student_alumni` WRITE;
/*!40000 ALTER TABLE `erp_student_alumni` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_student_alumni` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-04 14:11:56
