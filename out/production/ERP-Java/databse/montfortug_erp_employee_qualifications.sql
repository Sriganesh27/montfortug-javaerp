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
-- Table structure for table `erp_employee_qualifications`
--

DROP TABLE IF EXISTS `erp_employee_qualifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_employee_qualifications` (
  `employee_qualification_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `employee_id` bigint(20) NOT NULL,
  `employee_qualification_level` enum('PRIMARY','O_LEVEL','A_LEVEL','CERTIFICATE','DIPLOMA','ADVANCED_DIPLOMA','BACHELOR','POST_GRADUATE','POST_GRADUATE_DIPLOMA','MASTER','M_PHIL','PHD','PROFESSIONAL','VOCATIONAL','SHORT_COURSE','TRAINING','CERTIFICATION','OTHER') NOT NULL,
  `employee_qualification_name` varchar(255) NOT NULL,
  `employee_qualification_specialization` varchar(255) DEFAULT NULL,
  `employee_qualification_institution_name` varchar(255) NOT NULL,
  `employee_qualification_board_university` varchar(255) DEFAULT NULL,
  `employee_qualification_country` varchar(100) DEFAULT NULL,
  `employee_qualification_start_year` year(4) DEFAULT NULL,
  `employee_qualification_completion_year` year(4) DEFAULT NULL,
  `employee_qualification_duration_months` int(11) DEFAULT NULL,
  `employee_qualification_grade` varchar(50) DEFAULT NULL,
  `employee_qualification_percentage` decimal(5,2) DEFAULT NULL,
  `employee_qualification_cgpa` decimal(4,2) DEFAULT NULL,
  `employee_qualification_certificate_number` varchar(100) DEFAULT NULL,
  `employee_qualification_registration_number` varchar(100) DEFAULT NULL,
  `employee_qualification_document_file` varchar(500) DEFAULT NULL,
  `employee_qualification_verified` tinyint(1) NOT NULL DEFAULT 0,
  `employee_qualification_verified_by` int(11) DEFAULT NULL,
  `employee_qualification_verified_at` datetime DEFAULT NULL,
  `employee_qualification_remarks` text DEFAULT NULL,
  `employee_qualification_active` tinyint(1) NOT NULL DEFAULT 1,
  `created_by` int(11) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` int(11) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  `version` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`employee_qualification_id`),
  KEY `fk_empqualification_employee` (`employee_id`),
  KEY `fk_empqualification_verifiedby` (`employee_qualification_verified_by`),
  CONSTRAINT `fk_empqualification_employee` FOREIGN KEY (`employee_id`) REFERENCES `erp_employees` (`employee_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_empqualification_verifiedby` FOREIGN KEY (`employee_qualification_verified_by`) REFERENCES `erp_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_employee_qualifications`
--

LOCK TABLES `erp_employee_qualifications` WRITE;
/*!40000 ALTER TABLE `erp_employee_qualifications` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_employee_qualifications` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-14 12:47:38
