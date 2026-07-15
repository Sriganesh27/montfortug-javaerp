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
-- Table structure for table `erp_designations`
--

DROP TABLE IF EXISTS `erp_designations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_designations` (
  `designation_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `designation_code` varchar(20) NOT NULL,
  `designation_name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `created_by` varchar(100) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` varchar(100) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`designation_id`),
  UNIQUE KEY `uk_designation_code` (`designation_code`),
  UNIQUE KEY `uk_designation_name` (`designation_name`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_designations`
--

LOCK TABLES `erp_designations` WRITE;
/*!40000 ALTER TABLE `erp_designations` DISABLE KEYS */;
INSERT INTO `erp_designations` VALUES (1,'PRINCIPAL','Principal','Head of the school','ACTIVE',1,0,NULL,'2026-07-06 09:11:23',NULL,'2026-07-06 09:11:23'),(2,'VICE_PRINCIPAL','Vice Principal','Assists the Principal','ACTIVE',1,0,NULL,'2026-07-06 09:11:23',NULL,'2026-07-06 09:11:23'),(3,'HEAD_TEACHER','Head Teacher','Head of academic activities','ACTIVE',1,0,NULL,'2026-07-06 09:11:23',NULL,'2026-07-06 09:11:23'),(4,'TEACHER','Teacher','Teaching staff','ACTIVE',1,0,NULL,'2026-07-06 09:11:23',NULL,'2026-07-06 09:11:23'),(5,'ADMISSIONS_OFFICER','Admissions Officer','Handles admissions and enrollment process','ACTIVE',1,0,NULL,'2026-07-06 09:11:23',NULL,'2026-07-06 09:11:23'),(6,'ACCOUNTANT','Accountant','Manages finance and accounts','ACTIVE',1,0,NULL,'2026-07-06 09:11:23',NULL,'2026-07-06 09:11:23'),(7,'LIBRARIAN','Librarian','Manages library resources','ACTIVE',1,0,NULL,'2026-07-06 09:11:23',NULL,'2026-07-06 09:11:23'),(8,'ICT_OFFICER','ICT Officer','Provides IT support','ACTIVE',1,0,NULL,'2026-07-06 09:11:23',NULL,'2026-07-06 09:11:23'),(9,'SECRETARY','Secretary','Administrative secretary','ACTIVE',1,0,NULL,'2026-07-06 09:11:23',NULL,'2026-07-06 09:11:23'),(10,'RECEPTIONIST','Receptionist','Front office and visitor management','ACTIVE',1,0,NULL,'2026-07-06 09:11:23',NULL,'2026-07-06 09:11:23'),(11,'SCHOOL_NURSE','School Nurse','Provides medical assistance','ACTIVE',1,0,NULL,'2026-07-06 09:11:23',NULL,'2026-07-06 09:11:23'),(12,'COUNSELOR','Counselor','Student guidance and counseling','ACTIVE',1,0,NULL,'2026-07-06 09:11:23',NULL,'2026-07-06 09:11:23'),(13,'DRIVER','Driver','School transport driver','ACTIVE',1,0,NULL,'2026-07-06 09:11:23',NULL,'2026-07-06 09:11:23'),(14,'SECURITY_GUARD','Security Guard','Campus security','ACTIVE',1,0,NULL,'2026-07-06 09:11:23',NULL,'2026-07-06 09:11:23'),(15,'CLEANER','Cleaner','Cleaning and housekeeping','ACTIVE',1,0,NULL,'2026-07-06 09:11:23',NULL,'2026-07-06 09:11:23'),(16,'hre;llp','mksbakcb','xzc vm ','ACTIVE',1,0,'1','2026-07-11 15:58:26','1','2026-07-11 15:58:26'),(17,'JJHJ','MM','MN','ACTIVE',1,0,'1','2026-07-11 18:24:29','1','2026-07-11 18:24:29');
/*!40000 ALTER TABLE `erp_designations` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-14 12:46:39
