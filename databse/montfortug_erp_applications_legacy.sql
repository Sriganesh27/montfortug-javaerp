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
-- Table structure for table `erp_applications_legacy`
--

DROP TABLE IF EXISTS `erp_applications_legacy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_applications_legacy` (
  `app_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ref_number` varchar(50) NOT NULL,
  `branch_id` bigint(20) NOT NULL,
  `status` varchar(50) DEFAULT NULL,
  `scholarship_status` varchar(50) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `academic_year` varchar(10) DEFAULT NULL,
  `term` varchar(20) DEFAULT NULL,
  `date_of_registration` varchar(50) DEFAULT NULL,
  `level` varchar(50) NOT NULL,
  `applied_class` varchar(50) NOT NULL,
  `class_code` varchar(10) NOT NULL,
  `student_name` varchar(100) NOT NULL,
  `middle_name` varchar(100) DEFAULT NULL,
  `student_surname` varchar(100) NOT NULL,
  `gender` varchar(10) NOT NULL,
  `dob` varchar(20) DEFAULT NULL,
  `nationality` varchar(100) DEFAULT 'Uganda',
  `photo_path` varchar(255) DEFAULT NULL,
  `address_country` varchar(100) DEFAULT 'Uganda',
  `address_state` varchar(100) DEFAULT NULL,
  `address_district` varchar(100) DEFAULT NULL,
  `address_village` varchar(100) DEFAULT NULL,
  `address_street` varchar(100) DEFAULT NULL,
  `address_house` varchar(100) DEFAULT NULL,
  `address_postal` varchar(20) DEFAULT NULL,
  `father_name` varchar(100) DEFAULT NULL,
  `father_age` int(11) DEFAULT NULL,
  `father_contact` varchar(50) DEFAULT NULL,
  `father_email` varchar(100) DEFAULT NULL,
  `father_occupation` varchar(100) DEFAULT NULL,
  `father_education` varchar(100) DEFAULT NULL,
  `mother_name` varchar(100) DEFAULT NULL,
  `mother_age` int(11) DEFAULT NULL,
  `mother_contact` varchar(50) DEFAULT NULL,
  `mother_email` varchar(100) DEFAULT NULL,
  `mother_occupation` varchar(100) DEFAULT NULL,
  `mother_education` varchar(100) DEFAULT NULL,
  `guardian_name` varchar(100) DEFAULT NULL,
  `guardian_relation` varchar(50) DEFAULT NULL,
  `guardian_age` int(11) DEFAULT NULL,
  `guardian_contact` varchar(50) DEFAULT NULL,
  `guardian_email` varchar(100) DEFAULT NULL,
  `guardian_occupation` varchar(100) DEFAULT NULL,
  `guardian_education` varchar(100) DEFAULT NULL,
  `former_school` varchar(255) DEFAULT NULL,
  `former_school_code` varchar(50) DEFAULT NULL,
  `former_school_lin` varchar(50) DEFAULT NULL,
  `ple_score` int(11) DEFAULT NULL,
  `ple_ref` varchar(50) DEFAULT NULL,
  `uce_score` int(11) DEFAULT NULL,
  `uce_ref` varchar(50) DEFAULT NULL,
  `subject_marks` longtext DEFAULT NULL,
  `prev_marks_doc` varchar(255) DEFAULT NULL,
  `more_info` text DEFAULT NULL,
  `guardian_location` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`app_id`),
  UNIQUE KEY `UK41obtiqpwhdoxlx32ywfxjw5b` (`ref_number`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_applications_legacy`
--

LOCK TABLES `erp_applications_legacy` WRITE;
/*!40000 ALTER TABLE `erp_applications_legacy` DISABLE KEYS */;
INSERT INTO `erp_applications_legacy` VALUES (1,'APP-2026-U021-001',2,'Pending','No','2026-06-27 21:28:13','2026','Term I','','Nursery','Baby Class','N1','SRI','GANESH','SUDHAGANI','Male','2026-06-20','Uganda',NULL,'Uganda','','d','','','','','',NULL,'','','','','',NULL,'','','','','',NULL,NULL,'','','','','',NULL,'',NULL,NULL,NULL,NULL,NULL,NULL,'','');
/*!40000 ALTER TABLE `erp_applications_legacy` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-04 14:11:58
